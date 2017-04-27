package nz.ac.aut.comp705.sortmystuff.di;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;

/**
 * Created by Yuan on 2017/4/25.
 */

public interface IFactory {

    IDataManager getDataManager();

    IJsonHelper getJsonHelper();
}
