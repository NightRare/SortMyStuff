package nz.ac.aut.comp705.sortmystuff.ui.swipe;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.util.AppCode;

public class SwipeActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        File outputImage = new File(getExternalCacheDir(),
                "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(this,
                    "sortmystuff.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, AppCode.INTENT_TAKE_PHOTO);
    }

    /**
     * Handle photo cropping or update a cropped photo.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppCode.INTENT_TAKE_PHOTO:
                    performCrop(imageUri);
                    break;
                case AppCode.INTENT_CROP_PHOTO:
                    try {
                        Bitmap croppedBmp = BitmapFactory.decodeStream
                                (getContentResolver().openInputStream(imageUri));
                        swipeAdapter.getDetailsPresenter().updateImage(croppedBmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    }

    //region PRIVATE STUFF

    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";

    private Menu menu;

    private Uri imageUri;

    /**
     * Call the standard crop action intent (the user device may not support it)
     * @param imageUri
     */
    private void performCrop(Uri imageUri) {
        Preconditions.checkNotNull(imageUri, "The image Uri cannot be null");
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(imageUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 10);
            cropIntent.putExtra("aspectY", 6);
            cropIntent.putExtra("outputX", 1000);
            cropIntent.putExtra("outputY", 600);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cropIntent, AppCode.INTENT_CROP_PHOTO);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                    "Sorry, your device doesn't support the crop action.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //endregion
}
