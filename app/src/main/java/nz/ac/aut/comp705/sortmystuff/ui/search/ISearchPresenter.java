package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;
import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by DonnaCello on 23 May 2017.
 */

public interface ISearchPresenter extends IPresenter {

    /**
     * Load the result of the query into the activity
     * @param query
     */
    public void loadResult(String query);

    /**
     * Setup the event to go to detail page
     * @param assetId
     */
    public void goToDetailPage(String assetId);
}
