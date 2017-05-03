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
    public boolean isRootCurrentAsset(){
        return dm.getRootAsset().getId() == getCurrentAssetId();
    }

    @Deprecated
    @Override
    public List<Asset> loadContents(){
        final ArrayList assetList = new ArrayList();

        dm.getContentAssetsAsync(currentAssetId, new IDataManager.LoadAssetsCallback() {

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

    public void addAsset(String assetName) {
        dm.createAsset(assetName, currentAssetId);
        loadCurrentContents(false);
    }

    public String getParentOf(String currentAssetId){
        final String[] parentID = new String[1];
        dm.getParentAssetsAsync(currentAssetId, new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                if(getCurrentAssetId() == dm.getRootAsset().getId()){
                    parentID[0] = dm.getRootAsset().getId();
                } else {
                    parentID[0] = assets.get(0).getId();
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
        return parentID[0];
    }

    public String getAssetName(String assetID){
        final String[] assetName = new String[1];
        dm.getAssetAsync(assetID, new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                assetName[0] = asset.getName();
            }

            @Override
            public void dataNotAvailable(int errorCode) {
                Log.i("Name not found","Error:"+errorCode);
            }
        });
        return assetName[0];
    }

}
