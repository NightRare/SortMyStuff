package nz.ac.aut.comp705.sortmystuff.data.local;

import android.app.Application;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import nz.ac.aut.comp705.sortmystuff.utils.BitmapHelper;
import nz.ac.aut.comp705.sortmystuff.utils.Log;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Takes in the AssetManager of Android and loads the assets into memory.
 * <p>
 * @author Yuan
 */

@Singleton
public class LocalResourceLoader {

    private static final String TAG_DEFAULT_PHOTO = "TAG_DEFAULT_PHOTO";
    private static final String TAG_DEFAULT_THUMBNAIL = "TAG_DEFAULT_THUMBNAIL";
    private static final String TAG_DEFAULT_PHOTO_DATASTRING = "TAG_DEFAULT_PHOTO_DATASTRING";
    private static final String TAG_DEFAULT_THUMBNAIL_DATASTRING = "TAG_DEFAULT_PHOTO_DATASTRING";
    private static final String TAG_DEFAULT_TEXT = "TAG_DEFAULT_TEXT";
    private static final String TAG_CATEGORIES_JSON = "TAG_CATEGORIES_JSON";
    private static final String TAG_DEMO_PHOTOS = "TAG_DEMO_PHOTOS";

    public final static String IMAGE_DETAIL_FORMAT = ".jpg";
    public final static String DEFAULT_PHOTO_FILENAME = "default_square" + IMAGE_DETAIL_FORMAT;
    public final static String DEFAULT_PHOTO_PATH = "images" + File.separator + DEFAULT_PHOTO_FILENAME;
    public final static String CATEGORIES_FILE_NAME = "categories.json";

    public final static String DEMO_PHOTO_DIR = "images" + File.separator + "demo";

    /**
     * Initialises a LocalResourceLoader
     *
     * @param app the Application
     * @throws NullPointerException if am is {@code null}
     */
    @Inject
    public LocalResourceLoader(Application app) {
        mAssetManager = checkNotNull(app).getAssets();
        loadResources();
    }

    /**
     * Gets the categories definition json file as a String object.
     * It is defined in assets/categories.json.
     *
     * @return the categories definition json file.
     */
    public String getCategoriesJson() {
        return (String) mResources.get(TAG_CATEGORIES_JSON);
    }

    /**
     * Gets the default photo (placeholder image) of an asset as a Bitmap instance.
     * It is defined in assets/images/default.png
     *
     * @return the default photo (placeholder image) of an asset
     */
    public Bitmap getDefaultPhoto() {
        return (Bitmap) mResources.get(TAG_DEFAULT_PHOTO);
    }

    public String getDefaultPhotoDataString() {
        return (String) mResources.get(TAG_DEFAULT_PHOTO_DATASTRING);
    }

    public Bitmap getDefaultThumbnail() {
        return (Bitmap) mResources.get(TAG_DEFAULT_THUMBNAIL);
    }

    public String getDefaultThumbnailDatastring() {
        return (String) mResources.get(TAG_DEFAULT_THUMBNAIL_DATASTRING);
    }

    public String getDefaultText() {
        return (String) mResources.get(TAG_DEFAULT_TEXT);
    }

    public Map<String, Bitmap> getDemoPhotos() {
        Map<String, Bitmap> m = new HashMap<String, Bitmap>();
        m.putAll((Map<? extends String, ? extends Bitmap>) mResources.get(TAG_DEMO_PHOTOS));
        return m;
    }

    /**
     * Reloads all the resources from the local.
     */
    public void reloadFromStorage() {
        loadResources();
    }

    //region PRIVATE STUFF

    private void loadResources() {
        mResources = new HashMap<>();
        InputStream is = null;
        // default photo of assets
        try {
            // default photo
            is = mAssetManager.open(DEFAULT_PHOTO_PATH);
            Bitmap defaultPhoto = BitmapFactory.decodeStream(is);
            mResources.put(TAG_DEFAULT_PHOTO, defaultPhoto);
            mResources.put(TAG_DEFAULT_PHOTO_DATASTRING, BitmapHelper.toString(defaultPhoto));

            Bitmap thumbnail = BitmapHelper.toThumbnail(defaultPhoto);
            mResources.put(TAG_DEFAULT_THUMBNAIL, thumbnail);
            mResources.put(TAG_DEFAULT_THUMBNAIL_DATASTRING, BitmapHelper.toString(thumbnail));

            mResources.put(TAG_DEFAULT_TEXT, "");

            is = mAssetManager.open(CATEGORIES_FILE_NAME);
            mResources.put(TAG_CATEGORIES_JSON, IOUtils.toString(is, Charsets.UTF_8));

            mResources.put(TAG_DEMO_PHOTOS, loadPhotosInDir(DEMO_PHOTO_DIR));
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
            for (String imageName : mAssetManager.list(dirPath)) {
                is = mAssetManager.open(dirPath + File.separator + imageName);
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

    private Map<String, Object> mResources;
    private AssetManager mAssetManager;

    //endregion

}
