package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.GsonBuilder;

import java.io.File;

import nz.ac.aut.comp705.sortmystuff.data.DataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.IDebugHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.FileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.data.remote.FirebaseHelper;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ImmediateSchedularProvider;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.SchedulerProvider;

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
    }

    @Override
    public ISchedulerProvider getImmediateSchedulerProvider() {
        return new ImmediateSchedularProvider();
    }

    @Override
    public ISchedulerProvider getSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }

    @Override
    public synchronized IDataManager getDataManager() {
        if (mDataManager != null)
            return mDataManager;

//        dataManager = new DataManager(getFileHelper(), getLocalResourceLoader());
        mDataManager = new DataManager(getRemoteRepository(), getFileHelper(),
                getLocalResourceLoader(), getSchedulerProvider());
        return mDataManager;
    }

    public synchronized IFileHelper getFileHelper() {
        if (mFileHelper != null)
            return mFileHelper;

        File userDir = new File(
                mApp.getFilesDir().getPath() + File.separator + mUserId);

        mFileHelper = new FileHelper(getLocalResourceLoader(), userDir, new GsonBuilder());
        return mFileHelper;
    }

    @Override
    public IDataRepository getRemoteRepository() {
        if (mRemoteRepo != null)
            return mRemoteRepo;

        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child(USER_DATA).child(mUserId);
        DatabaseReference appResDB = FirebaseDatabase.getInstance().getReference().child(APP_RESOURCES);
        StorageReference storage = FirebaseStorage.getInstance().getReference().child(USER_DATA).child(mUserId);

        mRemoteRepo = new FirebaseHelper(getLocalResourceLoader(), userDB, appResDB, storage, getSchedulerProvider());
        return mRemoteRepo;
    }

    @Override
    public IDataRepository getLocalRepository() {
        if(mLocalRepo != null)
            return mLocalRepo;

        return null;
    }

    @Override
    public IDebugHelper getDataDebugHelper() {
        IDataManager dataManager = getDataManager();
        if(dataManager instanceof IDebugHelper) {
            return (IDebugHelper) dataManager;
        }

        // avoid NullPointerException when dataManager does not implement IDebugHelper
        return () -> { };
    }

    @Override
    public LocalResourceLoader getLocalResourceLoader() {
        if (mResLoader != null)
            return mResLoader;

        mResLoader = new LocalResourceLoader(mApp.getAssets());
        return mResLoader;
    }

    @Override
    public void setUserId(String userId) {
        if(checkNotNull(userId).equals(mUserId)) return;

        mUserId = userId;
        mFileHelper = null;
        mRemoteRepo = null;
        mLocalRepo = null;
        mDataManager = null;
    }

    @Override
    public String getUserId() {
        return mUserId;
    }


    //region Private stuff

    private static final String USER_DATA = "user_data";
    private static final String APP_RESOURCES = "app_resources";

    private Application mApp;
    private String mUserId;
    private LocalResourceLoader mResLoader;
    private IFileHelper mFileHelper;
    private IDataRepository mRemoteRepo;
    private IDataRepository mLocalRepo;
    private IDataManager mDataManager;

    private Factory() {
    }

    //endregion
}
