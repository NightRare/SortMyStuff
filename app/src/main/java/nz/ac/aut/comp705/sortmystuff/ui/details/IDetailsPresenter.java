package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public interface IDetailsPresenter extends IPresenter {

    /**
     * Retrieve the current asset's ID
     * @return AssetID
     */
    String getCurrentAssetID();

    /**
     * Load the details associated with the current asset
     * @return DetailList
     */
    List<Detail> loadDetails();

    void updateTextDetail(TextDetail detail, String newText);

    /**
     * Update the image of an asset given an Bitmap type image
     * @param image
     */
    void updateAssetPhoto(Bitmap image);

    /**
     * Reset the image of an asset to default placeholder
     * @param imageDetail
     */
    void resetImage(ImageDetail imageDetail);
}
