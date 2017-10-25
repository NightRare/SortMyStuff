package nz.ac.aut.comp705.sortmystuff.data.remote;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.common.base.Optional;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kelvinapps.rxfirebase.RxFirebaseChildEvent;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;
import com.kelvinapps.rxfirebase.RxFirebaseStorage;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FCategory;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.data.models.FDetail.DETAIL_ID;
import static nz.ac.aut.comp705.sortmystuff.data.models.FDetail.DETAIL_TYPE;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.MAX_DOWNLOAD_BYTES;

public class FirebaseHelper implements IDataRepository {

    public static final String DB_ASSETS = "assets";
    public static final String DB_DETAILS = "details";
    public static final String DB_STORAGEPHOTOS = "storagePhotos";
    public static final String STORAGE_PHOTOS = "photos";


    public FirebaseHelper(LocalResourceLoader resLoader, DatabaseReference databaseReference,
                          StorageReference storageReference) {
        mResLoader = checkNotNull(resLoader);
        mDatabase = checkNotNull(databaseReference);
        mStroage = checkNotNull(storageReference);

        RxFirebaseDatabase.observeSingleValueEvent(mDatabase.child(DB_STORAGEPHOTOS))
                .doOnNext(dataSnapshot -> {
                    Map<String, Object> members = (Map) dataSnapshot.getValue();
                })
                //if not exists
                .doOnError(databaseError -> mDatabase.child(DB_STORAGEPHOTOS).setValue(new ArrayList()))
                .subscribe();
    }

