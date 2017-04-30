package nz.ac.aut.comp705.sortmystuff.ui.detail;

import android.view.View;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.ui.IView;

/**
 * Created by DonnaCello on 30 Apr 2017.
 */

public interface IDetailView extends IView<IDetailPresenter> {
    void setPresenter(IDetailPresenter presenter);
    void showDetails(List<Detail> detailList);
    void showAddDetailDialog(View view);
    void showMessage(String message);
}
