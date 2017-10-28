package nz.ac.aut.comp705.sortmystuff.di;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataRepository;
import nz.ac.aut.comp705.sortmystuff.data.IDebugHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;
import nz.ac.aut.comp705.sortmystuff.utils.schedulers.ISchedulerProvider;

/**
 * A factory for dependency injection.
 *
 * @author Yuan
 */

public interface IFactory {

    ISchedulerProvider getImmediateSchedulerProvider();

    ISchedulerProvider getSchedulerProvider();

    IDataManager getDataManager();

    IFileHelper getFileHelper();

    IDataRepository getRemoteRepository();

    IDataRepository getLocalRepository();

    IDebugHelper getDataDebugHelper();

    LocalResourceLoader getLocalResourceLoader();

    void setUserId(String userId);

    String getUserId();
}
