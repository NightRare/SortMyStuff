package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.graphics.Bitmap;

import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

public interface IDetailsPresenter extends IPresenter {

    /**
     * Load the details associated with the current asset
     * @return DetailList
     */
    void loadDetails();

    void updateTextDetail(IDetail<String> detail, String newText);

    /**
     * Update the image of an asset given an Bitmap type image
     * @param image
     */
    void updateAssetPhoto(IDetail<Bitmap> photo, Bitmap image);

    /**
     * Reset the image of an asset to default placeholder
     * @param imageDetail
     */
    void resetImage(IDetail<Bitmap> imageDetail);

    void setCurrentAsset(String currentAssetId);
}
