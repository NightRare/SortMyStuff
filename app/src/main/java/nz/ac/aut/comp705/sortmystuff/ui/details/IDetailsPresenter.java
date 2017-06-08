package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public interface IDetailsPresenter extends IPresenter {

    /**
     * {@inheritDoc}
     */
    void start();

    /**
     * Set the current asset associated with the given ID
     * @param assetID
     */
    void setCurrentAsset(String assetID);

    /**
     * Retrieve the current asset's ID
     * @return AssetID
     */
    String getCurrentAssetID();

    /**
     * Retrieve the current asset's name
     * @return AssetName
     */
    String getCurrentAssetName();

    /**
     * Load the details associated with the current asset
     * @return DetailList
     */
    List<Detail> loadDetails();

    /**
     * Show the dialog box associated
     * with adding details
     * @param view
     */
    void showDialogBox(View view);

    /**
     * Update the image of an asset given an Bitmap type image
     * @param image
     */
    void updateImage(Bitmap image);

    /**
     * Reset the image of an asset to default placeholder
     * @param imageDetail
     */
    void resetImage(ImageDetail imageDetail);
}
