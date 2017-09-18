package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.ui.swipe.SwipeActivity;
import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.util.Log;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public class DetailsPresenter implements IDetailsPresenter {

    IDataManager dm;
    IDetailsView view;
    SwipeActivity activity;
    String currentAssetId;

    /**
     * Initialises the detail presenter
     *
     * @param dm       the IDataManager instance
     * @param view     the IContentsView instance
     * @param activity the ContentsActivity instance
     */
    public DetailsPresenter(IDataManager dm, IDetailsView view, SwipeActivity activity) {
        this.dm = dm;
        this.view = view;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        // if the view is not inflated then do not start the presenter
        if(activity.findViewById(R.id.details_page_categories_title) == null) return;

        currentAssetId = activity.getCurrentAssetId();

        // show nothing if its root asset
        if(currentAssetId.equals(AppConstraints.ROOT_ASSET_ID)) {
            activity.findViewById(R.id.details_page_categories_title).setVisibility(View.GONE);
            activity.findViewById(R.id.assetCategory_detail).setVisibility(View.GONE);
            activity.findViewById(R.id.details_list).setVisibility(View.GONE);
        }
        else {
            activity.findViewById(R.id.details_page_categories_title).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.assetCategory_detail).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.details_list).setVisibility(View.VISIBLE);
            setAsset();
            view.showDetails(loadDetails());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getCurrentAssetID() {
        return currentAssetId;
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
                        detailList.add(d);
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {
            }
        });
        return  detailList;
    }

    @Override
    public void updateTextDetail(final TextDetail detail, String newText) {
        try {
            dm.updateTextDetail(getCurrentAssetID(),detail.getId(),detail.getLabel(),newText);
        } catch (IllegalArgumentException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        view.showDetails(loadDetails());
        view.showMessage("Edited " + detail.getLabel());
    }

    private void setAsset(){
        TextView assetCategory = (TextView) activity.findViewById(R.id.assetCategory_detail);
        assetCategory.setText(getCategory().toUpperCase());
    }

    /**
     * {@inheritDoc}
     * @param newImage
     */
    @Override
    public void updateAssetPhoto(final Bitmap newImage) {
        Preconditions.checkNotNull(newImage, "The image cannot be null");
        dm.getDetailsAsync(currentAssetId, new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                for(Detail d : details) {
                    if(d.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
                        dm.updateImageDetail((ImageDetail) d, d.getLabel(), newImage);
                        view.showDetails(details);
                        break;
                    }
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    /**
     * {@inheritDoc}
     * @param imageDetail
     */
    @Override
    public void resetImage(ImageDetail imageDetail) {
        Preconditions.checkNotNull(imageDetail, "The image detail cannot be null");
        dm.resetImageDetail(imageDetail);
        dm.getDetailsAsync(currentAssetId, new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                view.showDetails(details);
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
