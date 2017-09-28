package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.ui.main.SwipeActivity;
import nz.ac.aut.comp705.sortmystuff.utils.AppCode;

import static android.app.Activity.RESULT_OK;
import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
import static com.google.common.base.Preconditions.checkNotNull;

public class DetailsFragment extends Fragment implements IDetailsView{

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance() { return new DetailsFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailsAdapter = new DetailsAdapter(mActivity, new ArrayList<>(), mViewListeners);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (SwipeActivity) getActivity();
        mActivity.setDetailsViewListeners(mViewListeners);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.details_frag, container, false);

        mDetails = (ListView) mRootView.findViewById(R.id.details_list);
        mDetails.setAdapter(mDetailsAdapter);
        mDetails.setOnItemClickListener((parent, view, position, id) ->
                mViewListeners.onItemClick(view, (IDetail) parent.getItemAtPosition(position)));

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (isGranted) {
                    launchCamera();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case AppCode.INTENT_TAKE_PHOTO:
                Uri photoUri = data.getData();
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(
                            mActivity.getContentResolver(), photoUri);
                    mPresenter.updateAssetPhoto(mPhotoToBeReplaced, bm);
                    mPhotoToBeReplaced = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setPresenter(IDetailsPresenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void showDetails(IAsset asset, List<IDetail> detailList) {
        mActivity.findViewById(R.id.details_page_categories_title).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.assetCategory_detail).setVisibility(View.VISIBLE);
        mActivity.findViewById(R.id.details_list).setVisibility(View.VISIBLE);

        showCategoryInfo(asset.getCategoryType().toString().toUpperCase());
        mDetailsAdapter.replaceData(detailList);
    }

    @Override
    public void showRootAssetDetailPage() {
        mActivity.findViewById(R.id.details_page_categories_title).setVisibility(View.GONE);
        mActivity.findViewById(R.id.assetCategory_detail).setVisibility(View.GONE);
        mActivity.findViewById(R.id.details_list).setVisibility(View.GONE);
    }

    @Override
    public void turnToCamera(IDetail<Bitmap> photo) {
        final String permission = Manifest.permission.CAMERA;
        mPhotoToBeReplaced = photo;
        if (ContextCompat.checkSelfPermission(mActivity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                showPermissionRationaleDialog("Test", permission);
            } else {
                requestForPermission(permission);
            }
        } else {
            launchCamera();
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT);
    }

    @Override
    public void showLoadingDetailsError(Throwable exception) {
        //TODO: to be implemented
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        //TODO: to be implemented
    }

    @Override
    public boolean isReady() {
        return mRootView != null;
    }

    //region PRIVATE STUFF

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton(R.string.permission_allow_button, (dialog, which) -> requestForPermission(permission))
                .setNegativeButton(R.string.cancel_button, (dialog, which) -> { })
                .create()

                .show();
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(this.getActivity(), new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void launchCamera() {
        if(mPhotoToBeReplaced == null) return;
        Intent intent = new Intent(getContext(), CameraActivity.class);
        startActivityForResult(intent, AppCode.INTENT_TAKE_PHOTO);
    }

    /**
     * Creates an editable text area for the dialog box
     * @param context
     * @param layout
     * @return editText
     */
    private EditText createEditText(Context context, LinearLayout layout){
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        editText.setInputType(TYPE_TEXT_FLAG_CAP_SENTENCES);
        layout.addView(editText);
        return editText;
    }

    private void showCategoryInfo(String categoryString) {
        TextView assetCategory = (TextView) mActivity.findViewById(R.id.assetCategory_detail);
        assetCategory.setText(categoryString);
    }

    private ViewListeners mViewListeners = new ViewListeners() {

        @Override
        public void onItemClick(View view, IDetail item) {
            if(!item.getType().equals(DetailType.Text))
                return;

            AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
            dialog.setTitle(item.getLabel());
            //dialog box setup
            Context context = view.getContext();
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            //field text configuration
            final EditText fieldText = createEditText(context,layout);
            fieldText.setText((String) item.getField());
            //button setup
            dialog.setView(layout);
            dialog.setPositiveButton(R.string.edit_detail_confirm_button, (dialog2, which) ->
                    onConfirmEditTextDetail(item, fieldText.getText().toString()));
            dialog.setNegativeButton(R.string.cancel_button, (dialog2, which) ->
                    onCancelEditTextDetail(dialog2, item, fieldText.getText().toString()));
            dialog.show();
        }

        @Override
        public void onImageClick(IDetail<Bitmap> item) {
            turnToCamera(item);
        }

        @Override
        public void onImageLongClick(IDetail<Bitmap> item) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
            dialog.setTitle(R.string.photo_remove_dialog_title);
            dialog.setMessage("Are you sure of removing this photo?");
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.photo_remove_confirm_button,
                    (dialog1, which) -> onConfirmResetImageClick(item));
            dialog.setNegativeButton(R.string.cancel_button,
                    (dialog12, which) -> onCancelResetImageClick(item));
            dialog.show();
        }

        @Override
        public void onConfirmResetImageClick(IDetail<Bitmap> item) {
            mPresenter.resetImage(item);
        }

        @Override
        public void onCancelResetImageClick(IDetail<Bitmap> item) {
            showMessage("Removing photo cancelled.");
        }

        @Override
        public void onConfirmEditTextDetail(IDetail<String> item, String text) {
            mPresenter.updateTextDetail(item, text);
        }

        @Override
        public void onCancelEditTextDetail(DialogInterface dialog, IDetail<String> item, String text) {
            dialog.cancel();
        }
    };

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private SwipeActivity mActivity;
    private IDetailsPresenter mPresenter;
    private DetailsAdapter mDetailsAdapter;
    private ListView mDetails;
    private View mRootView;

    private IDetail<Bitmap> mPhotoToBeReplaced;

    //endregion


}
