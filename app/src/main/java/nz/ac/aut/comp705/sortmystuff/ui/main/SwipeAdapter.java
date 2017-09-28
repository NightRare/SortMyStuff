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
        checkNotNull(activity, "The activity cannot be null.");
        IFactory factory = ((SortMyStuffApp) activity.getApplication()).getFactory();
        mDataManager = factory.getDataManager();
        mSchedulerProvider = factory.getSchedulerProvider();
        mImmediateSchedulerProvider = factory.getImmediateSchedulerProvider();
        mTabsAmount = DEFAULT_TABS_AMOUNT;
        initialiseViewsAndPresenters(currentAssetId);
    }

    @Override
    public Fragment getItem(int position) {
        if(mContentsView == null || mDetailsView == null) {
            initialiseViewsAndPresenters(null);
        }

        switch (position) {
            case 0:
                return (ContentsFragment) mContentsView;
            case 1:
                return (DetailsFragment) mDetailsView;
            default:
                return (ContentsFragment) mContentsView;
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
        return mContentsPresenter;
    }

    @Nullable
    public IDetailsPresenter getDetailsPresenter() {
        return mDetailsPresenter;
    }

    @Nullable
    public IContentsView getContentsView() {
        return mContentsView;
    }

    public IDetailsView getDetailsView() {
        return mDetailsView;
    }

    public void setTabsAmount(int amount) {
        if(amount < 1 || amount > DEFAULT_TABS_AMOUNT)
            throw new IllegalArgumentException("The amount of tabs is out of range.");
        mTabsAmount = amount;
        notifyDataSetChanged();
    }

    //region PRIVATE STUFF

    private void initialiseViewsAndPresenters(String currentAssetId) {
        mDetailsView = DetailsFragment.newInstance();
        mDetailsPresenter = new DetailsPresenter(mDataManager, mDetailsView,
                mSchedulerProvider, currentAssetId);

        mContentsView = ContentsFragment.newInstance();
        mContentsPresenter = new ContentsPresenter(mDataManager, mContentsView,
                mSchedulerProvider, mImmediateSchedulerProvider, currentAssetId);
    }

    private IContentsView mContentsView;
    private IDetailsView mDetailsView;
    private IContentsPresenter mContentsPresenter;
    private IDetailsPresenter mDetailsPresenter;

    private IDataManager mDataManager;
    private ISchedulerProvider mSchedulerProvider;
    private ISchedulerProvider mImmediateSchedulerProvider;
    private int mTabsAmount;

    //endregion
}
