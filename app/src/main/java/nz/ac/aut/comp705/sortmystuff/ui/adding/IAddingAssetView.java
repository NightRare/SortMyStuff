package nz.ac.aut.comp705.sortmystuff.ui.adding;

import android.graphics.Bitmap;
import android.view.View;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

public interface IAddingAssetView extends IView<IAddingAssetPresenter>{

    void showAssetPhoto(Bitmap photo);

    void showAssetName(String text);

    void showSpinner();

    void showDetails(List<IDetail> details);

    void turnToCamera();

    void goBack();

    interface ViewListeners {

        void onClickItem(View view, IDetail item);

        void onClickPhoto(View view);

        void onLongClickPhoto(View view);

        void onConfirmEditTextDetail(IDetail<String> item, String text);

        void onConfirmAddAsset();
    }
}
