package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

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

    /**
     * Shows message on screen
     * @param message
     */
    void showMessage(String message);

    void showLoadingDetailsError(Throwable exception);

    void setLoadingIndicator(boolean active);


    interface DetailsItemListener {

        void onItemClick(View view, IDetail item);

        void onImageClick(IDetail<Bitmap> item);

        void onImageLongClick(IDetail<Bitmap> item);

        void onConfirmResetImageClick(IDetail<Bitmap> item);

        void onCancelResetImageClick(IDetail<Bitmap> item);

        void onConfirmEditTextDetail(IDetail<String> item, String text);

        void onCancelEditTextDetail(DialogInterface dialog, IDetail<String> item, String text);
    }
}
