package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;
import android.content.Context;

import com.google.gson.GsonBuilder;

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

    //********************************************
    // PRIVATE
    //********************************************

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

        return new JsonHelper(app, "default-user", new GsonBuilder());
    }

    private void initialise() {
        getJsonHelper();
        getDataManager();
    }


    //********************************************
    // PRIVATE
    //********************************************
    private Application app;

    private IDataManager dataManager;

    private IJsonHelper jsonHelper;

    private Factory() { }
}
