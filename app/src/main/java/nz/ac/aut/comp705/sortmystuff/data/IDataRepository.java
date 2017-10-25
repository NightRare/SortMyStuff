package nz.ac.aut.comp705.sortmystuff.data;

import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FCategory;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import rx.Observable;

public interface IDataRepository {

    Observable<List<FAsset>> retrieveAllAssets();

    Observable<Map<String, FAsset>> retrieveAllAssetsAsMap();

    Observable<FAsset> retrieveAllAssetsAsStream();

    Observable<FAsset> retrieveAsset(String assetId);

    Observable<List<FDetail>> retrieveAllDetails();

    Observable<Map<String, FDetail>> retrieveAllDetailsAsMap();

    Observable<FDetail> retrieveAllDetailsAsStream();

    Observable<List<FDetail>> retrieveDetails(String assetId);

    Observable<Map<String, FDetail>> retrieveDetailsAsMap(String assetId);

    Observable<FDetail> retrieveDetailsAsStream(String assetId);

    Observable<FDetail> retrieveDetail(String detailId);

    Observable<List<FCategory>> retrieveCategories();

    Observable<Map<String, FCategory>> retrieveCategoriesAsMap();

    Observable<FCategory> retrieveCategoriesAsStream();

    @Deprecated
    List<FCategory> loadCategories();

    void addOrUpdateAsset(FAsset asset);

    void addOrUpdateAsset(FAsset asset, OnUpdatedCallback onUpdatedCallback);

    void updateAsset(String assetId, String key, Object value);

    void updateAsset(String assetId, String key, Object value, OnUpdatedCallback onUpdatedCallback);

    /**
     * Adds the detail to the data repository if not exists, otherwise updates the existing record
     * of the detail. If updatingImage is set to true, then the field of the image detail will be updated
     * to the data repository. If the detail is not an image detail then this argument will be
     * ignored.
     *
     * @param detail the detail to be added or updated
     * @param updatingImage set to true only if the field of the image detail is updated; set to false
     *                      if no image is updated or the detail is just created
     * @throws NullPointerException if detail is {@code null}
     */
    void addOrUpdateDetail(FDetail detail, boolean updatingImage);

    void addOrUpdateDetail(FDetail detail, boolean updatingImage, OnUpdatedCallback onUpdatedCallback);

    void setOnDataChangeCallback(IDataManager.onDataChangeCallback onDataChangeCallback);

    interface OnUpdatedCallback {

        void onSuccess(Void aVoid);

        void onFailure(Throwable e);

        void onComplete(Task task);
    }
}
