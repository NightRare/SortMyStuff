package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Intent;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IContentsView extends IView<IContentsPresenter> {

    @Deprecated
    void showRootAssetList();

    @Deprecated
    void showContainerAsset(Asset asset);

    @Deprecated
    void showAssetList(String assetID);


    // refactored methods

    void showAssetTitle(String name);

    void showAssetContents(List<Asset> assets);

    void showAddDialog();

    void showMessageOnScreen(String message);

    void showPath(List<Asset> assets);
}
