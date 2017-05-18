package nz.ac.aut.comp705.sortmystuff.ui.details;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public interface IDetailsView extends IView<IDetailsPresenter> {

    /**
     * Set the presenter as given
     * @param presenter the presenter
     */
    void setPresenter(IDetailsPresenter presenter);

    /**
     * Show list of details
     * @param detailList
     */
    void showDetails(List<Detail> detailList);

    /**
     * Show message on screen
     * @param message
     */
    void showMessage(String message);
}
