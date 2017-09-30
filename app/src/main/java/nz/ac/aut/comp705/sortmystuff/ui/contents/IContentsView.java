package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
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
     * @param asset the asset
     */
    void showTitle(IAsset asset);

    /**
     * Displays the assets as a list.
     *
     * @param assets the assets
     * @param mode   the code of the display mode
     */
    void showAssetContents(List<IAsset> assets, int mode);

    /**
     * Displays the path bar according to the order of the list: from left to right in view <=>
     * from the first to last in list.
     * <p>
     * Root asset will not be displayed by this method.
     *
     * @param assets the list of parent assets, excluding Root asset.
     */
    void showPath(List<IAsset> assets);

    /**
     * Displays the delete confirm dialog.
     *
     * @param deletingCurrentAsset true if it is deleting the currentAsset
     */
    void showDeleteDialog(boolean deletingCurrentAsset);

    void showMessage(String message);

    void showLoadingContentsError(Throwable exception);

    void setLoadingIndicator(boolean active);

    interface ViewListeners {

        void onContentAssetClick(AdapterView<?> parent, View view, int position, long id);

        boolean onContentAssetLongClick();

        void onOptionsDeleteCurrentAssetSelected();

        void onOptionsSelectionModeSelected();

        void onAddAssetFabClick();

        void onAddAssetConfirmClick(String name, CategoryType category);

        void onPathbarRootClick();

        void onPathbarItemClick(String intendingAssetId);

        void onSelectionModeCancelClick();

        void onSelectionModeSelectAllClick();

        void onSelectionModeDeleteClick();

        void onSelectionModeMoveClick();

        void onMovingModeConfirmClick();

        void onMovingModeCancelClick();

        void onDeleteDialogConfirmClick(boolean deletingCurrentAsset);
    }
}
