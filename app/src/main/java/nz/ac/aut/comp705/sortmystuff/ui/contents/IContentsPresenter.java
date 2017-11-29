package nz.ac.aut.comp705.sortmystuff.ui.contents;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;
import nz.ac.aut.comp705.sortmystuff.utils.AppStrings;

/**
 * The Presenter interface for "Contents View" (a.k.a. Index Page) where the contained assets of the
 * container asset will be displayed and ready for interactions.
 *
 * @author Yuan
 */

public interface IContentsPresenter extends IPresenter {

    /**
     * Loads the contents (contained assets) of the current asset as in the previous interface mode.
     */
    void loadCurrentContents();

    /**
     * Loads the contents (contained assets) of the current asset as in the specified interface mode.
     *
     * @param mode the code of the mode, refer to {@link AppStrings}
     */
    void loadCurrentContentsWithMode(ContentsViewMode mode);

    /**
     * Sets the current asset id to the given one. This will change the current asset.
     *
     * @param assetId the asset id
     */
    void setCurrentAssetId(String assetId);

    /**
     * Sets the current asset id to Root asset's id, and changes the current asset to the
     * Root asset.
     */
    void setCurrentAssetIdToRoot();

    /**
     * Gets the asset id of the current asset. This method is mainly for persist the state of
     * current asset in ContentsActivity.
     *
     * @return the id of the current asset.
     */
    String getCurrentAssetId();

    /**
     * Creates a new asset whose name is as given.
     *
     * @param assetName the name of the new asset.
     */
    void createAsset(String assetName, CategoryType category);

    /**
     * Move a list of assets to a new container.
     *
     * @param assetIds the assets to move.
     */
    void moveAssets(List<String> assetIds);

    void deleteCurrentAsset();

    /**
     * Recycle the current asset and all its contained assets.
     */
    void recycleCurrentAssetRecursively();

    /**
     * Recycle the selected assets and all their contained assets.
     *
     * @param assetIds the list of the assets to be recycled
     */
    void recycleAssetsRecursively(List<String> assetIds);

}
