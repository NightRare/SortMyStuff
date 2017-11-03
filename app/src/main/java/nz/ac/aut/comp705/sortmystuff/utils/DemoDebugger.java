package nz.ac.aut.comp705.sortmystuff.utils;

import android.graphics.Bitmap;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class DemoDebugger {

    public DemoDebugger(IDataManager dataManager, ISchedulerProvider schedulerProvider) {
        mDataManager = checkNotNull(dataManager);
        mSchedulerProvider = checkNotNull(schedulerProvider);
    }

    public void setPhoto(String assetId, final Bitmap photo) {
        mDataManager.getDetails(assetId)
                .subscribeOn(mSchedulerProvider.newThread())
                .flatMap(Observable::from)
                .filter(d -> d.getLabel().equals(CategoryType.BasicDetail.PHOTO)
                        && d.getType().equals(DetailType.Image))
                .subscribe(
                        //onNext
                        d -> mDataManager.updateDetail(assetId, d.getId(), d.getType(), null, photo)
                );
    }

    public void updateTextDetail(String assetId, String label, String field) {
        mDataManager.getDetails(assetId)
                .subscribeOn(mSchedulerProvider.newThread())
                .flatMap(Observable::from)
                .filter(d -> d.getLabel().equals(label)
                        && (d.getField().getClass().equals(String.class)))
                .subscribe(
                        //onNext
                        d -> mDataManager.updateDetail(assetId, d.getId(), d.getType(), null, field)
                );
    }

    private IDataManager mDataManager;
    private ISchedulerProvider mSchedulerProvider;
}
