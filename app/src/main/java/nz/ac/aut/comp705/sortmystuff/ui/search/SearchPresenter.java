package nz.ac.aut.comp705.sortmystuff.ui.search;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

public class SearchPresenter implements ISearchPresenter{

    public SearchPresenter(IDataManager dataManager, ISearchView view, ISchedulerProvider schedulerProvider){
        mDataManager = checkNotNull(dataManager, "The dataManager cannot be null.");
        mView = checkNotNull(view, "The view cannot be null.");
        mSchedulerProvider = checkNotNull(schedulerProvider, "The schedulerProvider cannot be null.");

        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    /**
     * When the corresponding mActivity is on created, this method will be invoked.
     */
    @Override
    public void subscribe() {
        loadResult("");
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    /**
     * Load the result of the query into the mActivity
     * @param query
     */
    @Override
    public void loadResult(String query) {
        if(query.replaceAll(" ", "").isEmpty()) {
            mView.showResultList(new ArrayList<>());
            return;
        }

        String regex = "(?i).*"+query+".*";

        mSubscriptions.clear();
        Subscription subscription = mDataManager
                .getAssets()
                .flatMap(Observable::from)
                .filter(asset -> !asset.isRoot())
                .filter(asset -> asset.getName().matches(regex))
                .toList()
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(
                        //onNext
                        this::processSearchResults,
                        //onError
                        mView::showSearchError,
                        //onCompleted
                        () -> mView.setLoadingIndicator(false)
                );

        mSubscriptions.add(subscription);
    }

    //region PRIVATE STUFF

    private void processSearchResults(List<IAsset> assets) {
        if(assets.isEmpty())
            mView.showMessage("No results found");
        mView.showResultList(assets);
    }

    private IDataManager mDataManager;
    private ISearchView mView;
    private CompositeSubscription mSubscriptions;
    private ISchedulerProvider mSchedulerProvider;

    //endregion
}
