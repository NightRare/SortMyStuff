package nz.ac.aut.comp705.sortmystuff.data;

import android.graphics.Bitmap;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.ISortMyStuffAppComponent;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;
import nz.ac.aut.comp705.sortmystuff.utils.exceptions.ReadLocalStorageFailedException;
import rx.Observable;

/**
 * IDataManager is responsible for providing data for Presenters, as well as updating or deleting
 * data according to Presenter's requests. It basically includes CRUD and other related methods.
 *
 * @author Yuan
 */

public interface IDataManager extends ISortMyStuffAppComponent {

    //region READ DATA METHODS

    /**
     * Get the root asset, emitted from an Observable. If the root asset does not exist, a new one
     * will be created.
     *
     * @return the Observable emitting the root asset
     */
    Observable<IAsset> getRootAsset();

    /**
     * Get all the non-recycled assets (including root asset).
     *
     * @return the Observable which emits a list of Assets
     * @throws ReadLocalStorageFailedException if error occurred during reading data from local storage
     */
    Observable<List<IAsset>> getAssets();

    /**
     * Get all the recycled assets.
     *
     * @return the Observable which emits one list of Assets
     * @throws ReadLocalStorageFailedException if error occurred during reading data from local storage
     */
    Observable<List<IAsset>> getRecycledAssets();

    /**
     * Get the list of details of the asset.
     * <p>
     * Will always return an Observable of an empty list if it's root asset id.
     *
     * @param assetId the id of the owner asset
     * @return the Observable which emits one list of Details; or which emits {@code null} if the
     * assetId cannot be found
     * @throws NullPointerException if any argument is {@code null}
     */
    Observable<List<IDetail>> getDetails(String assetId);

    Observable<IDetail<Bitmap>> getPhotoDetail(String assetId);

    /**
     * Get an asset according to the id.
     *
     * @param assetId the id of the Asset
     * @return the Observable emitting one Asset; or which emits {@code null} if the
     * assetId cannot be found
     * @throws NullPointerException if any argument is {@code null}
     */
    Observable<IAsset> getAsset(String assetId);

    /**
     * Get the content assets of the given asset.
     * <p>
     * The list is ordered according to the sequence of {@link FAsset#getContentIds()}.
     *
     * @param containerId the id of the container asset
     * @return the Observable emitting one list of Assets; or which emits {@code null} if the
     * assetId cannot be found
     * @throws NullPointerException if any argument is {@code null}
     */
    Observable<List<IAsset>> getContentAssets(String containerId);

    /**
     * Get the parent assets stored in a list.
     * <p>
     * If rootToChildren is true, then the list will be ordered as the first element is the
     * Root asset and the last element is the asset itself.
     * For example, let the structure be Root -> A -> B -> C -> D,
     * if query the parent Assets of D the items in list would be
     * [Root, A, B, C].
     * If rootToChildren is false, then the resultRaw will be [C, B, A, Root]
     *
     * @param assetId        the id of the asset whose parent assets are queried
     * @param rootToChildren true if the resultRaw of the order is root to children
     * @return the Observable emitting one list of Assets; or which emits {@code null} if the
     * assetId cannot be found
     * @throws NullPointerException if assetId is {@code null}
     */
    Observable<List<IAsset>> getParentAssets(String assetId, boolean rootToChildren);

    //endregion

    //region CREATE DATA METHODS

    /**
     * Create an asset as a content of the given container.
     * The default details of the asset are generated according to "Miscellaneous" category.
     * <p>
     * It will only create the asset only if the containerId exists and the container is not recycled,
     * otherwise nothing will be performed.
     *
     * @param name        the name
     * @param containerId the id of the container asset
     * @return the id of the created asset; {@code null} if failed
     * @throws NullPointerException     if any argument is {@code null};
     * @throws IllegalArgumentException if name is empty or length exceeds app constraints
     */
    String createAsset(String name, String containerId);

    /**
     * Create an asset as a content of the given container.
     * The default details of the asset are generated according to the given category.
     * <p>
     * It will only create the asset only if the containerId exists and the container is not recycled,
     * otherwise nothing will be performed.
     *
     * @param name         the name
     * @param containerId  the id of the container asset
     * @param categoryType the CategoryType
     * @return the id of the created asset; {@code null} if failed
     * @throws NullPointerException     if any argument is {@code null};
     * @throws IllegalArgumentException if name is empty or length exceeds app constraints; or
     *                                  containerId cannot be found
     */
    String createAsset(String name, String containerId, CategoryType categoryType);

    String createAsset(
            String name,
            String containerId,
            CategoryType categoryType,
            Bitmap photo,
            List<IDetail> details);

    Observable<String> createAssetSafely(String name, String containerId, CategoryType categoryType);

    Observable<String> createAssetSafely(
            String name,
            String containerId,
            CategoryType categoryType,
            Bitmap photo,
            List<IDetail> details);

    //endregion

    //region UPDATE DATA METHODS

    /**
     * Update the name of the asset to the given new name.
     * It does nothing if assetId equals to the id of the root asset.
     *
     * @param assetId the id of the asset to be updated
     * @param newName the new name
     * @return true if successful update
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if newName is empty or length exceeds app constraints
     */
    void updateAssetName(String assetId, String newName);

    /**
     * Move the asset with given id to another container.
     * It does nothing if assetId equals to the id of the root asset, or assetId or newContainerId
     * does not exist.
     *
     * @param assetId        the asset id
     * @param newContainerId the id of the new container
     * @throws NullPointerException if any argument is {@code null}
     */
    void moveAsset(String assetId, String newContainerId);

    /**
     * Recycle the asset with given id and all its children assets.
     * It does nothing if assetId equals to the id of the root asset, or assetId does not exist.
     *
     * @param assetId the id of the asset to be recycled
     * @throws NullPointerException if any argument is {@code null}
     */
    void recycleAssetAndItsContents(String assetId);

    /**
     * Update a detail with the given new label and/or field.
     * It won't update the detail if newLabel and newField are both {@code null}; or the detailId
     * does not belong to the asset.
     *
     * @param assetId  the id of the owner asset
     * @param detailId the id of the detail
     * @param type     the DetailType of the detail
     * @param newLabel the new label, or {@code null} if no need to update
     * @param newField the new Field, or {@code null} if no need to update
     * @param <T>      the type of the field
     * @throws NullPointerException     if assetId or detailId is {@code null}
     * @throws IllegalArgumentException if newLabel is empty or overlong; or if newField is overlong when
     *                                  updating Text or Date Detail; or if the type of newField is
     *                                  not consistent with the DetailType
     */
    <T> void updateDetail(String assetId, String detailId, DetailType type, String newLabel, T newField);

    /**
     * Reset the field (image) of an ImageDetail to the default image.
     * It won't reset the detail if newLabel and newField are both {@code null}; or the detailId
     * does not belong to the asset.
     *
     * @param assetId  the id of the asset to which this detail belongs
     * @param detailId the id of the detail
     * @throws NullPointerException if assetId or detailId is {@code null}
     */
    void resetImageDetail(String assetId, String detailId);

    //endregion

    //region OTHER METHODS

    Observable<String> getNewAssetName();

    Observable<String> getNewAssetName(Bitmap photo);

    Observable<List<IDetail>> getDetailsFromCategory(CategoryType category);

    /**
     * Re-cache the data from remote data source.
     */
    void reCacheFromRemoteDataSource();

    /*
    TODO methods to be added in the future
    void clearRecycledAsset();
     */

    //endregion
}
