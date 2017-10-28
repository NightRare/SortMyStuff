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
import java.util.NoSuchElementException;

import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.Category;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import nz.ac.aut.comp705.sortmystuff.utils.Log;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_CONTAINERID;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_CONTENTIDS;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_MODIFYTIMESTAMP;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_RECYCLED;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_THUMBNAIL;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ROOT_ASSET_ID;

/**
 * Implementation class of IDataManager
 *
 * @author Yuan
 */

public class DataManager implements IDataManager, IDebugHelper {

    public DataManager(IDataRepository remoteRepo, IFileHelper fileHelper, LocalResourceLoader resLoader,
                       ISchedulerProvider schedulerProvider) {
        mRemoteRepo = checkNotNull(remoteRepo);
        mFileHelper = checkNotNull(fileHelper);
        mResLoader = checkNotNull(resLoader);
        mSchedulerProvider = checkNotNull(schedulerProvider);

        synchronized (this) {
            mDirtyCachedAssets = true;
            mDirtyCachedDetails = true;
        }

        initCachedDetails();
        mRemoteRepo.setOnDataChangeCallback(new OnDetailsDataChangeListeners());
        mRemoteRepo.setOnDataChangeCallback(new OnAssetsDataChangeListeners());

        cacheAssets();
    }

    //region RETRIEVE METHODS

    @Override
    public Observable<IAsset> getRootAsset() {
        if (mDirtyCachedAssets) {
            return mRemoteRepo.retrieveAsset(ROOT_ASSET_ID)
                    .map(asset -> {
                        if (asset == null) {
                            return createRootAsset();
                        } else {
                            return asset;
                        }
                    });
        } else {
            return Observable.just(mCachedRootAsset);
        }
    }

    @Override
    public Observable<List<IAsset>> getAssets() {

        if (mDirtyCachedAssets || mCachedAssets == null) {
            return mRemoteRepo.retrieveAllAssets()
                    .flatMap(assets -> {
                        // if root asset is not ready yet
                        if (assets.isEmpty()) {
                            return getRootAsset();
                        } else {
                            return Observable.from(assets);
                        }
                    })
                    .filter(asset -> !asset.isRecycled())
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
                .flatMap(details -> {
                    if (details == null)
                        throw new NoSuchElementException();
                    return Observable.from(details);
                })
                .map(detail -> (IDetail) detail)
                .toList()
                .onErrorReturn(throwable -> null);
    }

    @Override
    public Observable<IAsset> getAsset(String id) {
        checkNotNull(id);

        if (id.equals(ROOT_ASSET_ID))
            return getRootAsset();

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
                    .flatMap(asset -> {
                        if (asset == null) {
                            // if the root asset has not been initialised/stored yet
                            if (assetId.equals(ROOT_ASSET_ID))
                                return Observable.from(new ArrayList<>());
                            else
                                throw new NoSuchElementException();
                        }
                        return Observable.from(asset.getContentIds());
                    })
                    .flatMap(this::getAsset)
                    .toList()
                    .onErrorReturn(throwable -> null);
        }

        FAsset container = mCachedAssets.get(assetId);
        if (container == null)
            return Observable.just(null);

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
            return mRemoteRepo.retrieveAllAssets()
                    .flatMap(Observable::from)
                    .filter(asset -> !asset.isRecycled())
                    .toMap(FAsset::getId)
                    .map(allNonRecycledAssets -> {
                        // if the root asset has not been initialised/stored yet
                        if (allNonRecycledAssets == null || allNonRecycledAssets.isEmpty()) {
                            return new ArrayList<IAsset>();
                        }
                        FAsset asset = allNonRecycledAssets.get(assetId);
                        if (asset == null)
                            throw new NoSuchElementException();

                        return getParentAssetsList(asset, rootToChildren, allNonRecycledAssets);
                    })
                    .onErrorReturn(throwable -> null);
        }

        FAsset asset = mCachedAssets.get(assetId);
        if (asset == null)
            throw new IllegalArgumentException("Asset not exists, id: " + assetId);

