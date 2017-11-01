package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;

import nz.ac.aut.comp705.sortmystuff.di.Factory;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;

public class SortMyStuffApp extends Application {

    private IFactory factory;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

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
