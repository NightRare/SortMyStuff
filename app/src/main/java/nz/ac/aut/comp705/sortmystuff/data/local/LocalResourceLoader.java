package nz.ac.aut.comp705.sortmystuff.data.local;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.base.Preconditions;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.utils.Log;

/**
 * Takes in the AssetManager of Android and loads the assets into memory.
 * <p>
 * Created by Yuan on 2017/5/18.
 */

public class LocalResourceLoader {

    private static final String TAG_DEFAULT_PHOTO = "TAG_DEFAULT_PHOTO";
    private static final String TAG_CATEGORIES_JSON = "TAG_CATEGORIES_JSON";
    private static final String TAG_DEMO_PHOTOS = "TAG_DEMO_PHOTOS";

    /**
     * When change the value of this constant, also need to change the format in
     * {@link FileHelper#writeToImageFile(Bitmap, File)}
     */
    public final static String IMAGE_DETAIL_FORMAT = ".png";
    public final static String DEFAULT_PHOTO_FILENAME = "default_square" + IMAGE_DETAIL_FORMAT;
    public final static String DEFAULT_PHOTO_PATH = "images" + File.separator + DEFAULT_PHOTO_FILENAME;
    public final static String CATEGORIES_FILE_NAME = "categories.json";

    public final static String DEMO_PHOTO_DIR = "images" + File.separator + "demo";

    /**
     * Initialises a LocalResourceLoader
     *
     * @param am the AssetManager
     * @throws NullPointerException if am is {@code null}
     */
    public LocalResourceLoader(AssetManager am) {
        Preconditions.checkNotNull(am);
        this.am = am;
        loadResources();
    }

    /**
     * Gets the categories definition json file as a String object.
     * It is defined in assets/categories.json.
     *
     * @return the categories definition json file.
     */
    public String getCategoriesJson() {
        return (String) resDict.get(TAG_CATEGORIES_JSON);
    }

    /**
     * Gets the default photo (placeholder image) of an asset as a Bitmap instance.
     * It is defined in assets/images/default.png
     *
     * @return the default photo (placeholder image) of an asset
     */
    public Bitmap getDefaultPhoto() {
        return (Bitmap) resDict.get(TAG_DEFAULT_PHOTO);
    }

    public Map<String, Bitmap> getDemoPhotos() {
        Map<String, Bitmap> m = new HashMap<String, Bitmap>();
        m.putAll((Map<? extends String, ? extends Bitmap>) resDict.get(TAG_DEMO_PHOTOS));
        return m;
    }

    /**
     * Reloads all the resources from the local.
     */
    public void reloadFromStorage() {
        loadResources();
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

            resDict.put(TAG_DEMO_PHOTOS, loadPhotosInDir(DEMO_PHOTO_DIR));
        } catch (IOException e) {
            Log.e(Log.ASSETMANAGER_READ_FAILED, "Load application assets failed", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "", e);
                }
            }
        }
    }

    private Map<String, Bitmap> loadPhotosInDir(String dirPath) {
        HashMap<String, Bitmap> dict = new HashMap<>();
        InputStream is = null;
        try {
            for (String imageName : am.list(dirPath)) {
                is = am.open(dirPath + File.separator + imageName);
                dict.put(imageName, BitmapFactory.decodeStream(is));
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "", e);
                }
            }
        }
        return dict;
    }

    //endregion

}