    @Override
    public Observable<List<FAsset>> retrieveAllAssets() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_ASSETS))
                .map(dataSnapshot -> dataToList(dataSnapshot, FAsset.class, null));
    }

    @Override
    public Observable<Map<String, FAsset>> retrieveAllAssetsAsMap() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_ASSETS))
                .map(dataSnapshot -> dataToMap(dataSnapshot, FAsset.class, null));
    }

    @Override
    public Observable<FAsset> retrieveAllAssetsAsStream() {
        // TODO: to be implemented
        return null;
    }

    @Override
    public Observable<FAsset> retrieveAsset(String assetId) {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_ASSETS).child(checkNotNull(assetId)))
                .map(dataSnapshot -> dataToObject(dataSnapshot, FAsset.class, null));
    }

    @Override
    public Observable<List<FDetail>> retrieveAllDetails() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_DETAILS))
                .zipWith(getAllPhotos(), (dataSnapshot, photoMap) ->
                        dataToList(dataSnapshot, FDetail.class, photoMap));
    }

    @Override
    public Observable<Map<String, FDetail>> retrieveAllDetailsAsMap() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_DETAILS))
                .zipWith(getAllPhotos(), (dataSnapshot, photoMap) ->
                        dataToMap(dataSnapshot, FDetail.class, photoMap));
    }

    @Override
    public Observable<FDetail> retrieveAllDetailsAsStream() {
        // TODO: to be implemented
        return null;
    }

    @Override
    public Observable<List<FDetail>> retrieveDetails(String assetId) {
        return retrieveAsset(assetId)
                .flatMap(asset -> Observable.from(asset.getDetailIds()))
                .flatMap(this::retrieveDetail)
                .toList();
    }

    @Override
    public Observable<Map<String, FDetail>> retrieveDetailsAsMap(String assetId) {
        return null;
    }

    @Override
    public Observable<FDetail> retrieveDetailsAsStream(String assetId) {
        return null;
    }

    @Override
    public Observable<FDetail> retrieveDetail(String detailId) {
        return RxFirebaseDatabase.observeSingleValueEvent(mDatabase.child(DB_DETAILS).child(detailId))
                .zipWith(getPhoto(detailId), (dataSnapshot, photoOptional) -> {
                    Bitmap photo = null;
                    // if there's a photo then get it
                    if (photoOptional.isPresent()) {
                        photo = photoOptional.get();
                    }
                    return dataToObject(dataSnapshot, FDetail.class, photo);
                });
    }

    @Override
    public Observable<List<FCategory>> retrieveCategories() {
        return null;
    }

    @Override
    public Observable<Map<String, FCategory>> retrieveCategoriesAsMap() {
        return null;
    }

    @Override
    public Observable<FCategory> retrieveCategoriesAsStream() {
        // TODO: to be implemented
        return null;
    }

    @Override
    public List<FCategory> loadCategories() {
        return null;
    }

    @Override
    public void addOrUpdateAsset(FAsset asset) {
        mDatabase.child(DB_ASSETS).child(checkNotNull(asset).getId()).setValue(asset.toMap());
    }

    @Override
    public void addOrUpdateAsset(FAsset asset, OnUpdatedCallback onUpdatedCallback) {
        mDatabase.child(DB_ASSETS).child(checkNotNull(asset).getId()).setValue(asset.toMap())
                .addOnSuccessListener(onUpdatedCallback::onSuccess)
                .addOnFailureListener(onUpdatedCallback::onFailure)
                .addOnCompleteListener(onUpdatedCallback::onComplete);
    }

    @Override
    public void updateAsset(String assetId, String key, Object value) {
        mDatabase.child(DB_ASSETS).child(checkNotNull(assetId)).child(checkNotNull(key))
                .setValue(checkNotNull(value));
    }

    @Override
    public void updateAsset(String assetId, String key, Object value, OnUpdatedCallback onUpdatedCallback) {
        mDatabase.child(DB_ASSETS).child(checkNotNull(assetId)).child(checkNotNull(key))
                .setValue(checkNotNull(value))
                .addOnSuccessListener(onUpdatedCallback::onSuccess)
                .addOnFailureListener(onUpdatedCallback::onFailure)
                .addOnCompleteListener(onUpdatedCallback::onComplete);
    }

    @Override
    public void addOrUpdateDetail(FDetail detail, boolean updatingImage) {
        addOrUpdateDetail(detail, updatingImage, null);
    }

    @Override
    public void addOrUpdateDetail(FDetail detail, boolean updatingImage, OnUpdatedCallback onUpdatedCallback) {
        checkNotNull(detail, "The detail cannot be null");

        mDatabase.child(DB_DETAILS).child(detail.getId()).setValue(detail.toMap())
                .addOnFailureListener(onUpdatedCallback::onFailure);

        if (!updatingImage) return;

        // if it's an image detail with non-default image then need to upload the image to storage
        if (detail.getType().equals(DetailType.Image)) {
            Bitmap photo = (Bitmap) detail.getField();

            // if the image is changed back to default photo then remove the image file from storage
            // instead of uploading it
            if (mResLoader.getDefaultPhoto().sameAs(photo)) {
                removeImageFromStorage(detail.getId(), onUpdatedCallback);
            } else {
                uploadImageToStorage(detail.getId(), photo, onUpdatedCallback);
            }
        }
    }

    @Override
    public void setOnDataChangeCallback(IDataManager.onDataChangeCallback onDataChangeCallback) {
        checkNotNull(onDataChangeCallback);
        if (onDataChangeCallback instanceof IDataManager.OnAssetsDataChangeCallback) {
            mOnAssetsDataChangeCallback = (IDataManager.OnAssetsDataChangeCallback) onDataChangeCallback;
            registerAssetsDataChangeListener();

        } else if (onDataChangeCallback instanceof IDataManager.OnDetailsDataChangeCallback) {
            mOnDetailsDataChangeCallback = (IDataManager.OnDetailsDataChangeCallback) onDataChangeCallback;
            registerDetailsDataChangeListener();
        }
    }

    //region PRIVATE STUFF

    private Observable<List<String>> getAllCustomisedImageDetailIds() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_STORAGEPHOTOS))
                .map(dataSnapshot -> {
                    List<String> imageDetailIds = (List) dataSnapshot.getValue();
                    return imageDetailIds;
                });
    }

    private Observable<Map<String, Bitmap>> getAllPhotos() {
        return getAllCustomisedImageDetailIds()
                .flatMap(Observable::from)
                .flatMap(id -> RxFirebaseStorage
                        .getBytes(mStroage.child(STORAGE_PHOTOS).child(id), MAX_DOWNLOAD_BYTES)
                        .map(bytes -> new AbstractMap.SimpleEntry<String, byte[]>(id, bytes)))
                .map(byteEntry -> new AbstractMap.SimpleEntry<String, Bitmap>(byteEntry.getKey(),
                        BitmapHelper.toBitmap(byteEntry.getValue())))
                .toList()
                .map(photoEntryList -> {
                    Map<String, Bitmap> photoMap = new HashMap<String, Bitmap>();
                    for (Map.Entry<String, Bitmap> photoEntry : photoEntryList) {
                        photoMap.put(photoEntry.getKey(), photoEntry.getValue());
                    }
                    return photoMap;
                });
    }

    private Observable<Optional<Bitmap>> getPhoto(String detailId) {
        StorageReference sRef = mStroage.child(STORAGE_PHOTOS).child(detailId);
        return RxFirebaseStorage.getBytes(sRef, MAX_DOWNLOAD_BYTES)
                .map(bytes -> Optional.of(BitmapHelper.toBitmap(bytes)))
                .onErrorReturn(throwable -> Optional.absent());
    }


    private <E> E dataToObject(DataSnapshot dataSnapshot, Class<E> type, Bitmap imageField) {

        Map<String, Object> members = (Map) dataSnapshot.getValue();
        if (members == null) return null;
        if (type.equals(FAsset.class)) {
            FAsset asset = FAsset.fromMap(members);
            if (asset.getThumbnail() == null)
                asset.setThumbnail(mResLoader.getDefaultThumbnail(), true);
            return (E) asset;

        } else if (type.equals(FDetail.class)) {
            FDetail detail = FDetail.fromMap(members);
            if (detail.getField() == null && detail.getType().equals(DetailType.Image)) {
                if (imageField != null)
                    detail.setField(imageField);
                else
                    detail.setField(mResLoader.getDefaultPhoto());
            }
            return (E) detail;

        } else return null;
    }

    private <E> Map<String, E> dataToMap(DataSnapshot dataSnapshot, Class<E> type, Map<String, Bitmap> imageFields) {

        Map<String, E> objects = new HashMap<String, E>();
        for (DataSnapshot objectData : dataSnapshot.getChildren()) {
            if (type.equals(FAsset.class)) {
                FAsset asset = dataToObject(objectData, FAsset.class, null);
                objects.put(asset.getId(), (E) asset);

            } else if (type.equals(FDetail.class)) {
                Map<String, Object> members = (Map) objectData.getValue();
                String detailId = (String) members.get(DETAIL_ID);
                FDetail detail = dataToObject(objectData, FDetail.class, checkNotNull(imageFields).get(detailId));
                objects.put(detail.getId(), (E) detail);
            }
        }
        return objects;
    }

    private <E> List<E> dataToList(DataSnapshot dataSnapshot, Class<E> type, Map<String, Bitmap> imageFields) {
        return new ArrayList<E>(dataToMap(dataSnapshot, type, imageFields).values());
    }

    private void uploadImageToStorage(String detailId, Bitmap image, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback;
        if (onUpdatedCallback == null) {
            callback = new OnUpdatedCallback() {
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
        } else callback = onUpdatedCallback;

        byte[] encodedImage = BitmapHelper.toByteArray(image);
        StorageReference imageRef = mStroage.child(STORAGE_PHOTOS).child(detailId);

        UploadTask uploadTask = imageRef.putBytes(encodedImage);
        Observable<DataSnapshot> onUploadSuccess = RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_STORAGEPHOTOS))
                .doOnNext(dataSnapshot -> {
                    List<String> imageDetailIds = (List) dataSnapshot.getValue();
                    if (imageDetailIds == null) imageDetailIds = new ArrayList<String>();
                    imageDetailIds.add(detailId);
                    mDatabase.child(DB_STORAGEPHOTOS).setValue(imageDetailIds)
                            .addOnFailureListener(callback::onFailure);
                });

        uploadTask.addOnSuccessListener(taskSnapshot -> onUploadSuccess.subscribe(
                //onNext
                dataSnapshot -> callback.onSuccess(null),
                //onError
                callback::onFailure
        ));
        uploadTask.addOnFailureListener(callback::onFailure);
        uploadTask.addOnCompleteListener(callback::onComplete);
    }

    private void removeImageFromStorage(String detailId, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback;
        if (onUpdatedCallback == null) {
            callback = new OnUpdatedCallback() {
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
        } else callback = onUpdatedCallback;

        StorageReference imageRef = mStroage.child(STORAGE_PHOTOS).child(detailId);

        Task<Void> task = imageRef.delete();
        Observable<DataSnapshot> onDeleteSuccess = RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_STORAGEPHOTOS))
                .doOnNext(dataSnapshot -> {
                    List<String> imageDetailIds = (List) dataSnapshot.getValue();
                    if (imageDetailIds == null) imageDetailIds = new ArrayList<String>();
                    imageDetailIds.remove(detailId);
                    mDatabase.child(DB_STORAGEPHOTOS).setValue(imageDetailIds)
                            .addOnFailureListener(callback::onFailure);
                });

        task.addOnSuccessListener(aVoid -> onDeleteSuccess.subscribe(
                //onNext
                dataSnapshot -> callback.onSuccess(null),
                //onError
                callback::onFailure
        ));
        task.addOnFailureListener(callback::onFailure);
        task.addOnCompleteListener(callback::onComplete);
    }

    private void registerAssetsDataChangeListener() {
        if (mOnAssetsDataChangeCallback == null) return;

        RxFirebaseDatabase.observeChildEvent(mDatabase.child(DB_ASSETS))
                .subscribe(
                        //onNext
                        dataSnapshotRxFirebaseChildEvent -> {
                            DataSnapshot dataSnapshot = dataSnapshotRxFirebaseChildEvent.getValue();
                            FAsset asset = dataToObject(dataSnapshot, FAsset.class, null);
                            processAssetsListChildData(asset, dataSnapshotRxFirebaseChildEvent.getEventType());
                        }
                        //onError
                );
    }

    private void registerDetailsDataChangeListener() {
        if (mOnDetailsDataChangeCallback == null) return;

        RxFirebaseDatabase.observeChildEvent(mDatabase.child(DB_DETAILS))
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

                            // if the detail does not need get image data from the storage
                            Map<String, Object> members = (Map) dataSnapshot.getValue();
                            DetailType type = DetailType.valueOf(members.get(DETAIL_TYPE).toString());

                            if (type.equals(DetailType.Text) || type.equals(DetailType.Date)) {
                                processDetailsListChildData(dataToObject(dataSnapshot, FDetail.class, null),
                                        eventType);
                            } else if (type.equals(DetailType.Image)) {
                                String detailId = dataSnapshot.getKey();
                                StorageReference sRef = mStroage.child(STORAGE_PHOTOS).child(detailId);
                                RxFirebaseStorage.getBytes(sRef, MAX_DOWNLOAD_BYTES)
                                        .subscribe(
                                                //onNext
                                                bytes -> {
                                                    Bitmap image = BitmapHelper.toBitmap(bytes);
                                                    FDetail detail = dataToObject(dataSnapshot, FDetail.class, image);
                                                    processDetailsListChildData(detail, eventType);
                                                },
                                                //onError if there's no image file then use the default image
                                                e -> {
                                                    FDetail detail = dataToObject(dataSnapshot, FDetail.class,
                                                            mResLoader.getDefaultPhoto());
                                                    processDetailsListChildData(detail, eventType);
                                                }
                                        );
                            }
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

    private DatabaseReference mDatabase;
    private StorageReference mStroage;
    private LocalResourceLoader mResLoader;
    private IDataManager.OnAssetsDataChangeCallback mOnAssetsDataChangeCallback;
    private IDataManager.OnDetailsDataChangeCallback mOnDetailsDataChangeCallback;


    //region
}
