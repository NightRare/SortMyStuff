package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;
import android.content.Context;

import com.google.gson.GsonBuilder;

import java.io.File;

import nz.ac.aut.comp705.sortmystuff.data.DataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.JsonHelper;

/**
 * An implementation of {@link IFactory}.
 *
 * @author Yuan
 */

public class Factory implements IFactory {

    public Factory (Application app) {
        this.app = app;

        initialise();
    }

    @Override
    public synchronized IDataManager getDataManager() {
        if(dataManager != null)
            return dataManager;

        jsonHelper = getJsonHelper();
        return new DataManager(jsonHelper);
    }

    @Override
    public synchronized IJsonHelper getJsonHelper() {
        if(jsonHelper != null)
            return jsonHelper;

        File userDir = new File(
                app.getFilesDir().getPath() + File.separator + "default-user");
        return new JsonHelper(userDir, new GsonBuilder(), new JsonHelper.FileCreator());
    }

    private void initialise() {
        getJsonHelper();
        getDataManager();
    }

    //region Private stuff

    private Application app;

    private IDataManager dataManager;

    private IJsonHelper jsonHelper;

    private Factory() { }

    //endregion
}
