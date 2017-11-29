package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.view.MenuItem;

import java.util.List;

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
     * @param viewMode   the code of the display mode
     */
    void showAssetContents(List<IAsset> assets, ContentsViewMode viewMode);

    /**
     * Displays the path bar according to the order of the list: from left to right in view <=>
     * from the first to last in list.
     * <p>
     * Root asset will not be displayed by this method.
     *
     * @param assets the list of parent assets, excluding Root asset.
     */
    void showPath(List<IAsset> assets);

    void showDeleteDialog();

    void showDeleteDialog(IAsset asset);

    void showRenameAssetDialog(String assetId, String oldName);

    void showLoadingContentsError(Throwable exception);

    interface ViewListeners {

        void onContentAssetClick(IAsset clickedAsset);

        boolean onAssetMoreOptionsClick(IAsset clickedAsset, MenuItem clickedOption);

        boolean onContentAssetLongClick();

        void onSortContentClick();

        void onAddAssetFabClick();

        void onPathbarRootClick();

        void onPathbarItemClick(String intendingAssetId);

        void onSelectionModeCancelClick();

        void onSelectionModeSelectAllClick();

        void onSelectionModeDeleteClick();

        void onSelectionModeMoveClick();

        void onMovingModeConfirmClick();

        void onMovingModeCancelClick();

        void onDeleteDialogConfirmClick(List<String> deletingAssetIds);
    }
}
