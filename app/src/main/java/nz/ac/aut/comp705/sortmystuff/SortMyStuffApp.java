package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import nz.ac.aut.comp705.sortmystuff.di.Factory;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;

public class SortMyStuffApp extends Application {

    private IFactory factory;

    @Override
    public void onCreate() {
        super.onCreate();

        // enable local data persistent to deal with offline situation
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public IFactory getFactory() {
        return factory;
    }

    public void initialiseFactory(String userId) {
        factory = new Factory(this, userId);
    }
}
