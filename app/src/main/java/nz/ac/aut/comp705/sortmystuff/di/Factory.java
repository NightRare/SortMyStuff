package nz.ac.aut.comp705.sortmystuff.di;

import android.app.Application;

import com.google.gson.GsonBuilder;

import java.io.File;

import nz.ac.aut.comp705.sortmystuff.data.DataManager;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.local.FileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.IFileHelper;
import nz.ac.aut.comp705.sortmystuff.data.local.LocalResourceLoader;

/**
 * An implementation of {@link IFactory}.
 *
 * @author Yuan
 */

public class Factory implements IFactory {

    public Factory (Application app) {
        this.app = app;
    }

    @Override
    public synchronized IDataManager getDataManager() {
        if(dataManager != null)
            return dataManager;

        dataManager = new DataManager(getFileHelper(), getLocalResourceLoader());
        return dataManager;
    }

    public synchronized IFileHelper getFileHelper() {
        if(fileHelper != null)
            return fileHelper;

        File userDir = new File(
                app.getFilesDir().getPath() + File.separator + "default-user");

        fileHelper = new FileHelper(getLocalResourceLoader(), userDir, new GsonBuilder());
        return fileHelper;
    }

    @Override
    public LocalResourceLoader getLocalResourceLoader() {
        if(resLoader != null)
            return resLoader;

        resLoader = new LocalResourceLoader(app.getAssets());
        return resLoader;
    }


    //region Private stuff

    private Application app;

    private IDataManager dataManager;

    private IFileHelper fileHelper;

    private LocalResourceLoader resLoader;

    private Factory() { }

    //endregion
}
