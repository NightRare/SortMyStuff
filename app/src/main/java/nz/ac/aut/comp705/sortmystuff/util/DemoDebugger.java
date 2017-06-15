package nz.ac.aut.comp705.sortmystuff.util;

import android.graphics.Bitmap;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;

/**
 * Created by Vince on 2017/6/16.
 */

public class DemoDebugger {

    private SortMyStuffApp app;
    private IFileHelper fh;
    private IDataManager dm;

    public DemoDebugger(SortMyStuffApp app) {
        this.app = app;
        fh = app.getFactory().getFileHelper();
        dm = app.getFactory().getDataManager();
    }

    public void cleanExistingData() {

        try {
            FileUtils.cleanDirectory(fh.getUserDir());
            dm.refreshFromLocal();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPhoto(String assetId, final Bitmap photo) {
        dm.getDetailsAsync(assetId, new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                for (Detail d : details) {
                    if (d.getLabel().equals(CategoryType.BasicDetail.PHOTO)
                            && d.getType().equals(DetailType.Image)) {
                        dm.updateImageDetail((ImageDetail) d, d.getLabel(), photo);
                    }
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    public void resetPhoto(String assetId) {
        dm.getDetailsAsync(assetId, new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                for (Detail d : details) {
                    if (d.getLabel().equals(CategoryType.BasicDetail.PHOTO)
                            && d.getType().equals(DetailType.Image)) {
                        dm.resetImageDetail((ImageDetail) d);
                    }
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }

    public void updateTextDetail(String assetId, final String label, final String field) {
        dm.getDetailsAsync(assetId, new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                for (Detail d : details) {
                    if (d.getLabel().equals(label)
                            && (d instanceof TextDetail)) {
                        dm.updateTextDetail((TextDetail) d, label, field);
                    }
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {

            }
        });
    }
}
