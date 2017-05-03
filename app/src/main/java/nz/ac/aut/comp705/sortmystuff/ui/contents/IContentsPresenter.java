package nz.ac.aut.comp705.sortmystuff.ui.contents;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IContentsPresenter extends IPresenter {

    void start();

    List<Asset> loadContents(String assetID);

    void addAsset(String assetName);

    void setCurrentAssetId(String assetId);

    String getCurrentAssetId();

    boolean isRootCurrentAsset();

    String getParentOf(String currentAssetId);

    String getAssetName(String assetID);
}
