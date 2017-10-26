package nz.ac.aut.comp705.sortmystuff.data.remote;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.kelvinapps.rxfirebase.RxFirebaseChildEvent;
import com.kelvinapps.rxfirebase.RxFirebaseDatabase;

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
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.data.models.FDetail.DETAIL_FIELD;

public class FirebaseHelper implements IDataRepository {

    public static final String DB_ASSETS = "assets";
    public static final String DB_DETAILS = "details";

    public FirebaseHelper(LocalResourceLoader resLoader, DatabaseReference databaseReference,
                          StorageReference storageReference, ISchedulerProvider schedulerProvider) {
        mResLoader = checkNotNull(resLoader);
        mDatabase = checkNotNull(databaseReference);
        mStroage = checkNotNull(storageReference);
        mSchedulerProvider = checkNotNull(schedulerProvider);
    }

    @Override
    public Observable<List<FAsset>> retrieveAllAssets() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_ASSETS))
                .map(dataSnapshot -> dataToList(dataSnapshot, FAsset.class));
    }

    @Override
    public Observable<Map<String, FAsset>> retrieveAllAssetsAsMap() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_ASSETS))
                .map(dataSnapshot -> dataToMap(dataSnapshot, FAsset.class));
    }

    @Override
    public Observable<FAsset> retrieveAsset(String assetId) {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_ASSETS).child(checkNotNull(assetId)))
                .map(dataSnapshot -> dataToObject(dataSnapshot, FAsset.class));
    }

    @Override
    public Observable<List<FDetail>> retrieveAllDetails() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_DETAILS))
                .map(dataSnapshot -> dataToList(dataSnapshot, FDetail.class));
    }

    @Override
    public Observable<Map<String, FDetail>> retrieveAllDetailsAsMap() {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_DETAILS))
                .map(dataSnapshot -> dataToMap(dataSnapshot, FDetail.class));
    }

    @Override
    public Observable<List<FDetail>> retrieveDetails(String assetId) {
        return retrieveAsset(assetId)
                .flatMap(asset -> Observable.from(asset.getDetailIds()))
                .flatMap(this::retrieveDetail)
                .toSortedList();
    }

    @Override
    public Observable<Map<String, FDetail>> retrieveDetailsAsMap(String assetId) {
        return retrieveAsset(assetId)
                .flatMap(asset -> Observable.from(asset.getDetailIds()))
                .flatMap(this::retrieveDetail)
                .toMap(FDetail::getId);
    }

    @Override
    public Observable<FDetail> retrieveDetail(String detailId) {
        return RxFirebaseDatabase
                .observeSingleValueEvent(mDatabase.child(DB_DETAILS).child(detailId))
                .map(dataSnapshot -> dataToObject(dataSnapshot, FDetail.class));
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
    public void addOrUpdateAsset(FAsset asset, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        mDatabase.child(DB_ASSETS).child(checkNotNull(asset).getId()).setValue(asset.toMap())
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure)
                .addOnCompleteListener(callback::onComplete);
    }

    @Override
    public void addDetail(FDetail detail, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        mDatabase.child(DB_DETAILS).child(detail.getId()).setValue(detail.toMap())
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure)
                .addOnCompleteListener(callback::onComplete);
    }

    @Override
    public void updateAsset(String assetId, String key, Object value, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        mDatabase.child(DB_ASSETS).child(checkNotNull(assetId)).child(checkNotNull(key))
                .setValue(value)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure)
                .addOnCompleteListener(callback::onComplete);
    }

    @Override
    public void updateDetail(FDetail detail, boolean updatingField, OnUpdatedCallback onUpdatedCallback) {
        OnUpdatedCallback callback = onUpdatedCallback == null ? mDoNothingCallback : onUpdatedCallback;

        if (updatingField) {
            mDatabase.child(DB_DETAILS).child(detail.getId()).setValue(detail.toMap())
                    .addOnSuccessListener(callback::onSuccess)
                    .addOnFailureListener(callback::onFailure)
                    .addOnCompleteListener(callback::onComplete);
            return;
        }

        Map<String, Object> members = detail.toMap();
        List<Task<Void>> tasks = new ArrayList<>();

        for (Map.Entry<String, Object> member : members.entrySet()) {
            if (!member.getKey().equals(DETAIL_FIELD)) {
                Task<Void> updateTask = mDatabase.child(DB_DETAILS).child(detail.getId()).child(member.getKey())
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

    /**
     * Transforms a dataSnapshot of an object node into the object whose type is specified as the
     * given argument. The key of the dataSnapshot should be the id while the value should be
     * the Map containing all the object members.
     *
     * @param dataSnapshot retrieved from the object node
     * @param type         the class of the objects
     * @param <E>          the type of the objects
     * @return the object
     */
    private <E> E dataToObject(DataSnapshot dataSnapshot, Class<E> type) {

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

        } else return null;
    }

    /**
     * Transforms a dataSnapshot in a map of objects whose type is specified as the given argument.
     * The keys of the map are the ids while the values are the objects.
     * The direct children of the given dataSnapshot should be the object nodes whose key is the id
     * of the object and whose value is the Map containing all the object members.
     * <p>
     * The sequence of the map is NOT specified/sorted.
     *
     * @param dataSnapshot retrieved from the parent node of list of type-specified objects
     * @param type         the class of the objects
     * @param <E>          the type of the objects
     * @return a map containing objects transformed from dataSnapshot
     */
    private <E> Map<String, E> dataToMap(DataSnapshot dataSnapshot, Class<E> type) {

        Map<String, E> objects = new HashMap<String, E>();
        if (type.equals(FAsset.class)) {
            for (DataSnapshot objectData : dataSnapshot.getChildren()) {
                FAsset asset = dataToObject(objectData, FAsset.class);
                objects.put(asset.getId(), (E) asset);
            }
        } else if (type.equals(FDetail.class)) {
            for (DataSnapshot objectData : dataSnapshot.getChildren()) {
                FDetail detail = dataToObject(objectData, FDetail.class);
                objects.put(detail.getId(), (E) detail);
            }
        }

        return objects;
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
        return new ArrayList<E>(dataToMap(dataSnapshot, type).values());
    }

    private void registerAssetsDataChangeListener() {
        if (mOnAssetsDataChangeCallback == null) return;

        RxFirebaseDatabase.observeChildEvent(mDatabase.child(DB_ASSETS))
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

    private DatabaseReference mDatabase;
    private StorageReference mStroage;
    private LocalResourceLoader mResLoader;
    private ISchedulerProvider mSchedulerProvider;
    private IDataManager.OnAssetsDataChangeCallback mOnAssetsDataChangeCallback;
    private IDataManager.OnDetailsDataChangeCallback mOnDetailsDataChangeCallback;

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

    //region
}
