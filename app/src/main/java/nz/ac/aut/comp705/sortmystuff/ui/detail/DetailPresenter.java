package nz.ac.aut.comp705.sortmystuff.ui.detail;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.util.Log;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public class DetailPresenter implements IDetailPresenter {

    IDataManager dm;
    IDetailView view;
    String currentAsset;

    public DetailPresenter(IDataManager dm, IDetailView view) {
        this.dm = dm;
        this.view = view;
    }
    @Override
    public void start() {
        addBasicDetail();
    }

    @Override
    public void setCurrentAsset(String assetID) {
        currentAsset = assetID;
    }

    @Override
    public String getCurrentAssetID() {
        return currentAsset;
    }

    @Override
    public String getCurrentAssetName() {
        final String[] name = new String[1];
        dm.getAssetAsync(getCurrentAssetID(), new IDataManager.GetAssetCallback() {
            @Override
            public void onAssetLoaded(Asset asset) {
                name[0] = asset.getName();
            }

            @Override
            public void dataNotAvailable(int errorCode) {
                Log.e("Data not found ","Error: "+errorCode);
            }
        });
        return name[0];
    }

    @Override
    public List<Detail> loadDetails() {
        final ArrayList detailList = new ArrayList();
        dm.getDetailsAsync(getCurrentAssetID(), new IDataManager.LoadDetailsCallback() {
            @Override
            public void onDetailsLoaded(List<Detail> details) {
                if(details.isEmpty()){
                    addBasicDetail();
                }
                for(Detail d: details){
                    detailList.add(d);
                }
            }

            @Override
            public void dataNotAvailable(int errorCode) {
                addBasicDetail();

            }
        });
        return  detailList;
    }

    @Override
    public void addDetail(String label, String field) {
        dm.createTextDetail(getCurrentAssetID(), label, field);
    }

    @Override
    public void addBasicDetail() {
        addDetail("Name",getCurrentAssetName());
    }


}
