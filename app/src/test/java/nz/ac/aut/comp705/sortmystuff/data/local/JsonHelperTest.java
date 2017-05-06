package nz.ac.aut.comp705.sortmystuff.data.local;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.TextDetail;
import nz.ac.aut.comp705.sortmystuff.util.JsonDetailAdapter;

import static nz.ac.aut.comp705.sortmystuff.testutils.TestUtil.areIdenticalAssets;
import static nz.ac.aut.comp705.sortmystuff.testutils.TestUtil.areIdenticalDetails;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Yuan on 2017/5/5.
 */

public class JsonHelperTest {

    @Before
    public void setup() throws IOException {
        userDir = new File(TEST_USER_DIR);
        jh = new JsonHelper(userDir, new GsonBuilder(),
                new JsonHelper.FileCreator());
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.cleanDirectory(userDir);
        jh = null;
    }

    //region Tests
    @Test
    public void deserialiseAsset_assetDeserialisedFromLocalStorage() throws IOException {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.create(ASSET_NAME1, root);
        jh.serialiseAsset(asset1);

        Asset desAsset = jh.deserialiseAsset(asset1.getId());
        assertTrue(areIdenticalAssets(asset1, desAsset));
    }

    @Test
    public void deserialiseAsset_rootAssetNotExists() {
        Asset asset1 = Asset.create(ASSET_NAME1, Asset.createRoot());
        jh.serialiseAsset(asset1);

        Asset desAsset = jh.deserialiseAsset(asset1.getId());
        assertEquals(null, desAsset);
    }

    @Test
    public void deserialiseAsset_assetNotExists() {
        Asset desAsset = jh.deserialiseAsset("NoSuchAsset");
        assertEquals(null, desAsset);
    }

    @Test
    public void deserialiseRootAsset_rootAssetDeserialisedFromLocalStorage() {
        Asset root = prepareRootAsset();

        Asset desRoot = jh.deserialiseRootAsset();
        assertTrue(areIdenticalAssets(root, desRoot));
    }

    @Test
    public void deserialiseRootAsset_rootAssetNotExists() {
        Asset desRoot = jh.deserialiseRootAsset();
        assertEquals(null, desRoot);
    }

    @Test
    public void deserialiseAllAssets_allAssetsDeserialisedFromLocalStorage() {
        Asset root = prepareRootAsset();
        List<Asset> assets = new ArrayList<>();
        assets.add(Asset.create(ASSET_NAME1, root));
        assets.add(Asset.create(ASSET_NAME2, root));
        assets.add(Asset.create(ASSET_NAME3, assets.get(0)));

        for (Asset a : assets) {
            jh.serialiseAsset(a);
        }

        List<Asset> desAssets = jh.deserialiseAllAssets();

        //remove root Asset so that can be compared with assets
        assertTrue(desAssets.remove(root));

        assertTrue(areIdenticalAssets(assets, desAssets));
    }

    @Test
    public void deserialiseAllAssets_rootAssetNotExists() {
        // root asset not serialised
        Asset root = Asset.createRoot();
        Asset[] assets = new Asset[3];
        assets[0] = Asset.create(ASSET_NAME1, root);
        assets[1] = Asset.create(ASSET_NAME2, root);
        assets[2] = Asset.create(ASSET_NAME3, assets[0]);

        for (Asset a : assets) {
            jh.serialiseAsset(a);
        }

        List<Asset> desAssets = jh.deserialiseAllAssets();
        assertEquals(null, desAssets);
    }

    @Test
    public void deserialiseAllAssets_onlyHasRootAsset() {
        Asset root = prepareRootAsset();
        jh.serialiseAsset(root);

        List<Asset> desAssets = jh.deserialiseAllAssets();
        assertTrue(desAssets.size() == 1);
        assertTrue(areIdenticalAssets(desAssets.get(0), root));
    }

