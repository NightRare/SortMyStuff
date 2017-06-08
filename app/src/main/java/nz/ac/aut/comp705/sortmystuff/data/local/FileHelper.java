package nz.ac.aut.comp705.sortmystuff.data.local;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.Category;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.util.JsonDetailAdapter;
import nz.ac.aut.comp705.sortmystuff.util.Log;

/**
 * An implementation class of {@link IFileHelper}.
 *
 * @author Yuan
 */

public class FileHelper implements IFileHelper {

    public static final String ASSET_FILENAME = "asset.json";
    public static final String DETAILS_FILENAME = "details.json";
    public static final String ROOT_ASSET_DIR = "root";

    /**
     * Initialises a FileHelper.
     *
     * @param userDir  the directory of the current user
     * @param gBuilder the GsonBuilder
     * @throws NullPointerException if any argument is {@code null}
     */
    @Deprecated
    public FileHelper(File userDir, GsonBuilder gBuilder) {
        Preconditions.checkNotNull(userDir);
        Preconditions.checkNotNull(gBuilder);

        this.gBuilder = gBuilder;
        this.userDir = userDir;

        this.gBuilder.serializeNulls();
        this.gBuilder.setPrettyPrinting();
        this.gBuilder.registerTypeAdapter(Detail.class, new JsonDetailAdapter());

        if (!this.userDir.exists())
            this.userDir.mkdirs();
    }

    public FileHelper(LocalResourceLoader resLoader, File userDir, GsonBuilder gBuilder) {
        Preconditions.checkNotNull(resLoader);
        Preconditions.checkNotNull(userDir);

        this.gBuilder = gBuilder;
        this.userDir = userDir;

        this.gBuilder.serializeNulls();
        this.gBuilder.setPrettyPrinting();
        this.gBuilder.registerTypeAdapter(Detail.class, new JsonDetailAdapter());

        if (!this.userDir.exists())
            this.userDir.mkdirs();

        this.resLoader = resLoader;
    }

