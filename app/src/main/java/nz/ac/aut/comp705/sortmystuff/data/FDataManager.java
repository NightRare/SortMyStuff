package nz.ac.aut.comp705.sortmystuff.data;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.Category;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import nz.ac.aut.comp705.sortmystuff.utils.Log;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_CONTAINERID;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_CONTENTIDS;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_MODIFYTIMESTAMP;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_NAME;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_RECYCLED;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_THUMBNAIL;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ROOT_ASSET_ID;

/**
 * Implementation class of IDataManager
 *
 * @author Yuan
 */

public class FDataManager implements IDataManager {


    public FDataManager(IDataRepository remoteRepo, IFileHelper fileHelper, LocalResourceLoader resLoader,
                        ISchedulerProvider schedulerProvider) {
        mRemoteRepo = checkNotNull(remoteRepo);
        mFileHelper = checkNotNull(fileHelper);
        mResLoader = checkNotNull(resLoader);
        mSchedulerProvider = checkNotNull(schedulerProvider);

        mDirtyCachedAssets = true;
        mDirtyCachedDetails = true;

        initCachedDetails();
        mRemoteRepo.setOnDataChangeCallback(new OnDetailsDataChangeListeners());
        mRemoteRepo.setOnDataChangeCallback(new OnAssetsDataChangeListeners());

        cacheAssets();
    }

    //region IDataManger METHODS

    @Override
    public Observable<List<IAsset>> getAssets() {

        if (mDirtyCachedAssets || mCachedAssets == null) {
            return mRemoteRepo.retrieveAllAssets()
                    .flatMap(Observable::from)
                    .filter(fAsset -> !fAsset.isRecycled())
                    .map(fAsset -> (IAsset) fAsset)
                    .toList();
        }

        return Observable.from(mCachedAssets.values())
                .map(asset -> (IAsset) asset)
                .toList();
    }

    @Override
    public Observable<List<IAsset>> getRecycledAssets() {
        if (mDirtyCachedAssets || mCachedRecycledAssets == null) {
            return mRemoteRepo.retrieveAllAssets()
                    .flatMap(Observable::from)
                    .filter(FAsset::isRecycled)
                    .map(fAsset -> (IAsset) fAsset)
                    .toList();
        }

        return Observable.from(mCachedRecycledAssets.values())
                .map(asset -> (IAsset) asset)
                .toList();
    }

    @Override
    public Observable<List<IDetail>> getDetails(String assetId) {
        checkNotNull(assetId);

        // if get the Details of the Root asset, always return empty list
        if (assetId.equals(ROOT_ASSET_ID)) {
            return Observable.just(new ArrayList<>());
        }

        return loadAndCacheDetails(assetId)
                .flatMap(Observable::from)
                .map(detail -> (IDetail) detail)
                .toList();
    }

    @Override
    public Observable<IAsset> getAsset(String id) {
        checkNotNull(id);

        if (mDirtyCachedAssets || mCachedAssets == null) {
            return mRemoteRepo.retrieveAsset(id)
                    .map(asset -> (IAsset) asset);
        }

        return Observable.just(mCachedAssets.get(id));
    }

    @Override
    public Observable<List<IAsset>> getContentAssets(String assetId) {
        checkNotNull(assetId);

        if (mDirtyCachedAssets || mCachedAssets == null) {
            return mRemoteRepo.retrieveAsset(assetId)
                    .flatMap(fAsset -> Observable.from(fAsset.getContentIds()))
                    .flatMap(this::getAsset)
                    .toList();
        }

        FAsset container = mCachedAssets.get(assetId);
        List<IAsset> contents = new ArrayList<>();
        for (String childId : container.getContentIds()) {
            contents.add(mCachedAssets.get(childId));
        }
        return Observable.just(contents);
    }

    @Override
    public Observable<List<IAsset>> getParentAssets(String assetId, boolean rootToChildren) {
        checkNotNull(assetId, "The assetId cannot be null.");

        if (mDirtyCachedAssets || mCachedAssets == null) {
            return mRemoteRepo.retrieveAllAssetsAsMap()
                    .map(allAssets -> getParentAssetsList(
                            allAssets.get(assetId), rootToChildren, allAssets))
                    .flatMap(Observable::from)
                    .map(fAsset -> (IAsset) fAsset)
                    .toList();
        }

        List<FAsset> results = getParentAssetsList(mCachedAssets.get(assetId), rootToChildren);
        return Observable.from(results)
                .map(a -> (IAsset) a)
                .toList();
    }

