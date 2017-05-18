package nz.ac.aut.comp705.sortmystuff.di;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;

/**
 * A factory for dependency injection.
 *
 * @author Yuan
 */

public interface IFactory {

    IDataManager getDataManager();

    IFileHelper getFileHelper();

    LocalResourceLoader getLocalResourceLoader();
}
