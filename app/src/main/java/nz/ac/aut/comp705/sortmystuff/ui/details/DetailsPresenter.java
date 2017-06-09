package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.util.Log;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public class DetailsPresenter implements IDetailsPresenter {

    IDataManager dm;
    IDetailsView view;
    DetailsActivity activity;
    String currentAsset;

    /**
     * Initialises the detail presenter
     *
     * @param dm       the IDataManager instance
     * @param view     the IContentsView instance
     * @param activity the ContentsActivity instance
     */
    public DetailsPresenter(IDataManager dm, IDetailsView view, DetailsActivity activity) {
        this.dm = dm;
        this.view = view;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        Intent intent = activity.getIntent();
        setCurrentAsset(intent.getStringExtra("AssetID"));
        setTitleOnToolbar();
        setAsset();
        view.showDetails(loadDetails());
    }

    /**
     * {@inheritDoc}
     *
     * @param assetID
     */
    @Override
    public void setCurrentAsset(String assetID) {
        currentAsset = assetID;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getCurrentAssetID() {
        return currentAsset;
    }

    /**
     * {@inheritDoc}
     *
     * @return assetName
     */
    @Override
    public String getCurrentAssetName() {
        final String[] name = new String[1];
        dm.getAssetAsync(getCurrentAssetID(), new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                name[0] = asset.getName();
            }

            @Override
            public void dataNotAvailable(int errorCode) {
                Log.e("Data not found ","Error: "+errorCode);
            }
        });
        return name[0];
    }

    /**
     * {@inheritDoc}
     *
     * @return detailList
     */
    @Override
    public List<Detail> loadDetails() {
        final ArrayList detailList = new ArrayList();
        dm.getDetailsAsync(getCurrentAssetID(), new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                for(Detail d: details){
                    if(d.getType()!= DetailType.Image){
                        detailList.add(d);
                    }
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {
            }
        });
        return  detailList;
    }

    /**
     * {@inheritDoc}
     *
     * @param view
     */
    @Override
    public void showDialogBox(View view, Detail detail){
        getEditDetailDialogBox(view, detail).create().show();
    }

    private void setAsset(){
        TextView assetName = (TextView) activity.findViewById(R.id.assetName_detail);
        assetName.setText(getCurrentAssetName());
        TextView assetCategory = (TextView) activity.findViewById(R.id.assetCategory_detail);
        assetCategory.setText(getCategory().toUpperCase());
    }

    /**
     * Edit selected detail of current asset
     * @param  detailId
     * @param label
     * @param field
     */
    private void editDetail(String detailId, String label, String field) {
        //dm.createTextDetail(getCurrentAssetID(), label, field);
        dm.updateTextDetail(getCurrentAssetID(),detailId,label,field);
    }

    /**
     * Setup the dialog box for adding details
     * that enables two single line inputs for
     * detail label and field, and has a
     * functional save and cancel button
     * @param view
     * @return dialog
     */
    private AlertDialog.Builder getEditDetailDialogBox(View view, final Detail detail){
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
        dialog.setTitle(detail.getLabel());
        //dialog box setup
        Context context = view.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        //field text configuration
        final EditText fieldText = createEditText(context,layout);
        fieldText.setText(detail.getField().toString());
        //button setup
        dialog.setView(layout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editDetail(detail.getId(),detail.getLabel(),fieldText.getText().toString());
                        activity.showDetails(loadDetails());
                        activity.showMessage("Edited " + detail.getLabel());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.cancel();
                    }
                });
        return dialog;
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

//    /**
//     * Add basic details to the current asset as
//     * with the label of "Name" and the current
//     * asset name as the field
//     */
//    private void addBasicDetail() {
//        addDetail("Name",getCurrentAssetName());
//    }

    /**
     * Set toolbar title
     */
    private void setTitleOnToolbar(){
        activity.setTitle("Detail: " + getCurrentAssetName());
    }

    public void updateImage(final Bitmap newImage) {
        dm.getDetailsAsync(currentAsset, new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                for(Detail d : details) {
                    if(d.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
                        dm.updateImageDetail((ImageDetail) d, d.getLabel(), newImage);
                    }
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    private String getCategory(){
        final String[] name = new String[1];
        dm.getAssetAsync(getCurrentAssetID(), new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                name[0] = asset.getCategoryType().toString();
            }

            @Override
            public void dataNotAvailable(int errorCode) {
                Log.e("Data not found ","Error: "+errorCode);
            }
        });
        return name[0];
    }

}
