package nz.ac.aut.comp705.sortmystuff.data;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.ISortMyStuffAppComponent;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.Category;
import rx.Observable;

public interface IDataRepository extends ISortMyStuffAppComponent{

    //region RETRIEVE METHODS

    /**
     * Retrieve all assets, including recycled ones.
     *
     * @return an Observable emitting one list of all assets
     */
    Observable<List<Asset>> retrieveAllAssets();

    /**
     * Retrieve an asset according to the given id.
     *
     * @param assetId the id of the asset
     * @return an Observable emitting the queried asset; or emitting {@code null} if no such asset
     * @throws NullPointerException if assetId is {@code null}
     */
    Observable<Asset> retrieveAsset(String assetId);

    /**
     * Retrieve all details in an unordered list.
     *
     * @return an Observable emitting one list of all details
     */
    Observable<List<Detail>> retrieveAllDetails();

    /**
     * Retrieve details of the specified asset.
     * Details are ordered according to the sequence of {@link Asset#getDetailIds()}.
     *
     * @param assetId the id of the owner asset
     * @return an Observable emitting one list of details; or emitting {@code null} if no such asset
     * @throws NullPointerException if assetId is {@code null}
     */
    Observable<List<Detail>> retrieveDetails(String assetId);

    /**
     * Retrieve a detail according to the given id.
     *
     * @param detailId the id of the detail
     * @return an Observable emitting the detail; or emitting {@code null} if no such detail
     * @throws NullPointerException if detailId is {@code null}
     */
    Observable<Detail> retrieveDetail(String detailId);

    /**
     * Retrieve all the categories.
     *
     * @return an Observable emitting one list of categories
     */
    Observable<List<Category>> retrieveCategories();

    //endregion

    //region CREATE METHODS

    /**
     * Add the asset to the data repository. If the asset already exists then update the record
     * with the given one.
     *
     * @param asset             the asset to be added or updated
     * @param onUpdatedCallback the callback methods when the task is successful, failed or completed;
     *                          put in {@code null} if no following actions required; onFailure won't
     *                          be invoked if NullPointerException is thrown due to null arguments
     * @throws NullPointerException if asset is {@code null}
     */
    void addOrUpdateAsset(Asset asset, OnUpdatedCallback onUpdatedCallback);

    /**
     * Add the detail to the data repository. If the id of the given detail is found in the data repository,
     * no further actions will be taken (will not overwrite the old record).
     *
     * @param detail            the detail to be added
     * @param onUpdatedCallback the callback methods when the task is successful, failed or completed;
     *                          put in {@code null} if no following actions required; onFailure won't
     *                          be invoked if NullPointerException is thrown due to null arguments
     * @throws NullPointerException if detail is {@code null}
     */
    void addDetail(Detail detail, OnUpdatedCallback onUpdatedCallback);

    //endregion

    //region UPDATE METHODS

    /**
     * Update a specific member of the given asset to the data repository.
     *
     * @param assetId           the id of the asset
     * @param key               the name of the member as listed in {@link Asset}
     * @param value             the value of the member
     * @param onUpdatedCallback the callback methods when the task is successful, failed or completed;
     *                          put in {@code null} if no following actions required; onFailure won't
     *                          be invoked if NullPointerException is thrown due to null arguments
     * @throws NullPointerException     if assetId or key is {@code null}
     * @throws IllegalArgumentException if value is not {@code null} and is not of the correct class
     *                                  as specified in {@link Asset#getMemberType(String)}
     */
    <E> void updateAsset(String assetId, String key, E value, OnUpdatedCallback onUpdatedCallback);

    /**
     * Update the detail to the data repository.
     *
     * @param detail            the detail to be updated
     * @param updatingField     the field of the detail will be updated as well if set to true, otherwise
     *                          the field won't be updated
     * @param onUpdatedCallback the callback methods when the task is successful, failed or completed;
     *                          put in {@code null} if no following actions required; onFailure won't
     *                          be invoked if NullPointerException is thrown due to null arguments
     * @throws NullPointerException if detail or key is {@code null}
     */
    void updateDetail(Detail detail, boolean updatingField, OnUpdatedCallback onUpdatedCallback);

    //endregion

    //region OTHER METHODS

    /**
     * Corresponding callback methods will be called whenever the attached data has changed in the
     * data repository.
     * All the data will be retrieved from the repository every time a new OnDataChangeCallback is
     * attached to this repository.
     *
     * Pass in {@code null} to detach ALL the listeners.
     *
     * @param onDataChangeCallback the listener; or {@code null} to detach all the listeners
     */

    /**
     * Corresponding callback methods will be called whenever the attached data has changed in the
     * data repository.
     * All the data will be retrieved from the repository every time a new OnDataChangeCallback is
     * attached to this repository.
     *
     * @param onDataChangeCallback the listener
     * @param type                 the type of the data
     * @param <T>                  the type of the data
     * @throws NullPointerException if any argument is {@code null}
     */
    <T> void setOnDataChangeCallback(OnDataChangeCallback<T> onDataChangeCallback, Class<T> type);

    /**
     * Detach the callback of the specified type of data.
     *
     * Pass in {@code null} to detach all the callback.
     *
     * @param type the type of the data
     * @param <T> the type of the data
     */
    <T> void removeOnDataChangeCallback(Class<T> type);

    //endregion

    interface OnDataChangeCallback<T> {

        void onDataAdded(T object);

        void onDataChanged(T object);

        void onDataRemoved(T object);

        void onDataMoved(T object);
    }

    interface OnUpdatedCallback {

        void onSuccess(Void aVoid);

        void onFailure(Throwable e);

        void onComplete();
    }
}
