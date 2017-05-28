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



    public void loadResult(String query);
}
