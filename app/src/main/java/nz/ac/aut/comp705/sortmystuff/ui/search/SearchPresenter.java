package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.details.DetailsActivity;

/**
 * Created by DonnaCello on 23 May 2017.
 */

public class SearchPresenter implements ISearchPresenter{

    /**
     * When the corresponding activity is on created, this method will be invoked.
     */
    @Override
    public void start() {
        assetList = getAllAssets();
    }

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

    private List<Asset> search(String query){
        String regex = "(?i).*"+query+".*";
        final ArrayList<Asset> resultList = new ArrayList<Asset>();
        for(Asset asset: assetList){
            if(!asset.isRoot() && asset.getName().matches(regex)){
                resultList.add(asset);
            }
        }
        if(resultList.isEmpty()){ showMessage("No results found.");}
        if(query =="" || query.length()==0){
            resultList.clear();
            showMessage("Please enter a valid keyword.");
        }
        return resultList;
    }

    @Override
    public void loadResult(String query){
        view.showResultList(search(query));
    }

    @Override
    public void goToDetailPage(String assetId) {
        Intent goToDetail = new Intent(activity, DetailsActivity.class);
        goToDetail.putExtra("AssetID", assetId);
        activity.startActivity(goToDetail);

    }

    private void showMessage(String message){
        Toast msg = Toast.makeText(activity,message,Toast.LENGTH_LONG);
        msg.setGravity(Gravity.CENTER, 0, 0);
        msg.show();
    }


    //*****PRIVATE STUFF*****//
    private IDataManager dm;
    private ISearchView view;
    private SearchActivity activity;
    private List<Asset> assetList;

    public SearchPresenter(IDataManager dm, ISearchView view, SearchActivity activity){
        this.dm = dm;
        this.view = view;
        this.activity = activity;
    }
}
