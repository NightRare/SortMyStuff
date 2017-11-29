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
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ROOT_ASSET_ID;

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

        mViewMode = ContentsViewMode.Default;
        mSubscriptions = new CompositeSubscription();
        mFirstLaunch = true;

        mView.setPresenter(this);
    }

    //region IContentsPresenter METHODS

    @Override
    public void start() {
        loadCurrentContents();
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void loadCurrentContents() {
        if(mFirstLaunch) {
            mView.setLoadingIndicator(true);
            mFirstLaunch = false;
        }

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
        mSubscriptions.add(subscription);
    }

    @Override
    public void loadCurrentContentsWithMode(ContentsViewMode mode) {
        mViewMode = mode;
        loadCurrentContents();
    }

    @Override
    public void setCurrentAssetId(String assetId) {
        mCurrentAssetId = checkNotNull(assetId);
    }

    @Override
    public void setCurrentAssetIdToRoot() {
        setCurrentAssetId(ROOT_ASSET_ID);
    }

    public String getCurrentAssetId() {
        return mCurrentAssetId;
    }

    @Override
    public void createAsset(String assetName, CategoryType category) {
        try {
            mDataManager.createAsset(assetName, mCurrentAssetId, category);
            mView.showMessage("Successfully added " + assetName);
        } catch (NullPointerException | IllegalArgumentException e) {
            mView.showMessage(e.getMessage());
        }
        loadCurrentContents();
    }

    @Override
    public void moveAssets(List<String> assetIds) {
        checkNotNull(assetIds, "The assets to move cannot be null.");
        if(assetIds.isEmpty()) return;

        for (String id : assetIds) {
            mDataManager.moveAsset(id, mCurrentAssetId);
        }
        mView.showMessage(assetIds.size() + " asset" + (assetIds.size() > 1 ? "s" : "") + " moved");
    }

    @Override
    public void renameAsset(String assetId, String newName) {
        try {
            mDataManager.updateAssetName(assetId, newName);
            mView.showMessage("Asset renamed.");
            loadCurrentContents();
        } catch (IllegalArgumentException e) {
            mView.showMessage(e.getMessage());
        }
    }

    @Override
    public void recycleAssetsRecursively(List<String> assetIds) {
        checkNotNull(assetIds);
        for (String id : assetIds) {
            mDataManager.recycleAssetAndItsContents(id);
        }
        mView.showMessage(assetIds.size() + " assets deleted");
        loadCurrentContentsWithMode(ContentsViewMode.Default);
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
        mView.showAssetContents(contentAssets, mViewMode);
        mView.showPath(parentAssets);
    }

    private IContentsView mView;
    private IDataManager mDataManager;
    private String mCurrentAssetId;
    private ContentsViewMode mViewMode;
    private ISchedulerProvider mSchedulerProvider;
    private ISchedulerProvider mImmediateSchedulerProvider;
    private CompositeSubscription mSubscriptions;

    private boolean mFirstLaunch;
    //endregion

}