    @Override
    public String createAsset(String name, String containerId) {
        return createAsset(name, containerId, CategoryType.Miscellaneous);
    }

    @Override
    public String createAsset(String name, String containerId, CategoryType categoryType) {
        checkNotNull(name);
        checkNotNull(containerId);
        Preconditions.checkArgument(!name.replaceAll(" ", "").isEmpty(), "The name cannot be empty");
        Preconditions.checkArgument(name.length() <= AppConstraints.ASSET_NAME_CAP, "The length of the name should be shorter than "
                + AppConstraints.ASSET_NAME_CAP + " characters");

        FAsset asset = FAsset.create(name, containerId, categoryType);
        List<FDetail> details = getCategory(categoryType).generateFDetails(asset.getId());
        for (FDetail d : details) {
            asset.addDetailId(d.getId());
            mRemoteRepo.addOrUpdateDetail(d, false, mOnDetailUpdated);
        }
        mRemoteRepo.addOrUpdateAsset(asset, mOnAssetUpdated);

        LoggedAction updateCacheData = () -> {
            mCachedAssets.get(containerId).addContentId(asset.getId());
            mCachedAssets.put(asset.getId(), asset);
        };

        if (mDirtyCachedAssets) {
            // needs to update the contentIds of the container asset
            mRemoteRepo.retrieveAsset(containerId)
                    .subscribeOn(mSchedulerProvider.io())
                    .subscribe(container -> {
                        container.addContentId(asset.getId());
                        mRemoteRepo.addOrUpdateAsset(container, mOnAssetUpdated);
                        mActionsQueue.add(updateCacheData);
                    });
        } else {
            updateCacheData.execute();
            mRemoteRepo.addOrUpdateAsset(mCachedAssets.get(containerId), mOnAssetUpdated);
        }

        return asset.getId();
    }

    @Deprecated
    @Override
    public String createTextDetail(Asset asset, String label, String field) {
        // no implementation for deprecated method
        return null;
    }

    @Deprecated
    @Override
    public String createTextDetail(final String assetId, String label, String field) {
        // no implementation for deprecated method
        return null;
    }

    @Override
    public IAsset getRoot() {
        return null;
    }

    @Override
    public Observable<IAsset> getRootAsset() {
        if (mDirtyCachedAssets) {
            return mRemoteRepo.retrieveAsset(ROOT_ASSET_ID)
                    .map(asset -> {
                        if (asset == null) {
                            return (IAsset) createRootAsset();
                        } else {
                            return (IAsset) asset;
                        }
                    });
        } else {
            return Observable.just(mCachedRootAsset);
        }
    }

    @Override
    @Deprecated
    public void updateAssetName(Asset asset, String newName) {
        checkNotNull(asset);

        updateAssetName(asset.getId(), newName);
    }

