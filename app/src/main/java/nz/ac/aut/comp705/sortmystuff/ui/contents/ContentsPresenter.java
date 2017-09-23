package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.ui.search.SearchActivity;
import nz.ac.aut.comp705.sortmystuff.ui.swipe.SwipeActivity;
import nz.ac.aut.comp705.sortmystuff.util.AppCode;

import static nz.ac.aut.comp705.sortmystuff.util.AppCode.CONTENTS_DEFAULT_MODE;
import static nz.ac.aut.comp705.sortmystuff.util.AppCode.CONTENTS_SELECTION_MODE;

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
    public ContentsPresenter(IDataManager dm, IContentsView view, SwipeActivity activity, String currentAssetId) {
        this.dm = dm;
        this.view = view;
        this.activity = activity;
        this.currentAssetId = currentAssetId;
        contentsDisplayMode = CONTENTS_DEFAULT_MODE;

        view.setPresenter(this);
    }

    //region IContentsPresenter methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe() {
        String intendedId = activity.getIntent().getStringExtra(AppCode.INTENT_ASSET_ID);
        if(intendedId != null)
            setCurrentAssetId(intendedId);

        // if the app just launched, display Root Asset
        else if (currentAssetId == null) {
            setCurrentAssetId(dm.getRootAsset().getId());
        }
        loadCurrentContents(true);
    }

    @Override
    public void unsubscribe() {
        //TODO: to be implemented
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    public void loadCurrentContents(boolean forceRefreshFromLocal, int mode) {
        contentsDisplayMode = mode;
        loadCurrentContents(forceRefreshFromLocal);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId the asset id
     */
    @Override
    public void setCurrentAssetId(String assetId) {
        currentAssetId = assetId;
        activity.refreshDetails(assetId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentAssetIdToRoot() {
        setCurrentAssetId(dm.getRootAsset().getId());
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
                    ContentsPresenter.this.setCurrentAssetId(assets.get(0).getId());
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
    public void createAsset(String assetName, CategoryType category) {
        try {
            dm.createAsset(assetName, currentAssetId, category);
            Toast.makeText(activity, "Successfully added " + assetName, Toast.LENGTH_SHORT).show();
        } catch (NullPointerException | IllegalArgumentException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        loadCurrentContents(false);
    }

    @Override
    public void moveAssets(List<Asset> assets) {
        Preconditions.checkNotNull(assets, "The assets to move cannot be null.");
        Preconditions.checkArgument(!assets.isEmpty(), "The assets to move cannot be empty");

        //reject the attempt to move to current directory
        if (assets.get(0).getContainerId().equals(currentAssetId)) {
            Toast.makeText(activity, "The assets are already here :)", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Asset a : assets) {
            dm.moveAsset(a, currentAssetId);
        }

        int size = assets.size();
        String msg = " asset moved.";
        if (size > 1)
            msg = " assets moved.";
        Toast.makeText(activity, size + msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * {@inheritDoc}
     *
     * @param item the menu item
     * @return
     */
    @Override
    public boolean selectOptionItem(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.search_view_button:
                Intent searchIntent = new Intent(activity, SearchActivity.class);
                activity.startActivity(searchIntent);
                return true;

            case R.id.selection_mode_button:
                loadCurrentContents(false, CONTENTS_SELECTION_MODE);
                return true;

            case R.id.delete_current_asset_button:
                if (currentAssetId.equals(dm.getRootAsset().getId())) {
                    Toast.makeText(activity, "Cannot delete the root asset", Toast.LENGTH_LONG).show();
                    return false;
                }
                view.showDeleteDialog(true);
                return true;

            default:
                return false;
        }
    }


    @Override
    public void recycleCurrentAssetRecursively() {
        String deleteAssetId = currentAssetId;
        setCurrentAssetIdToContainer();
        dm.recycleAssetRecursively(deleteAssetId);
        loadCurrentContents(true);
    }

    @Override
    public void recycleAssetsRecursively(List<Asset> assets) {
        Preconditions.checkNotNull(assets);
        for(Asset a : assets) {
            dm.recycleAssetRecursively(a);
        }
        loadCurrentContents(true);
    }

    //endregion

    //region Private stuff

    private SwipeActivity activity;
    private IContentsView view;
    private IDataManager dm;
    private String currentAssetId;
    private int contentsDisplayMode;

    /**
     * Loads the contents of the asset.
     *
     * @param asset
     */
    private void loadContents(Asset asset) {
        dm.getContentAssetsAsync(asset, new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                view.showAssetContents(assets, contentsDisplayMode);
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

    //endregion

}
