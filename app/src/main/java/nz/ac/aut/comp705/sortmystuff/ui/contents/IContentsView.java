package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.view.View;

import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IContentsView extends IView<IContentsPresenter> {

    void showAssetList(String assetID);

    void showMessageOnScreen(View view, CharSequence msg, int length);

}
