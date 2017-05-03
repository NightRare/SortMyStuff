package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

/**
 * Created by Yuan on 2017/4/28.
 */

public class ContentsPresenter implements IContentsPresenter{

    private IContentsView view;

    private IDataManager dm;

    private String currentAssetId;

    public ContentsPresenter(IDataManager dm, IContentsView view) {
        this.dm = dm;
        this.view = view;
    }

    @Override
    public void start() {
        // if the app just launched, display Root Asset
        if(currentAssetId == null) {
            Asset root = dm.getRootAsset();
            if(root != null) {
                currentAssetId = dm.getRootAsset().getId();
            }
            else {
                dm.createRootAsset();
                currentAssetId = dm.getRootAsset().getId();
            }
        }
        loadCurrentContents(false);
    }

    @Override
    public void loadCurrentContents(boolean forceRefreshFromLocal) {
        if(forceRefreshFromLocal)
            dm.refreshFromLocal();

        dm.getAssetAsync(currentAssetId, new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                view.showAssetTitle(asset.getName());

                dm.getContentAssetsAsync(asset, new IDataManager.LoadAssetsCallback() {
                    @Override
                    public void onAssetsLoaded(List<Asset> assets) {
                        view.showAssetContents(assets);
                    }

                    @Override
                    public void dataNotAvailable(int errorCode) {

                    }
                });
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    @Override
    public void setCurrentAssetId(String assetId) {
        currentAssetId = assetId;
    }

    @Override
    public void setCurrentAssetIdToRoot() {
        currentAssetId = dm.getRootAsset().getId();
    }

    @Override
    public void setCurrentAssetIdToContainer() {
        dm.getParentAssetsAsync(currentAssetId, new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                if(!assets.isEmpty()) {
                    currentAssetId = assets.get(0).getId();
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    @Override
    public String getCurrentAssetId() {
        return currentAssetId;
    }

    @Override
    public void addAsset(String assetName) {
        dm.createAsset(assetName, currentAssetId);
        loadCurrentContents(false);
    }

    @Deprecated
    @Override
    public List<Asset> loadContents(String assetID) {
        // deprecated method
        return null;
    }

    @Deprecated
    @Override
    public boolean isRootCurrentAsset() {
        // deprecated method
        return false;
    }

    @Deprecated
    @Override
    public String getParentOf(String currentAssetId) {
        // deprecated method
        return null;
    }

    @Deprecated
    @Override
    public String getAssetName(String assetID) {
        // deprecated method
        return null;
    }

}
