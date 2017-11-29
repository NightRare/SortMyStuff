package nz.ac.aut.comp705.sortmystuff.data.remote;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import nz.ac.aut.comp705.sortmystuff.Features;
import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.IDebugHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.Category;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.AppResDatabaseRef;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.RegularScheduler;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.UserDatabaseRef;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import nz.ac.aut.comp705.sortmystuff.utils.Log;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.data.models.Detail.DETAIL_FIELD;

public class FirebaseHelper implements IDataRepository, IDebugHelper {

    public static final String DB_ASSETS = "assets";
    public static final String DB_DETAILS = "details";
    public static final String DB_CATEGORIES = "categories";
    public static final String ST_DETAIL_IMAGES = "detail_images";
    public static final Long MAX_BYTES_LENGTH = 10L * 1024L * 1024L;  // 10 mb

    @Inject
    public FirebaseHelper(
            LocalResourceLoader resLoader,
            @UserDatabaseRef DatabaseReference userDB,
            @AppResDatabaseRef DatabaseReference appResDB,
            StorageReference storageReference,
            @RegularScheduler ISchedulerProvider schedulerProvider,
            Features featToggle) {
        mResLoader = checkNotNull(resLoader);
        mUserDB = checkNotNull(userDB);
        mAppResDB = checkNotNull(appResDB);
        mUserST = checkNotNull(storageReference);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mFeatureToggle = checkNotNull(featToggle);

        if (mFeatureToggle.DevelopmentMode) {
            deleteAllRecycledAssets();
            deleteAllRecycledDetails();
        }
    }

