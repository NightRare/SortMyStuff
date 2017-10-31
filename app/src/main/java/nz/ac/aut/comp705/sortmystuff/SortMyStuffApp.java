package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;
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

    public void initialiseFactory(FirebaseUser user) {
        if (factory == null) {
            factory = new Factory(this, user);
        } else {
            factory.setUser(user);
        }
    }

    public void initialiseFactory(FirebaseUser user, GoogleApiClient googleApiClient) {
        if (factory == null) {
            factory = new Factory(this, user, googleApiClient);
        } else {
            factory.setUser(user, googleApiClient);
        }
    }
}
