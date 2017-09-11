package nz.ac.aut.comp705.sortmystuff.ui.search;

import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by DonnaCello on 23 May 2017.
 */

public interface ISearchPresenter extends IPresenter {

    /**
     * Load the result of the query into the activity
     * @param query
     */
    void loadResult(String query);

    /**
     * Set up the event to go to the swipe page of the asset whose id is as given.
     *
     * @param assetId the id of the asset
     */
    void goToAssetPage(String assetId);
}
