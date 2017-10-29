package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;

import javax.inject.Inject;
import javax.inject.Singleton;

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

public class Factory implements IFactory {

    public Factory(Application app, String userId) {
        mApp = checkNotNull(app);
        mUserId = checkNotNull(userId);

        mFactoryModule = new FactoryModule(mApp, mUserId);
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
    public synchronized void setUserId(String userId) {
        if (checkNotNull(userId).equals(mUserId)) return;

        mFactoryModule.setUserId(userId);
        mFactoryComponent.inject(this);
    }

    @Override
    public String getUserId() {
        return mUserId;
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
    private String mUserId;
    private FactoryComponent mFactoryComponent;
    private FactoryModule mFactoryModule;

    private Factory() {
    }

    //endregion
}
