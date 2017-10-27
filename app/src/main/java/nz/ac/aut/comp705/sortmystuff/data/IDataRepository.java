package nz.ac.aut.comp705.sortmystuff.data;

import com.google.android.gms.tasks.Task;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FCategory;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import rx.Observable;

public interface IDataRepository {

    //region RETRIEVE METHODS

    /**
     * Retrieve all assets, including recycled ones.
     *
     * @return an Observable emitting one list of all assets
     */
    Observable<List<FAsset>> retrieveAllAssets();

    /**
     * Retrieve an asset according to the given id.
     *
     * @param assetId the id of the asset
     * @return an Observable emitting the queried asset; or emitting {@code null} if no such asset
     * @throws NullPointerException if assetId is {@code null}
     */
    Observable<FAsset> retrieveAsset(String assetId);

    /**
     * Retrieve all details in an unordered list.
     *
     * @return an Observable emitting one list of all details
     */
    Observable<List<FDetail>> retrieveAllDetails();

    /**
     * Retrieve details of the specified asset.
     * Details are ordered according to the sequence of {@link FAsset#getDetailIds()}.
     *
     * @param assetId the id of the owner asset
     * @return an Observable emitting one list of details; or emitting {@code null} if no such asset
     * @throws NullPointerException if assetId is {@code null}
     */
    Observable<List<FDetail>> retrieveDetails(String assetId);

    /**
     * Retrieve a detail according to the given id.
     *
     * @param detailId the id of the detail
     * @return an Observable emitting the detail; or emitting {@code null} if no such detail
     * @throws NullPointerException if detailId is {@code null}
     */
    Observable<FDetail> retrieveDetail(String detailId);

    /**
     * Retrieve all the categories.
     *
     * @return an Observable emitting one list of categories
     */
    Observable<List<FCategory>> retrieveCategories();

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
    void addOrUpdateAsset(FAsset asset, OnUpdatedCallback onUpdatedCallback);

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
    void addDetail(FDetail detail, OnUpdatedCallback onUpdatedCallback);

    //endregion

    //region UPDATE METHODS

    /**
     * Update a specific member of the given asset to the data repository.
     *
     * @param assetId           the id of the asset
     * @param key               the name of the member as listed in {@link FAsset}
     * @param value             the value of the member
     * @param onUpdatedCallback the callback methods when the task is successful, failed or completed;
     *                          put in {@code null} if no following actions required; onFailure won't
     *                          be invoked if NullPointerException is thrown due to null arguments
     * @throws NullPointerException     if assetId or key is {@code null}
     * @throws IllegalArgumentException if value is not {@code null} and is not of the correct class
     *                                  as specified in {@link FAsset#getMemberClassForDatabase(String)}
     */
    <E> void updateAsset(String assetId, String key, E value, OnUpdatedCallback onUpdatedCallback);

    /**
     * Update the detail to the data repository.
     *
     * @param detail the detail to be updated
     * @param updatingField the field of the detail will be updated as well if set to true, otherwise
     *                      the field won't be updated
     * @param onUpdatedCallback the callback methods when the task is successful, failed or completed;
     *                          put in {@code null} if no following actions required; onFailure won't
     *                          be invoked if NullPointerException is thrown due to null arguments
     * @throws NullPointerException     if detail or key is {@code null}
     */
    void updateDetail(FDetail detail, boolean updatingField, OnUpdatedCallback onUpdatedCallback);

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
    void setOnDataChangeCallback(OnDataChangeCallback onDataChangeCallback);

    //endregion

    interface OnDataChangeCallback {

    }

    interface OnAssetsDataChangeCallback extends OnDataChangeCallback {

        void onAssetAdded(FAsset asset);

        void onAssetChanged(FAsset asset);

        void onAssetRemoved(FAsset asset);

        void onAssetMoved(FAsset asset);
    }

    interface OnDetailsDataChangeCallback extends OnDataChangeCallback {

        void onDetailAdded(FDetail detail);

        void onDetailChanged(FDetail detail);

        void onDetailRemoved(FDetail detail);

        void onDetailMoved(FDetail detail);
    }

    interface OnUpdatedCallback {

        void onSuccess(Void aVoid);

        void onFailure(Throwable e);

        void onComplete(Task task);
    }
}
