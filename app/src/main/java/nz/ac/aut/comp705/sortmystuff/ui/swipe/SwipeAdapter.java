package nz.ac.aut.comp705.sortmystuff.ui.swipe;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.ui.PlaceHolderFragment;
import nz.ac.aut.comp705.sortmystuff.ui.contents.ContentsFragment;
import nz.ac.aut.comp705.sortmystuff.ui.contents.ContentsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.contents.IContentsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.contents.IContentsView;
import nz.ac.aut.comp705.sortmystuff.ui.details.DetailsFragment;
import nz.ac.aut.comp705.sortmystuff.ui.details.DetailsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.details.IDetailsPresenter;
import nz.ac.aut.comp705.sortmystuff.ui.details.IDetailsView;

/**
 * Created by YuanY on 2017/9/4.
 */

public class SwipeAdapter extends FragmentPagerAdapter {

    public SwipeAdapter(FragmentManager fm, SwipeActivity activity, String currentAssetId) {
        super(fm);
        this.activity = activity;
        this.dm = ((SortMyStuffApp) activity.getApplication()).getFactory().getDataManager();
        this.tabsAmount = MAX_TABS_AMOUNT;
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
        return tabsAmount;
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
        if(amount < 1 || amount > MAX_TABS_AMOUNT)
            throw new IllegalArgumentException("The amount of tabs is out of range.");
        tabsAmount = amount;
        notifyDataSetChanged();
    }

    private void initialiseViewsAndPresenters(String currentAssetId) {
        detailsView = DetailsFragment.newInstance();
        detailsPresenter = new DetailsPresenter(dm, detailsView, activity);
        detailsView.setPresenter(detailsPresenter);

        contentsView = ContentsFragment.newInstance();
        contentsPresenter = new ContentsPresenter(dm, contentsView, activity);
        contentsView.setPresenter(contentsPresenter);
        if(currentAssetId != null)
            contentsPresenter.setCurrentAssetId(currentAssetId);
    }

    private static final int MAX_TABS_AMOUNT = 2;

    private IContentsView contentsView;
    private IDetailsView detailsView;
    private IContentsPresenter contentsPresenter;
    private IDetailsPresenter detailsPresenter;

    private IDataManager dm;
    private SwipeActivity activity;
    private int tabsAmount;
}
