package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

public interface IDetailsView extends IView<IDetailsPresenter> {

    /**
     * Sets the presenter as given
     * @param presenter the presenter
     */
    void setPresenter(IDetailsPresenter presenter);

    /**
     * Shows list of details
     * @param detailList
     */
    void showDetails(IAsset asset, List<IDetail> detailList);

    void showRootAssetDetailPage();

    void turnToCamera(IDetail<Bitmap> photo);

    void showLoadingDetailsError(Throwable exception);

    /**
     * Checks if the view is ready.
     *
     * @return true if the view is ready
     */
    boolean isReady();


    interface ViewListeners {

        void onItemClick(View view, IDetail item);

        void onImageClick(IDetail<Bitmap> item);

        void onImageLongClick(IDetail<Bitmap> item);

        void onConfirmResetImageClick(IDetail<Bitmap> item);

        void onCancelResetImageClick(IDetail<Bitmap> item);

        void onConfirmEditTextDetail(IDetail<String> item, String text);

        void onCancelEditTextDetail(DialogInterface dialog, IDetail<String> item, String text);
    }
}
