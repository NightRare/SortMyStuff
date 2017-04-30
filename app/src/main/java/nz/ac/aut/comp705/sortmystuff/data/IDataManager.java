package nz.ac.aut.comp705.sortmystuff.data;

import android.support.annotation.NonNull;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.util.exceptions.*;

/**
 * Created by Yuan on 2017/4/24.
 */

public interface IDataManager {

    //********************************************
    // CREATE DATA
    //********************************************

    /**
     * Create an asset and save it to the local storage.
     *
     * @param name        the name
     * @param containerId the id of the container asset
     * @return the id of the created asset; {@code null} if failed
     * @throws NullPointerException              if any argument is {@code null};
     * @throws IllegalArgumentException          if name is empty or length exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    String createAsset(@NonNull String name, @NonNull String containerId);

    /**
     * Create the Root asset and save it to the local storage.
     *
     * @return the id of the created asset; {@code null} if failed (e.g. already exists)
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    String createRootAsset();

    /**
     * Create a TextDetail and save it to the local storage. Cannot create Detail for Root
     * asset.
     *
     * @param asset the owner
     * @param label the title
     * @param field the content of this detail
     * @return the Id of the created TextDetail; or null if failed
     * @throws NullPointerException              if any argument is {@code null}
     * @throws IllegalArgumentException          if label is empty string; or the length of
     *                                           label or field exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    String createTextDetail(@NonNull Asset asset, @NonNull String label, @NonNull String field);

    /**
     * Create a TextDetail and save it to the local storage. Cannot create Detail for Root
     * asset.
     *
     * @param assetId the id of the owner
     * @param label   the title
     * @param field   the content of this detail
     * @return the Id of the created TextDetail; or null if failed
     * @throws NullPointerException              if any argument is {@code null}
     * @throws IllegalArgumentException          if label is empty string; or the length of
     *                                           label or field exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    String createTextDetail(@NonNull String assetId, @NonNull String label, @NonNull String field);

    //********************************************
    // READ DATA
    //********************************************

    /**
     * Get the Root asset from the local data source.
     *
     * @return the Root asset of the current user; or null if no root asset record
     */
    Asset getRootAsset();

    /**
     * Get the Root asset from the data sources.
     *
     * @param callback see {@link GetAssetCallback}
     * @throws NullPointerException if callback is {@code null}
     */
    @Deprecated
    void getRootAssetAsync(@NonNull GetAssetCallback callback);

    /**
     * Get all the non-recycled assets (including root asset).
     *
     * @param callback see {@link LoadAssetsCallback}
     * @throws NullPointerException if callback is {@code null}
     */
    void getAllAssetsAsync(@NonNull LoadAssetsCallback callback);

    /**
     * Get all the recycled assets.
     *
     * @param callback see {@link LoadAssetsCallback}
     * @throws NullPointerException if callback is {@code null}
     */
    void getRecycledAssetsAsync(@NonNull LoadAssetsCallback callback);

