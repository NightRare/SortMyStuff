package nz.ac.aut.comp705.sortmystuff.data.remote;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.kelvinapps.rxfirebase.RxFirebaseChildEvent;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.IDebugHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FCategory;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import nz.ac.aut.comp705.sortmystuff.utils.Log;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.data.models.FDetail.DETAIL_FIELD;

public class FirebaseHelper implements IDataRepository, IDebugHelper {

    public static final String DB_ASSETS = "assets";
    public static final String DB_DETAILS = "details";
    public static final String DB_CATEGORIES = "categories";

    public FirebaseHelper(LocalResourceLoader resLoader, DatabaseReference userDB,
                          DatabaseReference appResDB, StorageReference storageReference,
                          ISchedulerProvider schedulerProvider) {
        mResLoader = checkNotNull(resLoader);
        mUserDB = checkNotNull(userDB);
        mAppResDB = checkNotNull(appResDB);
        mStroage = checkNotNull(storageReference);
        mSchedulerProvider = checkNotNull(schedulerProvider);
    }

    @Override
    public Observable<List<FAsset>> retrieveAllAssets() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_ASSETS))
                .map(dataSnapshot -> dataToList(dataSnapshot, FAsset.class));
    }

    @Override
    public Observable<FAsset> retrieveAsset(String assetId) {
        checkNotNull(assetId, "The assetId cannot be null.");
        return RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_ASSETS).child(checkNotNull(assetId)))
                .map(dataSnapshot -> dataToObject(dataSnapshot, FAsset.class))
                .onErrorReturn(throwable -> {
                    Log.e(FirebaseHelper.class.getName(), throwable.getMessage(), throwable);
                    return null;
                });
    }

    @Override
    public Observable<List<FDetail>> retrieveAllDetails() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_DETAILS))
                .map(dataSnapshot -> dataToList(dataSnapshot, FDetail.class));
    }

    @Override
    public Observable<List<FDetail>> retrieveDetails(String assetId) {
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
    public Observable<FDetail> retrieveDetail(String detailId) {
        checkNotNull(detailId, "The detailId cannot be null.");
        return RxFirebaseDatabase
                .observeSingleValueEvent(mUserDB.child(DB_DETAILS).child(detailId))
                .map(dataSnapshot -> dataToObject(dataSnapshot, FDetail.class));
    }

    @Override
    public Observable<List<FCategory>> retrieveCategories() {
        return RxFirebaseDatabase.observeSingleValueEvent(mAppResDB.child(DB_CATEGORIES))
                .map(dataSnapshot -> dataToList(dataSnapshot, FCategory.class));
    }

    @Override
    public void addOrUpdateAsset(FAsset asset, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        mUserDB.child(DB_ASSETS).child(checkNotNull(asset).getId()).setValue(asset.toMap())
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure)
                .addOnCompleteListener(callback::onComplete);
    }

    @Override
    public void addDetail(FDetail detail, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        retrieveDetail(checkNotNull(detail).getId())
                .subscribeOn(mSchedulerProvider.io())
                .doOnNext(detailFromFirebase -> {
                    // should not update an existing record
                    if (detailFromFirebase != null) throw new IllegalStateException();
                })
                .subscribe(
                        //onNext
                        isEmpty -> mUserDB.child(DB_DETAILS).child(detail.getId()).setValue(detail.toMap())
                                .addOnSuccessListener(callback::onSuccess)
                                .addOnFailureListener(callback::onFailure)
                                .addOnCompleteListener(callback::onComplete),
                        //onError
                        throwable -> {
                            callback.onFailure(throwable);
                            callback.onComplete(null);
                        }
                );
    }

    @Override
    public <E> void updateAsset(String assetId, String key, E value, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        if (value != null && !FAsset.getMemberClassForDatabase(key).isAssignableFrom(value.getClass())) {
            IllegalArgumentException e = new IllegalArgumentException("Incorrect value type.");
            callback.onFailure(e);
            throw e;
        }

        mUserDB.child(DB_ASSETS).child(checkNotNull(assetId)).child(checkNotNull(key))
                .setValue(value)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure)
                .addOnCompleteListener(callback::onComplete);
    }

    @Override
    public void updateDetail(FDetail detail, boolean updatingField, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        if (updatingField) {
            mUserDB.child(DB_DETAILS).child(detail.getId()).setValue(detail.toMap())
                    .addOnSuccessListener(callback::onSuccess)
                    .addOnFailureListener(callback::onFailure)
                    .addOnCompleteListener(callback::onComplete);
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
                .addOnCompleteListener(callback::onComplete);
    }

    @Override
    public void setOnDataChangeCallback(OnDataChangeCallback onDataChangeCallback) {
        if (onDataChangeCallback == null) {
            mOnAssetsDataChangeCallback = null;
            mOnDetailsDataChangeCallback = null;
        } else if (onDataChangeCallback instanceof OnAssetsDataChangeCallback) {
            mOnAssetsDataChangeCallback = (OnAssetsDataChangeCallback) onDataChangeCallback;
            attachAssetsDataChangeListener();

        } else if (onDataChangeCallback instanceof OnDetailsDataChangeCallback) {
            mOnDetailsDataChangeCallback = (OnDetailsDataChangeCallback) onDataChangeCallback;
            attachDetailsDataChangeListener();
        }
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
    private <E> E dataToObject(DataSnapshot dataSnapshot, Class<E> type) {
        if (dataSnapshot == null) return null;
        Map<String, Object> members = (Map) dataSnapshot.getValue();
        if (members == null) return null;

        if (type.equals(FAsset.class)) {
            FAsset asset = FAsset.fromMap(members);
            if (asset.getThumbnail() == null)
                asset.setThumbnail(mResLoader.getDefaultThumbnail(), true);
            return (E) asset;

        } else if (type.equals(FDetail.class)) {
            FDetail detail = FDetail.fromMap(members);

            if (detail.isDefaultFieldValue() && detail.getType().equals(DetailType.Image)) {
                detail.setField(mResLoader.getDefaultPhoto(), true);
            }
            return (E) detail;

        } else if (type.equals(FCategory.class)) {
            FCategory category = FCategory.fromMap(members);
            category.setDefaultFieldValue(mResLoader.getDefaultText(), mResLoader.getDefaultPhoto());

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
    private <E> List<E> dataToList(DataSnapshot dataSnapshot, Class<E> type) {
        List<E> objects = new ArrayList<>();
        for (DataSnapshot objectData : dataSnapshot.getChildren()) {
            objects.add((E) dataToObject(objectData, type));
        }
        return objects;
    }

    private void attachAssetsDataChangeListener() {
        if (mOnAssetsDataChangeCallback == null) return;

        RxFirebaseDatabase.observeChildEvent(mUserDB.child(DB_ASSETS))
                .subscribe(
                        //onNext
                        dataSnapshotRxFirebaseChildEvent -> {
                            DataSnapshot dataSnapshot = dataSnapshotRxFirebaseChildEvent.getValue();
                            FAsset asset = dataToObject(dataSnapshot, FAsset.class);
                            processAssetsListChildData(asset, dataSnapshotRxFirebaseChildEvent.getEventType());
                        }
                        //onError
                );
    }

    private void attachDetailsDataChangeListener() {
        if (mOnDetailsDataChangeCallback == null) return;

        RxFirebaseDatabase.observeChildEvent(mUserDB.child(DB_DETAILS))
                .subscribe(
                        //onNext
                        dataSnapshotRxFirebaseChildEvent -> {
                            DataSnapshot dataSnapshot = dataSnapshotRxFirebaseChildEvent.getValue();
                            RxFirebaseChildEvent.EventType eventType = dataSnapshotRxFirebaseChildEvent.getEventType();

                            // if the event is Detail Added then do nothing, otherwise every detail
                            // including images will be downloaded at the initialisation of this FirebaseHelper
                            if (eventType.equals(RxFirebaseChildEvent.EventType.ADDED)) {
                                mOnDetailsDataChangeCallback.onDetailAdded(null);
                                return;
                            }

                            processDetailsListChildData(dataToObject(dataSnapshot, FDetail.class), eventType);
                        }
                        //onError
                );
    }

    private void processAssetsListChildData(FAsset asset, RxFirebaseChildEvent.EventType eventType) {
        switch (eventType) {
            case ADDED:
                mOnAssetsDataChangeCallback.onAssetAdded(asset);
                break;
            case CHANGED:
                mOnAssetsDataChangeCallback.onAssetChanged(asset);
                break;
            case REMOVED:
                mOnAssetsDataChangeCallback.onAssetRemoved(asset);
                break;
            case MOVED:
                mOnAssetsDataChangeCallback.onAssetMoved(asset);
                break;
            default:
        }
    }

    private void processDetailsListChildData(FDetail detail, RxFirebaseChildEvent.EventType eventType) {


        switch (eventType) {
            case ADDED:
                mOnDetailsDataChangeCallback.onDetailAdded(detail);
                break;
            case CHANGED:
                mOnDetailsDataChangeCallback.onDetailChanged(detail);
                break;
            case REMOVED:
                mOnDetailsDataChangeCallback.onDetailRemoved(detail);
                break;
            case MOVED:
                mOnDetailsDataChangeCallback.onDetailMoved(detail);
                break;
            default:
        }
    }

    private DatabaseReference mUserDB;
    private DatabaseReference mAppResDB;
    private StorageReference mStroage;
    private LocalResourceLoader mResLoader;
    private ISchedulerProvider mSchedulerProvider;
    private OnAssetsDataChangeCallback mOnAssetsDataChangeCallback;
    private OnDetailsDataChangeCallback mOnDetailsDataChangeCallback;

    private OnUpdatedCallback mDoNothingCallback = new OnUpdatedCallback() {
        @Override
        public void onSuccess(Void aVoid) {
        }

        @Override
        public void onFailure(Throwable e) {
        }

        @Override
        public void onComplete(Task task) {
        }
    };

    @Override
    public void removeCurrentUserData() {
        mUserDB.removeValue();
    }

    //region
}
