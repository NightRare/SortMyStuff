package nz.ac.aut.comp705.sortmystuff.data;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;
import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.util.Log;
import nz.ac.aut.comp705.sortmystuff.util.exceptions.UpdateLocalStorageFailedException;

import static nz.ac.aut.comp705.sortmystuff.util.AppStatusCode.ASSET_NOT_EXISTS;
import static nz.ac.aut.comp705.sortmystuff.util.AppStatusCode.LOCAL_DATA_CORRUPT;
import static nz.ac.aut.comp705.sortmystuff.util.AppStatusCode.NO_ROOT_ASSET;
import static nz.ac.aut.comp705.sortmystuff.util.AppStatusCode.OK;
import static nz.ac.aut.comp705.sortmystuff.util.AppStatusCode.UNEXPECTED_ERROR;

/**
 * Implementation class of IDataManager
 *
 * @author Yuan
 */

public class DataManager implements IDataManager {

    public DataManager(IJsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
        dirtyCachedAssets = true;
        dirtyCachedDetails = true;
    }

    //region IDataManger METHODS

    /**
     * {@inheritDoc}
     *
     * @param name        the name
     * @param containerId the id of the container asset
     * @return
     */
    @Override
    public String createAsset(@NonNull String name, @NonNull String containerId) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(containerId);
        Preconditions.checkArgument(!name.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(name.length() < AppConstraints.ASSET_NAME_CAP);

        if (!assetExists(containerId)) {
            Log.e(getClass().getName(),
                    "container asset not exists, container id: " + containerId);
            return null;
        }

        Asset asset = Asset.create(name, cachedAssets.get(containerId));
        if (!jsonHelper.serialiseAsset(asset)) {
            throw new UpdateLocalStorageFailedException("Serialising asset failed, asset id: "
                    + asset.getId());
        }
        cachedAssets.put(asset.getId(), asset);
        return asset.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String createRootAsset() {
        if (dirtyCachedAssets) {
            loadCachedAssetsFromLocal();
        }

        if (cachedRootAsset != null) {
            Log.e(getClass().getName(), "Root asset already exists.");
            return null;
        }

        Asset root = Asset.createRoot();
        if (!jsonHelper.serialiseAsset(root)) {
            throw new UpdateLocalStorageFailedException("Serialising Root asset failed");
        }
        cachedRootAsset = root;
        return root.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @param asset the owner
     * @param label the title
     * @param field the content of this detail
     * @return
     */
    @Override
    public String createTextDetail(@NonNull Asset asset, @NonNull String label, @NonNull String field) {
        Preconditions.checkNotNull(asset);

        return createTextDetail(asset.getId(), label, field);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId the id of the owner
     * @param label   the title
     * @param field   the content of this detail
     * @return
     */
    @Override
    public String createTextDetail(@NonNull final String assetId, @NonNull String label, @NonNull String field) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(!label.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(label.length() < AppConstraints.DETAIL_LABEL_CAP);
        Preconditions.checkArgument(field.length() < AppConstraints.TEXTDETAIL_FIELD_CAP);

        // cannot create detail for Root asset
        if (assetId.equals(getRootAsset().getId()))
            return null;

        TextDetail td = addOrUpdateTextDetail(assetId, null, label, field);
        return td == null ? null : td.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Asset getRootAsset() {
        if (dirtyCachedAssets || cachedRootAsset == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                Log.e(getClass().getName(), "Root asset not available. Error code: " + code);
                return null;
            }
        }
        return cachedRootAsset;
    }

    /**
     * {@inheritDoc}
     *
     * @param callback see {@link GetAssetCallback}
     */
    @Override
    public void getRootAssetAsync(@NonNull GetAssetCallback callback) {
        Preconditions.checkNotNull(callback);

        if (dirtyCachedAssets || cachedRootAsset == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                callback.dataNotAvailable(code);
                return;
            }
        }

        callback.onAssetLoaded(cachedRootAsset);
    }

    /**
     * {@inheritDoc}
     *
     * @param callback see {@link LoadAssetsCallback}
     */
    @Override
    public void getAllAssetsAsync(@NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(callback);

        if (dirtyCachedAssets || cachedAssets == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                callback.dataNotAvailable(code);
                return;
            }
        }

        List<Asset> list = new LinkedList<>(cachedAssets.values());
        callback.onAssetsLoaded(list);
    }

    /**
     * {@inheritDoc}
     *
     * @param callback see {@link LoadAssetsCallback}
     */
    @Override
    public void getRecycledAssetsAsync(@NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(callback);

        if (dirtyCachedAssets || cachedRecycledAssets == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                callback.dataNotAvailable(code);
                return;
            }
        }

        List<Asset> list = new LinkedList<>(cachedRecycledAssets.values());
        callback.onAssetsLoaded(list);
    }

