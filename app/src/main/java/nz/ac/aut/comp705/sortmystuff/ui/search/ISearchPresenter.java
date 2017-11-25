package nz.ac.aut.comp705.sortmystuff.ui.search;

import nz.ac.aut.comp705.sortmystuff.ui.IPresenter;

public interface ISearchPresenter extends IPresenter {

    /**
     * Load the resultRaw of the query into the activity
     * @param query the query
     */
    void loadResult(String query);
}
