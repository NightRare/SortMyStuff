package nz.ac.aut.comp705.sortmystuff.utils;

import android.graphics.Bitmap;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

public class DemoDebugger {

    private IFactory mFactory;
    private IDataManager mDataManager;
    private ISchedulerProvider mSchedulerProvider;

    public DemoDebugger(IFactory factory) {
        mFactory = checkNotNull(factory);
        mDataManager = mFactory.getDataManager();
        mSchedulerProvider = mFactory.getSchedulerProvider();
    }

    public void cleanExistingData() {
        mFactory.getDatabaseReference().removeValue();
        mFactory.getStorageReference().delete();
        mDataManager.reCacheFromRemoteDataSource();
    }

    public void setPhoto(String assetId, final Bitmap photo) {
        mDataManager.getDetails(assetId)
                .subscribeOn(mSchedulerProvider.io())
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
                .subscribeOn(mSchedulerProvider.io())
                .flatMap(Observable::from)
                .filter(d -> d.getLabel().equals(label)
                        && (d instanceof TextDetail))
                .subscribe(
                        //onNext
                        d -> mDataManager.updateDetail(assetId, d.getId(), d.getType(), null, field)
                );
    }
}
