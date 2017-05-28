package nz.ac.aut.comp705.sortmystuff.ui.contents;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * The View interface for "Contents View" (a.k.a. Index Page) where the contained assets of the
 * container asset will be displayed and ready for interactions.
 *
 * @author Yuan
 */

public interface IContentsView extends IView<IContentsPresenter> {

    /**
     * Displays the name of the asset as the title in toolbar.
     *
     * @param name the name of the asset
     */
    void showAssetTitle(String name);

    /**
     * Displays the assets as a list.
     *
     * @param assets the assets
     */
    void showAssetContents(List<Asset> assets, boolean enableEditMode);

    /**
     * Displays the "add asset dialog".
     */
    void showAddDialog();

    /**
     * Displays a toast message at the bottom area of the screen.
     *
     * @param message the message
     */
    void showMessageOnScreen(String message);

    /**
     * Displays the path bar according to the order of the list: from left to right in view <=>
     * from the first to last in list.
     *
     * Root asset will not be displayed by this method.
     *
     * @param assets the list of parent assets, excluding Root asset.
     */
    void showPath(List<Asset> assets);

    /**
     * Displays the delete confirm dialog.
     *
     * @param deletingCurrentAsset true if it is deleting the currentAsset
     */
    void showDeleteDialog(boolean deletingCurrentAsset);
}