    @Test
    public void deserialiseDetails_detailsDeserialisedFromLocalStorage() {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.create(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        Detail detail1 = TextDetail.create(asset1.getId(), TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        Detail detail2 = TextDetail.create(asset1.getId(), TEXTDETAIL_LABEL2, TEXTDETAIL_FIELD2);
        Collections.addAll(details, detail1, detail2);

        jh.serialiseAsset(asset1);
        jh.serialiseDetails(details);

        List<Detail> desDetails = jh.deserialiseDetails(asset1.getId());
        assertTrue(areIdenticalDetails(details, desDetails));
    }

    @Test
    public void deserialiseDetails_assetNotExists() {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.create(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        Detail detail1 = TextDetail.create(asset1.getId(), TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        Detail detail2 = TextDetail.create(asset1.getId(), TEXTDETAIL_LABEL2, TEXTDETAIL_FIELD2);
        Collections.addAll(details, detail1, detail2);

        // asset1 is not serialised
        jh.serialiseDetails(details);

        List<Detail> desDetails = jh.deserialiseDetails(asset1.getId());
        assertEquals(null, desDetails);
    }

    @Test
    public void serialiseAsset_assetSerialisedToLocalStorage() throws IOException {
        Gson gson = customiseGsonBuilder().create();
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.create(ASSET_NAME1, root);

        assertTrue(jh.serialiseAsset(root));
        assertTrue(jh.serialiseAsset(asset1));

        Reader reader = new FileReader(new File(ROOT_ASSET_FILE));
        Asset desRoot = gson.fromJson(reader, Asset.class);
        if(reader != null)
            reader.close();

        reader = new FileReader(new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + JsonHelper.ASSET_FILENAME));
        Asset desAsset1 = gson.fromJson(reader, Asset.class);
        if(reader != null)
            reader.close();

        assertTrue(areIdenticalAssets(root, desRoot));
        assertTrue(areIdenticalAssets(asset1, desAsset1));
    }

    @Test
    public void serialiseAsset_rootAssetNotExists() throws IOException {
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.create(ASSET_NAME1, root);

        // root asset is not serialised
        // should not serialise
        assertFalse(jh.serialiseAsset(asset1));

        File asset1File = new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + JsonHelper.ASSET_FILENAME);

        // the asset file should not exist
        assertFalse(asset1File.exists());
    }

    @Test
    public void serialiseDetails_detailsSerialisedToLocalStorage() throws IOException {
        Gson gson = customiseGsonBuilder().create();
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.create(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        details.add(TextDetail.create(asset1.getId(), TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1));
        details.add(TextDetail.create(asset1.getId(), TEXTDETAIL_LABEL2, TEXTDETAIL_FIELD2));

        assertTrue(jh.serialiseAsset(root));
        assertTrue(jh.serialiseAsset(asset1));
        assertTrue(jh.serialiseDetails(details));

        Reader reader = new FileReader(new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + JsonHelper.DETAILS_FILENAME));
        Detail[] desDetails = gson.fromJson(reader, Detail[].class);
        if(reader != null)
            reader.close();

        assertTrue(areIdenticalDetails(details, Lists.newArrayList(desDetails)));
    }

    @Test
    public void serialiseDetails_detailsWithDifferentAssetIds() throws IOException {
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.create(ASSET_NAME1, root);
        Asset asset2 = Asset.create(ASSET_NAME2, root);

        // detail1 and detail2 have different asset ids
        List<Detail> details = new ArrayList<>();
        details.add(TextDetail.create(asset1.getId(), TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1));
        details.add(TextDetail.create(asset2.getId(), TEXTDETAIL_LABEL2, TEXTDETAIL_FIELD2));

        assertTrue(jh.serialiseAsset(root));
        assertTrue(jh.serialiseAsset(asset1));

        // should not serialise when details belong to different assets are in the list
        assertFalse(jh.serialiseDetails(details));

        File detailsFile = new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + JsonHelper.DETAILS_FILENAME);

        // details file should exist yet empty
        assertTrue(detailsFile.exists());
        assertTrue(detailsFile.length() == 0);
    }

    @Test
    public void serialiseDetails_emptyDetailsList() {
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.create(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();

        assertTrue(jh.serialiseAsset(root));
        assertTrue(jh.serialiseAsset(asset1));

        // if details is empty, should not serialise and return false
        assertFalse(jh.serialiseDetails(details));

        File detailsFile = new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + JsonHelper.DETAILS_FILENAME);

        // details file should exist yet empty
        assertTrue(detailsFile.exists());
        assertTrue(detailsFile.length() == 0);
    }

    @Test
    public void rootExists_rootExists() {
        prepareRootAsset();
        assertTrue(jh.rootExists());
    }

    @Test
    public void rootExists_rootNotExists() {
        assertFalse(jh.rootExists());
    }

    //endregion

    //region Private stuff

    private static final String ASSET_NAME1 = "Asset_1";
    private static final String ASSET_NAME2 = "Asset_2";
    private static final String ASSET_NAME3 = "Asset_3";
    private static final String TEXTDETAIL_LABEL1 = "TextDetail_1";
    private static final String TEXTDETAIL_FIELD1 = "TextDetail_Field_1";
    private static final String TEXTDETAIL_LABEL2 = "TextDetail_2";
    private static final String TEXTDETAIL_FIELD2 = "TextDetail_Field_2";
    private static final String TEST_USER_DIR = "testdata";
    private static final String ROOT_ASSET_FILE = TEST_USER_DIR + File.separator +
            JsonHelper.ROOT_ASSET_DIR + File.separator + JsonHelper.ASSET_FILENAME;

    private IJsonHelper jh;

    private File userDir;

    private Asset prepareRootAsset() {
        Asset root = Asset.createRoot();
        jh.serialiseAsset(root);
        return root;
    }

    /**
     * The GsonBuilder should be customised according to the one in {@link JsonHelper}.
     *
     * @return
     */
    private GsonBuilder customiseGsonBuilder() {
        GsonBuilder gb = new GsonBuilder();
        gb.serializeNulls();
        gb.setPrettyPrinting();
        gb.registerTypeAdapter(Detail.class, new JsonDetailAdapter());
        return gb;
    }

    //endregion
}
