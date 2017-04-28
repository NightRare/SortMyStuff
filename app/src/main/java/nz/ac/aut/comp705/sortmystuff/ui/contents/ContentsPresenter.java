//package nz.ac.aut.comp705.sortmystuff.ui.contents;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import nz.ac.aut.comp705.sortmystuff.R;
//import nz.ac.aut.comp705.sortmystuff.data.Asset;
//import nz.ac.aut.comp705.sortmystuff.data.Detail;
//import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
//
///**
// * Created by Yuan on 2017/4/27.
// */
//
//public class ContentsPresenter implements IContentsPresenter {
//
//    private IContentsView view;
//
//    private IDataManager dm;
//
//
//    public ContentsPresenter(IDataManager dm, IContentsView view) {
//        this.dm = dm;
//        this.view = view;
//    }
//
//    @Override
//    public void start() {
//        getRoot();
//    }
//
//    public String getRoot(){
//        final String[] root = new String[1];
//        dm.getRootAssetAsync(new IDataManager.GetAssetCallback() {
//            @Override
//            public void onAssetLoaded(Asset asset) {
//                root[0] = asset.getId();
//            }
//            @Override
//            public void dataNotAvailable(int errorCode) {
//                root[0] = dm.createRootAsset();
//                createDefaultAssets(root[0]);
//            }
//        });
//        return root[0];
//    }
//
//    @Override
//    public void loadAssetList(final String assetID) {
//    }
//
//    public ArrayList loadContents(String assetID){
//        final ArrayList assetList = new ArrayList();
//
//        dm.getContentAssetsAsync(assetID, new IDataManager.LoadAssetsCallback() {
//
//            @Override
//            public void onAssetsLoaded(List<Asset> assets) {
//                for(Asset a: assets){assetList.add(a);}
//            }
//
//
//            @Override
//            public void dataNotAvailable(int errorCode) {
//                Log.e("Does not have assets","Error " + errorCode);
//            }
//        });
//        return assetList;
//    }
//
//    private void createDefaultAssets(String assetID){
//        dm.createAsset("Kitchen",assetID);
//        dm.createAsset("Bedroom",assetID);
//    }
//
//
//
//    public ArrayList getRootContents(){
//
//        return loadContents(getRoot());
//    }
//
//
//
//
//    public void addAsset(String assetName, String containerID) {
//        dm.createAsset(assetName,containerID);
//    }
//
//
//
//
//}
