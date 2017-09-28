package nz.ac.aut.comp705.sortmystuff.data;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.utils.exceptions.*;
import rx.Observable;

/**
 * IDataManager is responsible for providing data for Presenters, as well as updating or deleting
 * data according to Presenter's requests. It basically includes CRUD and other related methods.
 *
 * @author Yuan
 */

public interface IDataManager {

    //region RXJAVA METHODS

    Observable<List<IAsset>> getAssets();

    Observable<List<IAsset>> getRecycledAssets();

    Observable<List<IDetail>> getDetails(String assetId);

    Observable<IAsset> getAsset(String id);

    Observable<List<IAsset>> getContentAssets(String containerId);

    /**
     * Get the parent assets stored in a list.
     *
     * If rootToChildren is true, then the list will be ordered as the first element is the
     * Root asset and the last element is the asset itself.
     * For example, let the structure be Root -> A -> B -> C,
     * if query the parent Assets of Drawer the items in list would be
     * [Root, A, B, C].
     * If rootToChildren is false, then the result will be [C, B, A, Root]
     *
     * @param assetId  the id of the asset whose parent assets are queried
     * @param rootToChildren true if the result of the order is root to children
     * @throws NullPointerException if assetId is {@code null}
     */
    Observable<List<IAsset>> getParentAssets(String assetId, boolean rootToChildren);

    //endregion

    //region CREATE DATA METHODS

    /**
     * Create an asset and save it to the local storage.
     * The default details of the asset are generated according to "Miscellaneous" category.
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
     * Create an asset and save it to the local storage.
     * The default details of the asset are generated according to the given category.
     *
     * @param name         the name
     * @param containerId  the id of the container asset
     * @param categoryType the CategoryType
     * @return the id of the created asset; {@code null} if failed
     * @throws NullPointerException              if any argument is {@code null};
     * @throws IllegalArgumentException          if name is empty or length exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    String createAsset(@NonNull String name, @NonNull String containerId, CategoryType categoryType);


    /**
     * Create a TextDetail and save it to the local storage. Cannot createAsMisc Detail for Root
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
    @Deprecated
    String createTextDetail(@NonNull Asset asset, @NonNull String label, @NonNull String field);

    /**
     * Create a TextDetail and save it to the local storage. Cannot createAsMisc Detail for Root
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
    @Deprecated
    String createTextDetail(@NonNull String assetId, @NonNull String label, @NonNull String field);

    //endregion

    //region READ DATA METHODS

    /**
     * Get the Root asset from the local data source.
     *
     * @return the Root asset of the current user
     */
    Asset getRootAsset();

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
     * For example, Root -> Apartment -> Bookshelf -> Drawer, if query the parent Assets of Drawer
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
     * For example, Root -> Apartment -> Bookshelf -> Drawer, if query the parent Assets of Drawer
     * the items in list would be [Bookshelf, Apartment, Root].
     *
     * @param assetId  the id of the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getParentAssetsAsync(@NonNull String assetId, @NonNull LoadAssetsCallback callback);

    /**
     * Get the parent assets stored in a list in which the first element is the Root asset
     * and the last element is the asset itself (if this asset is not contained by Root).
     * For example, Root -> Apartment -> Bookshelf -> Drawer, if query the parent Assets of Drawer
     * in descendant order, the items in list would be [Root, Apartment, Bookshelf, Drawer].
     *
     * @param asset    the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getParentAssetsDescAsync(@NonNull Asset asset, @NonNull LoadAssetsCallback callback);

    /**
     * Get the parent assets stored in a list in which the first element is the Root asset
     * and the last element is the asset itself (if this asset is not contained by Root).
     * For example, Root -> Apartment -> Bookshelf -> Drawer, if query the parent Assets of Drawer
     * in descendant order, the items in list would be [Root, Apartment, Bookshelf, Drawer].
     *
     * @param assetId  the id of the asset whose parent assets are queried
     * @param callback see {@link LoadAssetsCallback}
     * @throws NullPointerException if any argument is {@code null}
     */
    void getParentAssetsDescAsync(@NonNull String assetId, @NonNull LoadAssetsCallback callback);


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

    /*
    TODO add getDetailAsync(@NonNull String detailId, @NonNull GetDetailCallback callback) method
     */

    //endregion

    //region UPDATE DATA METHODS

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
    @Deprecated
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
     * Recycle the asset with given id and all its children assets.
     *
     * @param assetId the id of the asset to be recycled
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void recycleAssetRecursively(@NonNull String assetId);

    /**
     * Recycle the asset with given id and all its children assets.
     *
     * @param asset the asset to be recycled
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    @Deprecated
    void recycleAssetRecursively(@NonNull Asset asset);

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

    /**
     * Reset the field (image) of the ImageDetail to the default image.
     *
     * @param detail the ImageDetail to be reset
     */
    void resetImageDetail(@NonNull ImageDetail detail);

    void resetImageDetail(@NonNull IDetail<Bitmap> detail);

    /**
     * Update the ImageDetail according to the given arguments.
     *
     * @param detail the detail
     * @param label  the new label
     * @param field  the new field
     * @throws NullPointerException              if any argument is {@code null}
     * @throws IllegalArgumentException          if label is empty string; or the length of
     *                                           label exceeds app constraints
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    @Deprecated
    void updateImageDetail(@NonNull ImageDetail detail, @NonNull String label, @NonNull Bitmap field);

    void updateImageDetail(@NonNull IDetail<Bitmap> detail, @NonNull String label, @NonNull Bitmap field);

    /*
    TODO methods to be added in the future
    void restoreAsset(@NonNull Asset asset);

    void restoreAsset(@NonNull String assetId);
     */

    //endregion

    //region DELETE DATA METHODS

    /**
     * Remove the detail from the asset.
     *
     * @param detail the detail
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void removeDetail(@NonNull Detail detail);

    /**
     * Remove the detail from the asset.
     *
     * @param assetId  the id of the owner asset
     * @param detailId the id of the detail
     * @throws NullPointerException              if any argument is {@code null}
     * @throws UpdateLocalStorageFailedException if update local storage failed
     */
    void removeDetail(@NonNull String assetId, @NonNull String detailId);

    //endregion

    //region OTHER METHODS

    /**
     * Force reload data from local storage.
     */
    void refreshFromLocal();

    /*
    TODO methods to be added in the future
    void clearRecycledAsset();
     */

    //endregion

    //region CALLBACK INTERFACES

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
        void onDetailLoaded(Detail detail);
    }

    //endregion
}