        List<IAsset> results = getParentAssetsList(asset, rootToChildren, mCachedAssets);
        return Observable.just(results);
    }

    //endregion

    //region CREATE METHODS

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
            mRemoteRepo.addDetail(d, mOnDetailUpdated);
        }
        mRemoteRepo.addOrUpdateAsset(asset, mOnAssetUpdated);

        LoggedAction updateCacheData = executedFromLog -> {
            FAsset container = mCachedAssets.get(containerId);
            if (container == null) return;

            container.addContentId(asset.getId());
            mCachedAssets.put(asset.getId(), asset);

            if (!executedFromLog)
                mRemoteRepo.addOrUpdateAsset(container, mOnAssetUpdated);
        };

        if (mDirtyCachedAssets) {
            // needs to update the contentIds of the container asset
            mRemoteRepo.retrieveAsset(containerId)
                    .subscribeOn(mSchedulerProvider.io())
                    .subscribe(container -> {
                        if (container == null) return;
                        container.addContentId(asset.getId());
                        mRemoteRepo.addOrUpdateAsset(container, mOnAssetUpdated);
                        mActionsQueue.add(updateCacheData);
                    });
        } else {
            updateCacheData.execute(false);
        }

        return asset.getId();
    }

    //endregion

    //region  UPDATE METHODS

    @Override
    public void updateAssetName(String assetId, final String newName) {
        checkNotNull(assetId);
        checkNotNull(newName);
        Preconditions.checkArgument(!newName.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(newName.length() < AppConstraints.ASSET_NAME_CAP);

        long modifyTimestamp = System.currentTimeMillis();
        LoggedAction updateAsset = executedFromLog -> {
            FAsset asset = mCachedAssets.get(assetId);
            if (asset == null) return;

            asset.setName(newName);
            asset.setModifyTimestamp(modifyTimestamp);
            if (!executedFromLog)
                mRemoteRepo.addOrUpdateAsset(asset, mOnAssetUpdated);
        };

        if (mDirtyCachedAssets) {
            mRemoteRepo.retrieveAsset(assetId)
                    .doOnNext(asset -> {
                        if (asset == null) return;
                        asset.setName(newName);
                        asset.setModifyTimestamp(modifyTimestamp);
                        mRemoteRepo.addOrUpdateAsset(asset, mOnAssetUpdated);
                    })
                    .subscribe(asset -> {
                        if (asset != null)
                            mActionsQueue.add(updateAsset);
                    });
        } else {
            updateAsset.execute(false);
        }
    }

    @Override
    public void moveAsset(String assetId, String newContainerId) {
        checkNotNull(assetId);
        checkNotNull(newContainerId);

        if (assetId.equals(ROOT_ASSET_ID))
            return;

        LoggedAction moveAsset = executedFromLog -> {
            FAsset asset = mCachedAssets.get(assetId);
            if (asset == null) return;

            FAsset from = mCachedAssets.get(asset.getContainerId());

            FAsset to = mCachedAssets.get(newContainerId);
            if (to == null) return;

            if (isParentOf(asset, to) || !asset.move(from, to)) {
                Log.e(getClass().getName(), "move asset failed, asset id: " + asset.getId()
                        + " container id: " + to.getId());
                return;
            }

            if (!executedFromLog)
                moveAssetUpdateRemoteRepo(asset, from, to);
        };

        if (mDirtyCachedAssets) {
            Observable<FAsset> assetObservable = mRemoteRepo.retrieveAsset(assetId)
                    .subscribeOn(mSchedulerProvider.io());

            Observable<FAsset> fromObservable = mRemoteRepo.retrieveAsset(assetId)
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(asset -> mRemoteRepo.retrieveAsset(asset.getContainerId()));

            Observable<FAsset> toObservable = mRemoteRepo.retrieveAsset(newContainerId)
                    .subscribeOn(mSchedulerProvider.io());

            Observable.zip(assetObservable, fromObservable, toObservable, (asset, from, to) -> {
                if (asset == null || from == null || to == null) throw new NoSuchElementException();

                if (isParentOf(asset, to) || !asset.move(from, to)) {
                    Log.e(getClass().getName(), "move asset failed, asset id: " + asset.getId()
                            + " container id: " + to.getId());
                    return null;
                }

                moveAssetUpdateRemoteRepo(asset, from, to);
                return null;
            }).subscribe(
                    //onNext
                    o -> mActionsQueue.add(moveAsset),
                    //onError, do nothing
                    throwable -> {
                    }
            );

        } else {
            moveAsset.execute(false);
        }
    }

    @Override
    public void recycleAssetAndItsContents(String assetId) {
        checkNotNull(assetId);

        if (assetId.equals(ROOT_ASSET_ID)) return;

        if (mDirtyCachedAssets) {
            mRemoteRepo.retrieveAsset(assetId)
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(
                            asset -> {
                                if (asset == null) throw new NoSuchElementException();
                                return recycleAssetGetAllChildrenAssets(asset);
                            }
                    )
                    .subscribe(
                            //onNext
                            this::recycleAsset,
                            //onError, do nothing
                            throwable -> {
                            }
                    );
        } else {
            FAsset asset = mCachedAssets.get(assetId);
            if (asset == null) return;

            if (!asset.getContentIds().isEmpty()) {
                for (String id : asset.getContentIds()) {
                    recycleAssetAndItsContents(id);
                }
            }
            recycleAsset(asset);
        }
    }

    @Override
    public void resetImageDetail(String assetId, String detailId) {
        checkNotNull(assetId);
        checkNotNull(detailId);

        updateDetailWithDefaultFieldValue(assetId, detailId, null);
    }

    public <T> void updateDetail(String assetId, String detailId, DetailType type, String newLabel, T newField) {
        checkUpdateDetailArguments(assetId, detailId, type, newLabel, newField);
        if (newLabel == null && newField == null) return;

        if (mDirtyCachedDetails) {
            initCachedDetails();
        }
        long modifyTimestamp = System.currentTimeMillis();

        if (mCachedDetails.containsKey(assetId)) {
            for (FDetail detail : mCachedDetails.get(assetId)) {
                if (detail.getId().equals(detailId) && detail.getAssetId().equals(assetId)) {
                    updateDetailAndUpdateInRemote(detail, newLabel, newField, false, modifyTimestamp);
                    updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    break;
                }
            }
        } else {
            mRemoteRepo.retrieveDetail(detailId)
                    .subscribeOn(mSchedulerProvider.io())
                    .doOnNext(detail -> {
                        if (detail == null || !detail.getAssetId().equals(assetId))
                            return;
                        updateDetailAndUpdateInRemote(detail, newLabel, newField, false, modifyTimestamp);
                        updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    })
                    .subscribe();
        }
    }


    //endregion

    //region OTHER METHODS

    @Override
    public void reCacheFromRemoteDataSource() {
        cacheAssets();
        mDirtyCachedDetails = true;
        initCachedDetails();
    }

    @Override
    public void removeCurrentUserData() {
        if (mRemoteRepo instanceof IDebugHelper) {
            ((IDebugHelper) mRemoteRepo).removeCurrentUserData();
            createRootAsset();
        }
        reCacheFromRemoteDataSource();
    }

    //endregion

    interface LoggedAction {

        void execute(boolean executedFromLog);
    }

    //region PRIVATE STUFF

    private synchronized void cacheAssets() {
        mDirtyCachedAssets = true;

        mActionsQueue = new ArrayList<>();
        mCachedAssets = new HashMap<>();
        mCachedRecycledAssets = new HashMap<>();

        getRootAsset()
                .subscribeOn(mSchedulerProvider.computation())
                .flatMap(asset -> mRemoteRepo.retrieveAllAssets())
                .flatMap(Observable::from)
                .doOnNext(this::putAssetIntoCacheObjects)
                .doOnCompleted(() -> {
                    // apply logged changes to the cache as the caching has ended
                    while (!mActionsQueue.isEmpty()) {
                        mActionsQueue.remove(0).execute(true);
                    }
                    synchronized (DataManager.this) {
                        mDirtyCachedAssets = false;
                    }
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

    private synchronized void initCachedDetails() {
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
                    if (details != null) {
                        mCachedDetails.put(assetId, details);
                        mCachedDetailsKeyList.add(assetId);
                    }
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
        mRemoteRepo.addOrUpdateAsset(root, null);
        return root;
    }

    private void updateAssetModifyTimestamp(String assetId, long modifyTimestamp) {
        LoggedAction updateModifyTimestamp = executedFromLog ->
                mCachedAssets.get(assetId).setModifyTimestamp(modifyTimestamp);
        if (mDirtyCachedAssets) {
            mActionsQueue.add(updateModifyTimestamp);
        } else {
            updateModifyTimestamp.execute(false);
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

    private List<IAsset> getParentAssetsList(FAsset asset, boolean rootToChildren,
                                             Map<String, FAsset> allNonRecycledAssets) {
        List<IAsset> results = new ArrayList<>();
        //children to root order
        while (!asset.isRoot()) {
            results.add(asset);
            asset = allNonRecycledAssets.get(asset.getContainerId());
        }
        // add the root
        results.add(asset);
        if (rootToChildren) Collections.reverse(results);
        return results;
    }

    private void moveAssetUpdateRemoteRepo(FAsset asset, FAsset from, FAsset to) {
        mRemoteRepo.updateAsset(asset.getId(), ASSET_CONTAINERID, asset.getContainerId(), mOnAssetUpdated);
        mRemoteRepo.updateAsset(from.getId(), ASSET_CONTENTIDS, from.getContentIds(), mOnAssetUpdated);
        mRemoteRepo.updateAsset(to.getId(), ASSET_CONTENTIDS, to.getContentIds(), mOnAssetUpdated);
    }

    private void recycleAsset(FAsset asset) {
        checkNotNull(asset);

        LoggedAction recycleAction = executedFromLog -> {
            // the containerId of the recycled asset is not removed on purpose
            // in case it needs to be restored in the future
            FAsset container = mCachedAssets.get(asset.getContainerId());
            container.removeContentId(asset.getId());
            mCachedAssets.get(asset.getId()).setRecycled(true);
            mCachedRecycledAssets.put(asset.getId(), mCachedAssets.remove(asset.getId()));

            if (!executedFromLog)
                mRemoteRepo.updateAsset(container.getId(), ASSET_CONTENTIDS,
                        container.getContentIds(), mOnAssetUpdated);
        };

        if (mDirtyCachedAssets) {
            mRemoteRepo.retrieveAsset(asset.getContainerId())
                    .doOnNext(container -> {
                        container.removeContentId(asset.getId());
                        mRemoteRepo.updateAsset(container.getId(),
                                ASSET_CONTENTIDS, container.getContentIds(), mOnAssetUpdated);
                    })
                    .subscribe(container -> mActionsQueue.add(recycleAction));
        } else {
            recycleAction.execute(false);
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

        LoggedAction updateThumbnail = executedFromLog -> {
            FAsset asset = mCachedAssets.get(assetId);
            if (asset != null) {
                asset.setThumbnail(thumbnail, usingDefaultPhoto);
            }
        };

        if (mDirtyCachedAssets) {
            mActionsQueue.add(updateThumbnail);
        } else {
            updateThumbnail.execute(false);
        }

        mRemoteRepo.updateAsset(assetId, ASSET_THUMBNAIL, thumbnailDataString, mOnAssetUpdated);
    }

    private void updateDetailWithDefaultFieldValue(String assetId, String detailId, String newLabel) {
        checkNotNull(assetId, "The assetId cannot be null.");
        checkNotNull(detailId, "The detailId cannot be null.");

        if (mDirtyCachedDetails) {
            initCachedDetails();
        }
        long modifyTimestamp = System.currentTimeMillis();

        if (mCachedDetails.containsKey(assetId)) {
            for (FDetail detail : mCachedDetails.get(assetId)) {
                if (detail.getId().equals(detailId) && detail.getAssetId().equals(assetId)) {
                    if (detail.getType().equals(DetailType.Text) || detail.getType().equals(DetailType.Date)) {
                        updateDetailAndUpdateInRemote(detail, newLabel, mResLoader.getDefaultText(),
                                true, modifyTimestamp);
                    } else if (detail.getType().equals(DetailType.Image)) {
                        updateDetailAndUpdateInRemote(detail, newLabel, mResLoader.getDefaultPhoto(),
                                true, modifyTimestamp);
                    }
                    updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    break;
                }
            }
        } else {
            mRemoteRepo.retrieveDetail(detailId)
                    .subscribeOn(mSchedulerProvider.io())
                    .doOnNext(detail -> {
                        if (detail == null || !detail.getAssetId().equals(assetId))
                            return;
                        if (detail.getType().equals(DetailType.Text) || detail.getType().equals(DetailType.Date)) {
                            updateDetailAndUpdateInRemote(detail, newLabel, mResLoader.getDefaultText(),
                                    true, modifyTimestamp);
                        } else if (detail.getType().equals(DetailType.Image)) {
                            updateDetailAndUpdateInRemote(detail, newLabel, mResLoader.getDefaultPhoto(),
                                    true, modifyTimestamp);
                        }
                        updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    })
                    .subscribe();
        }
    }

    private <E> void updateDetailAndUpdateInRemote(FDetail<E> detail, String newLabel,
                                                   E newField, boolean defaultFieldValue, long modifyTimestamp) {
        Class fieldType = newField == null ? null : newField.getClass();

        if (fieldType != null) {
            if (!detail.getType().getFieldClass().equals(fieldType))
                throw new IllegalArgumentException("Incorrect type of detail.");
        }

        if (newLabel != null)
            detail.setLabel(newLabel);

        boolean updatingField = newField != null;
        if (updatingField)
            detail.setField(newField, defaultFieldValue);

        detail.setModifyTimestamp(modifyTimestamp);

        mRemoteRepo.updateDetail(detail, updatingField, mOnDetailUpdated);

        // if the detail is "Photo", then the thumbnail of the asset should also be updated
        if (updatingField && detail.getType().equals(DetailType.Image) &&
                detail.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
            updateAssetThumbnailAndUpdateInRemote(detail.getAssetId(), (Bitmap) newField, defaultFieldValue);
        }
    }

    private class OnAssetsDataChangeListeners implements IDataRepository.OnAssetsDataChangeCallback {

        @Override
        public void onAssetAdded(FAsset asset) {
            if (!mCachedAssets.containsKey(asset.getId()))
                putAssetIntoCacheObjects(asset);
        }

        @Override
        public void onAssetChanged(FAsset asset) {
            LoggedAction updateCache = executedFromLog -> {
                FAsset cachedOne = mCachedAssets.get(asset.getId());
                if (cachedOne == null) return;

                cachedOne.overwrittenBy(asset);
            };

            if (mDirtyCachedAssets) {
                mActionsQueue.add(updateCache);
            } else {
                updateCache.execute(false);
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

    private <T> void checkUpdateDetailArguments(String assetId, String detailId, DetailType type, String newLabel, T newField) {
        checkNotNull(assetId);
        checkNotNull(detailId);
        if (newLabel != null) {
            checkArgument(!newLabel.replaceAll(" ", "").isEmpty(), "The new label cannot be empty.");
            checkArgument(newLabel.length() <= AppConstraints.DETAIL_LABEL_CAP,
                    "Please keep the length of the text within " + AppConstraints.DETAIL_LABEL_CAP + " characters");
        }
        if (newField != null) {
            checkArgument(type.getFieldClass().equals(newField.getClass()), "Incorrect type of newField.");
            if (type.equals(DetailType.Text) || type.equals(DetailType.Date)) {
                checkArgument(((String) newField).length() <= AppConstraints.TEXTDETAIL_FIELD_CAP,
                        "Please keep the length of the text within " + AppConstraints.TEXTDETAIL_FIELD_CAP + " characters");
            }
        }
    }

    private class OnDetailsDataChangeListeners implements IDataRepository.OnDetailsDataChangeCallback {

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