    //region IJSonHelper methods

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset deserialiseAsset(final String assetId) {
        Preconditions.checkNotNull(assetId);

        if (!rootExists()) {
            Log.e(getClass().getName(), "Root Asset file not exists or valid.");
            return null;
        }

        return deserialiseAssetFromFile(assetJsonFile(assetId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset deserialiseRootAsset() {
        return deserialiseAssetFromFile(rootAssetJsonFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Asset> deserialiseAllAssets() {
        List<Asset> assets = new LinkedList<>();

        if (!rootExists()) {
            Log.e(getClass().getName(), "Root Asset file not exists or valid.");
            return null;
        }

        for (File assetDir : userDir.listFiles()) {
            if (!isValidAssetDir(assetDir))
                continue;

            Asset asset = deserialiseAssetFromFile(new File(assetDir, ASSET_FILENAME));
            if (asset != null && !assets.contains(asset)) {
                assets.add(asset);
            }
        }
        return assets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Detail> deserialiseDetails(String assetId) {
        Preconditions.checkNotNull(assetId);

        File file = detailsJsonFile(assetId);
        if (!file.exists()) {
            Log.e(Log.FILE_NOT_EXISTS, file.getPath());
            return null;
        }

        Detail[] details = readJsonFile(file, Detail[].class);
        List<Detail> list = new ArrayList<>();

        if (details != null) {
            for (Detail d : details) {
                if (d != null && !list.contains(d))
                    list.add(d);
            }
        }

        return deserialiseImageFiles(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> deserialiseCategories() {
        String json = resLoader.getCategoriesJson();
        Category[] categories = gBuilder.create().fromJson(json, Category[].class);
        for (Category cat : categories) {
            for (Detail d : cat.getDetails()) {
                if (d.getType().equals(DetailType.Image))
                    d.setField(resLoader.getDefaultPhoto());
            }
        }
        return Arrays.asList(categories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean serialiseAsset(final Asset asset) {
        Preconditions.checkNotNull(asset);

        final File assetDir, file;
        if (asset.isRoot()) {
            if (rootExists()) {
                Log.e(getClass().getName(), "Already have a Root asset.");
                return false;
            }

            assetDir = prepareAssetDir(ROOT_ASSET_DIR);
        } else if (!rootExists()) {
            Log.e(Log.FILE_NOT_EXISTS, "Cannot serialise \"" + asset.getId()
                    + "\". Must serialise Root asset at first.");
            return false;
        } else {
            assetDir = prepareAssetDir(asset.getId());
        }

        file = new File(assetDir, ASSET_FILENAME);

        return writeJsonFile(asset, file, Asset.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean serialiseDetails(List<Detail> details, boolean imageUpdated) {
        Preconditions.checkNotNull(details);
        Preconditions.checkArgument(!details.contains(null), "details should not contain null.");
        if (details.isEmpty() || !checkBelongToSameAsset(details))
            return false;

        String assetId = details.get(0).getAssetId();
        final File assetDir, detailFile, assetFile;
        assetDir = new File(userDir, assetId);
        assetFile = new File(assetDir, ASSET_FILENAME);

        if (!assetDir.exists() || !assetFile.exists()) {
            Log.e(getClass().getName(), "Asset \"" + assetId + "\" not exists");
            return false;
        }

        if (imageUpdated) {
            // if serialising image files fails
            if (!serialiseImageFiles(details)) {
                return false;
            }
        }

        detailFile = new File(assetDir, DETAILS_FILENAME);

        final Detail[] dArray = details.toArray(new Detail[details.size()]);
        return writeJsonFile(dArray, detailFile, Detail[].class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rootExists() {
        boolean rootExists;

        rootExists = rootAssetJsonFile().exists();
        if (!rootExists) {
            Log.e(Log.FILE_NOT_EXISTS, "Root json file not exists.");
            return false;
        }

        // check whether the file corrupt
        Asset asset = deserialiseAssetFromFile(rootAssetJsonFile());
        if (asset == null) {
            Log.e(Log.LOCAL_FILE_CORRUPT, "Root json file corrupted.");
            return false;
        }
        return true;
    }

    //endregion

    //region Inner classes

//    /**
//     * A wrapper class for File constructors so that they can be mocked.
//     */
//    public static class FileCreator {
//
//        public File createFile(String pathname) {
//            return new File(pathname);
//        }
//
//
//        public File createFile(File parent, String child) {
//            return new File(parent, child);
//        }
//
//        public FileReader createFileReader(File file) throws FileNotFoundException {
//            return new FileReader(file);
//        }
//
//        public FileWriter createFileWriter(File file) throws IOException {
//            return new FileWriter(file);
//        }
//    }

    //endregion

    //region Private stuff

    private LocalResourceLoader resLoader;

    private File userDir;

    private GsonBuilder gBuilder;

//    private FileCreator fc;

    /**
     * Deserialises asset from file according to the given file name.
     *
     * @param file
     * @return
     */
    private Asset deserialiseAssetFromFile(final File file) {
        if (!file.exists())
            return null;

        Asset asset = readJsonFile(file, Asset.class);
        return asset;
    }

    private boolean checkBelongToSameAsset(List<Detail> details) {
        String assetId = null;
        for (Detail d : details) {
            if (assetId == null) {
                assetId = d.getAssetId();
            } else {
                if (!assetId.equals(d.getAssetId())) {
                    Log.e(getClass().getName(), "Details must belong to a same asset.");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the dir is a valid asset folder.
     *
     * @param dir
     * @return
     */
    private boolean isValidAssetDir(File dir) {
        String msg = "User dir data damaged: " + dir.getName();
        if (!dir.isDirectory()) {
            Log.e(Log.LOCAL_FILE_CORRUPT, msg);
            return false;
        }

        File assetFile = new File(dir, ASSET_FILENAME);
        File detailsFile = new File(dir, DETAILS_FILENAME);
        if(!assetFile.exists() || !assetFile.isFile()) {
            Log.e(Log.LOCAL_FILE_CORRUPT, msg);
            return false;
        }

        if(!detailsFile.exists() || !detailsFile.isFile()) {
            Log.e(Log.LOCAL_FILE_CORRUPT, msg);
            return false;
        }

        return true;
    }

    /**
     * Prepare the asset dir, including an empty asset file and empty details file.
     *
     * @param assetFolderName
     * @return the dir File of the asset
     */
    private File prepareAssetDir(String assetFolderName) {
        File assetDir = new File(userDir, assetFolderName);
        if (!assetDir.exists())
            assetDir.mkdirs();
        File assetFile = new File(assetDir, ASSET_FILENAME);
        File detailsFile = new File(assetDir, DETAILS_FILENAME);
        try {
            if (!assetFile.exists())
                assetFile.createNewFile();
            if (!detailsFile.exists())
                detailsFile.createNewFile();
        } catch (Exception e) {
            Log.e(getClass().getName(), "Unexpected error.", e);
        }
        return assetDir;
    }

    /**
     * Serialise an object to a json String and write it to a json file on local storage.
     *
     * @param srcObj the object to be written
     * @param file   the json file
     * @param klass  the class of the object
     * @param <T>    the type of the object
     * @return true if write successfully
     */
    private <T> boolean writeJsonFile(T srcObj, File file, Class<T> klass) {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            gBuilder.create().toJson(srcObj, klass, writer);
            return true;
        } catch (IOException e) {
            Log.e(Log.GSON_WRITE_FAILED, "IOException", e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "Unexpected error", e);
                }
            }
        }
    }

    /**
     * Deserialise an object from a json file on local storage.
     *
     * @param file  the json file
     * @param klass the class of the object
     * @param <T>   the type of the object
     * @return the object
     */
    private <T> T readJsonFile(File file, Class<T> klass) {
        Reader reader = null;
        try {
            reader = new FileReader(file);
            return gBuilder.create().fromJson(reader, klass);
        } catch (IOException e) {
            Log.e(Log.GSON_READ_FAILED, "IOException", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "Unexpected error", e);
                }
            }
        }
    }

    /**
     * @param image
     * @param file
     * @return
     */
    private boolean writeToImageFile(Bitmap image, File file) {
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                if (!file.createNewFile())
                    return false;
            }
            fos = new FileOutputStream(file);
            return image.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            Log.e(Log.BITMAP_WRITE_FAILED, "IOException", e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "Unexpected error", e);
                }
            }
        }
    }

    private Bitmap readFromImageFile(File file) {
        if (!file.exists()) {
            return null;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            Log.e(Log.BITMAP_WRITE_FAILED, "IOException", e);
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "Unexpected error", e);
                }
            }
        }
    }

    private boolean serialiseImageFiles(List<Detail> details) {
        for (Detail d : details) {
            if (!(d instanceof ImageDetail))
                continue;

            ImageDetail imageDetail = (ImageDetail) d;
            File imageFile = imageFile(d.getAssetId(), d.getId());

            if (imageDetail.getField().sameAs(resLoader.getDefaultPhoto())) {
                // if set back to default photo, then remove the customised image file
                if (imageFile.exists() && !imageFile.delete())
                    return false;
            }
            // if the image is customised, then save it to the local storage
            else {
                if (!writeToImageFile(imageDetail.getField(), imageFile))
                    return false;
            }
        }
        return true;
    }

    private List<Detail> deserialiseImageFiles(List<Detail> details) {
        for (Detail d : details) {
            if (!(d instanceof ImageDetail))
                continue;

            ImageDetail imageDetail = (ImageDetail) d;
            File imageFile = imageFile(d.getAssetId(), d.getId());

            Bitmap image = null;

            // if there is not customised image file, then set to the default file
            if (imageFile.exists()) {
                image = readFromImageFile(imageFile);
            }
            if (image == null) {
                image = resLoader.getDefaultPhoto();
            }
            imageDetail.setField(image);
        }
        return details;
    }

    private File assetJsonFile(String assetId) {
        return new File(userDir + File.separator + assetId + File.separator + ASSET_FILENAME);
    }

    private File rootAssetJsonFile() {
        return new File(userDir + File.separator + ROOT_ASSET_DIR + File.separator +
                ASSET_FILENAME);
    }

    private File detailsJsonFile(String assetId) {
        return new File(userDir.getPath() + File.separator + assetId + File.separator +
                DETAILS_FILENAME);
    }

    private File imageFile(String assetId, String detailId) {
        return new File(userDir + File.separator + assetId + File.separator + detailId +
                LocalResourceLoader.IMAGE_DETAIL_FORMAT);
    }

    //endregion
}
