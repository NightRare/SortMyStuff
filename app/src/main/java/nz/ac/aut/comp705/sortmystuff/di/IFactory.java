package nz.ac.aut.comp705.sortmystuff.di;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;

/**
 * A factory for dependency injection.
 *
 * @author Yuan
 */

public interface IFactory {

    IDataManager getDataManager();

    IJsonHelper getJsonHelper();
}
