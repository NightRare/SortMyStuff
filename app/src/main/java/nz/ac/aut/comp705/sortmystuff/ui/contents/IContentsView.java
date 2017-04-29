package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.view.View;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IContentsView extends IView<IContentsPresenter> {

    void showContainerAsset(Asset asset);

    void showAssetList(String assetID);

    void showAddDialog();

    void showMessageOnScreen(View view, CharSequence msg, int length);



}
