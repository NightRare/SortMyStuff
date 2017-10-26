package nz.ac.aut.comp705.sortmystuff.ui.contents;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppCode.CONTENTS_DEFAULT_MODE;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConstraints.ROOT_ASSET_ID;

/**
 * The implementation class of {@link IContentsPresenter}.
 *
 * @author Yuan
 */

public class ContentsPresenter implements IContentsPresenter {

    public ContentsPresenter(IDataManager dataManager, IContentsView view,
                             ISchedulerProvider schedulerProvider,
                             ISchedulerProvider immediateSchedulerProvider, String currentAssetId) {
        mDataManager = checkNotNull(dataManager, "The dataManager cannot be null.");
        mView = checkNotNull(view, "The view cannot be null.");
        mSchedulerProvider = checkNotNull(schedulerProvider, "The schedulerProvider cannot be null.");
        mImmediateSchedulerProvider = checkNotNull(immediateSchedulerProvider, "The immediateSchedulerProvider cannot be null.");
        mCurrentAssetId = checkNotNull(currentAssetId, "The currentAssetId cannot be null.");

        mContentsDisplayMode = CONTENTS_DEFAULT_MODE;
        mSubscriptions = new CompositeSubscription();

        mView.setPresenter(this);
    }

    //region IContentsPresenter METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe() {
        loadCurrentContents(true);
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCurrentContents(boolean forceRefreshFromLocal) {
        if (forceRefreshFromLocal)
            mDataManager.refreshFromLocal();

        mView.setLoadingIndicator(true);

        mSubscriptions.clear();
        Observable<IAsset> currentAssetObservable = mDataManager
                .getAsset(mCurrentAssetId)
                .subscribeOn(mSchedulerProvider.io());

        Observable<List<IAsset>> contentAssetsObservable = mDataManager
                .getContentAssets(mCurrentAssetId)
                .subscribeOn(mSchedulerProvider.io());

        Observable<List<IAsset>> parentAssetsObservable = mDataManager
                .getParentAssets(mCurrentAssetId, true)
                .subscribeOn(mSchedulerProvider.io())
                .flatMap(Observable::from)
                //get rid of the root
                .filter(a -> !a.isRoot())
                .toList();

        Subscription subscription = Observable.zip(currentAssetObservable, contentAssetsObservable,
                parentAssetsObservable, (currentAsset, contentAssets, parentAssets) -> {
                    if(currentAsset == null || contentAssets == null || parentAssets == null)
                        throw new IllegalStateException("Something went wrong trying to get data for Contents View");

                    return new Object[] {currentAsset, contentAssets, parentAssets};
                })
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(
                        //onNext nothing needs to be done
                        args -> processContentResults((IAsset) args[0], (List<IAsset>) args[1],
                                (List<IAsset>) args[2]),
                        //onError
                        throwable -> mView.showLoadingContentsError(throwable),
                        //onCompleted
                        () -> mView.setLoadingIndicator(false)
                );

//
//        Subscription subscription2 = mDataManager
//                .getAsset(mCurrentAssetId)
//                .zipWith(contentAssetsObservable, (currentAsset, contentAssets) -> {
//                    List<Object> args = new ArrayList<>();
//                    args.add(0, currentAsset);
//                    args.add(1, contentAssets);
//                    return args;
//                })
//                .zipWith(parentAssetsObservable, (args, parentAssets) -> {
//                    args.add(2, parentAssets);
//                    return args;
//                })
//                .subscribeOn(mSchedulerProvider.io())
//                .observeOn(mSchedulerProvider.ui())
//                .subscribe(
//                        //onNext
//                        args -> processContentResults((IAsset) args.get(0),
//                                (List<IAsset>) args.get(1),
//                                (List<IAsset>) args.get(2)),
//                        //onError
//                        throwable -> mView.showLoadingContentsError(throwable),
//                        //onCompleted
//                        () -> mView.setLoadingIndicator(false)
//                );
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCurrentContents(boolean forceRefreshFromLocal, int mode) {
        mContentsDisplayMode = mode;
        loadCurrentContents(forceRefreshFromLocal);
    }

    @Override
    public void setCurrentAssetId(String assetId) {
        mCurrentAssetId = checkNotNull(assetId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentAssetIdToRoot() {
        setCurrentAssetId(ROOT_ASSET_ID);
    }

    /**
     * {@inheritDoc}
     */
    public String getCurrentAssetId() {
        return mCurrentAssetId;
    }

    /**
     * {@inheritDoc}
     *
     * @param assetName the name of the new asset.
     */
    @Override
    public void createAsset(String assetName, CategoryType category) {
        try {
            mDataManager.createAsset(assetName, mCurrentAssetId, category);
            mView.showMessage("Successfully added " + assetName);
        } catch (NullPointerException | IllegalArgumentException e) {
            mView.showMessage(e.getMessage());
        }
        loadCurrentContents(false);
    }

    @Override
    public void moveAssets(List<IAsset> assets) {
        checkNotNull(assets, "The assets to move cannot be null.");
        if(assets.isEmpty()) return;

        //reject the attempt to move to current directory
        if (assets.get(0).getContainerId().equals(mCurrentAssetId)) {
            mView.showMessage("The assets are already here");
            return;
        }
        for (IAsset a : assets) {
            mDataManager.moveAsset(a.getId(), mCurrentAssetId);
        }
        mView.showMessage(assets.size() + " asset" + (assets.size() > 1 ? "s" : "") + " moved");
    }

    @Override
    public void deleteCurrentAsset() {
        if(mCurrentAssetId.equals(ROOT_ASSET_ID))
            mView.showMessage("Cannot delete the root asset");
        else
            mView.showDeleteDialog(true);
    }

    @Override
    public void recycleCurrentAssetRecursively() {
        String deleteAssetId = mCurrentAssetId;
        setCurrentAssetIdToContainer();
        mDataManager.recycleAssetRecursively(deleteAssetId);
        loadCurrentContents(true);
    }

    @Override
    public void recycleAssetsRecursively(List<IAsset> assets) {
        checkNotNull(assets);
        for (IAsset a : assets) {
            mDataManager.recycleAssetRecursively(a.getId());
        }
        loadCurrentContents(true);
    }

    //endregion

    //region Private stuff

    private void setCurrentAssetIdToContainer() {
        // does nothing if the current asset is Root asset
        if (mCurrentAssetId.equals(ROOT_ASSET_ID))
            return;

        mSubscriptions.clear();
        Subscription subscription = mDataManager
                .getParentAssets(mCurrentAssetId, false)
                .subscribeOn(mImmediateSchedulerProvider.io())
                .observeOn(mImmediateSchedulerProvider.ui())
                .subscribe(
                        //onNext
                        //the second one is the direct parent
                        parents -> setCurrentAssetId(parents.get(1).getId()),
                        //onError
                        throwable -> mView.showLoadingContentsError(throwable)
                );
        mSubscriptions.add(subscription);
    }

    private void processContentResults(IAsset asset, List<IAsset> contentAssets, List<IAsset> parentAssets) {
        mView.showTitle(asset);
        mView.showAssetContents(contentAssets, mContentsDisplayMode);
        mView.showPath(parentAssets);
    }

    private IContentsView mView;
    private IDataManager mDataManager;
    private String mCurrentAssetId;
    private int mContentsDisplayMode;
    private ISchedulerProvider mSchedulerProvider;
    private ISchedulerProvider mImmediateSchedulerProvider;
    private CompositeSubscription mSubscriptions;

    //endregion

}
