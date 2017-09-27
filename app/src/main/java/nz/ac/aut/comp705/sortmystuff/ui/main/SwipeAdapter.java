package nz.ac.aut.comp705.sortmystuff.ui.main;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.ui.contents.ContentsFragment;
import nz.ac.aut.comp705.sortmystuff.ui.contents.ContentsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.contents.IContentsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.contents.IContentsView;
import nz.ac.aut.comp705.sortmystuff.ui.details.DetailsFragment;
import nz.ac.aut.comp705.sortmystuff.ui.details.DetailsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.details.IDetailsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.details.IDetailsView;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class SwipeAdapter extends FragmentPagerAdapter {

    //region CONFIGS

    private static final int DEFAULT_TABS_AMOUNT = 2;

    //endregion

    public SwipeAdapter(FragmentManager fm, SwipeActivity activity, String currentAssetId) {
        super(fm);
        mActivity = checkNotNull(activity, "The activity cannot be null.");
        IFactory factory = ((SortMyStuffApp) activity.getApplication()).getFactory();
        mDataManager = factory.getDataManager();
        mSchedulerProvider = factory.getSchedulerProvider();
        mTabsAmount = DEFAULT_TABS_AMOUNT;
        initialiseViewsAndPresenters(currentAssetId);
    }

    @Override
    public Fragment getItem(int position) {
        if(contentsView == null || detailsView == null) {
            initialiseViewsAndPresenters(null);
        }

        switch (position) {
            case 0:
                return (ContentsFragment) contentsView;
            case 1:
                return (DetailsFragment) detailsView;
            default:
                return (ContentsFragment) contentsView;
        }
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return mTabsAmount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Contents";
            case 1:
                return "Details";
        }
        return null;
    }

    @Nullable
    public IContentsPresenter getContentsPresenter() {
        return contentsPresenter;
    }

    @Nullable
    public IDetailsPresenter getDetailsPresenter() {
        return detailsPresenter;
    }

    public void setTabsAmount(int amount) {
        if(amount < 1 || amount > DEFAULT_TABS_AMOUNT)
            throw new IllegalArgumentException("The amount of tabs is out of range.");
        mTabsAmount = amount;
        notifyDataSetChanged();
    }

    //region PRIVATE STUFF

    private void initialiseViewsAndPresenters(String currentAssetId) {
        detailsView = DetailsFragment.newInstance();
        detailsPresenter = new DetailsPresenter(mDataManager, detailsView,
                mSchedulerProvider, currentAssetId);

        contentsView = ContentsFragment.newInstance();
        contentsPresenter = new ContentsPresenter(mDataManager, contentsView, mActivity, currentAssetId);
    }

    private IContentsView contentsView;
    private IDetailsView detailsView;
    private IContentsPresenter contentsPresenter;
    private IDetailsPresenter detailsPresenter;

    private IDataManager mDataManager;
    private ISchedulerProvider mSchedulerProvider;
    private SwipeActivity mActivity;
    private int mTabsAmount;

    //endregion
}
