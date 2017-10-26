package nz.ac.aut.comp705.sortmystuff.data;

import com.google.android.gms.tasks.Task;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.FAsset;
import nz.ac.aut.comp705.sortmystuff.data.models.FCategory;
import nz.ac.aut.comp705.sortmystuff.data.models.FDetail;
import rx.Observable;

public interface IDataRepository {

    //region RETRIEVE METHODS

    Observable<List<FAsset>> retrieveAllAssets();

    Observable<FAsset> retrieveAsset(String assetId);

    Observable<List<FDetail>> retrieveAllDetails();

    Observable<List<FDetail>> retrieveDetails(String assetId);

    Observable<FDetail> retrieveDetail(String detailId);

    Observable<List<FCategory>> retrieveCategories();

    //endregion

    //region CREATE METHODS

    void addOrUpdateAsset(FAsset asset, OnUpdatedCallback onUpdatedCallback);

    void addDetail(FDetail detail, OnUpdatedCallback onUpdatedCallback);

    //endregion

    //region UPDATE METHODS

    void updateAsset(String assetId, String key, Object value, OnUpdatedCallback onUpdatedCallback);

    void updateDetail(FDetail detail, boolean updatingField, OnUpdatedCallback onUpdatedCallback);

    //endregion

    //region OTHER METHODS

    void setOnDataChangeCallback(IDataManager.onDataChangeCallback onDataChangeCallback);

    //endregion

    interface OnUpdatedCallback {

        void onSuccess(Void aVoid);

        void onFailure(Throwable e);

        void onComplete(Task task);
    }
}
