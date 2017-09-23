package nz.ac.aut.comp705.sortmystuff.ui.search;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by DonnaCello on 23 May 2017.
 */

public interface ISearchView extends IView<ISearchPresenter> {

    void showResultList(List<IAsset> resultList);

    void turnToAssetPage(String assetId);

    void showMessage(String message);

    void showSearchError(Throwable exception);

    void setLoadingIndicator(boolean active);
}
