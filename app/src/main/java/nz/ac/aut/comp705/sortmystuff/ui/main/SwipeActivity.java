package nz.ac.aut.comp705.sortmystuff.ui.main;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.utils.AppConstraints;

public class SwipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Load previously saved state, if available
        String currentAssetId = savedInstanceState == null ?
                AppConstraints.ROOT_ASSET_ID : savedInstanceState.getString(CURRENT_ASSET_ID);

        mSwipeAdapter = new SwipeAdapter(getSupportFragmentManager(), this, currentAssetId);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSwipeAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // prepared for camera
        Display display = getWindowManager().getDefaultDisplay();
        mScreenSize = new Point();
        display.getSize(mScreenSize);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mSwipeAdapter.getContentsPresenter() != null)
            outState.putString(CURRENT_ASSET_ID, mSwipeAdapter.getContentsPresenter().getCurrentAssetId());

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contents_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mSwipeAdapter.getContentsPresenter().selectOptionItem(item))
            return true;
        return super.onOptionsItemSelected(item);

    }

    public void setCurrentAsset(IAsset asset) {
        setTitle(asset.getName());
        if(mSwipeAdapter == null) return;
        refreshDetails(asset.getId());
    }

    //Show or hide the toolbar menu.
    public void toggleMenuDisplay(boolean showMenu) {
        if (mMenu == null)
            return;
        mMenu.setGroupVisible(R.id.main_menu_group, showMenu);
    }

    public void setDetailsPageVisibility(boolean isVisible) {
        if(isVisible)
            mSwipeAdapter.setTabsAmount(2);
        else
            mSwipeAdapter.setTabsAmount(1);
    }

    //region PRIVATE STUFF

    private void refreshDetails(String assetId){
        mSwipeAdapter.getDetailsPresenter().setCurrentAsset(assetId);
        mSwipeAdapter.getDetailsPresenter().subscribe();
    }

    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";
    private Menu mMenu;


    private Point mScreenSize;
    private SwipeAdapter mSwipeAdapter;
    private ViewPager mViewPager;


    //endregion
}
