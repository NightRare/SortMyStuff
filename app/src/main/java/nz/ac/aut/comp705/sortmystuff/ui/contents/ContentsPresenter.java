package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.ui.detail.DetailActivity;

/**
 * Created by Yuan on 2017/4/28.
 */

public class ContentsPresenter implements IContentsPresenter {

    private ContentsActivity activity;

    private IContentsView view;

    private IDataManager dm;

    private String currentAssetId;

    @Deprecated
    public ContentsPresenter(IDataManager dm, IContentsView view) {
        this.dm = dm;
        this.view = view;
        this.activity = (ContentsActivity) view;
    }

    public ContentsPresenter(IDataManager dm, IContentsView view, ContentsActivity activity) {
        this.dm = dm;
        this.view = view;
        this.activity = activity;
    }

    @Override
    public void start() {
        // if the app just launched, display Root Asset
        if (currentAssetId == null) {
            Asset root = dm.getRootAsset();
            if (root != null) {
                currentAssetId = dm.getRootAsset().getId();
            } else {
                dm.createRootAsset();
                currentAssetId = dm.getRootAsset().getId();
            }
        }
        loadCurrentContents(false);
    }

    @Override
    public void loadCurrentContents(boolean forceRefreshFromLocal) {
        if (forceRefreshFromLocal)
            dm.refreshFromLocal();

        dm.getAssetAsync(currentAssetId, new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                view.showAssetTitle(asset.getName());
                loadCurrentAssetContents(asset);
                loadPathBar(asset);
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
                if (!assets.isEmpty()) {
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

    @Override
    public boolean selectOptionItem(MenuItem item) {
        int id = item.getItemId();

        //action button stuff
        if (id == R.id.action_view_details) {
            // if it's Root Asset, do not show details
            if (currentAssetId.equals(dm.getRootAsset().getId())) {
                Toast.makeText(activity, "Root has no detail", Toast.LENGTH_LONG).show();
                return false;
            }
            Intent intent = new Intent(activity, DetailActivity.class);
            intent.putExtra("AssetID", currentAssetId);
            activity.startActivity(intent);
            return true;
        }
        return false;
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

    private void loadCurrentAssetContents(Asset asset) {
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

    private void loadPathBar(final Asset asset) {
        dm.getParentAssetsAsync(asset, new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                assets.add(0, asset);
                view.showPath(assets);
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

}
