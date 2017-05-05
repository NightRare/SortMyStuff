package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.view.MenuItem;
import android.view.View;

import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IContentsPresenter extends IPresenter {

    void loadCurrentContents(boolean forceRefreshFromLocal);

    void setCurrentAssetId(String assetId);

    void setCurrentAssetIdToRoot();

    void setCurrentAssetIdToContainer();

    String getCurrentAssetId();

    void addAsset(String assetName);

    boolean selectOptionItem(MenuItem item);

    void enableEditMode(View view);
}
