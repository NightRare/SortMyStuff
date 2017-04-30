package nz.ac.aut.comp705.sortmystuff.data.local;

import android.app.Application;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.util.DetailAdapter;
import nz.ac.aut.comp705.sortmystuff.util.Log;

/**
 * Created by Yuan on 2017/4/25.
 */

public class JsonHelper implements IJsonHelper {

    //********************************************
    // CONSTRUCTOR
    //********************************************
    public JsonHelper(Application app, String userId) {
        Preconditions.checkNotNull(app);
        Preconditions.checkNotNull(userId);
        if (userId.isEmpty())
            throw new IllegalArgumentException("The userId cannot be empty.");

        this.app = app;
        this.userId = userId;

        gBuilder = new GsonBuilder();
        gBuilder.serializeNulls();
        gBuilder.setPrettyPrinting();
        gBuilder.registerTypeAdapter(Detail.class, new DetailAdapter());

        userDir = new File(app.getFilesDir().getPath() + File.separator + userId);
        if (!userDir.exists())
            userDir.mkdirs();

        rootExists = false;
    }

    //********************************************
    // IJsonHelper METHODS
    //********************************************

    @Override
    public Asset deserialiseAsset(final String assetId) {
        Preconditions.checkNotNull(assetId);

        if (!rootExists()) {
            Log.e(getClass().getName(), "Root Asset file not exists or valid.");
            return null;
        }

        return deserialiseAssetFromFile(
                userDir + File.separator + assetId + File.separator + ASSET_FILENAME);
    }

    @Override
    public Asset deserialiseRootAsset() {
        return deserialiseAssetFromFile(
                userDir + File.separator + ROOT_ASSET_DIR + File.separator + ASSET_FILENAME);
    }

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

            Asset asset = deserialiseAssetFromFile(dir + File.separator + ASSET_FILENAME);
            if (asset != null && !assets.contains(asset)) {
                assets.add(asset);
            }
        }
        return assets;
    }

    @Override
    public List<Detail> deserialiseDetails(String assetId) {
        Preconditions.checkNotNull(assetId);

        String target = userDir.getPath() + File.separator + assetId;
        for (File dir : userDir.listFiles()) {
            if (!dir.getPath().equals(target))
                continue;

            final File file;
            file = new File(dir, DETAILS_FILENAME);
            if (!file.exists()) {
                Log.e(getClass().getName(), DETAILS_FILENAME + " does not exist in " + dir);
                return null;
            }

            Detail[] details = readJsonFile(gBuilder.create(), file, Detail[].class);

            List<Detail> list = new LinkedList();
            if(details != null) {
                for (Detail d : details) {
                    if(d != null && !list.contains(d))
                        list.add(d);
                }
            }

            return list;
        }

        return null;
    }

    @Override
    public boolean serialiseAsset(final Asset asset) {
        Preconditions.checkNotNull(asset);

        final File assetDir, file;
        if (asset.isRoot()) {
            if(rootExists()) {
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

        file = new File(assetDir, ASSET_FILENAME);

        return writeJsonFile(gBuilder.create(), asset, file, Asset.class);
    }

    @Override
    public boolean serialiseDetails(final List<Detail> details) {
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
        assetDir = new File(userDir, assetId);
        assetFile = new File(assetDir, ASSET_FILENAME);
        if (!assetDir.exists() || !assetFile.exists()) {
            Log.e(getClass().getName(), "Asset \"" + assetId + "\" not exists");
            return false;
        }

        detailFile = new File(assetDir, DETAILS_FILENAME);

        final Detail[] dArray = details.toArray(new Detail[details.size()]);
        return writeJsonFile(gBuilder.create(), dArray, detailFile, Detail[].class);
    }

    @Override
    public boolean rootExists() {
        if (rootExists)
            return true;

        File rootFile = new File(
                userDir + File.separator + ROOT_ASSET_DIR + File.separator + ASSET_FILENAME);
        rootExists = rootFile.exists();
        Asset asset = deserialiseAssetFromFile(rootFile.getPath());
        rootExists = (asset == null ? false : true);
        return rootExists;
    }
    //********************************************
    // PRIVATE
    //********************************************

    private String ASSET_FILENAME = "asset.json";

    private String DETAILS_FILENAME = "details.json";

    private String ROOT_ASSET_DIR = "root";

    private boolean rootExists;

    private File userDir;

    private GsonBuilder gBuilder;

    private String userId;

    private Application app;

    private Writer writer;

    private Reader reader;

    private Asset deserialiseAssetFromFile(String filename) {
        final File file = new File(filename);
        if (!file.exists())
            return null;

        Asset asset = readJsonFile(gBuilder.create(), file, Asset.class);
        return asset;
    }

    private boolean isValidAssetDir(File dir) {
        String msg = "User dir data damaged: " + dir.getName();
        if (!dir.isDirectory())
            return false;

        File[] files = dir.listFiles();
        if (files.length != 2)
            return false;
        for (File file : files) {
            if (!file.isFile())
                return false;

            boolean nameIsLegal = file.getName().equals(ASSET_FILENAME)
                    || file.getName().equals(DETAILS_FILENAME);
            if (!nameIsLegal)
                return false;
        }
        return true;
    }

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

    private static <T> boolean writeJsonFile(Gson gson, T srcObj, File file, Class<T> klass) {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            gson.toJson(srcObj, klass, writer);
            return true;
        } catch (IOException e) {
            Log.e(JsonHelper.class.getName(), "IOException", e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    Log.e(JsonHelper.class.getName(), "Unexpected error", e);
                }
            }
        }
    }

    private static <T> T readJsonFile(Gson gson, File file, Class<T> klass) {
        Reader reader = null;
        try {
            reader = new FileReader(file);
            return gson.fromJson(reader, klass);
        } catch (IOException e) {
            Log.e(JsonHelper.class.getName(), "IOException", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(JsonHelper.class.getName(), "Unexpected error", e);
                }
            }
        }
    }
}
