package nz.ac.aut.comp705.sortmystuff.data;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;
import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Yuan on 2017/4/29.
 */


public class DataManagerTest {

    private static final String ROOT_ASSET_NAME = "Root";
    private static final String ASSET_NAME1 = "Asset_1";
    private static final String TEXTDETAIL_LABEL1 = "TextDetail_1";
    private static final String TEXTDETAIL_FIELD1 = "TextDetail_Field_1";

    private IDataManager dataManager;

    private List<Asset> mockAssets;

    private List<Detail> mockDetails;

    @Mock
    private IJsonHelper mockJsonHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        dataManager = new DataManager(mockJsonHelper);
        mockAssets = new LinkedList<>();
        mockDetails = new LinkedList<>();

        // mocking the behaviours of jsonHelper
        when(mockJsonHelper.serialiseAsset(any(Asset.class))).thenReturn(true);
        when(mockJsonHelper.serialiseDetails(any(List.class))).thenReturn(true);
        when(mockJsonHelper.deserialiseAllAssets()).thenReturn(mockAssets);
        when(mockJsonHelper.deserialiseDetails(anyString())).thenReturn(mockDetails);
    }

    @After
    public void tearDown() {
        dataManager = null;
    }

    @Test
    public void createRootAsset_rootAssetCreatedAndSaveToJsonFile() {
        final String rootId = dataManager.createRootAsset();
        verify(mockJsonHelper).serialiseAsset(argThat(new ArgumentMatcher<Asset>() {
            @Override
            public boolean matches(Asset argument) {
                return areIdenticalAssets(argument, rootId, ROOT_ASSET_NAME, null, null, null);
            }
        }));
    }

    @Test
    public void createRootAsset_ifRootAssetAlreadyExists() {
        // "create" a root asset
        prepareRootAsset();

        // if a root asset exists, serialiseAsset should not be called
        verify(mockJsonHelper, never()).serialiseAsset(any(Asset.class));
    }

    @Test
    public void createAsset_assetCreatedAndSaveToJsonFile() {
        // prepare a root asset as container
        final Asset root = prepareRootAsset();

        final String assetId = dataManager.createAsset(ASSET_NAME1, root.getId());
        verify(mockJsonHelper).serialiseAsset(argThat(new ArgumentMatcher<Asset>() {
            @Override
            public boolean matches(Asset argument) {
                return areIdenticalAssets(argument, assetId, ASSET_NAME1, root.getId(), null, null);
            }
        }));
    }

    @Test
    public void createAsset_containerAssetNotExists() {
        prepareRootAsset();
        dataManager.createAsset(ASSET_NAME1, "notAnId");

        // if a the container does not exist, serialiseAsset should not be called
        verify(mockJsonHelper, never()).serialiseAsset(any(Asset.class));
    }

    @Test
    public void createAsset_assetNameTooLong() {
        final Asset root = prepareRootAsset();
        try {
            String longName = "";
            for(int i = 0; i < AppConstraints.ASSET_NAME_CAP + 1; i ++) {
                longName += "c";
            }

            //if the length of name exceeds app constraints
            dataManager.createAsset(longName, root.getId());
        } catch (IllegalArgumentException e) {
            // pass test
            return;
        }
        Assert.fail();
    }

    @Test
    public void createTextDetail_textDetailCreatedAndSaveToJsonFile() {
        // prepare an asset
        Asset root = prepareRootAsset();
        final Asset asset = Asset.create(ASSET_NAME1, root);
        mockAssets.add(asset);

        final String detailId = dataManager.createTextDetail(asset, TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        verify(mockJsonHelper, times(1)).serialiseDetails(argThat(new ArgumentMatcher<List<Detail>>() {
            @Override
            public boolean matches(List<Detail> argument) {
                if(argument.size() != 1)
                    return false;
                return areIdenticalDetails(argument.get(0), detailId, asset.getId(), DetailType.Text,
                        TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
            }
        }));
    }

    @Test
    public void creatTextDetail_assetNotExists() {
        // prepare the root but no asset
        Asset root = prepareRootAsset();

        final String detailId = dataManager.createTextDetail(
                "noSuchAssetId", TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        verify(mockJsonHelper, never()).serialiseDetails(any(List.class));
        Assert.assertTrue(detailId == null);
    }

    @Test
    public void createTextDetail_emptyLabel() {
        // prepare an asset
        Asset root = prepareRootAsset();
        final Asset asset = Asset.create(ASSET_NAME1, root);
        mockAssets.add(asset);

        try {
            // empty label
            dataManager.createTextDetail(asset, "", TEXTDETAIL_FIELD1);
        } catch (IllegalArgumentException e) {
            // pass test
            return;
        }
        Assert.fail();
    }

    @Test
    public void createTextDetail_labelTooLong() {
        // prepare an asset
        Asset root = prepareRootAsset();
        final Asset asset = Asset.create(ASSET_NAME1, root);
        mockAssets.add(asset);

        try {
            String longLabel = "";
            for(int i = 0; i < AppConstraints.ASSET_NAME_CAP + 1; i ++) {
                longLabel += "c";
            }
            dataManager.createTextDetail(asset, longLabel, TEXTDETAIL_FIELD1);
        } catch (IllegalArgumentException e) {
            // pass test
            return;
        }
        Assert.fail();
    }

    @Test
    public void getRootAsset_getRootAsset() {
        Asset root = prepareRootAsset();
        Asset asset = dataManager.getRootAsset();
        Assert.assertTrue(root.equals(asset));
    }

    @Test
    public void getRootAsset_rootAssetNotExists() {
        Asset asset = dataManager.getRootAsset();
        Assert.assertTrue(asset == null);
    }

    @Test
    public void getRootAsset_loadFromLocalFileAtFirstTime() {
        prepareRootAsset();
        dataManager.getRootAsset();
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // getRootAsset() will not load from local file the second time
        dataManager.getRootAsset();
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    private Asset prepareRootAsset() {
        Asset root = Asset.createRoot();
        mockAssets.add(root);
        when(mockJsonHelper.rootExists()).thenReturn(true);
        return root;
    }

    private boolean areIdenticalAssets(Asset asset1, Asset asset2) {
        return areIdenticalAssets(asset1, asset2.getId(), asset2.getName(), asset2.getContainerId(),
                asset2.getCreateTimestamp(), asset2.getModifyTimestamp());
    }

    private boolean areIdenticalAssets(Asset asset, String id, String name,
                                       String containerId, Long createTS, Long modifyTS) {
        if(id != null) {
            if(!asset.getId().equals(id))
                return false;
        }

        if(name != null) {
            if(!asset.getName().equals(name))
                return false;
        }

        if (containerId != null) {
            if(!asset.getContainerId().equals(containerId))
                return false;
        }

        if(createTS != null) {
            if(!asset.getCreateTimestamp().equals(createTS))
                return false;
        }

        if(modifyTS != null) {
            if(!asset.getModifyTimestamp().equals(modifyTS))
                return false;
        }

        return true;
    }

    private boolean areIdenticalDetails(Detail detail1, Detail detail2) {
        return areIdenticalDetails(detail1, detail2.getId(), detail2.getAssetId(),
                detail2.getType(), detail2.getLabel(), detail2.getField());
    }

    private boolean areIdenticalDetails(Detail detail, String id, String assetId,
                                        DetailType type, String label, Object field) {
        if(id != null) {
            if(!detail.getId().equals(id))
                return false;
        }

        if(assetId != null) {
            if(!detail.getAssetId().equals(assetId))
                return false;
        }

        if(type != null) {
            if(!detail.getType().equals(type))
                return false;
        }

        if(label != null) {
            if(!detail.getLabel().equals(label))
                return false;
        }

        if(field != null) {
            if(!detail.getField().equals(field))
                return false;
        }

        return true;
    }
}