    /**
     * {@inheritDoc}
     *
     * @param container the container Asset
     * @param callback  see {@link LoadAssetsCallback}
     */
    @Override
    public void getContentAssetsAsync(@NonNull Asset container, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(container);

        getContentAssetsAsync(container.getId(), callback);
    }

    /**
     * {@inheritDoc}
     *
     * @param containerId the id of the container Asset
     * @param callback    see {@link LoadAssetsCallback}
     */
    @Override
    public void getContentAssetsAsync(@NonNull String containerId, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(containerId);
        Preconditions.checkNotNull(callback);

        if (dirtyCachedAssets || cachedAssets == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                callback.dataNotAvailable(code);
                return;
            }
        }

        if (!cachedAssets.containsKey(containerId)) {
            callback.dataNotAvailable(ASSET_NOT_EXISTS);
            return;
        }
        Asset container = cachedAssets.get(containerId);

        if (container.getContents() == null) {
            callback.dataNotAvailable(LOCAL_DATA_CORRUPT);
            return;
        }
        callback.onAssetsLoaded(container.getContents());
    }

    /**
     * {@inheritDoc}
     *
     * @param asset    the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     */
    @Override
    public void getParentAssetsAsync(@NonNull Asset asset, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(asset);

        getParentAssetsAsync(asset.getId(), callback);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId  the id of the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     */
    @Override
    public void getParentAssetsAsync(@NonNull String assetId, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(callback);

        if (dirtyCachedAssets || cachedAssets == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                callback.dataNotAvailable(code);
                return;
            }
        }
        if (!cachedAssets.containsKey(assetId)) {
            callback.dataNotAvailable(ASSET_NOT_EXISTS);
            return;
        }

        Asset asset = cachedAssets.get(assetId);
        List<Asset> parents = new LinkedList<>();
        while (!asset.isRoot()) {
            parents.add(asset.getContainer());
            asset = asset.getContainer();
        }
        callback.onAssetsLoaded(parents);
    }

    /**
     * {@inheritDoc}
     *
     * @param asset    the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     */
    @Override
    public void getParentAssetsDescAsync(@NonNull Asset asset, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(asset);

        getParentAssetsDescAsync(asset.getId(), callback);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId  the id of the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     */
    @Override
    public void getParentAssetsDescAsync(@NonNull String assetId, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(assetId);

        if (dirtyCachedAssets || cachedAssets == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                callback.dataNotAvailable(code);
                return;
            }
        }
        if (!cachedAssets.containsKey(assetId)) {
            callback.dataNotAvailable(ASSET_NOT_EXISTS);
            return;
        }

        Asset asset = cachedAssets.get(assetId);
        List<Asset> parents = new LinkedList<>();
        while (!asset.isRoot()) {
            parents.add(0, asset);
            asset = asset.getContainer();
        }
        // add the root
        parents.add(0, asset);
        callback.onAssetsLoaded(parents);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId  the id
     * @param callback see {@link GetAssetCallback}
     */
    @Override
    public void getAssetAsync(@NonNull String assetId, @NonNull GetAssetCallback callback) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(callback);

        if (dirtyCachedAssets || cachedAssets == null) {
            int code = loadCachedAssetsFromLocal();
            if (code != OK) {
                callback.dataNotAvailable(code);
                return;
            }
        }

        if (!cachedAssets.containsKey(assetId)) {
            callback.dataNotAvailable(ASSET_NOT_EXISTS);
            return;
        }
        callback.onAssetLoaded(cachedAssets.get(assetId));
    }

    /**
     * {@inheritDoc}
     *
     * @param asset    the owner asset
     * @param callback see {@link LoadDetailsCallback}
     */
    @Override
    public void getDetailsAsync(@NonNull Asset asset, @NonNull LoadDetailsCallback callback) {
        Preconditions.checkNotNull(asset);

        getDetailsAsync(asset.getId(), callback);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId  the id of the owner asset
     * @param callback see {@link LoadDetailsCallback}
     */
    @Override
    public void getDetailsAsync(@NonNull String assetId, @NonNull LoadDetailsCallback callback) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(callback);

        // if get the Details of the Root asset, always return empty list
        if (assetId.equals(getRootAsset().getId())) {
            callback.onDetailsLoaded(new LinkedList<Detail>());
            return;
        }

        int code = loadCachedDetailsFromLocal(assetId);
        if (code != OK) {
            callback.dataNotAvailable(code);
            return;
        }
        if (!cachedDetails.containsKey(assetId)) {
            callback.dataNotAvailable(UNEXPECTED_ERROR);
            return;
        }
        callback.onDetailsLoaded(cachedDetails.get(assetId));
    }

    /**
     * {@inheritDoc}
     *
     * @param detailId the id
     * @param callback see {@link GetDetailCallback}
     */
    @Override
    public void getDetailAsync(@NonNull String detailId, @NonNull GetDetailCallback callback) {
        Preconditions.checkNotNull(detailId);
        Preconditions.checkNotNull(!detailId.replaceAll(" ", "").isEmpty());

        // TODO complete getDetailAsync
        throw new IllegalStateException("Method not completed yet");
    }

    /**
     * {@inheritDoc}
     *
     * @param asset   the asset to be updated
     * @param newName the new name
     */
    @Override
    public void updateAssetName(@NonNull Asset asset, @NonNull String newName) {
        Preconditions.checkNotNull(asset);

        updateAssetName(asset.getId(), newName);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId the id of the asset to be updated
     * @param newName the new name
     */
    @Override
    public void updateAssetName(@NonNull String assetId, @NonNull final String newName) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(newName);
        Preconditions.checkArgument(!newName.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(newName.length() < AppConstraints.ASSET_NAME_CAP);

        if (!assetExists(assetId)) {
            Log.e(getClass().getName(), "asset not exists, failed to update");
            return;
        }
        Asset asset = cachedAssets.get(assetId);
        asset.setName(newName);
        if (!jsonHelper.serialiseAsset(asset)) {
            dirtyCachedAssets = true;
            throw new UpdateLocalStorageFailedException("Write asset failed, id: " + assetId);
        }
        return;
    }

    /**
     * {@inheritDoc}
     *
     * @param asset          the asset
     * @param newContainerId the id of the new container
     */
    @Override
    public void moveAsset(@NonNull Asset asset, @NonNull String newContainerId) {
        Preconditions.checkNotNull(asset);

        moveAsset(asset.getId(), newContainerId);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId        the asset id
     * @param newContainerId the id of the new container
     */
    @Override
    public void moveAsset(@NonNull String assetId, @NonNull String newContainerId) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(newContainerId);

        if (!assetExists(assetId) || !assetExists(newContainerId)) {
            Log.e(getClass().getName(), "asset not exists, failed to update, asset id: "
                    + assetId + " container id: " + newContainerId);
            return;
        }
        Asset asset = cachedAssets.get(assetId);
        if (!asset.moveTo(cachedAssets.get(newContainerId))) {
            Log.e(getClass().getName(), "move asset failed, asset id: " + assetId
                    + " container id: " + newContainerId);
            return;
        }
        if (!jsonHelper.serialiseAsset(asset)) {
            dirtyCachedAssets = true;
            throw new UpdateLocalStorageFailedException("Write asset failed, id: " + assetId);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param asset the asset to be recycled
     */
    @Override
    public void recycleAsset(@NonNull Asset asset) {
        Preconditions.checkNotNull(asset);

        recycleAsset(asset.getId());
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId the id of the asset to be recycled
     */
    @Override
    public void recycleAsset(@NonNull String assetId) {
        Preconditions.checkNotNull(assetId);

        if (!assetExists(assetId)) {
            Log.e(getClass().getName(), "asset not exists, failed to recycle, asset id: " + assetId);
            return;
        }
        Asset asset = cachedAssets.get(assetId);
        if (asset.isRoot()) {
            Log.e(getClass().getName(), "cannot recycle Root asset");
            return;
        }
        asset.recycle();
        cachedRecycledAssets.put(assetId, cachedAssets.remove(assetId));
        if (!jsonHelper.serialiseAsset(asset)) {
            dirtyCachedAssets = true;
            throw new UpdateLocalStorageFailedException("Write asset failed, id: " + assetId);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param asset
     */
    @Override
    public void restoreAsset(@NonNull Asset asset) {
        // TODO restoreAsset
        throw new IllegalStateException("Method not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId
     */
    @Override
    public void restoreAsset(@NonNull String assetId) {
        // TODO restoreAsset
        throw new IllegalStateException("Method not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @param detail the detail
     */
    @Override
    public void removeDetail(@NonNull Detail detail) {
        Preconditions.checkNotNull(detail);

        removeDetail(detail.getAssetId(), detail.getId());
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId  the id of the owner asset
     * @param detailId the id of the detail
     */
    @Override
    public void removeDetail(@NonNull String assetId, @NonNull String detailId) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(detailId);

        int code = loadCachedDetailsFromLocal(assetId);
        if (code != OK) {
            Log.e(getClass().getName(), "Cannot create detail, error code: " + code);
            return;
        }

        List<Detail> details = cachedDetails.get(assetId);
        boolean removed = false;
        for (Detail d : details) {
            if (d.getId().equals(detailId)) {
                details.remove(d);
                removed = true;
                break;
            }
        }

        // if nothing removed, do nothing
        if (!removed)
            return;

        cachedAssets.get(assetId).updateTimeStamp();
        if (!jsonHelper.serialiseDetails(details) ||
                !jsonHelper.serialiseAsset(cachedAssets.get(assetId))) {
            dirtyCachedAssets = true;
            dirtyCachedDetails = true;
            throw new UpdateLocalStorageFailedException(
                    "Serialising TextDetail failed, Detail id: " + detailId + "; Asset id: " + assetId);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param detail the detail
     * @param label  the new label
     * @param field  the new field
     */
    @Override
    public void updateTextDetail(@NonNull TextDetail detail,
                                 @NonNull String label, @NonNull String field) {
        Preconditions.checkNotNull(detail);

        updateTextDetail(detail.getAssetId(), detail.getId(), label, field);
    }

    /**
     * {@inheritDoc}
     *
     * @param assetId  the id of the owner asset
     * @param detailId the id of the detail
     * @param label    the new label
     * @param field    the new field
     */
    @Override
    public void updateTextDetail(@NonNull String assetId, @NonNull String detailId,
                                 @NonNull String label, @NonNull String field) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(detailId);
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(!label.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(label.length() < AppConstraints.DETAIL_LABEL_CAP);
        Preconditions.checkArgument(field.length() < AppConstraints.TEXTDETAIL_FIELD_CAP);

        addOrUpdateTextDetail(assetId, detailId, label, field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshFromLocal() {
        dirtyCachedAssets = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearRecycledAsset() {
        // TODO clearRecycledAsset
        throw new IllegalStateException("Method not implemented");
    }

    //endregion

    //region PRIVATE STUFF

    private IJsonHelper jsonHelper;

    private Asset cachedRootAsset;

    private Map<String, Asset> cachedAssets;

    private Map<String, List<Detail>> cachedDetails;

    private Map<String, Asset> cachedRecycledAssets;

    private boolean dirtyCachedAssets;

    private boolean dirtyCachedDetails;

    private synchronized int loadCachedAssetsFromLocal() {
        if (!jsonHelper.rootExists())
            return NO_ROOT_ASSET;

        cachedAssets = new HashMap<>();
        cachedRecycledAssets = new HashMap<>();
        List<Asset> allAssets = jsonHelper.deserialiseAllAssets();

        // assign each asset to corresponding cache
        for (Asset asset : allAssets) {
            if (asset.isRecycled()) {
                cachedRecycledAssets.put(asset.getId(), asset);
            } else {
                if (asset.isRoot()) {
                    cachedRootAsset = asset;
                }
                cachedAssets.put(asset.getId(), asset);
            }
        }

        // form the tree structure in user assets
        for (Asset asset : cachedAssets.values()) {
            // root does not need to attach to the tree
            if (asset.isRoot()) continue;

            asset.attachToTree(cachedAssets.get(asset.getContainerId()));
        }

        dirtyCachedAssets = false;
        return OK;
    }

    private synchronized int loadCachedDetailsFromLocal(String assetId) {
        if (!jsonHelper.rootExists())
            return NO_ROOT_ASSET;
        if (!assetExists(assetId)) {
            return ASSET_NOT_EXISTS;
        }
        if (dirtyCachedDetails || cachedDetails == null) {
            cachedDetails = new HashMap<>();
        }
        if (cachedDetails.containsKey(assetId)) {
            return OK;
        }
        releaseOneCachedDetails();
        List<Detail> details = jsonHelper.deserialiseDetails(assetId);
        if (details == null) {
            return LOCAL_DATA_CORRUPT;
        }
        cachedDetails.put(assetId, details);

        dirtyCachedDetails = false;
        return OK;
    }


    private boolean assetExists(String assetId) {
        if (cachedAssets == null || dirtyCachedAssets) {
            loadCachedAssetsFromLocal();
        }

        return cachedAssets.containsKey(assetId);
    }

    private boolean releaseOneCachedDetails() {
        if (cachedDetails.size() > AppConstraints.CACHED_DETAILS_LIST_NUM) {
            String key = null;
            for (String k : cachedDetails.keySet()) {
                key = k;
                break;
            }
            cachedDetails.remove(key);
            return true;
        }
        return false;
    }

    /**
     * @param assetId
     * @param detailId null if add, id if update
     * @param label
     * @param field
     * @return
     */
    private TextDetail addOrUpdateTextDetail(String assetId, String detailId, String label, String field) {

        int code = loadCachedDetailsFromLocal(assetId);
        if (code != OK) {
            Log.e(getClass().getName(), "Cannot create detail, error code: " + code);
            return null;
        }

        TextDetail td = null;
        if (detailId == null) {
            td = TextDetail.createTextDetail(assetId, label, field);
            cachedDetails.get(assetId).add(td);
        } else {
            boolean updated = false;
            for (Detail detail : cachedDetails.get(assetId)) {
                if (detail.getId().equals(detailId)) {
                    updated = true;
                    td = (TextDetail) detail;
                    td.setLabel(label);
                    td.setField(field);
                }
            }
            if (!updated)
                return td;
        }

        cachedAssets.get(assetId).updateTimeStamp();
        if (!jsonHelper.serialiseDetails(cachedDetails.get(assetId)) ||
                !jsonHelper.serialiseAsset(cachedAssets.get(assetId))) {
            dirtyCachedAssets = true;
            dirtyCachedDetails = true;
            throw new UpdateLocalStorageFailedException(
                    "Serialising TextDetail failed, Detail id: " + td.getId() + "; Asset id: " + assetId);
        }

        return td;
    }

    private static boolean identicalAssets(Asset a1, Asset a2) {
        if (!a1.equals(a2))
            return false;

        if (!a1.getName().equals(a2.getName()))
            return false;

        if (!a1.getModifyTimestamp().equals(a2.getModifyTimestamp()))
            return false;

        if (!a1.getCreateTimestamp().equals(a2.getCreateTimestamp()))
            return false;

        if (!a1.getContainerId().equals(a2.getContainerId()))
            return false;

        return true;
    }

    //endregion
}