    @Override
    public Observable<List<Asset>> retrieveAllAssets() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_ASSETS))
                .map(dataSnapshot -> jsonDataToList(dataSnapshot, Asset.class));
    }

    @Override
    public Observable<Asset> retrieveAsset(String assetId) {
        checkNotNull(assetId, "The assetId cannot be null.");
        return RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_ASSETS).child(checkNotNull(assetId)))
                .map(dataSnapshot -> jsonDataToObject(dataSnapshot, Asset.class))
                .onErrorReturn(throwable -> {
                    Log.e(FirebaseHelper.class.getName(), throwable.getMessage(), throwable);
                    return null;
                });
    }

    @Override
    public Observable<List<Detail>> retrieveAllDetails() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_DETAILS))
                .map(dataSnapshot -> jsonDataToList(dataSnapshot, Detail.class));
    }

    @Override
    public Observable<List<Detail>> retrieveDetails(String assetId) {
        checkNotNull(assetId, "The assetId cannot be null.");
        return retrieveAsset(assetId)
                .doOnNext(asset -> {
                    if (asset == null)
                        throw new NoSuchElementException();
                })
                .flatMap(asset -> Observable.from(asset.getDetailIds()))
                .flatMap(this::retrieveDetail)
                .toSortedList()
                .onErrorReturn(throwable -> null);
    }

    @Override
    public Observable<Detail> retrieveDetail(String detailId) {
        checkNotNull(detailId, "The detailId cannot be null.");

        return assembleDetail(detailId);
    }

    @Override
    public Observable<List<Category>> retrieveCategories() {
        return RxFirebaseDatabase.observeSingleValueEvent(mAppResDB.child(DB_CATEGORIES))
                .map(dataSnapshot -> jsonDataToList(dataSnapshot, Category.class));
    }

    @Override
    public void addOrUpdateAsset(Asset asset, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        mUserDB.child(DB_ASSETS).child(checkNotNull(asset).getId()).setValue(asset.toMap())
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure)
                .addOnCompleteListener(task -> callback.onComplete());
    }

    @Override
    public void addDetail(Detail detail, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        retrieveDetail(checkNotNull(detail).getId())
                .subscribeOn(mSchedulerProvider.io())
                .doOnNext(detailFromFirebase -> {
                    // should not update an existing record
                    if (detailFromFirebase != null) throw new IllegalStateException();
                })
                .flatMap(isNewDetail ->
                        Observable.create((Action1<Emitter<Boolean>>) emitter -> {
                            OnSuccessListener<Void> onNext = aVoid -> {
                                Boolean requireImageLoading = detail.getType().equals(DetailType.Image)
                                        && !detail.isDefaultFieldValue();
                                emitter.onNext(requireImageLoading);
                            };

                            OnFailureListener onError = emitter::onError;
                            OnCompleteListener<Void> onCompleted = task -> emitter.onCompleted();

                            mUserDB.child(DB_DETAILS).child(detail.getId()).setValue(detail.toMap())
                                    .addOnSuccessListener(onNext)
                                    .addOnFailureListener(onError)
                                    .addOnCompleteListener(onCompleted);
                        }, Emitter.BackpressureMode.BUFFER))
                .flatMap(requireImageLoading -> {
                    if (requireImageLoading) {
                        return Observable.create((Action1<Emitter<Void>>) emitter -> {
                            OnSuccessListener<UploadTask.TaskSnapshot> onNext = taskSnapshot -> {
                                emitter.onNext(null);
                            };

                            OnFailureListener onError = emitter::onError;
                            OnCompleteListener<UploadTask.TaskSnapshot> onCompleted = taskSnapshot -> {
                                emitter.onCompleted();
                            };

                            mUserST.child(ST_DETAIL_IMAGES).child(detail.getId())
                                    .putBytes(BitmapHelper.toByteArray(detail.getFieldData()))
                                    .addOnSuccessListener(onNext)
                                    .addOnFailureListener(onError)
                                    .addOnCompleteListener(onCompleted);
                        }, Emitter.BackpressureMode.BUFFER);
                    }

                    return Observable.just(null);
                })
                .doOnNext(callback::onSuccess)
                .doOnError(callback::onFailure)
                .doOnCompleted(callback::onComplete)
                .subscribe();
    }

    @Override
    public <E> void updateAsset(String assetId, String key, E value, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        Class fieldType =  Asset.getMemberType(key);
        if (value != null && (fieldType == null || !fieldType.isAssignableFrom(value.getClass()))) {
            IllegalArgumentException e = new IllegalArgumentException("Incorrect value type.");
            callback.onFailure(e);
            throw e;
        }

        mUserDB.child(DB_ASSETS).child(checkNotNull(assetId)).child(checkNotNull(key))
                .setValue(value)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure)
                .addOnCompleteListener(task -> callback.onComplete());
    }

    @Override
    public void updateDetail(Detail detail, boolean updatingField, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        if (updatingField) {
            Task<Void> uploadTask = mUserDB.child(DB_DETAILS).child(detail.getId()).setValue(detail.toMap())
                    .addOnFailureListener(callback::onFailure);

            if (detail.getType().equals(DetailType.Image)) {

                // if set back to default photo
                if (detail.isDefaultFieldValue()) {
                    mUserST.child(ST_DETAIL_IMAGES).child(detail.getId()).delete()
                            .addOnSuccessListener(taskSnapshot -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure)
                            .addOnCompleteListener(task -> callback.onComplete());
                } else {
                    mUserST.child(ST_DETAIL_IMAGES).child(detail.getId())
                            .putBytes(BitmapHelper.toByteArray(detail.getFieldData()))
                            .addOnSuccessListener(taskSnapshot -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure)
                            .addOnCompleteListener(task -> callback.onComplete());
                }

            } else {
                uploadTask.addOnSuccessListener(callback::onSuccess)
                        .addOnCompleteListener(task -> callback.onComplete());
            }

            return;
        }

        Map<String, Object> members = detail.toMap();
        List<Task<Void>> tasks = new ArrayList<>();

        for (Map.Entry<String, Object> member : members.entrySet()) {
            if (!member.getKey().equals(DETAIL_FIELD)) {
                Task<Void> updateTask = mUserDB.child(DB_DETAILS).child(detail.getId()).child(member.getKey())
                        .setValue(member.getValue())
                        .addOnFailureListener(callback::onFailure);
                tasks.add(updateTask);
            }
        }
        Tasks.whenAll(tasks)
                .addOnSuccessListener(callback::onSuccess)
                .addOnCompleteListener(task -> callback.onComplete());
    }

    @Override
    public <T> void setOnDataChangeCallback(OnDataChangeCallback<T> onDataChangeCallback, Class<T> type) {
        checkNotNull(onDataChangeCallback);
        checkNotNull(type);

        if (type.equals(Asset.class)) {
            mOnAssetsDataChangeCallback = (OnDataChangeCallback<Asset>) onDataChangeCallback;
            reloadDataChangeListener(type);

        } else if (type.equals(Detail.class)) {
            mOnDetailsDataChangeCallback = (OnDataChangeCallback<Detail>) onDataChangeCallback;
            reloadDataChangeListener(type);
        }
    }

    @Override
    public <T> void removeOnDataChangeCallback(Class<T> type) {
        if (type == null) {
            mOnAssetsDataChangeCallback = null;
            mOnDetailsDataChangeCallback = null;
            reloadDataChangeListener(Asset.class);
            reloadDataChangeListener(Detail.class);
        } else if (type.equals(Asset.class)) {
            mOnAssetsDataChangeCallback = null;
            reloadDataChangeListener(Asset.class);
        } else if (type.equals(Detail.class)) {
            mOnDetailsDataChangeCallback = null;
            reloadDataChangeListener(Detail.class);
        }
    }

    @Override
    public void removeCurrentUserData() {
        mUserDB.removeValue();
        mUserST.delete();
    }

    @Override
    public void setFeatureToggle(@NonNull Features featureToggle) {
        mFeatureToggle = checkNotNull(featureToggle);
    }

    @NonNull
    @Override
    public Features getFeatureToggle() {
        return mFeatureToggle;
    }

    //region PRIVATE STUFF

    /**
     * Transforms a dataSnapshot of an object node into the object whose type is specified as the
     * given argument. The key of the dataSnapshot should be the id while the value should be
     * the Map containing all the object members.
     *
     * @param dataSnapshot retrieved from the object node
     * @param type         the class of the objects
     * @param <E>          the type of the objects
     * @return the object or {@code null} if dataSnapshot or its value is {@code null}
     */
    private <E> E jsonDataToObject(DataSnapshot dataSnapshot, Class<E> type) {
        if (dataSnapshot == null) return null;
        Map<String, Object> members = (Map) dataSnapshot.getValue();
        if (members == null) return null;

        if (type.equals(Asset.class)) {
            Asset asset = Asset.fromMap(members);
            if (asset.getThumbnail() == null)
                asset.setThumbnail(mResLoader.getDefaultThumbnail(), true);
            return (E) asset;

        } else if (type.equals(Detail.class)) {
            Detail detail = Detail.fromMap(members);

            if (detail.isDefaultFieldValue() && detail.getType().equals(DetailType.Image)) {
                detail.setFieldData(mResLoader.getDefaultPhotoDataString(), true);
            }
            return (E) detail;

        } else if (type.equals(Category.class)) {
            Category category = Category.fromMap(members);
            category.setDefaultFieldValue(mResLoader.getDefaultText(), mResLoader.getDefaultPhotoDataString());

            return (E) category;

        } else return null;
    }

    /**
     * Transforms a dataSnapshot in a list of objects whose type is specified as the given argument.
     * The direct children of the given dataSnapshot should be the object nodes whose key is the id
     * of the object and whose value is the Map containing all the object members.
     * <p>
     * The sequence of the list is NOT specified/sorted.
     *
     * @param dataSnapshot retrieved from the parent node of list of type-specified objects
     * @param type         the class of the objects
     * @param <E>          the type of the objects
     * @return a list containing objects transformed from dataSnapshot
     */
    private <E> List<E> jsonDataToList(DataSnapshot dataSnapshot, Class<E> type) {
        List<E> objects = new ArrayList<>();
        for (DataSnapshot objectData : dataSnapshot.getChildren()) {
            objects.add((E) jsonDataToObject(objectData, type));
        }
        return objects;
    }

    private Observable<byte[]> getImageFile(String detailId) {
        return Observable.create(new Action1<Emitter<byte[]>>() {
            @Override
            public void call(Emitter<byte[]> emitter) {
                StorageReference sref = mUserST.child(ST_DETAIL_IMAGES).child(detailId);
                sref.getBytes(MAX_BYTES_LENGTH)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                emitter.onNext(bytes);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                emitter.onNext(null);
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<byte[]>() {
                            @Override
                            public void onComplete(@NonNull Task<byte[]> task) {
                                emitter.onCompleted();
                            }
                        });
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    private Observable<Detail> assembleDetail(String detailId) {
        Observable<byte[]> getImageObservable = getImageFile(detailId);
        Observable<Detail> getDetailObservable = RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_DETAILS).child(detailId))
                .map(dataSnapshot -> jsonDataToObject(dataSnapshot, Detail.class));

        return Observable.zip(getImageObservable, getDetailObservable, (bytes, detail) -> {
            if (detail == null) return null;
            if (detail.getType().equals(DetailType.Image)) {

                if (bytes == null) {
                    detail.setFieldData(mResLoader.getDefaultPhotoDataString(), true);
                } else {
                    detail.setFieldData(BitmapHelper.toString(bytes), false);
                }
            }

            // only Image detail needs to be zipped
            return detail;
        });
    }

    private <T> void reloadDataChangeListener(Class<T> type) {
        checkNotNull(type);
        if (type.equals(Asset.class)) {
            mUserDB.child(DB_ASSETS).removeEventListener(mAssetChildEventListener);
            if (mOnAssetsDataChangeCallback != null) {
                mUserDB.child(DB_ASSETS).addChildEventListener(mAssetChildEventListener);
            }
        } else if (type.equals(Detail.class)) {
            mUserDB.child(DB_DETAILS).removeEventListener(mDetailChildListener);
            if (mOnDetailsDataChangeCallback != null) {
                mUserDB.child(DB_DETAILS).addChildEventListener(mDetailChildListener);
            }
        }
    }

    ChildEventListener mAssetChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mOnAssetsDataChangeCallback.onDataAdded(jsonDataToObject(dataSnapshot, Asset.class));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            mOnAssetsDataChangeCallback.onDataChanged(jsonDataToObject(dataSnapshot, Asset.class));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mOnAssetsDataChangeCallback.onDataRemoved(jsonDataToObject(dataSnapshot, Asset.class));
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            mOnAssetsDataChangeCallback.onDataMoved(jsonDataToObject(dataSnapshot, Asset.class));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ChildEventListener mDetailChildListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mOnDetailsDataChangeCallback.onDataAdded(jsonDataToObject(dataSnapshot, Detail.class));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            mOnDetailsDataChangeCallback.onDataChanged(jsonDataToObject(dataSnapshot, Detail.class));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mOnDetailsDataChangeCallback.onDataRemoved(jsonDataToObject(dataSnapshot, Detail.class));
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            mOnDetailsDataChangeCallback.onDataMoved(jsonDataToObject(dataSnapshot, Detail.class));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private OnUpdatedCallback mDoNothingCallback = new OnUpdatedCallback() {
        @Override
        public void onSuccess(Void aVoid) {
        }

        @Override
        public void onFailure(Throwable e) {
        }

        @Override
        public void onComplete() {
        }
    };

    private void deleteAllRecycledAssets() {
        RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_ASSETS))
                .map(dataSnapshot -> jsonDataToList(dataSnapshot, Asset.class))
                .onBackpressureBuffer()
                .flatMap(Observable::from)
                .filter(Asset::isRecycled)
                .map(Asset::getId)
                .subscribe(id -> mUserDB.child(DB_ASSETS).child(id).removeValue());
    }

    private void deleteAllRecycledDetails() {
        RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_ASSETS))
                .map(dataSnapshot -> jsonDataToList(dataSnapshot, Asset.class))
                .onBackpressureBuffer()
                .flatMap(Observable::from)
                .filter(asset -> !asset.isRecycled())
                .map(Asset::getId)
                .toList()
                .subscribe(
                        assets -> RxFirebaseDatabase
                                .observeSingleValueEvent(mUserDB.child(DB_DETAILS))
                                .map(dataSnapshot -> jsonDataToList(dataSnapshot, Detail.class))
                                .onBackpressureBuffer()
                                .flatMap(Observable::from)
                                .filter(detail -> !assets.contains(detail.getAssetId()))
                                .map(Detail::getId)
                                .subscribe(id -> mUserDB.child(DB_DETAILS).child(id).removeValue())
                );
    }

    private final DatabaseReference mUserDB;
    private final DatabaseReference mAppResDB;
    private final StorageReference mUserST;
    private final LocalResourceLoader mResLoader;
    private final ISchedulerProvider mSchedulerProvider;
    private volatile Features mFeatureToggle;
    private volatile OnDataChangeCallback<Asset> mOnAssetsDataChangeCallback;
    private volatile OnDataChangeCallback<Detail> mOnDetailsDataChangeCallback;

}