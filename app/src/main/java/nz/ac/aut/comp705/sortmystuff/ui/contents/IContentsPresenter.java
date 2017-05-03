package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IContentsPresenter extends IPresenter {

    @Deprecated
    List<Asset> loadContents(String assetID);

    @Deprecated
    boolean isRootCurrentAsset();

    @Deprecated
    String getParentOf(String currentAssetId);

    @Deprecated
    String getAssetName(String assetID);

    // refactored methods

    void loadCurrentContents(boolean forceRefreshFromLocal);

    void setCurrentAssetId(String assetId);

    void setCurrentAssetIdToRoot();

    void setCurrentAssetIdToContainer();

    String getCurrentAssetId();

    void addAsset(String assetName);

    boolean selectOptionItem(MenuItem item);
}
