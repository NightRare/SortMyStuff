package nz.ac.aut.comp705.sortmystuff.data.local;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataOutput;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.util.JsonDetailAdapter;
import nz.ac.aut.comp705.sortmystuff.util.Log;

/**
 * An implementation class of {@link IJsonHelper}.
 *
 * @author Yuan
 */

public class JsonHelper implements IJsonHelper {

    public static final String ASSET_FILENAME = "asset.json";

    public static final String DETAILS_FILENAME = "details.json";

    public static final String ROOT_ASSET_DIR = "root";

    /**
     * Initialises a JsonHelper.
     *
     * @param userDir  the directory of the current user
     * @param gBuilder the GsonBuilder
     * @param fc       the FileCreator
     */
    public JsonHelper(File userDir, GsonBuilder gBuilder, FileCreator fc) {
        Preconditions.checkNotNull(userDir);

        this.gBuilder = gBuilder;
        this.userDir = userDir;
        this.fc = fc;

        this.gBuilder.serializeNulls();
        this.gBuilder.setPrettyPrinting();
        this.gBuilder.registerTypeAdapter(Detail.class, new JsonDetailAdapter());

        if (!this.userDir.exists())
            this.userDir.mkdirs();
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

        return deserialiseAssetFromFile(fc.createFile(
                userDir + File.separator + assetId + File.separator + ASSET_FILENAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Asset deserialiseRootAsset() {
        return deserialiseAssetFromFile(fc.createFile(
                userDir + File.separator + ROOT_ASSET_DIR + File.separator + ASSET_FILENAME));
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

        for (File dir : userDir.listFiles()) {
            if (!isValidAssetDir(dir))
                continue;

            Asset asset = deserialiseAssetFromFile(fc.createFile(
                    dir + File.separator + ASSET_FILENAME));
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

        String target = userDir.getPath() + File.separator + assetId;
        for (File dir : userDir.listFiles()) {
            if (!dir.getPath().equals(target))
                continue;

            final File file;
            file = fc.createFile(dir, DETAILS_FILENAME);
            if (!file.exists()) {
                Log.e(getClass().getName(), DETAILS_FILENAME + " does not exist in " + dir);
                return null;
            }

            Detail[] details = readJsonFile(file, Detail[].class);

            List<Detail> list = new LinkedList();
            if (details != null) {
                for (Detail d : details) {
                    if (d != null && !list.contains(d))
                        list.add(d);
                }
            }

            return list;
        }

        return null;
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
            Log.e(getClass().getName(), "Cannot serialise \"" + asset.getId()
                    + "\". Must serialise Root asset at first.");
            return false;
        } else {
            assetDir = prepareAssetDir(asset.getId());
        }

        file = fc.createFile(assetDir, ASSET_FILENAME);

        return writeJsonFile(asset, file, Asset.class);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public boolean serialiseDetails(final List<Detail> details) {
        return serialiseDetails(details, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean serialiseDetails(List<Detail> details, boolean imageUpdated) {
        Preconditions.checkNotNull(details);
        Preconditions.checkArgument(!details.contains(null), "details should not contain null.");
        if (details.isEmpty())
            return false;

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

        final File assetDir, detailFile, assetFile;
        assetDir = fc.createFile(userDir, assetId);
        assetFile = fc.createFile(assetDir, ASSET_FILENAME);
        if (!assetDir.exists() || !assetFile.exists()) {
            Log.e(getClass().getName(), "Asset \"" + assetId + "\" not exists");
            return false;
        }

        if(imageUpdated) {
            // if serialising image files fails
            if(!serialiseImageFiles(details, assetDir)) {
                return false;
            }
        }

        detailFile = fc.createFile(assetDir, DETAILS_FILENAME);

        final Detail[] dArray = details.toArray(new Detail[details.size()]);
        return writeJsonFile(dArray, detailFile, Detail[].class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rootExists() {
        boolean rootExists;

        File rootFile = fc.createFile(
                userDir + File.separator + ROOT_ASSET_DIR + File.separator + ASSET_FILENAME);
        rootExists = rootFile.exists();
        if (!rootExists)
            return false;

        // check whether the file corrupt
        Asset asset = deserialiseAssetFromFile(rootFile);
        rootExists = (asset == null ? false : true);
        return rootExists;
    }

    //endregion

    //region Inner classes

    /**
     * A wrapper class for File constructors so that they can be mocked.
     */
    public static class FileCreator {

        public File createFile(String pathname) {
            return new File(pathname);
        }


        public File createFile(File parent, String child) {
            return new File(parent, child);
        }

        public FileReader createFileReader(File file) throws FileNotFoundException {
            return new FileReader(file);
        }

        public FileWriter createFileWriter(File file) throws IOException {
            return new FileWriter(file);
        }
    }

    //endregion

    //region Private stuff

    private File userDir;

    private GsonBuilder gBuilder;

    private FileCreator fc;

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

        File[] files = dir.listFiles();
        if (files.length != 2) {
            Log.e(Log.LOCAL_FILE_CORRUPT, msg);
            return false;
        }

        for (File file : files) {
            if (!file.isFile()) {
                Log.e(Log.LOCAL_FILE_CORRUPT, msg);
                return false;
            }

            boolean nameIsLegal = file.getName().equals(ASSET_FILENAME)
                    || file.getName().equals(DETAILS_FILENAME);
            if (!nameIsLegal) {
                Log.e(Log.LOCAL_FILE_CORRUPT, msg);
                return false;
            }
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
        File assetDir = fc.createFile(userDir, assetFolderName);
        if (!assetDir.exists())
            assetDir.mkdirs();
        File assetFile = fc.createFile(assetDir, ASSET_FILENAME);
        File detailsFile = fc.createFile(assetDir, DETAILS_FILENAME);
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
            writer = fc.createFileWriter(file);
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
            reader = fc.createFileReader(file);
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
     *
     * @param image
     * @param file
     * @return
     */
    private boolean writeToImageFile(Bitmap image, File file) {
        FileOutputStream fos = null;
        try{
            if(!file.exists())
                file.createNewFile();
            fos = new FileOutputStream(file);
            return image.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            Log.e(Log.BITMAP_WRITE_FAILED, "IOException", e);
            return false;
        } finally {
            if(fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    Log.e(Log.CLOSING_STREAM_FAILED, "Unexpected error", e);
                }
            }
        }
    }

    private boolean serialiseImageFiles(List<Detail> details, File assetDir) {
        for (Detail d : details) {
            if (!(d instanceof ImageDetail))
                continue;

            ImageDetail imageDetail = (ImageDetail) d;
            File imageFile = fc.createFile(assetDir, imageDetail.getImageFileName());

            // if any image fails to write to the local storage, then return false;
            if(!writeToImageFile(imageDetail.getField(), imageFile))
                return false;
        }
        return true;
    }

    //endregion
}
