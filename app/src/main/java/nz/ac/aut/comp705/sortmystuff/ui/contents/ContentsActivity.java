package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;

public class ContentsActivity extends AppCompatActivity implements IContentsView{

    private IContentsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.index_list);

        IDataManager dm = ((SortMyStuffApp)getApplication()).getFactory().getDataManager();
        IContentsPresenter p = new ContentsPresenter(dm, this);
        setPresenter(p);


    }

    @Override
    public void setPresenter(IContentsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showAssetList() {

    }
}
