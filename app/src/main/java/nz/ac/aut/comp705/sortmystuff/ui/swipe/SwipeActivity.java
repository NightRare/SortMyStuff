package nz.ac.aut.comp705.sortmystuff.ui.swipe;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.desmond.squarecamera.CameraActivity;

import java.io.IOException;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.util.AppCode;
import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

public class SwipeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Load previously saved state, if available
        String currentAssetId = null;

        if (savedInstanceState != null) {
            currentAssetId = savedInstanceState.getString(CURRENT_ASSET_ID);
        }

        swipeAdapter = new SwipeAdapter(getSupportFragmentManager(), this, currentAssetId);

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(swipeAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // prepared for camera
        Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(swipeAdapter.getContentsPresenter() != null)
            outState.putString(CURRENT_ASSET_ID, swipeAdapter.getContentsPresenter().getCurrentAssetId());

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contents_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (swipeAdapter.getContentsPresenter().selectOptionItem(item))
            return true;
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (isGranted) {
                    launch();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case AppCode.INTENT_TAKE_PHOTO:
                Uri photoUri = data.getData();
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    swipeAdapter.getDetailsPresenter().updateAssetPhoto(bm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public String getCurrentAssetId() {
        if(swipeAdapter.getContentsPresenter() == null)
            return AppConstraints.ROOT_ASSET_ID;
        return swipeAdapter.getContentsPresenter().getCurrentAssetId();
    }

    //Show or hide the toolbar menu.
    public void toggleMenuDisplay(boolean showMenu) {
        if (menu == null)
            return;
        menu.setGroupVisible(R.id.main_menu_group, showMenu);
    }

    public void refreshDetails(){
        swipeAdapter.getDetailsPresenter().start();
    }

    public void takePhoto() {
        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(SwipeActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SwipeActivity.this, permission)) {
                showPermissionRationaleDialog("Test", permission);
            } else {
                requestForPermission(permission);
            }
        } else {
            launch();
        }
    }

    public void setDetailsPageVisibility(boolean isVisible) {
        if(isVisible)
            swipeAdapter.setTabsAmount(2);
        else
            swipeAdapter.setTabsAmount(1);
    }

    //region PRIVATE STUFF

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";
    private Menu menu;
    private Uri imageUri;
    private Point screenSize;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SwipeAdapter swipeAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager viewPager;

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new AlertDialog.Builder(SwipeActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.permission_allow_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SwipeActivity.this.requestForPermission(permission);
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()

                .show();
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(SwipeActivity.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void launch() {
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, AppCode.INTENT_TAKE_PHOTO);
    }


    //endregion
}
