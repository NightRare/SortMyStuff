package nz.ac.aut.comp705.sortmystuff.ui.search;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

public interface ISearchView extends IView<ISearchPresenter> {

    /**
     * Shows a list of search results.
     * @param resultList the list of assets to be showed
     */
    void showResultList(List<IAsset> resultList);

    void turnToAssetPage(String assetId);

    void showMessage(String message);

    void showSearchError(Throwable exception);

    void setLoadingIndicator(boolean active);
}
