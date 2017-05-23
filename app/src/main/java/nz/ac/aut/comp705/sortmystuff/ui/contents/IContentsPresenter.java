package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.view.MenuItem;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * The Presenter interface for "Contents View" (a.k.a. Index Page) where the contained assets of the
 * container asset will be displayed and ready for interactions.
 *
 * @author Yuan
 */

public interface IContentsPresenter extends IPresenter {

    /**
     * Loads the contents (contained assets) of the current asset.
     *
     * @param forceRefreshFromLocal true if want to force reload the contents from local storage
     */
    void loadCurrentContents(boolean forceRefreshFromLocal);

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
     * Sets the current asset id to the container of the current asset. If current asset is
     * Root asset, then nothing will be done.
     */
    void setCurrentAssetIdToContainer();

    /**
     * Gets the asset id of the current asset. This method is mainly for persist the state of
     * current asset in ContentsActivity.
     *
     * @return the id of the current asset.
     */
    String getCurrentAssetId();

    /**
     * Adds a new asset whose name is as given.
     *
     * @param assetName the name of the new asset.
     */
    void addAsset(String assetName);

    /**
     * Performs corresponding action when the given menu item is selected (interacted with) by
     * the user.
     *
     * @param item the menu item
     * @return true if the corresponding action is performed as expected
     */
    boolean selectOptionItem(MenuItem item);

    /**
     * Active selection mode (namely edit mode) where each asset can be selected/deselected
     * and then be modified.
     */
    void enableEditMode();

    /**
     * Quit selection mode (namely edit mode) where each asset can not be selected or modified.
     */
    void quitEditMode();

    /**
     * Recycle the current asset and all its contained assets.
     */
    void recycleCurrentAssetRecursively();

    /**
     * Recycle the selected assets and all their contained assets.
     *
     * @param assetIds the list of ids of the assets to be recycled
     */
    void recycleAssetsRecursively(List<String> assetIds);

//    void enableMoveMode();
//
//    void quitMoveMode();
}
