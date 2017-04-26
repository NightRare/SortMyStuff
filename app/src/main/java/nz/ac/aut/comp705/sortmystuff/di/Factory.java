package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;
import android.content.Context;

import nz.ac.aut.comp705.sortmystuff.data.DataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.JsonHelper;

/**
 * Created by Vince on 2017/4/25.
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

        return new JsonHelper(app, "default-user");
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
