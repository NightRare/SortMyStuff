package nz.ac.aut.comp705.sortmystuff.data;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;
import nz.ac.aut.comp705.sortmystuff.util.AppConstraints;
import nz.ac.aut.comp705.sortmystuff.util.AppStatusCode;
import nz.ac.aut.comp705.sortmystuff.util.exceptions.UpdateLocalStorageFailedException;

import static nz.ac.aut.comp705.sortmystuff.util.AppStatusCode.ASSET_NOT_EXISTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private static final String ASSET_NAME2 = "Asset_2";
    private static final String ASSET_NAME3 = "Asset_3";
    private static final String TEXTDETAIL_LABEL1 = "TextDetail_1";
    private static final String TEXTDETAIL_FIELD1 = "TextDetail_Field_1";

    private IDataManager dataManager;

    private List<Asset> mockAssets;

    private List<Detail> mockDetails;

    @Mock
    private IJsonHelper mockJsonHelper;

    @Mock
    private IDataManager.LoadAssetsCallback mockLoadAssetsCallback;

    @Mock
    private IDataManager.GetAssetCallback mockGetAssetCallback;

    @Mock
    private IDataManager.LoadDetailsCallback mockLoadDetailsCallback;

    @Mock
    private IDataManager.GetDetailCallback mockGetDetailsCallback;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        dataManager = new DataManager(mockJsonHelper);
        mockAssets = new LinkedList<>();
        mockDetails = new LinkedList<>();

        // mocking the behaviours of jsonHelper
        when(mockJsonHelper.serialiseAsset(any(Asset.class))).thenReturn(true);
        when(mockJsonHelper.serialiseDetails(ArgumentMatchers.<Detail>anyList())).thenReturn(true);
        when(mockJsonHelper.deserialiseAllAssets()).thenReturn(mockAssets);
        when(mockJsonHelper.deserialiseDetails(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                if(mockDetails.isEmpty())
                    return false;
                return argument.equals(mockDetails.get(0).getAssetId());
            }
        }))).thenReturn(mockDetails);
    }

    @After
    public void tearDown() {
        dataManager = null;
        mockAssets = null;
        mockDetails = null;
        mockJsonHelper = null;
        mockLoadAssetsCallback = null;
        mockGetAssetCallback = null;
        mockLoadDetailsCallback = null;
        mockGetDetailsCallback = null;
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
    public void createRootAsset_updateLocalStorageFailed() {
        // mocking serialise failed
        when(mockJsonHelper.serialiseAsset(any(Asset.class))).thenReturn(false);

        try {
            dataManager.createRootAsset();
        } catch (UpdateLocalStorageFailedException e) {
            // pass test
            return;
        }
        Assert.fail();
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
            for (int i = 0; i < AppConstraints.ASSET_NAME_CAP + 1; i++) {
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
    public void createAsset_updateLocalStorageFailed() {
        // mocking serialise failed
        when(mockJsonHelper.serialiseAsset(any(Asset.class))).thenReturn(false);

        try {
            // prepare a root asset as container
            final Asset root = prepareRootAsset();
            dataManager.createAsset(ASSET_NAME1, root.getId());
        } catch (UpdateLocalStorageFailedException e) {
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
                if (argument.size() != 1)
                    return false;
                return areIdenticalDetails(argument.get(0), detailId, asset.getId(), DetailType.Text,
                        TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
            }
        }));
    }

    @Test
    public void createTextDetail_assetNotExists() {
        // prepare the root but no asset
        Asset root = prepareRootAsset();

        final String detailId = dataManager.createTextDetail(
                "noSuchAssetId", TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        verify(mockJsonHelper, never()).serialiseDetails(ArgumentMatchers.<Detail>anyList());
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
            for (int i = 0; i < AppConstraints.ASSET_NAME_CAP + 1; i++) {
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
    public void createTextDetail_updateLocalStorageFailed() {
        // mocking serialise failed
        when(mockJsonHelper.serialiseAsset(any(Asset.class))).thenReturn(false);

        try {
            // prepare an asset
            Asset root = prepareRootAsset();
            final Asset asset = Asset.create(ASSET_NAME1, root);
            mockAssets.add(asset);

            dataManager.createTextDetail(asset, TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        } catch (UpdateLocalStorageFailedException e) {
            // pass test
            return;
        }
        Assert.fail();
    }

    @Test
    public void createTextDetail_createTextDetailForRootAsset() {
        // prepare the root but no asset
        Asset root = prepareRootAsset();

        final String detailId = dataManager.createTextDetail(
                root, TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        verify(mockJsonHelper, never()).serialiseDetails(ArgumentMatchers.<Detail>anyList());
        Assert.assertTrue(detailId == null);
    }

    @Test
    public void getRootAsset_getRootAsset() {
        Asset root = prepareRootAsset();
        Asset asset = dataManager.getRootAsset();
        Assert.assertTrue(areIdenticalAssets(root, asset));
    }

    @Test
    public void getRootAsset_rootAssetNotExists() {
        Asset asset = dataManager.getRootAsset();
        Assert.assertTrue(asset == null);
    }

    @Test
    public void getRootAsset_loadFromLocalFileAtFirstTime() {
        prepareRootAsset();

        verify(mockJsonHelper, never()).deserialiseAllAssets();
        // first time load from local file
        dataManager.getRootAsset();
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // second time do not load from local file
        dataManager.getRootAsset();
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    @Test
    public void getAllAssetsAsync_getAllAssets() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        final Asset asset2 = Asset.create(ASSET_NAME2, root);
        final Asset asset3 = Asset.create(ASSET_NAME3, asset2);
        mockAssets.add(asset1);
        mockAssets.add(asset2);
        mockAssets.add(asset3);

        dataManager.getAllAssetsAsync(mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback).onAssetsLoaded(argThat(new ArgumentMatcher<List<Asset>>() {
            @Override
            public boolean matches(List<Asset> argument) {
                return areIdenticalAssets(mockAssets, argument);
            }
        }));
        verify(mockLoadAssetsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getAllAssetsAsync_rootAssetNotExists() {
        // did not prepare root asset
        dataManager.getAllAssetsAsync(mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback, never()).onAssetsLoaded(ArgumentMatchers.<Asset>anyList());
        verify(mockLoadAssetsCallback).dataNotAvailable(
                ArgumentMatchers.eq(AppStatusCode.NO_ROOT_ASSET));
    }

    @Test
    public void getAllAssetsAsync_loadFromLocalFileAtFirstTime() {
        prepareRootAsset();

        verify(mockJsonHelper, never()).deserialiseAllAssets();
        // first time load from local file
        dataManager.getAllAssetsAsync(mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // second time do not load from local file
        dataManager.getAllAssetsAsync(mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    @Test
    public void getRecycledAssetsAsync_getRecycledAssets() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        final Asset asset2 = Asset.create(ASSET_NAME2, root);
        final Asset asset3 = Asset.create(ASSET_NAME3, asset2);

        //recycling
        asset1.recycle();
        asset2.recycle();
        asset3.recycle();

        mockAssets.add(asset1);
        mockAssets.add(asset2);
        mockAssets.add(asset3);

        dataManager.getRecycledAssetsAsync(mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback).onAssetsLoaded(argThat(new ArgumentMatcher<List<Asset>>() {
            @Override
            public boolean matches(List<Asset> argument) {
                for (Asset a : argument) {
                    if (!a.isRecycled())
                        return false;
                }
                // argument does not include Root asset
                mockAssets.remove(root);
                return areIdenticalAssets(mockAssets, argument);
            }
        }));
        verify(mockLoadAssetsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getRecycledAssetsAsync_rootAssetNotExists() {
        // did not prepare root asset
        dataManager.getRecycledAssetsAsync(mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback, never()).onAssetsLoaded(ArgumentMatchers.<Asset>anyList());
        verify(mockLoadAssetsCallback).dataNotAvailable(
                ArgumentMatchers.eq(AppStatusCode.NO_ROOT_ASSET));
    }

    @Test
    public void getRecycledAssetsAsync_noRecycledAssets() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        final Asset asset2 = Asset.create(ASSET_NAME2, root);
        final Asset asset3 = Asset.create(ASSET_NAME3, asset2);
        mockAssets.add(asset1);
        mockAssets.add(asset2);
        mockAssets.add(asset3);

        // no asset is not recycled
        dataManager.getRecycledAssetsAsync(mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback).onAssetsLoaded(argThat(new ArgumentMatcher<List<Asset>>() {
            @Override
            public boolean matches(List<Asset> argument) {
                return argument.size() == 0;
            }
        }));
        verify(mockLoadAssetsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getRecycledAssetsAsync_loadFromLocalFileAtFirstTime() {
        prepareRootAsset();

        verify(mockJsonHelper, never()).deserialiseAllAssets();
        // first time load from local file
        dataManager.getRecycledAssetsAsync(mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // second time do not load from local file
        dataManager.getRecycledAssetsAsync(mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    @Test
    public void getContentAssetsAsync_getContentAssets() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        final Asset asset2 = Asset.create(ASSET_NAME2, root);
        final Asset asset3 = Asset.create(ASSET_NAME3, asset2);
        mockAssets.add(asset1);
        mockAssets.add(asset2);
        mockAssets.add(asset3);

        // no asset is not recycled
        dataManager.getContentAssetsAsync(root, mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback).onAssetsLoaded(argThat(new ArgumentMatcher<List<Asset>>() {
            @Override
            public boolean matches(List<Asset> argument) {
                mockAssets.remove(root);
                // asset3 is contained by asset2
                mockAssets.remove(asset3);
                return areIdenticalAssets(mockAssets, argument);
            }
        }));
        verify(mockLoadAssetsCallback, never()).dataNotAvailable(anyInt());
    }


    @Test
    public void getContentAssetsAsync_containerIdNotExists() {
        final Asset root = prepareRootAsset();

        // no asset is not recycled
        dataManager.getContentAssetsAsync("noSuchId", mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback, never()).onAssetsLoaded(ArgumentMatchers.<Asset>anyList());
        verify(mockLoadAssetsCallback).dataNotAvailable(
                ArgumentMatchers.eq(ASSET_NOT_EXISTS));
    }

    @Test
    public void getContentAssetsAsync_noContents() {
        final Asset root = prepareRootAsset();

        // no asset is not recycled
        dataManager.getContentAssetsAsync(root, mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback).onAssetsLoaded(argThat(new ArgumentMatcher<List<Asset>>() {
            @Override
            public boolean matches(List<Asset> argument) {
                return argument.isEmpty();
            }
        }));
        verify(mockLoadAssetsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getContentAssetsAsync_loadFromLocalFileAtFirstTime() {
        final Asset root = prepareRootAsset();

        verify(mockJsonHelper, never()).deserialiseAllAssets();
        // first time load from local file
        dataManager.getContentAssetsAsync(root, mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // second time do not load from local file
        dataManager.getContentAssetsAsync(root, mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    @Test
    public void getParentAssetsAsync_getParentAssets() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        final Asset asset2 = Asset.create(ASSET_NAME2, asset1);
        final Asset asset3 = Asset.create(ASSET_NAME3, asset2);
        mockAssets.add(asset1);
        mockAssets.add(asset2);
        mockAssets.add(asset3);

        dataManager.getParentAssetsAsync(asset3, mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback).onAssetsLoaded(argThat(new ArgumentMatcher<List<Asset>>() {
            @Override
            public boolean matches(List<Asset> argument) {
                if (argument.size() != 3)
                    return false;
                // order should be
                // asset2 -> asset1 -> root
                return areIdenticalAssets(asset2, argument.get(0)) &&
                        areIdenticalAssets(asset1, argument.get(1)) &&
                        areIdenticalAssets(root, argument.get(2));
            }
        }));
        verify(mockLoadAssetsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getParentAssetsAsync_assetIdNotExists() {
        final Asset root = prepareRootAsset();

        dataManager.getParentAssetsAsync("NoSuchAssetId", mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback, never()).onAssetsLoaded(ArgumentMatchers.<Asset>anyList());
        verify(mockLoadAssetsCallback).dataNotAvailable(ArgumentMatchers.eq(ASSET_NOT_EXISTS));
    }

    @Test
    public void getParentAssetsAsync_getParentOfRoot() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        final Asset asset2 = Asset.create(ASSET_NAME2, asset1);
        final Asset asset3 = Asset.create(ASSET_NAME3, asset2);
        mockAssets.add(asset1);
        mockAssets.add(asset2);
        mockAssets.add(asset3);

        dataManager.getParentAssetsAsync(root, mockLoadAssetsCallback);
        verify(mockLoadAssetsCallback).onAssetsLoaded(argThat(new ArgumentMatcher<List<Asset>>() {
            @Override
            public boolean matches(List<Asset> argument) {
                return argument.isEmpty();
            }
        }));
        verify(mockLoadAssetsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getParentAssetsAsync_loadFromLocalFileAtFirstTime() {
        final Asset root = prepareRootAsset();

        verify(mockJsonHelper, never()).deserialiseAllAssets();
        // first time load from local file
        dataManager.getParentAssetsAsync(root, mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // second time do not load from local file
        dataManager.getParentAssetsAsync(root, mockLoadAssetsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    @Test
    public void getAssetAsync_getAsset() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        mockAssets.add(asset1);

        // test get asset1 by id
        dataManager.getAssetAsync(asset1.getId(), mockGetAssetCallback);
        verify(mockGetAssetCallback).onAssetLoaded(argThat(new ArgumentMatcher<Asset>() {
            @Override
            public boolean matches(Asset argument) {
                return areIdenticalAssets(asset1, argument);
            }
        }));
        verify(mockGetAssetCallback, never()).dataNotAvailable(anyInt());

        // test get Root asset by id
        dataManager.getAssetAsync(root.getId(), mockGetAssetCallback);
        verify(mockGetAssetCallback).onAssetLoaded(argThat(new ArgumentMatcher<Asset>() {
            @Override
            public boolean matches(Asset argument) {
                return areIdenticalAssets(root, argument);
            }
        }));
        verify(mockGetAssetCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getAssetAsync_assetNotExists() {
        prepareRootAsset();

        dataManager.getAssetAsync("NoSuchAssetId", mockGetAssetCallback);
        verify(mockGetAssetCallback, never()).onAssetLoaded(any(Asset.class));
        verify(mockGetAssetCallback).dataNotAvailable(ArgumentMatchers.eq(ASSET_NOT_EXISTS));
    }

    @Test
    public void getAssetAsync_loadFromLocalFileAtFirstTime() {
        final Asset root = prepareRootAsset();

        verify(mockJsonHelper, never()).deserialiseAllAssets();
        // first time load from local file
        dataManager.getAssetAsync(root.getId(), mockGetAssetCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // second time do not load from local file
        dataManager.getAssetAsync(root.getId(), mockGetAssetCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    @Test
    public void getDetailsAsync_getDetails() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        mockAssets.add(asset1);
        TextDetail td1 = TextDetail.create(
                asset1.getId(), TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        mockDetails.add(td1);

        dataManager.getDetailsAsync(asset1, mockLoadDetailsCallback);
        verify(mockLoadDetailsCallback).onDetailsLoaded(argThat(new ArgumentMatcher<List<Detail>>() {
            @Override
            public boolean matches(List<Detail> argument) {
                return areIdenticalDetails(mockDetails, argument);
            }
        }));
        verify(mockLoadDetailsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getDetailsAsync_getDetailsOfRootAsset() {
        final Asset root = prepareRootAsset();

        TextDetail td1 = TextDetail.create(
                root.getId(), TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        mockDetails.add(td1);

        dataManager.getDetailsAsync(root, mockLoadDetailsCallback);
        // should always return empty detail list since Root asset cannot have details
        verify(mockLoadDetailsCallback).onDetailsLoaded(argThat(new ArgumentMatcher<List<Detail>>() {
            @Override
            public boolean matches(List<Detail> argument) {
                return argument.isEmpty();
            }
        }));
        verify(mockLoadDetailsCallback, never()).dataNotAvailable(anyInt());
    }

    @Test
    public void getDetailsAsync_assetNotExists() {
        prepareRootAsset();

        TextDetail td1 = TextDetail.create(
                "NoSuchAssetId", TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        mockDetails.add(td1);

        dataManager.getDetailsAsync("NoSuchAssetId", mockLoadDetailsCallback);
        verify(mockLoadDetailsCallback, never()).onDetailsLoaded(ArgumentMatchers.<Detail>anyList());
        verify(mockLoadDetailsCallback).dataNotAvailable(ArgumentMatchers.eq(ASSET_NOT_EXISTS));
    }

    @Test
    public void getDetailsAsync_loadFromLocalFileAtFirstTime() {
        final Asset root = prepareRootAsset();
        final Asset asset1 = Asset.create(ASSET_NAME1, root);
        mockAssets.add(asset1);
        TextDetail td1 = TextDetail.create(
                asset1.getId(), TEXTDETAIL_LABEL1, TEXTDETAIL_FIELD1);
        mockDetails.add(td1);

        verify(mockJsonHelper, never()).deserialiseAllAssets();
        // first time load from local file
        dataManager.getDetailsAsync(asset1, mockLoadDetailsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();

        // second time do not load from local file
        dataManager.getDetailsAsync(asset1, mockLoadDetailsCallback);
        verify(mockJsonHelper, times(1)).deserialiseAllAssets();
    }

    @Test
    public void getDetailAsync() {
        // TODO getDetailAsync not implemented yet
    }

    //********************************************
    // PRIVATE
    //********************************************

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
        if (id != null) {
            if (!asset.getId().equals(id))
                return false;
        }

        if (name != null) {
            if (!asset.getName().equals(name))
                return false;
        }

        if (containerId != null) {
            if (!asset.getContainerId().equals(containerId))
                return false;
        }

        if (createTS != null) {
            if (!asset.getCreateTimestamp().equals(createTS))
                return false;
        }

        if (modifyTS != null) {
            if (!asset.getModifyTimestamp().equals(modifyTS))
                return false;
        }

        return true;
    }

    private boolean areIdenticalAssets(List<Asset> list1, List<Asset> list2) {
        if (list1.size() != list2.size())
            return false;

        for (Asset a1 : list1) {
            boolean included = false;
            for (Asset a2 : list2) {
                if (areIdenticalAssets(a1, a2))
                    included = true;
            }
            // if a1 is not included in a2
            if (!included)
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
        if (id != null) {
            if (!detail.getId().equals(id))
                return false;
        }

        if (assetId != null) {
            if (!detail.getAssetId().equals(assetId))
                return false;
        }

        if (type != null) {
            if (!detail.getType().equals(type))
                return false;
        }

        if (label != null) {
            if (!detail.getLabel().equals(label))
                return false;
        }

        if (field != null) {
            if (!detail.getField().equals(field))
                return false;
        }

        return true;
    }

    private boolean areIdenticalDetails(List<Detail> list1, List<Detail> list2) {
        if (list1.size() != list2.size())
            return false;

        for (Detail d1 : list1) {
            boolean included = false;
            for (Detail d2 : list2) {
                if (areIdenticalDetails(d1, d2))
                    included = true;
            }
            // if a1 is not included in a2
            if (!included)
                return false;
        }
        return true;
    }
}
