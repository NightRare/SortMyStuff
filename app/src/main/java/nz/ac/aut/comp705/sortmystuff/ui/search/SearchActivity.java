package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

        presenter = new SearchPresenter(((SortMyStuffApp) getApplication())
                .getFactory().getDataManager(), this, this);
        setPresenter(presenter);
        presenter.start();

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
                if(newText.replaceAll(" ", "").isEmpty())
                    return false;
                presenter.loadResult(newText);
                return true;
            }
        });

        return true;
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

    }
}