    /**
     * Get the list of assets which are contents to the given Asset.
     *
     * @param container the container Asset
     * @param callback  see {@link LoadAssetsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getContentAssetsAsync(@NonNull Asset container, @NonNull LoadAssetsCallback callback);

    /**
     * Get the list of assets which are contents to the Asset whose id as given.
     *
     * @param containerId the id of the container Asset
     * @param callback    see {@link LoadAssetsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getContentAssetsAsync(@NonNull String containerId, @NonNull LoadAssetsCallback callback);

    /**
     * Get the parent assets stored in a list in which the first element is the container of the
     * asset and the last element is the Root asset (if this asset is not contained by Root).
     * For example, Root -> Apartment -> Bookshelf -> Drawer, if query the parent Asset of Drawer
     * the items in list would be [Bookshelf, Apartment, Root].
     *
     * @param asset    the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getParentAssetsAsync(@NonNull Asset asset, @NonNull LoadAssetsCallback callback);

    /**
     * Get the parent assets stored in a list in which the first element is the container of the
     * asset and the last element is the Root asset (if this asset is not contained by Root).
     * For example, Root -> Apartment -> Bookshelf -> Drawer, if query the parent Asset of Drawer
     * the items in list would be [Bookshelf, Apartment, Root].
     *
     * @param assetId    the id of the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getParentAssetsAsync(@NonNull String assetId, @NonNull LoadAssetsCallback callback);

    /**
     * Get an asset according to the id.
     *
     * @param assetId  the id
     * @param callback see {@link GetAssetCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getAssetAsync(@NonNull String assetId, @NonNull GetAssetCallback callback);

    /**
     * Get the list of details of the owner asset.
     *
     * @param asset    the owner asset
     * @param callback see {@link LoadDetailsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getDetailsAsync(@NonNull Asset asset, @NonNull LoadDetailsCallback callback);

    /**
     * Get the list of details of the owner asset.
     *
     * @param assetId  the id of the owner asset
     * @param callback see {@link LoadDetailsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getDetailsAsync(@NonNull String assetId, @NonNull LoadDetailsCallback callback);

    /**
     * Get a detail according to the given id.
     *
     * @param detailId the id
     * @param callback see {@link GetDetailCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getDetailAsync(@NonNull String detailId, @NonNull GetDetailCallback callback);


    //********************************************
    // UPDATE/DELETE DATA
    //********************************************

    /**
     * Update the name of the asset in memory and local storage.
     *
     * @param asset   the asset to be updated
     * @param newName the new name
     * @return true if successful update
     * @throws NullPointerException              if any argument is {@code null}
     * @throws IllegalArgumentException          if newName is empty or length exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void updateAssetName(@NonNull Asset asset, @NonNull String newName);


    /**
     * Update the name of the asset in memory and local storage.
     *
     * @param assetId the id of the asset to be updated
     * @param newName the new name
     * @return true if successful update
     * @throws NullPointerException              if any argument is {@code null}
     * @throws IllegalArgumentException          if newName is empty or length exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void updateAssetName(@NonNull String assetId, @NonNull String newName);


    /**
     * Move an asset to another container.
     *
     * @param asset          the asset
     * @param newContainerId the id of the new container
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void moveAsset(@NonNull Asset asset, @NonNull String newContainerId);

    /**
     * Move the asset with given id to another container
     *
     * @param assetId        the asset id
     * @param newContainerId the id of the new container
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void moveAsset(@NonNull String assetId, @NonNull String newContainerId);

    /**
     * Recycle the asset.
     *
     * @param asset the asset to be recycled
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void recycleAsset(@NonNull Asset asset);

    /**
     * Recycle the asset with given id.
     *
     * @param assetId the id of the asset to be recycled
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void recycleAsset(@NonNull String assetId);

    // Not Implemented yet
    void restoreAsset(@NonNull Asset asset);

    // Not Implemented yet
    void restoreAsset(@NonNull String assetId);

    /**
     * Remove the detail from the asset.
     *
     * @param assetId the id of the owner asset
     * @param detail  the detail
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void removeDetail(@NonNull String assetId, @NonNull Detail detail);

    /**
     * Remove the detail from the asset.
     *
     * @param assetId  the id of the owner asset
     * @param detailId the id of the detail
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void removeDetail(@NonNull String assetId, @NonNull String detailId);

    /**
     * Update the TextDetail according to the given arguments.
     *
     * @param detail the detail
     * @param label  the new label
     * @param field  the new field
     * @throws NullPointerException              if any argument is {@code null}
     * @throws IllegalArgumentException          if label is empty string; or the length of
     *                                           label or field exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void updateTextDetail(@NonNull TextDetail detail,
                          @NonNull String label, @NonNull String field);

    /**
     * Update the TextDetail according to the given arguments.
     *
     * @param assetId  the id of the owner asset
     * @param detailId the id of the detail
     * @param label    the new label
     * @param field    the new field
     * @throws NullPointerException              if any argument is {@code null}
     * @throws IllegalArgumentException          if label is empty string; or the length of
     *                                           label or field exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void updateTextDetail(@NonNull String assetId, @NonNull String detailId,
                          @NonNull String label, @NonNull String field);


    //********************************************
    // UTIL
    //********************************************

    void refreshFromLocal();

    void clearRecycledAsset();



    //********************************************
    // CALLBACKS
    //********************************************

    /**
     * A callback interface for asynchronised loading methods.
     */
    interface LoaderCallback {

        /**
         * It fires when the requested data is not available.
         *
         * @param errorCode the errorCode
         */
        void dataNotAvailable(int errorCode);
    }

    interface LoadAssetsCallback extends LoaderCallback {

        /**
         * It fires when the requested list of assets is ready.
         *
         * @param assets the requested list of Asset
         */
        void onAssetsLoaded(List<Asset> assets);
    }

    interface GetAssetCallback extends LoaderCallback {

        /**
         * It fires when the requested asset is ready.
         *
         * @param asset the requested Asset
         */
        void onAssetLoaded(Asset asset);
    }

    interface LoadDetailsCallback extends LoaderCallback {

        /**
         * It fires when the requested list of details is ready.
         *
         * @param details the requested list of Detail
         */
        void onDetailsLoaded(List<Detail> details);
    }

    interface GetDetailCallback extends LoaderCallback {

        /**
         * It fires when the requested detail is ready.
         *
         * @param detail the requested Detail
         */
        void onDetailsLoaded(Detail detail);
    }
}
