package nz.ac.aut.comp705.sortmystuff.di;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;

import nz.ac.aut.comp705.sortmystuff.ISortMyStuffAppComponent;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.IDebugHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;

/**
 * A factory for dependency injection.
 *
 * @author Yuan
 */

public interface IFactory extends ISortMyStuffAppComponent {

    ISchedulerProvider getImmediateSchedulerProvider();

    ISchedulerProvider getSchedulerProvider();

    IDataManager getDataManager();

    IDataRepository getRemoteRepository();

    IDataRepository getLocalRepository();

    IDebugHelper getDataDebugHelper();

    LocalResourceLoader getLocalResourceLoader();

    void setUser(FirebaseUser user);

    void setUser(FirebaseUser user, GoogleApiClient googleApiClient);

    FirebaseUser getCurrentUser();

    GoogleApiClient getGoogleApiClient();
}
