package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;

public class SearchActivity extends AppCompatActivity implements ISearchView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_act);
        setTitle("Search");

        //setup the search toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);

        startPresenter();

        //initialise the input field and message to search (hint)
        final EditText searchField = (EditText) findViewById(R.id.search_text_bar);
        searchField.setHint("Input keyword here");

        //setup the search button
        Button searchButton = (Button) findViewById(R.id.search_now_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.loadResult(searchField.getText().toString());
            }
        });

        //initialise search result list
        result = (ListView) findViewById(R.id.result_list);
        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Asset asset = (Asset) parent.getItemAtPosition(position);
                presenter.goToDetailPage(asset.getId());
            }
        });

    }

    // **** PRIVATE STUFF **** //

    private ISearchPresenter presenter;
    private ListView result;

    @Override
    public void showResultList(List<Asset> resultList) {
        result.setAdapter(new ArrayAdapter<Asset>(this,
                android.R.layout.simple_list_item_1, resultList));
    }

    /**
     * Registers the presenter to this View.
     *
     * @param presenter the presenter
     */
    @Override
    public void setPresenter(ISearchPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Starts the presenter for this activity
     */
    private void startPresenter(){
        IDataManager dataManager = ((SortMyStuffApp) getApplication()).getFactory().getDataManager();
        presenter = new SearchPresenter(dataManager, this, this);
        setPresenter(presenter);
        presenter.start();
    }
}
