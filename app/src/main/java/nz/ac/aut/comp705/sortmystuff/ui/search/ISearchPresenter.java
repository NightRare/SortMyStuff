package nz.ac.aut.comp705.sortmystuff.ui.search;

import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

/**
 * Created by DonnaCello on 23 May 2017.
 */

public interface ISearchPresenter extends IPresenter {

    /**
     * Load the result of the query into the activity
     * @param query the query
     */
    void loadResult(String query);
}