    @Override
    public void updateAssetName(String assetId, final String newName) {
        checkNotNull(assetId);
        checkNotNull(newName);
        Preconditions.checkArgument(!newName.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(newName.length() < AppConstraints.ASSET_NAME_CAP);

        LoggedAction updateAsset = () -> mCachedAssets.get(assetId).setName(newName);

        if (mDirtyCachedAssets) {
            mActionsQueue.add(updateAsset);

        } else {
            updateAsset.execute();
        }

        mRemoteRepo.updateAsset(assetId, ASSET_NAME, newName, mOnAssetUpdated);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public void moveAsset(Asset asset, String newContainerId) {
        checkNotNull(asset);

        moveAsset(asset.getId(), newContainerId);
    }

    @Override
    public void moveAsset(String assetId, String newContainerId) {
        checkNotNull(assetId);
        checkNotNull(newContainerId);

        if (mDirtyCachedAssets) {
            mRemoteRepo.retrieveAsset(assetId)
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(asset -> mRemoteRepo.retrieveAsset(asset.getContainerId()))
                    .zipWith(mRemoteRepo.retrieveAsset(assetId), (from, asset) -> {
                        List<FAsset> args = new ArrayList<>();
                        args.add(0, asset);
                        args.add(1, from);
                        return args;
                    })
                    .zipWith(mRemoteRepo.retrieveAsset(newContainerId), (args, to) -> {
                        args.add(2, to);
                        return args;
                    })
                    .subscribe(
                            //onNext
                            args -> {
                                FAsset asset = args.get(0);
                                FAsset from = args.get(1);
                                FAsset to = args.get(2);

                                moveAssetAndUpdateRemoteRepo(asset, from, to);
                                mActionsQueue.add(() -> moveAssetUpdateCache(asset, from, to));
                            }
                    );

        } else {
            FAsset asset = mCachedAssets.get(assetId);
            FAsset from = mCachedAssets.get(asset.getContainerId());
            FAsset to = mCachedAssets.get(newContainerId);
            moveAssetAndUpdateRemoteRepo(asset, from, to);
            moveAssetUpdateCache(asset, from, to);
        }
    }

    @Override
    public void recycleAssetRecursively(String assetId) {
        checkNotNull(assetId);

        if (mDirtyCachedAssets) {
            mRemoteRepo.retrieveAsset(assetId)
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(this::recycleAssetGetAllChildrenAssets)
                    .subscribe(
                            //onNext
                            this::recycleAsset,
                            //onError
                            mOnAssetUpdated::onFailure
                    );
        } else {
            FAsset asset = mCachedAssets.get(assetId);

            if (!asset.getContentIds().isEmpty()) {
                for (String id : asset.getContentIds()) {
                    recycleAssetRecursively(id);
                }
            }
            recycleAsset(asset);
        }
    }

    @Deprecated
    @Override
    public void recycleAssetRecursively(Asset asset) {
        checkNotNull(asset);

        recycleAssetRecursively(asset.getId());
    }

    @Override
    @Deprecated
    public void removeDetail(Detail detail) {
        // no implementation for deprecated method
    }

    @Override
    @Deprecated
    public void removeDetail(String assetId, String detailId) {
        // no implementation for deprecated method
    }

    @Override
    @Deprecated
    public void updateTextDetail(TextDetail detail,
                                 String label, String field) {
        checkNotNull(detail);

        updateTextDetail(detail.getAssetId(), detail.getId(), label, field);
    }

    @Override
    public void updateTextDetail(String assetId, String detailId,
                                 String label, String field) {
        checkNotNull(assetId);
        checkNotNull(detailId);
        checkNotNull(label);
        checkNotNull(field);
        Preconditions.checkArgument(!label.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(label.length() <= AppConstraints.DETAIL_LABEL_CAP,
                "Please keep the length of the text within " + AppConstraints.DETAIL_LABEL_CAP + " characters");
        Preconditions.checkArgument(field.length() <= AppConstraints.TEXTDETAIL_FIELD_CAP,
                "Please keep the length of the text within " + AppConstraints.TEXTDETAIL_FIELD_CAP + " characters");

        updateDetail(assetId, detailId, label, field);
    }

    @Override
    @Deprecated
    public void resetImageDetail(IDetail<Bitmap> detail) {
        resetImageDetail(detail.getAssetId(), detail.getId());
    }


    @Override
    public void resetImageDetail(String assetId, String detailId) {
        checkNotNull(assetId);
        checkNotNull(detailId);

        updateDetail(assetId, detailId, null, mResLoader.getDefaultPhoto());
    }

    @Override
    @Deprecated
    public void updateImageDetail(ImageDetail detail, String label, Bitmap field) {
        updateImageDetail(detail.getAssetId(), detail.getId(), label, field);
    }

    @Override
    @Deprecated
    public void updateImageDetail(IDetail<Bitmap> detail, String label, Bitmap field) {
        updateImageDetail(detail.getAssetId(), detail.getId(), label, field);
    }

    @Override
    public void updateImageDetail(String assetId, String detailId, String label, Bitmap field) {
        checkNotNull(assetId);
        checkNotNull(detailId);
        checkNotNull(label);
        checkNotNull(field);
        Preconditions.checkArgument(!label.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(label.length() <= AppConstraints.DETAIL_LABEL_CAP,
                "Please keep the length of the text within " + AppConstraints.DETAIL_LABEL_CAP + " characters");

        updateDetail(assetId, detailId, label, field);
    }

    @Override
    public void refreshFromLocal() {

    }

    @Override
    public void reCacheFromRemoteDataSource() {
        cacheAssets();
        mDirtyCachedDetails = true;
        initCachedDetails();
    }

    //endregion

    //region DEPRECATED METHODS

    @Override
    public void getAllAssetsAsync(LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getRecycledAssetsAsync(LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getContentAssetsAsync(Asset container, LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getContentAssetsAsync(String containerId, LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getParentAssetsAsync(Asset asset, LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getParentAssetsAsync(String assetId, LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getParentAssetsDescAsync(Asset asset, LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getParentAssetsDescAsync(String assetId, LoadAssetsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getAssetAsync(String assetId, GetAssetCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getDetailsAsync(Asset asset, LoadDetailsCallback callback) {
        //TODO: TO BE DELETED
    }

    @Override
    public void getDetailsAsync(String assetId, LoadDetailsCallback callback) {
        //TODO: TO BE DELETED
    }

    //endregion

    interface LoggedAction {

        void execute();
    }

    //region PRIVATE STUFF

    private void cacheAssets() {
        mDirtyCachedAssets = true;
        mActionsQueue = new ArrayList<>();
        mCachedAssets = new HashMap<>();
        mCachedRecycledAssets = new HashMap<>();

        getRootAsset()
                .flatMap(asset -> mRemoteRepo.retrieveAllAssets())
                .doOnNext(assets -> {
                    for (FAsset a : assets) {
                        putAssetIntoCacheObjects(a);
                    }
                })
                .doOnCompleted(() -> {
                    // apply logged changes to the cache as the caching has ended
                    while (!mActionsQueue.isEmpty()) {
                        mActionsQueue.remove(0).execute();
                    }
                    mDirtyCachedAssets = false;
                })
                .subscribe();
    }

    private void putAssetIntoCacheObjects(FAsset asset) {
        if (asset.isRecycled()) {
            mCachedRecycledAssets.put(asset.getId(), asset);
        } else {
            if (asset.isRoot()) mCachedRootAsset = asset;
            mCachedAssets.put(asset.getId(), asset);
        }
    }

    private void initCachedDetails() {
        mCachedDetails = new HashMap<>();
        mCachedDetailsKeyList = new LinkedList<>();
        mDirtyCachedDetails = false;
    }

    private Observable<List<FDetail>> loadAndCacheDetails(String assetId) {
        if (mDirtyCachedDetails || mCachedDetails == null) {
            initCachedDetails();
        } else if (mCachedDetails.containsKey(assetId)) {
            return Observable.just(mCachedDetails.get(assetId));
        }

        // Releases one cache from the cachedDetails map if the size of the cache is bigger than the limit.
        // The cache algorithm now is LRU (Latest Recently Used).
        if (mCachedDetails.size() >= AppConstraints.CACHED_DETAILS_LIST_NUM) {
            mCachedDetails.remove(mCachedDetailsKeyList.get(0));
        }

        return mRemoteRepo.retrieveDetails(assetId)
                .doOnNext(details -> {
                    mCachedDetails.put(assetId, details);
                    mCachedDetailsKeyList.add(assetId);
                });
    }

    private Category getCategory(CategoryType categoryType) {
        if (mCategories == null) {
            mCategories = new HashMap<>();
            for (Category c : mFileHelper.deserialiseCategories()) {
                mCategories.put(Enum.valueOf(CategoryType.class, c.getName()),
                        c);
            }
        }

        return mCategories.get(categoryType);
    }

    private FAsset createRootAsset() {
        FAsset root = FAsset.createRoot();
        mRemoteRepo.addOrUpdateAsset(root);
        return root;
    }

    private void updateAssetModifyTimestamp(String assetId, long modifyTimestamp) {
        LoggedAction updateModifyTimestamp = () -> mCachedAssets.get(assetId).setModifyTimestamp(modifyTimestamp);
        if (mDirtyCachedAssets) {
            mActionsQueue.add(updateModifyTimestamp);
        } else {
            updateModifyTimestamp.execute();
        }
        mRemoteRepo.updateAsset(assetId, ASSET_MODIFYTIMESTAMP, modifyTimestamp, mOnAssetUpdated);
    }

    /**
     * Checks if a1 is the parent of a2.
     *
     * @param a1 the asset to be checked
     * @return true if this asset is the parent of the given asset
     */
    private boolean isParentOf(FAsset a1, FAsset a2) {
        if (a1.getId().equals(a2.getId())) return false;
        if (a1.isRoot()) return true;
        if (a2.isRoot()) return false;

        if (a2.getContainerId().equals(a1.getId()))
            return true;
        else {
            return isParentOf(a1, mCachedAssets.get(a2.getContainerId()));
        }
    }

    private List<FAsset> getParentAssetsList(FAsset asset, boolean rootToChildren) {
        return getParentAssetsList(asset, rootToChildren, mCachedAssets);
    }

    private List<FAsset> getParentAssetsList(FAsset asset, boolean rootToChildren,
                                             Map<String, FAsset> assetsMap) {
        List<FAsset> results = new ArrayList<>();
        //children to root order
        while (!asset.isRoot()) {
            results.add(asset);
            asset = assetsMap.get(asset.getContainerId());
        }
        // add the root
        results.add(asset);
        if (rootToChildren) Collections.reverse(results);
        return results;
    }

    private void moveAssetAndUpdateRemoteRepo(FAsset asset, FAsset from, FAsset to) {
        if (isParentOf(asset, to) || !asset.move(from, to)) {
            Log.e(getClass().getName(), "move asset failed, asset id: " + asset.getId()
                    + " container id: " + to.getId());
            return;
        }

        // update remote repo
        mRemoteRepo.updateAsset(asset.getId(), ASSET_CONTAINERID, asset.getContainerId(), mOnAssetUpdated);
        mRemoteRepo.updateAsset(from.getId(), ASSET_CONTENTIDS, from.getContentIds(), mOnAssetUpdated);
        mRemoteRepo.updateAsset(to.getId(), ASSET_CONTENTIDS, to.getContentIds(), mOnAssetUpdated);
    }

    private void moveAssetUpdateCache(FAsset asset, FAsset from, FAsset to) {
        mCachedAssets.put(asset.getId(), asset);
        mCachedAssets.put(from.getId(), from);
        mCachedAssets.put(to.getId(), to);
    }

    private void recycleAsset(FAsset asset) {
        checkNotNull(asset);

        LoggedAction recycleAction = () -> {
            mCachedAssets.get(asset.getId()).setRecycled(true);
            mCachedRecycledAssets.put(asset.getId(), mCachedAssets.remove(asset.getId()));
        };

        if (mDirtyCachedAssets) {
            mActionsQueue.add(recycleAction);
        } else {
            recycleAction.execute();
        }

        mRemoteRepo.updateAsset(asset.getId(), ASSET_RECYCLED, true, mOnAssetUpdated);
    }

    private Observable<FAsset> recycleAssetGetAllChildrenAssets(FAsset asset) {
        if (asset.getContentIds().isEmpty())
            return Observable.just(asset);

        return Observable.merge(
                Observable.just(asset),
                Observable.from(asset.getContentIds())
                        .flatMap(id -> mRemoteRepo.retrieveAsset(id))
                        .flatMap(this::recycleAssetGetAllChildrenAssets));
    }

    private void updateAssetThumbnailAndUpdateInRemote(String assetId, Bitmap originalPhoto, boolean usingDefaultPhoto) {
        Bitmap thumbnail = usingDefaultPhoto ?
                mResLoader.getDefaultThumbnail() : BitmapHelper.toThumbnail(originalPhoto);
        String thumbnailDataString = usingDefaultPhoto ? null : BitmapHelper.toString(thumbnail);

        LoggedAction updateThumbnail = () -> {
            FAsset asset = mCachedAssets.get(assetId);
            if (asset != null) {
                asset.setThumbnail(thumbnail, usingDefaultPhoto);
            }
        };

        if (mDirtyCachedAssets) {
            mActionsQueue.add(updateThumbnail);
        } else {
            updateThumbnail.execute();
        }

        mRemoteRepo.updateAsset(assetId, ASSET_THUMBNAIL, thumbnailDataString, mOnAssetUpdated);
    }

    private <E> void updateDetail(String assetId, String detailId, String newLabel, E newField) {
        if (mDirtyCachedDetails) {
            initCachedDetails();
        }

        // if field and label are all null then nothing is changed
        long modifyTimestamp = (newLabel == null && newField == null) ? 0L : System.currentTimeMillis();

        if (mCachedDetails.containsKey(assetId)) {
            for (FDetail detail : mCachedDetails.get(assetId)) {
                if (detail.getId().equals(detailId)) {
                    long newTimestamp = modifyTimestamp == 0 ? detail.getModifyTimestamp() : modifyTimestamp;
                    updateDetailAndUpdateInRemote(detail, newLabel, newField, newTimestamp);
                    updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    break;
                }
            }
        } else {
            mRemoteRepo.retrieveDetail(detailId)
                    .subscribeOn(mSchedulerProvider.io())
                    .doOnNext(detail -> {
                        long newTimestamp = modifyTimestamp == 0 ? detail.getModifyTimestamp() : modifyTimestamp;
                        updateDetailAndUpdateInRemote(detail, newLabel, newField, newTimestamp);
                        updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    })
                    .subscribe();
        }
    }

    /**
     * Updates the label and/or the field of the detail, and update the changes to the remote repository.
     *
     * @param detail          the detail
     * @param newLabel        the newLabel; or {@code null} if it won't be changed
     * @param newField        the newField; or {@code null} if it won't be changed
     * @param modifyTimestamp the modifyTimestamp
     * @param <E>             the type of the field
     */
    private <E> void updateDetailAndUpdateInRemote(FDetail<E> detail, String newLabel,
                                                   E newField, long modifyTimestamp) {
        Class fieldType = newField == null ? null : newField.getClass();

        if (fieldType != null) {
            if (!detail.getType().getFieldClass().equals(fieldType))
                throw new IllegalArgumentException("Incorrect type of detail.");
        }

        if (newLabel != null)
            detail.setLabel(newLabel);

        if (newField != null)
            detail.setField(newField);

        detail.setModifyTimestamp(modifyTimestamp);

        boolean updatingImage = detail.getType().equals(DetailType.Image)
                && (fieldType == null ? false : fieldType.equals(DetailType.Image.getFieldClass()));

        mRemoteRepo.addOrUpdateDetail(detail, updatingImage, mOnDetailUpdated);
        if (updatingImage) {
            boolean usingDefaultPhoto = mResLoader.getDefaultPhoto().sameAs((Bitmap) newField);
            updateAssetThumbnailAndUpdateInRemote(detail.getAssetId(), (Bitmap) newField, usingDefaultPhoto);
        }
    }

    private class OnAssetsDataChangeListeners implements OnAssetsDataChangeCallback {

        @Override
        public void onAssetAdded(FAsset asset) {
            putAssetIntoCacheObjects(asset);
        }

        @Override
        public void onAssetChanged(FAsset asset) {
            LoggedAction updateCache = () -> mCachedAssets.put(asset.getId(), asset);
            if (mDirtyCachedAssets) {
                mActionsQueue.add(updateCache);
            } else {
                updateCache.execute();
            }
        }

        @Override
        public void onAssetRemoved(FAsset asset) {
            // assets won't be removed
        }

        @Override
        public void onAssetMoved(FAsset asset) {
            // does not matter
        }
    }

    private class OnDetailsDataChangeListeners implements OnDetailsDataChangeCallback {

        @Override
        public void onDetailAdded(FDetail detail) {
            // does not matter for now, because details won't be added without adding a new asset
            // will pass in null, as currently set up in FirebaseHelper
        }

        @Override
        public void onDetailChanged(FDetail detail) {

            if (mDirtyCachedDetails) {
                initCachedDetails();
            } else {
                if (!mCachedDetails.containsKey(detail.getAssetId())) return;

                for (FDetail d : mCachedDetails.get(detail.getAssetId())) {
                    if (d.getId().equals(detail.getId())) {
                        if(!d.getModifyTimestamp().equals(detail.getModifyTimestamp()))
                            d.overwrittenBy(detail);
                        break;
                    }
                }
            }
        }

        @Override
        public void onDetailRemoved(FDetail detail) {
            // won't be removed for now
        }

        @Override
        public void onDetailMoved(FDetail detail) {
            // does not matter
        }
    }

    private IDataRepository mRemoteRepo;
    private ISchedulerProvider mSchedulerProvider;
    private IFileHelper mFileHelper;
    private LocalResourceLoader mResLoader;
    private Map<CategoryType, Category> mCategories;

    private List<LoggedAction> mActionsQueue;

    private FAsset mCachedRootAsset;
    private Map<String, FAsset> mCachedAssets;
    private Map<String, List<FDetail>> mCachedDetails;
    private Map<String, FAsset> mCachedRecycledAssets;
    private List<String> mCachedDetailsKeyList;

    private boolean mDirtyCachedAssets;
    private boolean mDirtyCachedDetails;
    private IDataRepository.OnUpdatedCallback mOnAssetUpdated = new IDataRepository.OnUpdatedCallback() {
        @Override
        public void onSuccess(Void aVoid) {
        }

        @Override
        public void onFailure(Throwable e) {
            cacheAssets();
        }

        @Override
        public void onComplete(Task task) {
        }
    };
    private IDataRepository.OnUpdatedCallback mOnDetailUpdated = new IDataRepository.OnUpdatedCallback() {
        @Override
        public void onSuccess(Void aVoid) {
        }

        @Override
        public void onFailure(Throwable e) {
            mDirtyCachedDetails = true;
            initCachedDetails();
        }

        @Override
        public void onComplete(Task task) {
        }
    };

    //endregion
}
