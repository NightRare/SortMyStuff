package nz.ac.aut.comp705.sortmystuff.data;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.ArrayMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import javax.inject.Inject;

import nz.ac.aut.comp705.sortmystuff.Features;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FCategory;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.data.remote.IImageDetectionHelper;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.RegularScheduler;
import nz.ac.aut.comp705.sortmystuff.utils.AppConfigs;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import nz.ac.aut.comp705.sortmystuff.utils.DemoDebugger;
import nz.ac.aut.comp705.sortmystuff.utils.Log;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_CONTAINERID;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_CONTENTIDS;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_MODIFYTIMESTAMP;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_RECYCLED;
import static nz.ac.aut.comp705.sortmystuff.data.models.FAsset.ASSET_THUMBNAIL;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ASSET_DEFAULT_NAME;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ASSET_PLACEHOLDER_ID;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ROOT_ASSET_ID;

/**
 * Implementation class of IDataManager
 *
 * @author Yuan
 */

public class DataManager implements IDataManager, IDebugHelper {

    @Inject
    public DataManager(
            IDataRepository remoteRepo,
            LocalResourceLoader resLoader,
            @RegularScheduler ISchedulerProvider schedulerProvider,
            Features featToggle,
            IImageDetectionHelper imageDetectionHelper) {
        mRemoteRepo = checkNotNull(remoteRepo);
        mResLoader = checkNotNull(resLoader);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mFeatToggle = checkNotNull(featToggle);
        mImageDetectionHelper = checkNotNull(imageDetectionHelper);

        synchronized (this) {
            mDirtyCachedAssets = true;
            mDirtyCachedDetails = true;
            mDirtyCachedCategories = true;
        }

        initCachedDetails();
//        mRemoteRepo.setOnDataChangeCallback(new OnDetailsDataChangeListeners(), FDetail.class);
        mRemoteRepo.setOnDataChangeCallback(new OnAssetsDataChangeListeners(), FAsset.class);

        cacheAssets();
        cacheCategories();
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

        // if get the Details of the Root asset, always return empty list
        if (checkNotNull(assetId).equals(ROOT_ASSET_ID)) {
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
    public Observable<IDetail<Bitmap>> getPhotoDetail(String assetId) {
        if (checkNotNull(assetId).equals(ROOT_ASSET_ID)) {
            return Observable.just(null);
        }

        return loadAndCacheDetails(assetId)
                .flatMap(details -> {
                    if (details == null)
                        throw new NoSuchElementException();
                    return Observable.from(details);
                })
                .filter(detail -> detail.getLabel().equals(CategoryType.BasicDetail.PHOTO) &&
                        detail.getType().equals(DetailType.Image))
                .first()
                .map(detail -> (IDetail<Bitmap>) detail)
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
        Preconditions.checkArgument(name.length() <= AppConfigs.ASSET_NAME_CAP, "The length of the name should be shorter than "
                + AppConfigs.ASSET_NAME_CAP + " characters");

        FAsset asset = FAsset.create(name, containerId, categoryType);

        if (mDirtyCachedCategories) {
            mRemoteRepo.retrieveCategories()
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(Observable::from)
                    .filter(category -> category.getName().equals(categoryType.toString()))
                    .first()
                    .map(category -> category.generateDetails(asset.getId()))
                    .subscribe(
                            //onNext
                            details -> createAssetProcess(asset, details)
                    );
        } else {
            List<FDetail> details = mCachedCategories.get(categoryType).generateDetails(asset.getId());
            createAssetProcess(asset, details);
        }

        return asset.getId();
    }

    @Override
    public String createAsset(String name, String containerId, CategoryType categoryType, Bitmap photo, List<IDetail> details) {
        checkNotNull(name);
        checkNotNull(containerId);
        checkNotNull(photo);
        checkNotNull(details);
        Preconditions.checkArgument(!name.replaceAll(" ", "").isEmpty(), "The name cannot be empty");
        Preconditions.checkArgument(name.length() <= AppConfigs.ASSET_NAME_CAP, "The length of the name should be shorter than "
                + AppConfigs.ASSET_NAME_CAP + " characters");

        FAsset asset = FAsset.create(name, containerId, categoryType);

        if (mDirtyCachedCategories) {
            mRemoteRepo.retrieveCategories()
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(Observable::from)
                    .filter(category -> category.getName().equals(categoryType.toString()))
                    .first()
                    .map(category -> category.generateDetails(asset.getId()))
                    .subscribe(
                            //onNext
                            fDetails -> {
                                createAssetProcessNewDetails(fDetails, details, photo, asset);
                                createAssetProcess(asset, fDetails);
                            }
                    );
        } else {
            List<FDetail> newDetails = mCachedCategories.get(categoryType).generateDetails(asset.getId());
            createAssetProcessNewDetails(newDetails, details, photo, asset);
            createAssetProcess(asset, newDetails);
        }

        return asset.getId();
    }

    @Override
    public Observable<String> createAssetSafely(String name, String containerId, CategoryType categoryType) {
        checkNotNull(name);
        checkNotNull(containerId);
        Preconditions.checkArgument(!name.replaceAll(" ", "").isEmpty(), "The name cannot be empty");
        Preconditions.checkArgument(name.length() <= AppConfigs.ASSET_NAME_CAP, "The length of the name should be shorter than "
                + AppConfigs.ASSET_NAME_CAP + " characters");

        FAsset asset = FAsset.create(name, containerId, categoryType);

        if (mDirtyCachedCategories) {
            return mRemoteRepo.retrieveCategories()
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(Observable::from)
                    .filter(category -> category.getName().equals(categoryType.toString()))
                    .first()
                    .map(category -> category.generateDetails(asset.getId()))
                    .flatMap(details -> createAssetProcessSafely(asset, details));
        } else {
            List<FDetail> details = mCachedCategories.get(categoryType).generateDetails(asset.getId());
            return createAssetProcessSafely(asset, details);
        }
    }

    @Override
    public Observable<String> createAssetSafely(String name, String containerId, CategoryType categoryType, Bitmap photo, List<IDetail> details) {
        checkNotNull(name);
        checkNotNull(containerId);
        checkNotNull(photo);
        checkNotNull(details);
        Preconditions.checkArgument(!name.replaceAll(" ", "").isEmpty(), "The name cannot be empty");
        Preconditions.checkArgument(name.length() <= AppConfigs.ASSET_NAME_CAP, "The length of the name should be shorter than "
                + AppConfigs.ASSET_NAME_CAP + " characters");

        FAsset asset = FAsset.create(name, containerId, categoryType);

        if (mDirtyCachedCategories) {
            return mRemoteRepo.retrieveCategories()
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(Observable::from)
                    .filter(category -> category.getName().equals(categoryType.toString()))
                    .first()
                    .map(category -> category.generateDetails(asset.getId()))
                    .flatMap(
                            //onNext
                            fDetails -> {
                                createAssetProcessNewDetails(fDetails, details, photo, asset);
                                return createAssetProcessSafely(asset, fDetails);
                            }
                    );
        } else {
            List<FDetail> newDetails = mCachedCategories.get(categoryType).generateDetails(asset.getId());
            createAssetProcessNewDetails(newDetails, details, photo, asset);
            return createAssetProcessSafely(asset, newDetails);
        }
    }

    //endregion

    //region  UPDATE METHODS

    @Override
    public void updateAssetName(String assetId, final String newName) {
        checkNotNull(assetId);
        checkNotNull(newName);
        Preconditions.checkArgument(!newName.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(newName.length() < AppConfigs.ASSET_NAME_CAP);

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

    @Override
    public <T> void updateDetail(String assetId, String detailId, DetailType type, String newLabel, T newField) {
        checkUpdateDetailArguments(assetId, detailId, type, newLabel, newField);
        if (newLabel == null && newField == null) return;

        String stupidLambdaValueContainer = null;
        if (newField != null && type.equals(DetailType.Image)) {
            stupidLambdaValueContainer = BitmapHelper.toString((Bitmap) newField);
        } else {
            stupidLambdaValueContainer = (String) newField;
        }
        final String fieldData = stupidLambdaValueContainer;

        if (mDirtyCachedDetails) {
            initCachedDetails();
        }
        long modifyTimestamp = System.currentTimeMillis();

        if (mCachedDetails.containsKey(assetId)) {
            for (FDetail detail : mCachedDetails.get(assetId)) {
                if (detail.getId().equals(detailId) && detail.getAssetId().equals(assetId)) {
                    updateDetailAndUpdateInRemote(detail, newLabel, fieldData, false, modifyTimestamp);
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
                        updateDetailAndUpdateInRemote(detail, newLabel, fieldData, false, modifyTimestamp);
                        updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    })
                    .subscribe();
        }
    }


    //endregion

    //region OTHER METHODS

    @Override
    public Observable<String> getNewAssetName() {
        return generateNewAssetName(ASSET_DEFAULT_NAME);

    }

    @Override
    public Observable<String> getNewAssetName(Bitmap photo) {
        if (mFeatToggle.PhotoDetection && !mResLoader.getDefaultPhoto().sameAs(photo)) {
            return mImageDetectionHelper
                    .result(BitmapHelper.toCloudSightString(photo))
                    .map(result -> result == null ? null : result.name())
                    .map(name -> {
                        if (name == null) return null;
                        if (name.length() > AppConfigs.ASSET_NAME_CAP) {
                            return name.substring(0, AppConfigs.ASSET_NAME_CAP);
                        }
                        return name;
                    });
        } else {
            return getNewAssetName();
        }
    }

    @Override
    public Observable<List<IDetail>> getDetailsFromCategory(CategoryType categoryType) {
        checkNotNull(categoryType);

        if (mDirtyCachedCategories) {
            return mRemoteRepo.retrieveCategories()
                    .subscribeOn(mSchedulerProvider.io())
                    .flatMap(Observable::from)
                    .toMap(FCategory::getName)
                    .map(categories -> categories.get(categoryType.toString())
                            .generateDetails(ASSET_PLACEHOLDER_ID))
                    .flatMap(Observable::from)
                    .map(fDetail -> (IDetail) fDetail)
                    .toSortedList();
        }

        return Observable.from(mCachedCategories.get(categoryType).generateDetails(ASSET_PLACEHOLDER_ID))
                .map(fDetail -> (IDetail) fDetail)
                .toSortedList();
    }

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

    //region ISortMyStuffAppComponent METHODS

    @Override
    public void setFeatureToggle(Features featureToggle) {
        mFeatToggle = checkNotNull(featureToggle);
    }

    @Override
    public Features getFeatureToggle() {
        return mFeatToggle;
    }

    //endregion

    interface LoggedAction {

        void execute(boolean executedFromLog);
    }

    //region PRIVATE STUFF

    private synchronized void cacheCategories() {
        mCachedCategories = new ArrayMap<>();

        mRemoteRepo.retrieveCategories()
                .subscribeOn(mSchedulerProvider.computation())
                .flatMap(Observable::from)
                .doOnNext(category -> mCachedCategories.put(CategoryType.valueOf(category.getName()), category))
                .doOnCompleted(() -> {
                    synchronized (DataManager.this) {
                        mDirtyCachedCategories = false;
                    }
                })
                .subscribe();
    }

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
                    synchronized (DataManager.this) {
                        // apply logged changes to the cache as the caching has ended
                        while (!mActionsQueue.isEmpty()) {
                            mActionsQueue.remove(0).execute(true);
                        }

                        mDirtyCachedAssets = false;
//                        prepareDemoData();
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
        mCachedDetails = new WeakHashMap<>();
        mCachedDetailsKeyList = new LinkedList<>();
        mDirtyCachedDetails = false;
    }

    private Observable<List<FDetail>> loadAndCacheDetails(String assetId) {
        if (mDirtyCachedDetails || mCachedDetails == null) {
            initCachedDetails();
        } else if (mCachedDetails.containsKey(assetId)) {
            return Observable.just(mCachedDetails.get(assetId));
        }

        return mRemoteRepo.retrieveDetails(assetId)
                .doOnNext(details -> {
                    if (details != null) {
                        addToCachedDetails(assetId, details);
                    }
                });
    }

    private void addToCachedDetails(String assetId, List<FDetail> details) {

        // Releases one cache from the cachedDetails map if the size of the cache is bigger than the limit.
        // The cache algorithm now is LRU (Latest Recently Used).
        if (mCachedDetails.size() >= AppConfigs.CACHED_DETAILS_LIST_NUM
                && mCachedDetailsKeyList.size() > 0) {
            mCachedDetails.remove(mCachedDetailsKeyList.remove(0));
        }

        mCachedDetails.put(assetId, details);
        mCachedDetailsKeyList.add(assetId);
    }

    private FAsset createRootAsset() {
        FAsset root = FAsset.createRoot();
        mRemoteRepo.addOrUpdateAsset(root, null);
        return root;
    }

    private Observable<String> createAssetProcessSafely(FAsset asset, List<FDetail> details) {

        Observable<String> outputObservable = null;
        Emitter.BackpressureMode backpressureMode = Emitter.BackpressureMode.BUFFER;

        // POST all the details
        if (!details.isEmpty()) {
            for (int i = 0; i < details.size(); i++) {
                final int index = i;
                asset.addDetailId(details.get(i).getId());
                Action1<Emitter<String>> emitDetailId = new Action1<Emitter<String>>() {
                    @Override
                    public void call(Emitter<String> stringEmitter) {
                        IDataRepository.OnUpdatedCallback onAddDetail = new IDataRepository.OnUpdatedCallback() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                stringEmitter.onNext(details.get(index).getId());
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                stringEmitter.onError(e);
                            }

                            @Override
                            public void onComplete() {
                                stringEmitter.onCompleted();
                            }
                        };

                        mRemoteRepo.addDetail(details.get(index), onAddDetail);
                    }
                };

                if (outputObservable == null) {
                    outputObservable = Observable.create(emitDetailId, backpressureMode);
                } else {
                    outputObservable = outputObservable.flatMap(detailId ->
                            Observable.create(emitDetailId, backpressureMode));
                }
            }
        }

        // POST asset
        Action1<Emitter<String>> emitAssetId = new Action1<Emitter<String>>() {
            @Override
            public void call(Emitter<String> stringEmitter) {
                IDataRepository.OnUpdatedCallback onAddDetail = new IDataRepository.OnUpdatedCallback() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        stringEmitter.onNext(asset.getId());
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        stringEmitter.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        stringEmitter.onCompleted();
                    }
                };

                mRemoteRepo.addOrUpdateAsset(asset, onAddDetail);
            }
        };

        if (outputObservable == null) {
            outputObservable = Observable.create(emitAssetId, backpressureMode);
        } else {
            outputObservable = outputObservable
                    .flatMap(detailId -> Observable.create(emitAssetId, backpressureMode));
        }

        // PUT container asset
        if (mDirtyCachedAssets) {
            outputObservable = outputObservable
                    .flatMap(assetId -> mRemoteRepo.retrieveAsset(asset.getContainerId()))
                    .flatMap(container -> {
                        if (container == null)
                            throw new IllegalStateException();
                        container.addContentId(asset.getId());

                        return Observable.create((Action1<Emitter<String>>) emitter -> {
                            IDataRepository.OnUpdatedCallback onAddContainer = new IDataRepository.OnUpdatedCallback() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mActionsQueue.add(executedFromLog -> {
                                        FAsset cachedContainer = mCachedAssets.get(asset.getContainerId());
                                        if (cachedContainer == null) return;

                                        cachedContainer.addContentId(asset.getId());
                                        mCachedAssets.put(asset.getId(), asset);
                                    });
                                    emitter.onNext(container.getId());
                                }

                                @Override
                                public void onFailure(Throwable e) {
                                    emitter.onError(e);
                                }

                                @Override
                                public void onComplete() {
                                    emitter.onCompleted();
                                }
                            };

                            mRemoteRepo.addOrUpdateAsset(container, onAddContainer);
                        }, backpressureMode);
                    });
        } else {
            FAsset container = mCachedAssets.get(asset.getContainerId());
            outputObservable = outputObservable
                    .flatMap(assetId -> {
                        if (container == null)
                            throw new IllegalStateException();
                        container.addContentId(asset.getId());

                        return Observable.create((Action1<Emitter<String>>) emitter -> {
                            IDataRepository.OnUpdatedCallback onAddContainer = new IDataRepository.OnUpdatedCallback() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mCachedAssets.put(asset.getId(), asset);
                                    emitter.onNext(container.getId());
                                }

                                @Override
                                public void onFailure(Throwable e) {
                                    emitter.onError(e);
                                }

                                @Override
                                public void onComplete() {
                                    emitter.onCompleted();
                                }
                            };

                            mRemoteRepo.addOrUpdateAsset(container, onAddContainer);
                        }, backpressureMode);
                    });
        }

        return outputObservable.map(containerId -> asset.getId());
    }

    private void createAssetProcessNewDetails(
            List<FDetail> newDetails,
            List<IDetail> sourceDetails,
            Bitmap photo,
            FAsset newAsset) {

        Map<String, IDetail> sourceMap = Maps.uniqueIndex(sourceDetails, IDetail::getLabel);

        for (FDetail nd : newDetails) {
            if (nd.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
                boolean isDefaultValue = photo.sameAs(mResLoader.getDefaultPhoto());
                nd.setFieldData(
                        isDefaultValue ? mResLoader.getDefaultPhotoDataString()
                                : BitmapHelper.toString(photo),
                        isDefaultValue);

                if (!isDefaultValue) {
                    newAsset.setThumbnail(BitmapHelper.toThumbnail(photo), false);
                }
            }
            IDetail correspondingDetail = sourceMap.get(nd.getLabel());
            if (correspondingDetail == null ||
                    !nd.getType().getFieldClass().equals(String.class))
                continue;

            nd.setFieldData((String) correspondingDetail.getField(),
                    correspondingDetail.isDefaultFieldValue());
        }
    }

    private void createAssetProcess(FAsset asset, List<FDetail> details) {

        for (FDetail d : details) {
            asset.addDetailId(d.getId());
            mRemoteRepo.addDetail(d, mOnDetailUpdated);
        }
        mRemoteRepo.addOrUpdateAsset(asset, mOnAssetUpdated);

        LoggedAction updateCacheData = executedFromLog -> {
            FAsset container = mCachedAssets.get(asset.getContainerId());
            if (container == null) return;

            container.addContentId(asset.getId());
            mCachedAssets.put(asset.getId(), asset);
            addToCachedDetails(asset.getId(), details);

            if (!executedFromLog)
                mRemoteRepo.addOrUpdateAsset(container, mOnAssetUpdated);
        };

        if (mDirtyCachedAssets) {
            // needs to update the contentIds of the container asset
            mRemoteRepo.retrieveAsset(asset.getContainerId())
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

    private void updateAssetThumbnailAndUpdateInRemote(String assetId, Bitmap thumbnail, boolean usingDefaultPhoto) {
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
                        updateDetailAndUpdateInRemote(detail, newLabel, mResLoader.getDefaultPhotoDataString(),
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
                            updateDetailAndUpdateInRemote(detail, newLabel, mResLoader.getDefaultPhotoDataString(),
                                    true, modifyTimestamp);
                        }
                        updateAssetModifyTimestamp(detail.getAssetId(), modifyTimestamp);
                    })
                    .subscribe();
        }
    }

    private <E> void updateDetailAndUpdateInRemote(FDetail<E> detail, String newLabel,
                                                   String newFieldData, boolean defaultFieldValue, long modifyTimestamp) {
        if (newLabel != null)
            detail.setLabel(newLabel);

        boolean updatingField = newFieldData != null;
        if (updatingField)
            detail.setFieldData(newFieldData, defaultFieldValue);

        detail.setModifyTimestamp(modifyTimestamp);

        mRemoteRepo.updateDetail(detail, updatingField, mOnDetailUpdated);

        // if the detail is "Photo", then the thumbnail of the asset should also be updated
        if (updatingField && detail.getType().equals(DetailType.Image) &&
                detail.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
            Bitmap thumbnail = defaultFieldValue ?
                    mResLoader.getDefaultThumbnail() :
                    BitmapHelper.toThumbnail(BitmapHelper.toBitmap(newFieldData));
            updateAssetThumbnailAndUpdateInRemote(detail.getAssetId(), thumbnail, defaultFieldValue);
        }
    }

    private class OnAssetsDataChangeListeners implements IDataRepository.OnDataChangeCallback<FAsset> {

        @Override
        public void onDataAdded(FAsset asset) {
            if (!mCachedAssets.containsKey(asset.getId()))
                putAssetIntoCacheObjects(asset);
        }

        @Override
        public void onDataChanged(FAsset asset) {
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
        public void onDataRemoved(FAsset asset) {
            // assets won't be removed
        }

        @Override
        public void onDataMoved(FAsset asset) {
            // does not matter
        }
    }

    private <T> void checkUpdateDetailArguments(String assetId, String detailId, DetailType type, String newLabel, T newField) {
        checkNotNull(assetId);
        checkNotNull(detailId);
        if (newLabel != null) {
            checkArgument(!newLabel.replaceAll(" ", "").isEmpty(), "The new label cannot be empty.");
            checkArgument(newLabel.length() <= AppConfigs.DETAIL_LABEL_CAP,
                    "Please keep the length of the text within " + AppConfigs.DETAIL_LABEL_CAP + " characters");
        }
        if (newField != null) {
            checkArgument(type.getFieldClass().equals(newField.getClass()), "Incorrect type of newField.");
            if (type.equals(DetailType.Text) || type.equals(DetailType.Date)) {
                checkArgument(((String) newField).length() <= AppConfigs.TEXTDETAIL_FIELD_CAP,
                        "Please keep the length of the text within " + AppConfigs.TEXTDETAIL_FIELD_CAP + " characters");
            }
        }
    }

    private class OnDetailsDataChangeListeners implements IDataRepository.OnDataChangeCallback<FDetail> {

        @Override
        public void onDataAdded(FDetail detail) {
            // does not matter for now, because details won't be added without adding a new asset
            // will pass in null, as currently set up in FirebaseHelper
        }

        @Override
        public void onDataChanged(FDetail detail) {

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
        public void onDataRemoved(FDetail detail) {
            // won't be removed for now
        }

        @Override
        public void onDataMoved(FDetail detail) {
            // does not matter
        }
    }

    private synchronized Observable<String> generateNewAssetName(String prefix) {
        Observable<FAsset> assetStreams;

        if (mDirtyCachedAssets || mCachedAssets == null) {
            assetStreams = mRemoteRepo.retrieveAllAssets()
                    .flatMap(Observable::from);
        } else {
            assetStreams = Observable.from(mCachedAssets.values());
        }

        return assetStreams
                .map(FAsset::getName)
                .filter(assetName -> assetName.contains(prefix))
                .map(assetName -> getPostfixNum(assetName, prefix))
                .filter(num -> num != -1)
                .toSortedList()
                .map(prefixes -> {
                    Integer postfix = 0;
                    for (Integer num : prefixes) {
                        if (num.equals(postfix)) {
                            postfix++;
                        } else if (num > postfix) {
                            break;
                        }
                    }
                    return postfix;
                })
                .map(postfix -> prefix + " " + postfix);
    }

    /**
     * Get the postfix of a name.
     * For instance, names are like "New asset {num}", then "New asset" should be
     * the prefix and {num} is the postfix. If there's no {num} then -1 will be returned.
     * If name equals prefix, then 0 will be returned.
     *
     * @param name
     * @param prefix
     * @return
     */
    private int getPostfixNum(String name, String prefix) {
        String trimmedName = name.trim();

        if (trimmedName.equals(prefix))
            return 0;

        try {
            String head = trimmedName.substring(0, prefix.length());
            if (!head.equals(prefix))
                return -1;

            // prefix.length() + 1 for the following space
            String tail = trimmedName.substring(prefix.length() + 1);

            return Integer.parseInt(tail);
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return -1;
        }
    }

    private void prepareDemoData() {

        long idlingMillis = 3000;

        Map<String, Bitmap> photos = mResLoader.getDemoPhotos();

        DemoDebugger dd = new DemoDebugger(this, mSchedulerProvider);

        // Map<assetName, assetId>
        Map<String, String> demoAssets = new ArrayMap<>();

        createAssetSafely("Study Room", ROOT_ASSET_ID, CategoryType.Places)
                .subscribeOn(mSchedulerProvider.io())
                .flatMap(id -> {
                    demoAssets.put("Study Room", id);
                    return createAssetSafely("Office", ROOT_ASSET_ID, CategoryType.Places);
                })
                .flatMap(id -> {
                    demoAssets.put("Office", id);
                    return createAssetSafely("Bedroom", ROOT_ASSET_ID, CategoryType.Places);
                })
                .flatMap(id -> {
                    demoAssets.put("Bedroom", id);
                    return createAssetSafely("Bookshelf Philosophy", demoAssets.get("Study Room"), CategoryType.Miscellaneous);
                })
                .flatMap(id -> {
                    demoAssets.put("Bookshelf Philosophy", id);
                    return createAssetSafely("Bookshelf Literature", demoAssets.get("Study Room"), CategoryType.Miscellaneous);
                })
                .flatMap(id -> {
                    demoAssets.put("Bookshelf Literature", id);
                    return createAssetSafely("Kindle", demoAssets.get("Bedroom"), CategoryType.Appliances);
                })
                .flatMap(id -> {
                    demoAssets.put("Kindle", id);
                    return createAssetSafely("The Essential Husserl", demoAssets.get("Bookshelf Philosophy"), CategoryType.Books);
                })
                .flatMap(id -> {
                    demoAssets.put("The Essential Husserl", id);
                    return createAssetSafely("The Republic", demoAssets.get("Bookshelf Philosophy"), CategoryType.Books);
                })
                .flatMap(id -> {
                    demoAssets.put("The Republic", id);
                    return createAssetSafely("Being and Time", demoAssets.get("Bookshelf Philosophy"), CategoryType.Books);
                })
                .doOnNext(id -> {
                    demoAssets.put("Being and Time", id);
                })
                .doOnNext(id -> {
                    dd.setPhoto(demoAssets.get("Study Room"), photos.get("StudyRoom.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("Office"), photos.get("Office.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("Bedroom"), photos.get("Bedroom.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("Bookshelf Philosophy"), photos.get("BookshelfPhilosophy.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("Bookshelf Literature"), photos.get("BookshelfLiterature.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("Kindle"), photos.get("Kindle.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.updateTextDetail(demoAssets.get("Kindle"), "Purchase Date", "14/01/2016");
                    SystemClock.sleep(idlingMillis);

                    dd.updateTextDetail(demoAssets.get("Kindle"), "Warranty Expiry", "14/01/2019");
                    SystemClock.sleep(idlingMillis);

                    dd.updateTextDetail(demoAssets.get("Kindle"), "Model Number", "B0186FET66");
                    SystemClock.sleep(idlingMillis);

                    dd.updateTextDetail(demoAssets.get("Kindle"), "Serial Number", "9Q8EWR7923");
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("The Essential Husserl"), photos.get("TheEssentialHusserl.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("The Republic"), photos.get("TheRepublic.jpg"));
                    SystemClock.sleep(idlingMillis);

                    dd.setPhoto(demoAssets.get("Being and Time"), photos.get("BeingAndTime.jpg"));
                })
                .subscribe();
    }

    private IDataRepository.OnUpdatedCallback mOnAssetUpdated = new IDataRepository.OnUpdatedCallback() {
        @Override
        public void onSuccess(Void aVoid) {
        }

        @Override
        public void onFailure(Throwable e) {
            cacheAssets();
        }

        @Override
        public void onComplete() {
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
        public void onComplete() {
        }
    };

    private IDataRepository mRemoteRepo;
    private ISchedulerProvider mSchedulerProvider;
    private IImageDetectionHelper mImageDetectionHelper;
    private LocalResourceLoader mResLoader;
    private Features mFeatToggle;

    private List<LoggedAction> mActionsQueue;

    private FAsset mCachedRootAsset;
    private Map<String, FAsset> mCachedAssets;
    private Map<String, List<FDetail>> mCachedDetails;
    private Map<String, FAsset> mCachedRecycledAssets;
    private List<String> mCachedDetailsKeyList;
    private Map<CategoryType, FCategory> mCachedCategories;


    private boolean mDirtyCachedAssets;
    private boolean mDirtyCachedDetails;
    private boolean mDirtyCachedCategories;

    //endregion
}
