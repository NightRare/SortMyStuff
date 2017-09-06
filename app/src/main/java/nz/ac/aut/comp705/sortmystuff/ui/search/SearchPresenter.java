package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.swipe.SwipeActivity;
import nz.ac.aut.comp705.sortmystuff.util.AppCode;

/**
 * Created by Donna on 23 May 2017.
 */

public class SearchPresenter implements ISearchPresenter{

    /**
     * When the corresponding activity is on created, this method will be invoked.
     */
    @Override
    public void start() {
        assetList = getAllAssets();
    }

    /**
     * Load the result of the query into the activity
     * @param query
     */
    @Override
    public void loadResult(String query){
        if(query.replaceAll(" ", "").isEmpty())
            view.showResultList(new ArrayList<Asset>());
        else
            view.showResultList(search(query));
    }

    /**
     * Setup the event to go to detail page
     * @param assetId
     */
    @Override
    public void goToDetailPage(String assetId) {
        Intent goToDetail = new Intent(activity, SwipeActivity.class);
        goToDetail.putExtra(AppCode.INTENT_ASSET_ID, assetId);
        activity.startActivity(goToDetail);
    }


    // ***** PRIVATE STUFF ***** //

    private IDataManager dm;
    private ISearchView view;
    private SearchActivity activity;
    private List<Asset> assetList;

    /**
     * Lists all the assets ready for querying
     * @return list of all assets
     */
    private List<Asset> getAllAssets(){
        final ArrayList<Asset> queryList = new ArrayList<Asset>();
        dm.getAllAssetsAsync(new IDataManager.LoadAssetsCallback() {
            @Override
            public void onAssetsLoaded(List<Asset> assets) {
                for(Asset asset : assets){queryList.add(asset);}
            }

            @Override
            public void dataNotAvailable(int errorCode) {}
        });
        return queryList;
    }

    /**
     * Search through the assets and find assets being queried
     * @param query
     * @return list of assets corresponding to the keyword query
     */
    private List<Asset> search(String query){
        String regex = "(?i).*"+query+".*";
        final ArrayList<Asset> resultList = new ArrayList<Asset>();
        for(Asset asset: assetList){
            if(!asset.isRoot() && asset.getName().matches(regex)){
                resultList.add(asset);
            }
        }
        if(resultList.isEmpty()){ showMessage("No results found.");}
        return resultList;
    }

    /**
     * Show a message in the UI
     * @param message
     */
    private void showMessage(String message){
        Toast msg = Toast.makeText(activity,message,Toast.LENGTH_LONG);
        msg.setGravity(Gravity.CENTER, 0, 0);
        msg.show();
    }

    public SearchPresenter(IDataManager dm, ISearchView view, SearchActivity activity){
        this.dm = dm;
        this.view = view;
        this.activity = activity;
    }
}
