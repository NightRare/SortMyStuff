package nz.ac.aut.comp705.sortmystuff.ui.detail;

import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public interface IDetailView extends IView<IDetailPresenter> {
    void setPresenter(IDetailPresenter presenter);
}
