package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;
import android.util.Log;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.DataManager;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.TextDetail;
import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.JsonHelper;
import nz.ac.aut.comp705.sortmystuff.di.Factory;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;

/**
 * Created by Yuan on 2017/4/25.
 */

public class SortMyStuffApp extends Application {

    private IFactory factory;

    @Override
    public void onCreate() {
        super.onCreate();

        factory = new Factory(this);
    }

    public IFactory getFactory() {
        return factory;
    }
}
