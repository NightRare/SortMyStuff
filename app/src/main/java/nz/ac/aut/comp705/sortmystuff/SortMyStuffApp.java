package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;

import nz.ac.aut.comp705.sortmystuff.di.Factory;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;

public class SortMyStuffApp extends Application {

    private IFactory factory;

    @Override
    public void onCreate() {
        super.onCreate();

        factory = new Factory(this, "default");
    }

    public IFactory getFactory() {
        return factory;
    }

}
