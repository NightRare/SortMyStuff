package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;

/**
 * Created by DonnaCello on 23 May 2017.
 */

public class SearchPresenter implements ISearchPresenter{

    final String MSG_NO_ASSET_FOUND = "No results found. Try Again";
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

    private List search(String query){
        String regex = "(?i).*"+query+".*";
        final ArrayList resultList = new ArrayList();
        for(Asset asset: assetList){
            if(!asset.isRoot() && asset.getName().matches(regex)){
                resultList.add(asset);
            }
        }
        if(resultList.isEmpty()){
            resultList.add(MSG_NO_ASSET_FOUND);
        }
        return resultList;
    }

    @Override
    public void loadResult(String query){
        view.showResultList(search(query));
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
