package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;

public class SearchActivity extends AppCompatActivity implements ISearchView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_act);
        setTitle(null);

        //setup the search toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        presenter = new SearchPresenter(((SortMyStuffApp) getApplication())
                .getFactory().getDataManager(), this, this);
        setPresenter(presenter);
        presenter.start();

        //initialise search resultListView list
        resultListView = (ListView) findViewById(R.id.result_list);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Asset clickedAsset = (Asset) parent.getItemAtPosition(position);
                presenter.goToAssetPage(clickedAsset.getId());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchView searchView = (SearchView) menu.findItem(R.id.search_btn).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setQueryHint("Search all assets");
        searchView.setIconified(false);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.loadResult(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.loadResult(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // **** PRIVATE STUFF **** //

    private ISearchPresenter presenter;
    private ListView resultListView;

    @Override
    public void showResultList(List<Asset> resultList) {
        SearchListAdapter adapter = new SearchListAdapter(resultList, getApplicationContext());
        resultListView.setAdapter(adapter);
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
}
