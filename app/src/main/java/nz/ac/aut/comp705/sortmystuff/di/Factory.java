package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;
import javax.inject.Singleton;

import nz.ac.aut.comp705.sortmystuff.Features;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.IDebugHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.ImmediateScheduler;
import nz.ac.aut.comp705.sortmystuff.di.qualifiers.RegularScheduler;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of {@link IFactory}.
 *
 * @author Yuan
 */

public class Factory implements IFactory{

    public Factory(
            Application app,
            FirebaseUser user,
            Features featToggle) {

        mApp = checkNotNull(app);
        mCurrentUser = checkNotNull(user);
        mFeatToggle = checkNotNull(featToggle);

        mFactoryModule = new FactoryModule(mApp, mCurrentUser.getUid(), mFeatToggle);
        mFactoryComponent = DaggerFactoryComponent.builder().factoryModule(mFactoryModule).build();
        mFactoryComponent.inject(this);
    }

    public Factory(
            Application app,
            FirebaseUser user,
            GoogleApiClient googleApiClient,
            Features featToggle) {

        mApp = checkNotNull(app);
        mCurrentUser = checkNotNull(user);
        mGoogleApiClient = checkNotNull(googleApiClient);
        mFeatToggle = checkNotNull(featToggle);

        mFactoryModule = new FactoryModule(mApp, mCurrentUser.getUid(), mFeatToggle);
        mFactoryComponent = DaggerFactoryComponent.builder().factoryModule(mFactoryModule).build();
        mFactoryComponent.inject(this);
    }

    @Override
    public ISchedulerProvider getImmediateSchedulerProvider() {
        return mImmediateSchedulerProvider;
    }

    @Override
    public ISchedulerProvider getSchedulerProvider() {
        return mRegularSchedulerProvider;
    }

    @Override
    public IDataManager getDataManager() {
        return mDataManager;
    }

    @Override
    public IDataRepository getRemoteRepository() {
        return mRemoteRepo;
    }

    @Override
    public IDataRepository getLocalRepository() {
        return null;
    }

    @Override
    public IDebugHelper getDataDebugHelper() {
        if (mDataManager instanceof IDebugHelper) {
            return (IDebugHelper) mDataManager;
        }

        // avoid NullPointerException when dataManager does not implement IDebugHelper
        return () -> {
        };
    }

    @Override
    public LocalResourceLoader getLocalResourceLoader() {
        return mResLoader;
    }

    @Override
    public void setUser(FirebaseUser user) {
        mCurrentUser = checkNotNull(user);

        mFactoryModule.setUserId(mCurrentUser.getUid());
        mFactoryComponent.inject(this);
    }

    @Override
    public synchronized void setUser(FirebaseUser user, GoogleApiClient googleApiClient) {
        mCurrentUser = checkNotNull(user);
        mGoogleApiClient = checkNotNull(googleApiClient);

        mFactoryModule.setUserId(mCurrentUser.getUid());
        mFactoryComponent.inject(this);
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return mCurrentUser;
    }


    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void setFeatureToggle(@NonNull Features featureToggle) {
        mFeatToggle = checkNotNull(featureToggle);
        mDataManager.setFeatureToggle(featureToggle);
        mRemoteRepo.setFeatureToggle(featureToggle);
    }

    @NonNull
    @Override
    public Features getFeatureToggle() {
        return mFeatToggle;
    }

    //region INJECTION

    @Inject
    @Singleton
    @RegularScheduler
    ISchedulerProvider mRegularSchedulerProvider;

    @Inject
    @Singleton
    @ImmediateScheduler
    ISchedulerProvider mImmediateSchedulerProvider;

    @Inject
    @Singleton
    LocalResourceLoader mResLoader;

    @Inject
    IDataRepository mRemoteRepo;

    @Inject
    IDataManager mDataManager;

    //endregion

    //region PRIVATE STUFF

    private Application mApp;
    private FactoryComponent mFactoryComponent;
    private FactoryModule mFactoryModule;
    private FirebaseUser mCurrentUser;
    private GoogleApiClient mGoogleApiClient;
    private Features mFeatToggle;

    private Factory() {
    }

    //endregion
}
