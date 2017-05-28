package nz.ac.aut.comp705.sortmystuff.data.local;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.util.Log;

/**
 * Created by Yuan on 2017/5/18.
 */

public class LocalResourceLoader {

    private static final String TAG_DEFAULT_PHOTO = "TAG_DEFAULT_PHOTO";
    private static final String TAG_CATEGORIES_JSON = "TAG_CATEGORIES_JSON";

    /**
     * When change this, also need to change the format in
     * {@link FileHelper#writeToImageFile(Bitmap, File)}
     */
    public final static String IMAGE_DETAIL_FORMAT = ".png";
    public final static String DEFAULT_PHOTO_FILENAME = "default" + IMAGE_DETAIL_FORMAT;
    public final static String DEFAULT_PHOTO_PATH = "images" + File.separator + DEFAULT_PHOTO_FILENAME;
    public final static String CATEGORIES_FILE_NAME = "categories.json";

    /**
     *
     * @param am
     */
    public LocalResourceLoader(AssetManager am) {
        this.am = am;
        loadResources();
    }

    public String getCategoriesJson() {
        return (String) resDict.get(TAG_CATEGORIES_JSON);
    }

    public void reloadFromStorage() {
        loadResources();
    }

    public Bitmap getDefaultPhoto() {
        return (Bitmap) resDict.get(TAG_DEFAULT_PHOTO);
    }

    //region PRIVATE STUFF

    private Map<String, Object> resDict;

    private AssetManager am;

    private void loadResources() {
        resDict = new HashMap<>();
        InputStream is = null;
        // default photo of assets
        try {
            // default photo
            is = am.open(DEFAULT_PHOTO_PATH);
            Bitmap defaultPhoto = BitmapFactory.decodeStream(is);
            resDict.put(TAG_DEFAULT_PHOTO, defaultPhoto);

            is = am.open(CATEGORIES_FILE_NAME);
            resDict.put(TAG_CATEGORIES_JSON, IOUtils.toString(is, Charsets.UTF_8));
        } catch (IOException e) {
            Log.e(Log.ASSETMANAGER_READ_FAILED, "Load application assets failed", e);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "", e);
                }
            }
        }
    }

    //endregion

}
