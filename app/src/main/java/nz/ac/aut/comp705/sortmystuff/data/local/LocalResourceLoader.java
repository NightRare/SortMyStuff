package nz.ac.aut.comp705.sortmystuff.data.local;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuan on 2017/5/18.
 */

public class LocalResourceLoader {

    public static final String TAG_DEFAULT_PHOTO = "TAG_DEFAULT_PHOTO";

    /**
     * When change this, also need to change the format in
     * {@link FileHelper#writeToImageFile(Bitmap, File)}
     */
    public final static String IMAGE_DETAIL_FORMAT = ".png";

    public final static String DEFAULT_PHOTO_FILENAME = "default" + IMAGE_DETAIL_FORMAT;

    public final static String DEFAULT_PHOTO_PATH = "images" + File.separator + DEFAULT_PHOTO_FILENAME;

    /**
     *
     * @param am
     */
    public LocalResourceLoader(AssetManager am) {
        this.am = am;
        loadResources();
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

        // default photo of assets
        try {
            InputStream is = am.open(DEFAULT_PHOTO_PATH);
            Bitmap defaultPhoto = BitmapFactory.decodeStream(is);
            resDict.put(TAG_DEFAULT_PHOTO, defaultPhoto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion

}
