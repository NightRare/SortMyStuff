package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.ui.details.DetailsActivity;

/**
 * The implementation class of {@link IContentsPresenter}.
 *
 * @author Yuan
 */

public class ContentsPresenter implements IContentsPresenter {

    /**
     * Initialises a ContentsPresenter.
     *
     * @param dm       the IDataManager instance
     * @param view     the IContentsView instance
     * @param activity the ContentsActivity instance
     */
    public ContentsPresenter(IDataManager dm, IContentsView view, ContentsActivity activity) {
        this.dm = dm;
        this.view = view;
        this.activity = activity;
        editModeEnabled = false;
    }

    //region IContentsPresenter methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        // if the app just launched, display Root Asset
        if (currentAssetId == null) {
            currentAssetId = dm.getRootAsset().getId();
        }
        loadCurrentContents(false);
    }

    /**
     * {@inheritDoc}
     *
     * @param forceRefreshFromLocal true if want to force reload the contents from local storage
     */
    @Override
    public void loadCurrentContents(boolean forceRefreshFromLocal) {
        if (forceRefreshFromLocal)
            dm.refreshFromLocal();

        dm.getAssetAsync(currentAssetId, new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                view.showAssetTitle(asset.getName());
                loadContents(asset);
                loadPathBar(asset);
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId the asset id
     */
    @Override
    public void setCurrentAssetId(String assetId) {
        currentAssetId = assetId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentAssetIdToRoot() {
        currentAssetId = dm.getRootAsset().getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentAssetIdToContainer() {
        // does nothing if the current asset is Root asset
        if (currentAssetId.equals(dm.getRootAsset().getId()))
            return;

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


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentAssetId() {
        return currentAssetId;
    }

    /**
     * {@inheritDoc}
     *
     * @param assetName the name of the new asset.
     */
    @Override
    public void addAsset(String assetName) {
        dm.createAsset(assetName, currentAssetId);
        loadCurrentContents(false);
    }

    /**
     * {@inheritDoc}
     *
     * @param item the menu item
     * @return
     */
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
            Intent intent = new Intent(activity, DetailsActivity.class);
            intent.putExtra("AssetID", currentAssetId);
            activity.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void quitEditMode() {
        editModeEnabled = false;
        loadCurrentContents(false);
    }

    @Override
    public void enableEditMode() {
        editModeEnabled = true;
        loadCurrentContents(false);
    }

    //endregion

    //region Private stuff

    private ContentsActivity activity;

    private IContentsView view;

    private IDataManager dm;

    private String currentAssetId;

    private boolean editModeEnabled;
    /**
     * Loads the contents of the asset.
     *
     * @param asset
     */
    private void loadContents(Asset asset) {
        dm.getContentAssetsAsync(asset, new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                view.showAssetContents(assets, editModeEnabled);
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    /**
     * Loads the path bar for the given asset.
     *
     * @param asset
     */
    private void loadPathBar(final Asset asset) {
        dm.getParentAssetsDescAsync(asset, new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                // remove the Root
                if (!assets.isEmpty())
                    assets.remove(0);

                view.showPath(assets);
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }


    private void setCheckboxStatus(View view, boolean checked) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.asset_checkbox);
        checkBox.setVisibility(View.VISIBLE);
        if (checkBox != null)
            checkBox.setChecked(checked);
    }

    //endregion

}
