package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

/**
 * Created by Yuan on 2017/4/28.
 */

public class ContentsPresenterAlter implements IContentsPresenter{

    private IContentsView view;

    private IDataManager dm;

    private String currentAssetId;


    public ContentsPresenterAlter(IDataManager dm, IContentsView view) {
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

        dm.getAssetAsync(currentAssetId, new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                view.showContainerAsset(asset);
                view.showAssetList(asset.getId());
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
    public String getCurrentAssetId() {
        return currentAssetId;
    }

    @Override
    public List<String> loadContents(String assetID){
        final ArrayList assetList = new ArrayList();

        dm.getContentAssetsAsync(assetID, new IDataManager.LoadAssetsCallback() {

            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                for(Asset a: assets){assetList.add(a);}
            }


            @Override
            public void dataNotAvailable(int errorCode) {
                Log.e("Does not have assets","Error " + errorCode);
            }
        });
        return assetList;
    }

    public void addAsset(String assetName, String containerID) {
        dm.createAsset(assetName,containerID);
    }
}
