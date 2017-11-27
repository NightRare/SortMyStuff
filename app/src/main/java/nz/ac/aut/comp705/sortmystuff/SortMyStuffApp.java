package nz.ac.aut.comp705.sortmystuff;

import android.app.Application;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;

import nz.ac.aut.comp705.sortmystuff.di.Factory;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.services.PhotoRecognitionService;

public class SortMyStuffApp extends Application {

    private IFactory factory;
    private Features featureToggle;

    private final String[] enabledFeatures = new String[] {
            "PhotoDetection",
            "DelayPhotoDetection"
    };

    @Override
    public void onCreate() {
        super.onCreate();

//        enableLeakCanary();

        // enable local data persistent to deal with offline situation
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        featureToggle = Features.make(enabledFeatures);
    }

    public IFactory getFactory() {
        return factory;
    }

    public void initialiseFactory(FirebaseUser user) {
        if (factory == null) {
            factory = new Factory(this, user, featureToggle);
//            initialiseNameDetectionService();
        } else {
            factory.setUser(user);
        }
    }

    public void initialiseFactory(FirebaseUser user, GoogleApiClient googleApiClient) {
        if (factory == null) {
            factory = new Factory(this, user, googleApiClient, featureToggle);
//            initialiseNameDetectionService();
        } else {
            factory.setUser(user, googleApiClient);
        }
    }

    private void initialiseNameDetectionService() {
        if(featureToggle.PhotoDetection && featureToggle.DelayPhotoDetection) {
            startService(new Intent(this, PhotoRecognitionService.class));
        }
    }

    private void enableLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

    }
}
