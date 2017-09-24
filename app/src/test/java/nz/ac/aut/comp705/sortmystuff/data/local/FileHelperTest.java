package nz.ac.aut.comp705.sortmystuff.data.local;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.BuildConfig;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.data.models.Category;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.testutils.TestUtil;
import nz.ac.aut.comp705.sortmystuff.utils.JsonDetailAdapter;

import static nz.ac.aut.comp705.sortmystuff.testutils.TestUtil.areIdenticalAssets;
import static nz.ac.aut.comp705.sortmystuff.testutils.TestUtil.areIdenticalDetails;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Yuan on 2017/5/5.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class FileHelperTest {


    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        // setup default photo
        FileInputStream fis = new FileInputStream(TestUtil.TEST_DEFAULT_PHOTO);
        Bitmap defaultPhoto = BitmapFactory.decodeStream(fis);
        fis.close();
        when(mockResLoader.getDefaultPhoto()).thenReturn(defaultPhoto);

        userDir = new File(TEST_USER_DIR);
        fh = new FileHelper(mockResLoader, userDir, new GsonBuilder());
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.cleanDirectory(userDir);
        userDir = null;
        fh = null;
        mockResLoader = null;
    }

    //region Tests
    @Test
    public void deserialiseAsset_assetDeserialisedFromLocalStorage() throws IOException {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);
        fh.serialiseAsset(asset1);

        Asset desAsset = fh.deserialiseAsset(asset1.getId());
        assertTrue(areIdenticalAssets(asset1, desAsset));
    }

    @Test
    public void deserialiseAsset_rootAssetNotExists() {
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, Asset.createRoot());
        fh.serialiseAsset(asset1);

        Asset desAsset = fh.deserialiseAsset(asset1.getId());
        assertNull(desAsset);
    }

    @Test
    public void deserialiseAsset_assetNotExists() {
        Asset desAsset = fh.deserialiseAsset("NoSuchAsset");
        assertNull(desAsset);
    }

    @Test
    public void deserialiseRootAsset_rootAssetDeserialisedFromLocalStorage() {
        Asset root = prepareRootAsset();

        Asset desRoot = fh.deserialiseRootAsset();
        assertTrue(areIdenticalAssets(root, desRoot));
    }

    @Test
    public void deserialiseRootAsset_rootAssetNotExists() {
        Asset desRoot = fh.deserialiseRootAsset();
        assertNull(desRoot);
    }

    @Test
    public void deserialiseAllAssets_allAssetsDeserialisedFromLocalStorage() {
        Asset root = prepareRootAsset();
        List<Asset> assets = new ArrayList<>();
        assets.add(Asset.createAsMisc(ASSET_NAME1, root));
        assets.add(Asset.createAsMisc(ASSET_NAME2, root));
        assets.add(Asset.createAsMisc(ASSET_NAME3, assets.get(0)));

        for (Asset a : assets) {
            fh.serialiseAsset(a);
        }

        List<Asset> desAssets = fh.deserialiseAllAssets();

        //remove root Asset so that can be compared with assets
        assertTrue(desAssets.remove(root));

        assertTrue(areIdenticalAssets(assets, desAssets));
    }

    @Test
    public void deserialiseAllAssets_rootAssetNotExists() {
        // root asset not serialised
        Asset root = Asset.createRoot();
        Asset[] assets = new Asset[3];
        assets[0] = Asset.createAsMisc(ASSET_NAME1, root);
        assets[1] = Asset.createAsMisc(ASSET_NAME2, root);
        assets[2] = Asset.createAsMisc(ASSET_NAME3, assets[0]);

        for (Asset a : assets) {
            fh.serialiseAsset(a);
        }

        List<Asset> desAssets = fh.deserialiseAllAssets();
        assertEquals(null, desAssets);
    }

    @Test
    public void deserialiseAllAssets_onlyHasRootAsset() {
        Asset root = prepareRootAsset();
        fh.serialiseAsset(root);

        List<Asset> desAssets = fh.deserialiseAllAssets();
        assertTrue(desAssets.size() == 1);
        assertTrue(areIdenticalAssets(desAssets.get(0), root));
    }

    @Test
    public void deserialiseDetails_assetNotExists() {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        Detail detail1 = TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL1, TEXTDETAIL_FIELD1);
        Detail detail2 = TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL2, TEXTDETAIL_FIELD2);
        Collections.addAll(details, detail1, detail2);

        // asset1 is not serialised
        fh.serialiseDetails(details, false);

        List<Detail> desDetails = fh.deserialiseDetails(asset1.getId());
        assertEquals(null, desDetails);
    }

    @Test
    public void deserialiseDetails_deserialiseTextDetail() {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        Detail detail1 = TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL1, TEXTDETAIL_FIELD1);
        Detail detail2 = TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL2, TEXTDETAIL_FIELD2);
        Collections.addAll(details, detail1, detail2);

        fh.serialiseAsset(asset1);
        fh.serialiseDetails(details, false);

        List<Detail> desDetails = fh.deserialiseDetails(asset1.getId());
        assertTrue(areIdenticalDetails(details, desDetails));
    }

    @Test
    public void deserialiseDetails_deserialiseDateDetail() {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        Detail detail1 = TextDetail.createDateDetail(asset1.getId(), DETAIL_LABEL1, TEXTDETAIL_FIELD1);
        Detail detail2 = TextDetail.createDateDetail(asset1.getId(), DETAIL_LABEL2, TEXTDETAIL_FIELD2);
        Collections.addAll(details, detail1, detail2);

        fh.serialiseAsset(asset1);
        fh.serialiseDetails(details, false);

        List<Detail> desDetails = fh.deserialiseDetails(asset1.getId());
        assertTrue(areIdenticalDetails(details, desDetails));
    }

    @Test
    public void deserialiseDetails_deserialiseImageDetailWithDefaultPhoto() {
        Asset root = prepareRootAsset();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        Detail detail1 = ImageDetail.create(asset1.getId(), DETAIL_LABEL1, mockResLoader.getDefaultPhoto());
        details.add(detail1);

        fh.serialiseAsset(asset1);
        fh.serialiseDetails(details, true);

        List<Detail> desDetails = fh.deserialiseDetails(asset1.getId());
        assertTrue(areIdenticalDetails(desDetails.get(0), detail1.getId(), detail1.getAssetId(),
                DetailType.Image, detail1.getLabel(), null));
        Bitmap actualPhoto = (Bitmap) detail1.getField();
        assertTrue(mockResLoader.getDefaultPhoto().sameAs(actualPhoto));
    }

    @Test
    public void deserialiseDetails_deserialiseImageDetailWithCustomisedPhoto() throws IOException {
        FileInputStream fis = new FileInputStream(TestUtil.TEST_IMAGE_1);
        Bitmap customisedPhoto = BitmapFactory.decodeStream(fis);
        fis.close();

        Asset root = prepareRootAsset();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        Detail detail1 = ImageDetail.create(asset1.getId(), DETAIL_LABEL1, customisedPhoto);
        details.add(detail1);

        fh.serialiseAsset(asset1);
        fh.serialiseDetails(details, true);

        List<Detail> desDetails = fh.deserialiseDetails(asset1.getId());
        assertTrue(areIdenticalDetails(desDetails.get(0), detail1.getId(), detail1.getAssetId(),
                DetailType.Image, detail1.getLabel(), null));
        Bitmap actualPhoto = (Bitmap) detail1.getField();
        assertTrue(customisedPhoto.sameAs(actualPhoto));
    }

    @Test
    public void serialiseAsset_assetSerialisedToLocalStorage() throws IOException {
        Gson gson = customiseGsonBuilder().create();
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        assertTrue(fh.serialiseAsset(root));
        assertTrue(fh.serialiseAsset(asset1));

        Reader reader = new FileReader(new File(ROOT_ASSET_FILE));
        Asset desRoot = gson.fromJson(reader, Asset.class);
        if(reader != null)
            reader.close();

        reader = new FileReader(new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + FileHelper.ASSET_FILENAME));
        Asset desAsset1 = gson.fromJson(reader, Asset.class);
        if(reader != null)
            reader.close();

        assertTrue(areIdenticalAssets(root, desRoot));
        assertTrue(areIdenticalAssets(asset1, desAsset1));
    }

    @Test
    public void serialiseAsset_rootAssetNotExists() throws IOException {
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        // root asset is not serialised
        // should not serialise
        assertFalse(fh.serialiseAsset(asset1));

        File asset1File = new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + FileHelper.ASSET_FILENAME);

        // the asset file should not exist
        assertFalse(asset1File.exists());
    }

    @Test
    public void serialiseDetails_detailsSerialisedToLocalStorage() throws IOException {
        Gson gson = customiseGsonBuilder().create();
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();
        details.add(TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL1, TEXTDETAIL_FIELD1));
        details.add(TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL2, TEXTDETAIL_FIELD2));

        assertTrue(fh.serialiseAsset(root));
        assertTrue(fh.serialiseAsset(asset1));
        assertTrue(fh.serialiseDetails(details, false));

        Reader reader = new FileReader(new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + FileHelper.DETAILS_FILENAME));
        Detail[] desDetails = gson.fromJson(reader, Detail[].class);
        if(reader != null)
            reader.close();

        assertTrue(areIdenticalDetails(details, Lists.newArrayList(desDetails)));
    }

    @Test
    public void serialiseDetails_detailsWithDifferentAssetIds() throws IOException {
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);
        Asset asset2 = Asset.createAsMisc(ASSET_NAME2, root);

        // detail1 and detail2 have different asset ids
        List<Detail> details = new ArrayList<>();
        details.add(TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL1, TEXTDETAIL_FIELD1));
        details.add(TextDetail.createTextDetail(asset2.getId(), DETAIL_LABEL2, TEXTDETAIL_FIELD2));

        assertTrue(fh.serialiseAsset(root));
        assertTrue(fh.serialiseAsset(asset1));

        // should not serialise when details belong to different assets are in the list
        assertFalse(fh.serialiseDetails(details, false));

        File detailsFile = new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + FileHelper.DETAILS_FILENAME);

        // details file should exist yet empty
        assertTrue(detailsFile.exists());
        assertTrue(detailsFile.length() == 0);
    }

    @Test
    public void serialiseDetails_emptyDetailsList() {
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        List<Detail> details = new ArrayList<>();

        assertTrue(fh.serialiseAsset(root));
        assertTrue(fh.serialiseAsset(asset1));

        // if details is empty, should not serialise and return false
        assertFalse(fh.serialiseDetails(details, false));

        File detailsFile = new File(TEST_USER_DIR + File.separator
                + asset1.getId() + File.separator + FileHelper.DETAILS_FILENAME);

        // details file should exist yet empty
        assertTrue(detailsFile.exists());
        assertTrue(detailsFile.length() == 0);
    }

    @Test
    public void serialiseDetails_imageUpdated() throws IOException {
        Asset root = Asset.createRoot();
        Asset asset1 = Asset.createAsMisc(ASSET_NAME1, root);

        FileInputStream fis = new FileInputStream(TestUtil.TEST_IMAGE_1);
        Bitmap image = BitmapFactory.decodeStream(fis);
        fis.close();

        ImageDetail imageDetail = ImageDetail.create(asset1.getId(), IMAGEDETAIL_LABEL1, image);

        List<Detail> details = new ArrayList<>();
        details.add(TextDetail.createTextDetail(asset1.getId(), DETAIL_LABEL1, TEXTDETAIL_FIELD1));
        details.add(imageDetail);

        assertTrue(fh.serialiseAsset(root));
        assertTrue(fh.serialiseAsset(asset1));
        assertTrue(fh.serialiseDetails(details, true));

        File file = new File(TEST_USER_DIR + File.separator + asset1.getId() +
                File.separator + imageDetail.getId() + LocalResourceLoader.IMAGE_DETAIL_FORMAT);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

//        fis = new FileInputStream(TEST_USER_DIR + File.separator
//                + asset1.getId() + File.separator + imageDetail.getImageFileName());
//        Bitmap actualImage = BitmapFactory.decodeStream(fis);
//        assertTrue(image.sameAs(actualImage));
    }

    @Test
    public void rootExists_rootExists() {
        prepareRootAsset();
        assertTrue(fh.rootExists());
    }

    @Test
    public void rootExists_rootNotExists() {
        assertFalse(fh.rootExists());
    }

    @Test
    public void deserialiseCategories_categoriesDeserialised() {
        String categoryStrings = "[\n" +
                "  {\n" +
                "    \"name\": \"Collectibles\",\n" +
                "    \"details\": [\n" +
                "      {\n" +
                "        \"assetId\": \"DUMMY_ASSET_ID\",\n" +
                "        \"id\": \"BASIC_DUMMY_DETAIL_ID_1\",\n" +
                "        \"label\": \"Photo\",\n" +
                "        \"type\": \"Image\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"field\": \"\",\n" +
                "        \"assetId\": \"DUMMY_ASSET_ID\",\n" +
                "        \"id\": \"BASIC_DUMMY_DETAIL_ID_2\",\n" +
                "        \"label\": \"Notes\",\n" +
                "        \"type\": \"Text\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"field\": \"\",\n" +
                "        \"assetId\": \"DUMMY_ASSET_ID\",\n" +
                "        \"id\": \"EXTENDED_DUMMY_DETAIL_ID_1\",\n" +
                "        \"label\": \"Purchase Date\",\n" +
                "        \"type\": \"Date\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        when(mockResLoader.getCategoriesJson()).thenReturn(categoryStrings);
        List<Category> categories = fh.deserialiseCategories();
        assertEquals(1, categories.size());
        verify(mockResLoader, times(1)).getCategoriesJson();
        verify(mockResLoader, times(1)).getDefaultPhoto();

        Category collectibles = categories.get(0);
        assertEquals("Collectibles", collectibles.getName());

        List<Detail> details = collectibles.getDetails();
        for(int i = 0; i < details.size(); i++) {
            switch (i) {
                case 0:
                    assertTrue(TestUtil.areIdenticalDetails(details.get(i), "BASIC_DUMMY_DETAIL_ID_1",
                            "DUMMY_ASSET_ID", DetailType.Image, "Photo", null));
                    Bitmap actualPhoto = (Bitmap) details.get(0).getField();
                    assertTrue(mockResLoader.getDefaultPhoto().sameAs(actualPhoto));
                    break;

                case 1:
                    assertTrue(TestUtil.areIdenticalDetails(details.get(i), "BASIC_DUMMY_DETAIL_ID_2",
                            "DUMMY_ASSET_ID", DetailType.Text, "Notes", ""));
                    break;

                case 2:
                    assertTrue(TestUtil.areIdenticalDetails(details.get(i), "EXTENDED_DUMMY_DETAIL_ID_1",
                            "DUMMY_ASSET_ID", DetailType.Date, "Purchase Date", ""));
                    break;
            }
        }
    }

    //endregion

    //region Private stuff

    private static final String ASSET_NAME1 = "Asset_1";
    private static final String ASSET_NAME2 = "Asset_2";
    private static final String ASSET_NAME3 = "Asset_3";
    private static final String DETAIL_LABEL1 = "TextDetail_1";
    private static final String TEXTDETAIL_FIELD1 = "TextDetail_Field_1";
    private static final String DETAIL_LABEL2 = "TextDetail_2";
    private static final String TEXTDETAIL_FIELD2 = "TextDetail_Field_2";
    private static final String IMAGEDETAIL_LABEL1 = "ImageDetail_1";

    private static final String TEST_USER_DIR = "testdata";
    private static final String ROOT_ASSET_FILE = TEST_USER_DIR + File.separator +
            FileHelper.ROOT_ASSET_DIR + File.separator + FileHelper.ASSET_FILENAME;

    private IFileHelper fh;

    private File userDir;

    @Mock
    private LocalResourceLoader mockResLoader;

    private Asset prepareRootAsset() {
        Asset root = Asset.createRoot();
        fh.serialiseAsset(root);
        return root;
    }

    /**
     * The GsonBuilder should be customised according to the one in {@link FileHelper}.
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
