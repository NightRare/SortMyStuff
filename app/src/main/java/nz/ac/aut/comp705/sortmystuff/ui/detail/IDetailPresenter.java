package nz.ac.aut.comp705.sortmystuff.ui.detail;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public interface IDetailPresenter extends IPresenter {

    void start();

    void setCurrentAsset(String assetID);

    String getCurrentAssetID();

    String getCurrentAssetName();

    List<Detail> loadDetails();

    void addDetail(String label, String field);

    void addBasicDetail();

}
