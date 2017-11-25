package nz.ac.aut.comp705.sortmystuff.ui.adding;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.Features;
import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.services.NameDetectionService;
import nz.ac.aut.comp705.sortmystuff.ui.BaseActivity;
import nz.ac.aut.comp705.sortmystuff.utils.AppStrings;
import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
import static com.google.common.base.Preconditions.checkNotNull;
import static nz.ac.aut.comp705.sortmystuff.utils.AppStrings.ROOT_ASSET_ID;

public class AddingAssetActivity extends BaseActivity implements IAddingAssetView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adding_asset_layout);
        setTitle(R.string.add_asset_dialog_title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.aa_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDetailsAdapter = new DetailListAdapter(this, new ArrayList<>(), mViewListeners);
        mCameraInitialLaunch = true;

        String containerId = getIntent().getStringExtra(AppStrings.INTENT_CONTAINER_ID);
        String currentAssetId = containerId == null ? ROOT_ASSET_ID : containerId;

        mPhotoView = (ImageView) findViewById(R.id.aa_asset_photo);
        mPhotoView.setOnClickListener(v -> mViewListeners.onClickPhoto(v));
        mPhotoView.setOnLongClickListener(v -> {
            mViewListeners.onLongClickPhoto(v);
            return true;
        });

        mNameView = (EditText) findViewById(R.id.aa_asset_name_input);
        mNameView.setSelectAllOnFocus(true);

        initCategorySpinner();

        RecyclerView detailsView = (RecyclerView) findViewById(R.id.aa_details_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                detailsView.getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                detailsView.getContext(),
                layoutManager.getOrientation());
        detailsView.setLayoutManager(layoutManager);
        detailsView.addItemDecoration(dividerItemDecoration);
        detailsView.setAdapter(mDetailsAdapter);

        Button cancelBtn = (Button) findViewById(R.id.aa_cancel_button);
        Button confirmBtn = (Button) findViewById(R.id.aa_confirm_button);

        confirmBtn.setOnClickListener(v -> mViewListeners.onConfirmAddAsset());
        cancelBtn.setOnClickListener(v -> goBack());

        IFactory factory = ((SortMyStuffApp) getApplication()).getFactory();
        mFeatToggle = factory.getFeatureToggle();

        mPresenter = new AddingAssetPresenter(
                factory.getDataManager(),
                this,
                factory.getSchedulerProvider(),
                factory.getLocalResourceLoader(),
                currentAssetId,
                mFeatToggle);

        mPresenter.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case AppStrings.INTENT_TAKE_PHOTO:
                try {
                    Bitmap image = null;
                    if (resultCode == RESULT_OK) {
                        Uri photoUri = data.getData();
                        image = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), photoUri);
                        image = image == null ? null : BitmapHelper.rescaleToImageDetailSize(image);
                    }

                    if (mCameraInitialLaunch) {
                        mCameraInitialLaunch = false;
                        mPresenter.addingAsset(image);
                    } else if (image != null) {
                        mPresenter.updateAssetName(image);
                        mPhoto = image;
                        mPhotoView.setImageBitmap(image);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
        }
        return true;
    }

    //region IAddingAssetView METHODS

    @Override
    public void showAssetPhoto(Bitmap photo) {
        mPhoto = photo;
        mPhotoView.setImageBitmap(mPhoto);
    }

    @Override
    public void showAssetName(String text) {
        mNameView.setText(text == null ? "" : text);
    }

    @Override
    public void showSpinner() {
        if (mSpinner == null) {
            initCategorySpinner();
        }
    }

    @Override
    public void showDetails(List<IDetail> details) {
        List<IDetail> detailWrappers = new ArrayList<>();
        for (IDetail detail : details) {
            detailWrappers.add(new DetailModel(detail));
        }
        mDetailsAdapter.replaceData(detailWrappers);
    }

    @Override
    public void turnToCamera() {
        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showPermissionRationaleDialog("Test", permission);
            } else {
                requestForPermission(permission);
            }
        } else {
            launchCamera();
        }
    }

    @Override
    public void goBack() {
        String assetCreated = mPresenter.getCreatedAssetId();
        if (assetCreated != null &&
                mFeatToggle.PhotoDetection &&
                mFeatToggle.DelayPhotoDetection) {
            Intent detectName = new Intent(this, NameDetectionService.class);
            detectName.putExtra(AppStrings.INTENT_NAME_DETECTION_ASSET_ID, assetCreated);
            startService(detectName);
        }

        this.finish();
    }

    @Override
    public void setPresenter(IAddingAssetPresenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        // not supported in this view
    }

    //endregion

    //region PRIVATE STUFF

    private void initCategorySpinner() {
        mSpinner = (Spinner) findViewById(R.id.aa_category_spinner);

        List<CategoryType> categories = new ArrayList();
        for (CategoryType cy : CategoryType.values()) {
            if (cy.equals(CategoryType.None))
                continue;
            categories.add(cy);
        }
        mSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);

        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.selectCategory(mSpinnerAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinner.setSelection(mSpinnerAdapter.getPosition(CategoryType.Miscellaneous));
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.permission_allow_button, (dialog, which) -> requestForPermission(permission))
                .setNegativeButton(R.string.cancel_button, (dialog, which) -> {
                })
                .create()
                .show();
    }

    private void launchCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, AppStrings.INTENT_TAKE_PHOTO);
    }

    private EditText createEditText(Context context, LinearLayout layout) {
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        editText.setInputType(TYPE_TEXT_FLAG_CAP_SENTENCES);
        layout.addView(editText);
        return editText;
    }

    private class AddingAssetViewListener implements
            IAddingAssetView.ViewListeners,
            DetailListAdapter.DetailItemListener {

        @Override
        public void onClickItem(View view, IDetail item) {
            if (!item.getType().equals(DetailType.Text) &&
                    !item.getType().equals(DetailType.Date))
                return;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
            dialogBuilder.setTitle(item.getLabel());
            //dialog box setup
            Context context = view.getContext();
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            //field text configuration
            EditText fieldText = createEditText(context, layout);
            fieldText.setText((String) item.getField());

            //button setup
            dialogBuilder.setView(layout);
            dialogBuilder.setPositiveButton(R.string.edit_detail_confirm_button, (dialog2, which) ->
                    onConfirmEditTextDetail(item, fieldText.getText().toString()));
            dialogBuilder.setNegativeButton(R.string.cancel_button, (dialog, which) -> dialog.cancel());

            dialogBuilder.show()
                    // auto pop up the keyboard
                    .getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        @Override
        public void onClickPhoto(View view) {
            turnToCamera();
        }

        @Override
        public void onLongClickPhoto(View view) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(AddingAssetActivity.this);
            dialog.setTitle(R.string.photo_remove_dialog_title);
            dialog.setMessage("Are you sure of removing this photo?");
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.photo_remove_confirm_button,
                    (dialog1, which) -> mPresenter.resetPhoto());
            dialog.setNegativeButton(R.string.cancel_button,
                    (dialog2, which) -> dialog2.cancel());
            dialog.show();
        }

        @Override
        public void onConfirmEditTextDetail(IDetail<String> item, String text) {
            DetailModel<String> detail = (DetailModel<String>) item;
            detail.setField(text);
            mDetailsAdapter.notifyDataSetChanged();
        }

        @Override
        public void onConfirmAddAsset() {
            String name = mNameView.getText().toString();
            CategoryType category = (CategoryType) mSpinner.getSelectedItem();
            mPresenter.createAsset(name, category, mPhoto, mDetailsAdapter.getItems());
        }
    }

    private static class DetailModel<T> implements IDetail<T> {

        private IDetail<T> mEntity;
        private T mField;

        DetailModel(IDetail<T> entity) {
            mEntity = entity;
        }

        @Override
        public String getId() {
            return mEntity.getId();
        }

        @Override
        public String getAssetId() {
            return mEntity.getAssetId();
        }

        @Override
        public DetailType getType() {
            return mEntity.getType();
        }

        @Override
        public String getLabel() {
            return mEntity.getLabel();
        }

        @Override
        public T getField() {
            return mField == null ? mEntity.getField() : mField;
        }

        @Override
        public Long getCreateTimestamp() {
            return mEntity.getCreateTimestamp();
        }

        @Override
        public Long getModifyTimestamp() {
            return mEntity.getModifyTimestamp();
        }

        @Override
        public int getPosition() {
            return mEntity.getPosition();
        }

        @Override
        public boolean isDefaultFieldValue() {
            // if mField is not null, then return true
            // else return the value of the entity.isDefaultFieldValue()
            return mField == null && mEntity.isDefaultFieldValue();
        }

        public void setField(T field) {
            mField = field;
        }
    }

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private IAddingAssetPresenter mPresenter;
    private Features mFeatToggle;

    private AddingAssetViewListener mViewListeners = new AddingAssetViewListener();
    private boolean mCameraInitialLaunch;

    private ImageView mPhotoView;
    private Bitmap mPhoto;

    private EditText mNameView;

    private Spinner mSpinner;
    private ArrayAdapter<CategoryType> mSpinnerAdapter;

    private DetailListAdapter mDetailsAdapter;

    //endregion
}
