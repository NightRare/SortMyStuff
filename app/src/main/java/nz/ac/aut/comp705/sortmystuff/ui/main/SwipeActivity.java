package nz.ac.aut.comp705.sortmystuff.ui.main;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.net.MalformedURLException;
import java.net.URL;

import nz.ac.aut.comp705.sortmystuff.Features;
import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.services.PRSStatus;
import nz.ac.aut.comp705.sortmystuff.services.PhotoRecognitionService;
import nz.ac.aut.comp705.sortmystuff.ui.BaseActivity;
import nz.ac.aut.comp705.sortmystuff.ui.contents.IContentsView;
import nz.ac.aut.comp705.sortmystuff.ui.details.IDetailsView;
import nz.ac.aut.comp705.sortmystuff.ui.login.LoginActivity;
import nz.ac.aut.comp705.sortmystuff.ui.search.SearchActivity;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import nz.ac.aut.comp705.sortmystuff.utils.Log;
import rx.functions.Action1;

import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppConfigs.DELAYED_PHOTO_RECOGNITION_MILLIS;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ROOT_ASSET_ID;

public class SwipeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_act);

        isRootAsset = true;
        mFactory = ((SortMyStuffApp) getApplication()).getFactory();
        mFeatToggle = mFactory.getFeatureToggle();

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

        // Set up the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_drawer_view);
        navigationView.setItemIconTintList(null);
        Menu menu = navigationView.getMenu();
        mPRServiceItem = menu.findItem(R.id.nav_drawer_menu_prservice);

        setPhotoRecognitionServiceStatus(PRSStatus.Ready);

        if (navigationView != null) {
            registerDrawerMenuListener(navigationView);
            updateNavigationDrawerHeader(navigationView);
        }

        // Load previously saved state, if available
        String currentAssetId = ROOT_ASSET_ID;
        if (savedInstanceState != null) {
            String intendedAssetId = savedInstanceState.getString(CURRENT_ASSET_ID);
            currentAssetId = intendedAssetId == null ? currentAssetId : intendedAssetId;
            //remove it from the savedInstanceState
            savedInstanceState.remove(CURRENT_ASSET_ID);
        }

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

        if (mFeatToggle.PhotoDetection && mFeatToggle.DelayPhotoDetection)
            bindNameDetectionService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mSwipeAdapter.getContentsPresenter() != null)
            outState.putString(CURRENT_ASSET_ID, mSwipeAdapter.getContentsPresenter().getCurrentAssetId());

        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.contents_menu, mMenu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // set menu button visibility according to whether it's root asset
        MenuItem deleteBtn = mMenu.findItem(R.id.delete_current_asset_button);
        deleteBtn.setVisible(!isRootAsset);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_view_button:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                this.startActivity(searchIntent);
                return true;

            case R.id.selection_mode_button:
                mContentsViewListeners.onOptionsSelectionModeSelected();
                return true;

            case R.id.delete_current_asset_button:
                mContentsViewListeners.onOptionsDeleteCurrentAssetSelected();
                return true;

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // temporarily disable launching pr service automatically
//        if (requestCode == AppStrings.REQUEST_NEW_ASSET && resultCode == RESULT_OK) {
//            if (data.hasExtra(AppStrings.INTENT_ASSET_ID)) {
//
//                startPhotoRecognition(true);
//            }
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setContentsViewListeners(IContentsView.ViewListeners listener) {
        mContentsViewListeners = checkNotNull(listener);
    }

    public void setDetailsViewListeners(IDetailsView.ViewListeners listener) {
        mDetailsViewListeners = checkNotNull(listener);
    }

    public void setCurrentAsset(IAsset asset) {
        setTitle(asset.getName());

        //if the current asset changing from (root asset/other assets) to (other assets/root asset)
        //then prepare the options menu again
        if (isRootAsset != asset.isRoot()) {
            isRootAsset = asset.isRoot();
            onPrepareOptionsMenu(mMenu);
        }

        if (mSwipeAdapter == null) return;
        refreshDetails(asset.getId());
    }

    //Show or hide the toolbar menu.
    public void toggleMenuDisplay(boolean showMenu) {
        if (mMenu == null)
            return;
        mMenu.setGroupVisible(R.id.main_menu_group, showMenu);
    }

    public void setDetailsPageVisibility(boolean isVisible) {
        if (isVisible)
            mSwipeAdapter.setTabsAmount(2);
        else
            mSwipeAdapter.setTabsAmount(1);
    }

    public void updateNavigationDrawerHeader(NavigationView navigationView) {

        View navDrawerHeaderView = navigationView.inflateHeaderView(R.layout.nav_drawer_header);

        ImageView userPhotoView = (ImageView) navDrawerHeaderView.findViewById(R.id.nav_drawer_user_photo);
        TextView userNameView = (TextView) navDrawerHeaderView.findViewById(R.id.nav_drawer_user_name);
        TextView userEmailView = (TextView) navDrawerHeaderView.findViewById(R.id.nav_drawer_user_email);

        FirebaseUser currentUser = mFactory.getCurrentUser();
        if (currentUser == null) return;

        String urlString = currentUser.getPhotoUrl().toString();
        try {
            BitmapHelper.fromURI(new URL(urlString))
                    .subscribe(
                            //onNext
                            userPhoto -> {
                                if (userPhoto != null) {
                                    userPhotoView.setImageBitmap(userPhoto);
                                }
                            }
                    );
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        userNameView.setText(currentUser.getDisplayName());
        userEmailView.setText(currentUser.getEmail());
    }

    public void setPhotoRecognitionServiceStatus(PRSStatus status) {
        int color = 0;
        switch (status) {
            case Disabled:
                stopAnimatePRServiceIcon();
                mPRServiceItem.setIcon(R.drawable.ic_prservice_offline);
                mPRServiceItem.setTitle(R.string.nav_drawer_menu_prservice_offline);
                break;
            case Ready:
                stopAnimatePRServiceIcon();
                mPRServiceItem.setIcon(R.drawable.ic_prservice_ready);
                mPRServiceItem.setTitle(R.string.nav_drawer_menu_prservice_ready);
                break;
            case InProgress:
                mPRServiceItem.setIcon(R.drawable.ic_prservice_inprogress);
                mPRServiceItem.setTitle(R.string.nav_drawer_menu_prservice_inprogress);
                startAnimatePRServiceIcon(mPRServiceItem.getIcon());
                break;
            case Completed:
                stopAnimatePRServiceIcon();
                mPRServiceItem.setIcon(R.drawable.ic_prservice_completed);
                mPRServiceItem.setTitle(R.string.nav_drawer_menu_prservice_completed);
                break;
        }
    }

    //region PRIVATE STUFF

    private void startPhotoRecognition(boolean delayStart) {
        if (mPRServiceBinder == null) {
            setPhotoRecognitionServiceStatus(PRSStatus.Disabled);
            return;
        }

        if (mPRServiceBinder.isRunning())
            return;

        if (mPRServiceBinder.isPending()) {
            mPRServiceBinder.terminateTask();
        }

        setPhotoRecognitionServiceStatus(PRSStatus.InProgress);

        mPRServiceBinder.startTask(delayStart ? DELAYED_PHOTO_RECOGNITION_MILLIS : 0)
                .observeOn(mFactory.getSchedulerProvider().ui())
                .subscribe(
                        // onNext
                        list -> setPhotoRecognitionServiceStatus(PRSStatus.Completed),
                        // onError
                        throwable -> setPhotoRecognitionServiceStatus(PRSStatus.Completed)
                );
    }

    private void refreshDetails(String assetId) {
        mSwipeAdapter.getDetailsPresenter().setCurrentAsset(assetId);
        mSwipeAdapter.getDetailsPresenter().start();
    }

    private void registerDrawerMenuListener(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_drawer_menu_sign_out:
                            signOut();
                            break;

                        case R.id.nav_drawer_menu_prservice:
                            if (mPRServiceBinder != null &&
                                    !mPRServiceBinder.isRunning() &&
                                    !mPRServiceBinder.isPending())
                                startPhotoRecognition(false);
                        default:
                            break;
                    }
                    // Close the navigation drawer when an item is selected.
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    return true;
                });
    }

    private void signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();

        // remove all the event listeners
        mFactory.getRemoteRepository().removeOnDataChangeCallback(null);

        Action1<Status> signOutAction = status -> {
            Intent toLoginPage = new Intent(this, LoginActivity.class);
            toLoginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(toLoginPage);
            finish();
        };

        GoogleApiClient gac = mFactory.getGoogleApiClient();
        if (gac != null) {
            // Google sign out
            if (!gac.isConnected()) {
                gac.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Auth.GoogleSignInApi.signOut(gac)
                                .setResultCallback(signOutAction::call);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                });
                gac.connect();
            } else {
                Auth.GoogleSignInApi.signOut(gac)
                        .setResultCallback(signOutAction::call);
            }
        } else {
            signOutAction.call(null);
        }

    }

    private void bindNameDetectionService() {
        mPRServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mPRServiceBinder = (PhotoRecognitionService.ServiceBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        Intent intent = new Intent(this, PhotoRecognitionService.class);
        startService(intent);
        bindService(intent, mPRServiceConnection, BIND_AUTO_CREATE);
    }

    private void stopAnimatePRServiceIcon() {
        if (mPRServiceIconAnimator != null) {
            mPRServiceIconAnimator.end();
            mPRServiceIconAnimator = null;
        }
    }

    private void startAnimatePRServiceIcon(final Drawable drawable) {
        final int gray = getResources().getColor(R.color.light_grey);

        mPRServiceIconAnimator = ObjectAnimator.ofFloat(0f, 1f);
        mPRServiceIconAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mul = (Float) animation.getAnimatedValue();
                int alphaColor = adjustAlpha(gray, mul);
                drawable.setColorFilter(alphaColor, PorterDuff.Mode.SRC_ATOP);
                if (mul == 0.0) {
                    drawable.setColorFilter(null);
                }
            }
        });

        mPRServiceIconAnimator.setDuration(1000);
        mPRServiceIconAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mPRServiceIconAnimator.setRepeatCount(-1);
        mPRServiceIconAnimator.start();
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    // Photo Recognition Service Status

    private static final String TAG = "SwipeActivity";
    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";

    private Menu mMenu;
    private Point mScreenSize;
    private SwipeAdapter mSwipeAdapter;
    private ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private IContentsView.ViewListeners mContentsViewListeners;
    private IDetailsView.ViewListeners mDetailsViewListeners;
    private Features mFeatToggle;

    private PhotoRecognitionService.ServiceBinder mPRServiceBinder;
    private ServiceConnection mPRServiceConnection;
    private MenuItem mPRServiceItem;
    private ValueAnimator mPRServiceIconAnimator;

    private boolean isRootAsset;
    private IFactory mFactory;

    //endregion
}
