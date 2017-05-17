package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;

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
