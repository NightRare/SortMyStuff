package nz.ac.aut.comp705.sortmystuff.ui.contents;

import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

/**
 * Created by Yuan on 2017/4/27.
 */

public class ContentsPresenter implements IContentsPresenter {

    private IContentsView view;

    private IDataManager dm;

    public ContentsPresenter(IDataManager dm, IContentsView view) {
        this.dm = dm;
        this.view = view;
    }

    @Override
    public void start() {

    }

    @Override
    public void loadAssetList() {

    }
}
