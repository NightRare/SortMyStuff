package nz.ac.aut.comp705.sortmystuff.data;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.Category;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.util.AppStatusCode;
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

    public DataManager(IFileHelper fileHelper, LocalResourceLoader resLoader) {
        this.fileHelper = fileHelper;
        this.resLoader = resLoader;
        dirtyCachedAssets = true;
        dirtyCachedDetails = true;
    }

    //region IDataManger METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public String createAsset(@NonNull String name, @NonNull String containerId) {
        return createAsset(name, containerId, CategoryType.Miscellaneous);
    }

    @Override
    public String createAsset(@NonNull String name, @NonNull String containerId, CategoryType categoryType) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(containerId);
        Preconditions.checkArgument(!name.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(name.length() < AppConstraints.ASSET_NAME_CAP);

        if (!assetExists(containerId)) {
            Log.e(getClass().getName(),
                    "container asset not exists, container id: " + containerId);
            return null;
        }

        Asset asset = Asset.create(name, cachedAssets.get(containerId), categoryType);
        List<Detail> details = getCategory(categoryType).generateDetails(asset.getId());
        if (!fileHelper.serialiseAsset(asset) || !fileHelper.serialiseDetails(details, true)) {
            throw new UpdateLocalStorageFailedException("Serialising asset failed, asset id: "
                    + asset.getId());
        }
        cachedAssets.put(asset.getId(), asset);
        return asset.getId();
    }

    private void createRootAsset() {
        Asset root = Asset.createRoot();
        if (!fileHelper.serialiseAsset(root)) {
            throw new UpdateLocalStorageFailedException("Serialising Root asset failed");
        }
        cachedRootAsset = root;
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public String createTextDetail(@NonNull Asset asset, @NonNull String label, @NonNull String field) {
        Preconditions.checkNotNull(asset);

        return createTextDetail(asset.getId(), label, field);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public String createTextDetail(@NonNull final String assetId, @NonNull String label, @NonNull String field) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(!label.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(label.length() < AppConstraints.DETAIL_LABEL_CAP);
        Preconditions.checkArgument(field.length() < AppConstraints.TEXTDETAIL_FIELD_CAP);

        // cannot createAsMisc detail for Root asset
        if (assetId.equals(getRootAsset().getId()))
            return null;

        TextDetail td = (TextDetail) addDetail(assetId, DetailType.Text, label, field);
        return td == null ? null : td.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset getRootAsset() {
        if (dirtyCachedAssets || cachedRootAsset == null) {
            int code = loadCachedAssetsFromLocal();
            if (code == AppStatusCode.NO_ROOT_ASSET) {
                createRootAsset();
            }

            if (cachedRootAsset == null) {
                Log.e(Log.UNEXPECTED_ERROR, "Root asset not available. Error code: " + code);
                return null;
            }
        }
        return cachedRootAsset;
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    public void getContentAssetsAsync(@NonNull Asset container, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(container);

        getContentAssetsAsync(container.getId(), callback);
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    public void getParentAssetsAsync(@NonNull Asset asset, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(asset);

        getParentAssetsAsync(asset.getId(), callback);
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    public void getParentAssetsDescAsync(@NonNull Asset asset, @NonNull LoadAssetsCallback callback) {
        Preconditions.checkNotNull(asset);

        getParentAssetsDescAsync(asset.getId(), callback);
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    public void getDetailsAsync(@NonNull Asset asset, @NonNull LoadDetailsCallback callback) {
        Preconditions.checkNotNull(asset);

        getDetailsAsync(asset.getId(), callback);
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    public void updateAssetName(@NonNull Asset asset, @NonNull String newName) {
        Preconditions.checkNotNull(asset);

        updateAssetName(asset.getId(), newName);
    }

    /**
     * {@inheritDoc}
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
        if (!fileHelper.serialiseAsset(asset)) {
            dirtyCachedAssets = true;
            throw new UpdateLocalStorageFailedException("Write asset failed, id: " + assetId);
        }
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveAsset(@NonNull Asset asset, @NonNull String newContainerId) {
        Preconditions.checkNotNull(asset);

        moveAsset(asset.getId(), newContainerId);
    }

    /**
     * {@inheritDoc}
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
        if (!fileHelper.serialiseAsset(asset)) {
            dirtyCachedAssets = true;
            throw new UpdateLocalStorageFailedException("Write asset failed, id: " + assetId);
        }
    }


    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void recycleAssetRecursively(@NonNull String assetId) {
        Preconditions.checkNotNull(assetId);

        if (!assetExists(assetId) || assetId.equals(AppConstraints.ROOT_ASSET_ID)) {
            Log.e(getClass().getName(), "asset not exists or it is Root Asset, failed to recycle, asset id: " + assetId);
            return;
        }
        Asset asset = cachedAssets.get(assetId);

        if(!asset.getContents().isEmpty()) {
            for(Asset a : asset.getContents()) {
                recycleAssetRecursively(a.getId());
            }
        }
        recycleAsset(asset);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void recycleAssetRecursively(@NonNull Asset asset) {
        Preconditions.checkNotNull(asset);

        recycleAssetRecursively(asset.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDetail(@NonNull Detail detail) {
        Preconditions.checkNotNull(detail);

        removeDetail(detail.getAssetId(), detail.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDetail(@NonNull String assetId, @NonNull String detailId) {
        Preconditions.checkNotNull(assetId);
        Preconditions.checkNotNull(detailId);

        int code = loadCachedDetailsFromLocal(assetId);
        if (code != OK) {
            Log.e(getClass().getName(), "Cannot createAsMisc detail, error code: " + code);
            return;
        }

        List<Detail> details = cachedDetails.get(assetId);
        boolean removed = false;
        boolean updateImage = false;
        for (Detail d : details) {
            if (d.getId().equals(detailId)) {
                if (d.getType().equals(DetailType.Image))
                    updateImage = true;
                details.remove(d);
                removed = true;
                break;
            }
        }

        // if nothing removed, do nothing
        if (!removed)
            return;

        cachedAssets.get(assetId).updateTimeStamp();
        if (!fileHelper.serialiseDetails(details, updateImage) ||
                !fileHelper.serialiseAsset(cachedAssets.get(assetId))) {
            dirtyCachedAssets = true;
            dirtyCachedDetails = true;
            throw new UpdateLocalStorageFailedException(
                    "Serialising TextDetail failed, Detail id: " + detailId + "; Asset id: " + assetId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTextDetail(@NonNull TextDetail detail,
                                 @NonNull String label, @NonNull String field) {
        Preconditions.checkNotNull(detail);

        updateTextDetail(detail.getAssetId(), detail.getId(), label, field);
    }

    /**
     * {@inheritDoc}
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

        modifyDetail(assetId, detailId, label, field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetImageDetail(@NonNull ImageDetail detail) {
        Preconditions.checkNotNull(detail);

        modifyDetail(detail.getAssetId(), detail.getId(), detail.getLabel()
                , resLoader.getDefaultPhoto());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateImageDetail(@NonNull ImageDetail detail, @NonNull String label, @NonNull Bitmap field) {
        Preconditions.checkNotNull(detail);
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(field);
        Preconditions.checkArgument(!label.replaceAll(" ", "").isEmpty());
        Preconditions.checkArgument(label.length() < AppConstraints.DETAIL_LABEL_CAP);

        modifyDetail(detail.getAssetId(), detail.getId(), label, field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshFromLocal() {
        dirtyCachedAssets = true;
        dirtyCachedDetails = true;
    }

    //endregion

    //region PRIVATE STUFF

    private IFileHelper fileHelper;
    private LocalResourceLoader resLoader;
    private Map<CategoryType, Category> categories;

    private Asset cachedRootAsset;
    private Map<String, Asset> cachedAssets;
    private Map<String, List<Detail>> cachedDetails;
    private Map<String, Asset> cachedRecycledAssets;

    private boolean dirtyCachedAssets;
    private boolean dirtyCachedDetails;

    private Category getCategory(CategoryType categoryType) {
        if(categories == null) {
            categories = new HashMap<>();
            for(Category c : fileHelper.deserialiseCategories()) {
                categories.put(Enum.valueOf(CategoryType.class, c.getName()),
                        c);
            }
        }

        return categories.get(categoryType);
    }

    private synchronized int loadCachedAssetsFromLocal() {
        if (!fileHelper.rootExists())
            return NO_ROOT_ASSET;

        cachedAssets = new HashMap<>();
        cachedRecycledAssets = new HashMap<>();
        List<Asset> allAssets = fileHelper.deserialiseAllAssets();

        // assign each asset to corresponding cache
        for (Asset asset : allAssets) {
            if (asset.isRecycled()) {
                cachedRecycledAssets.put(asset.getId(), asset);
                continue;
            }

            cachedAssets.put(asset.getId(), asset);
            if (asset.isRoot()) {
                cachedRootAsset = asset;
            } else {
                asset.setPhoto(loadPhoto(asset.getId()));
            }

        }

        // form the tree structure in user assets
        for (Asset asset : cachedAssets.values()) {
            asset.attachToTree(asset.isRoot() ? null : cachedAssets.get(asset.getContainerId()));
        }

        dirtyCachedAssets = false;
        return OK;
    }

    private synchronized int loadCachedDetailsFromLocal(String assetId) {
        if (!fileHelper.rootExists())
            return NO_ROOT_ASSET;
        if (!assetExists(assetId))
            return ASSET_NOT_EXISTS;

        if (dirtyCachedDetails || cachedDetails == null) {
            cachedDetails = new HashMap<>();
        }
        else if (cachedDetails.containsKey(assetId)) {
            return OK;
        }
        releaseOneCachedDetails();
        List<Detail> details = fileHelper.deserialiseDetails(assetId);
        if (details == null) {
            return LOCAL_DATA_CORRUPT;
        }
        cachedDetails.put(assetId, details);

        dirtyCachedDetails = false;
        return OK;
    }

    private synchronized Bitmap loadPhoto(String assetId) {
        Bitmap photo = null;
        List<Detail> detailList = fileHelper.deserialiseDetails(assetId);
        for (Detail detail : detailList) {
            if (detail.getType().equals(DetailType.Image)
                    && detail.getLabel().equals(CategoryType.BasicDetail.PHOTO))
                photo = (Bitmap) detail.getField();
        }
        if (photo == null) {
            photo = resLoader.getDefaultPhoto();
        }
        return photo;
    }


    private boolean assetExists(String assetId) {
        if (cachedAssets == null || dirtyCachedAssets) {
            loadCachedAssetsFromLocal();
        }

        return cachedAssets.containsKey(assetId);
    }


    private void recycleAsset(@NonNull Asset asset) {
        Preconditions.checkNotNull(asset);

        asset.recycle();
        cachedRecycledAssets.put(asset.getId(), cachedAssets.remove(asset.getId()));
        if (!fileHelper.serialiseAsset(asset)) {
            dirtyCachedAssets = true;
            throw new UpdateLocalStorageFailedException("Write asset failed, id: " + asset.getId());
        }
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

    private <T> Detail addDetail(String assetId, DetailType type, String label, T field) {
        // check type of the field
        if (!field.getClass().equals(type.getFieldClass()))
            throw new IllegalArgumentException
                    ("Inconsistency between detail type and the type of the field.");

        int code = loadCachedDetailsFromLocal(assetId);
        if (code != OK) {
            Log.e(getClass().getName(), "Cannot createAsMisc detail, error code: " + code);
            return null;
        }

        Detail detail = null;
        boolean updateImage = false;

        if (type.equals(DetailType.Text)) {
            detail = TextDetail.createTextDetail(assetId, label, (String) field);

        } else if (type.equals(DetailType.Date)) {
            detail = TextDetail.createDateDetail(assetId, label, (String) field);

        } else if (type.equals(DetailType.Image)) {
            Bitmap defaultPhoto = resLoader.getDefaultPhoto();
            detail = ImageDetail.create(assetId, label, defaultPhoto);
            updateImage = true;
        }

        if (detail != null)
            cachedDetails.get(assetId).add(detail);
        else
            return null;

        cachedAssets.get(assetId).updateTimeStamp();

        if (!fileHelper.serialiseDetails(cachedDetails.get(assetId), updateImage) ||
                !fileHelper.serialiseAsset(cachedAssets.get(assetId))) {
            dirtyCachedAssets = true;
            dirtyCachedDetails = true;
            throw new UpdateLocalStorageFailedException(
                    "Serialising TextDetail failed, Detail id: " + detail.getId() + "; Asset id: " + assetId);
        }

        return detail;
    }

    private <T> Detail modifyDetail(String assetId, String detailId,
                                    @NonNull String label, @NonNull T field) {

        int code = loadCachedDetailsFromLocal(assetId);
        if (code != OK) {
            Log.e(getClass().getName(), "Cannot createAsMisc detail, error code: " + code);
            return null;
        }

        Detail detail = null;
        boolean updateImage = false;

        for (Detail d : cachedDetails.get(assetId)) {
            // if found the detail
            if (d.getId().equals(detailId)) {

                // check type of the field
                if (!field.getClass().equals(d.getType().getFieldClass()))
                    throw new IllegalArgumentException
                            ("Inconsistency between detail type and the type of the field.");

                detail = d;
                detail.setLabel(label);
                detail.setField(field);

                if (d.getType().equals(DetailType.Image)) {
                    updateImage = true;
                    // if it is the "Photo" detail of an asset, need to set the
                    // photo of asset as well
                    if(d.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
                        cachedAssets.get(assetId).setPhoto((Bitmap) field);
                    }
                }
                break;
            }
        }

        if (detail != null) {
            cachedAssets.get(assetId).updateTimeStamp();

            if (!fileHelper.serialiseDetails(cachedDetails.get(assetId), updateImage) ||
                    !fileHelper.serialiseAsset(cachedAssets.get(assetId))) {
                dirtyCachedAssets = true;
                dirtyCachedDetails = true;
                throw new UpdateLocalStorageFailedException(
                        "Serialising TextDetail failed, Detail id: " + detail.getId() + "; Asset id: " + assetId);
            }
        }

        return detail;
    }

    //endregion
}
