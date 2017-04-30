package nz.ac.aut.comp705.sortmystuff.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.local.IJsonHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Yuan on 2017/4/29.
 */


public class DataManagerTest {

    private static final String ROOT_ASSET_NAME = "Root";
    private static final String ASSET_NAME1 = "Asset_1";

    private IDataManager dataManager;

    @Mock
    private IJsonHelper mockJsonHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        dataManager = new DataManager(mockJsonHelper);

        // mocking the behaviours of jsonHelper
        when(mockJsonHelper.serialiseAsset(any(Asset.class))).thenReturn(true);
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
                if(argument.getName().equals(ROOT_ASSET_NAME) &&
                        argument.isRoot() &&
                        rootId.equals(argument.getId()))
                    return true;
                else
                    return false;
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
                if(argument.getName().equals(ASSET_NAME1) &&
                        !argument.isRoot() &&
                        assetId.equals(argument.getId()) &&
                        argument.getContainerId().equals(root.getId()))
                    return true;
                else
                    return false;
            }
        }));
    }

    @Test
    public void createAsset_containerAssetNotExists() {
        prepareRootAsset();
        final String assetId = dataManager.createAsset(ASSET_NAME1, "notAnId");

        // if a the container does not exist, serialiseAsset should not be called
        verify(mockJsonHelper, never()).serialiseAsset(any(Asset.class));
    }

    private Asset prepareRootAsset() {
        List<Asset> rootAsset = new LinkedList<>();
        Asset root = Asset.createRoot();
        rootAsset.add(root);
        when(mockJsonHelper.deserialiseAllAssets()).thenReturn(rootAsset);
        when(mockJsonHelper.rootExists()).thenReturn(true);
        return root;
    }
}
