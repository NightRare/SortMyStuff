package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import nz.ac.aut.comp705.sortmystuff.di.IFactory;
import nz.ac.aut.comp705.sortmystuff.ui.main.SwipeActivity;
import nz.ac.aut.comp705.sortmystuff.utils.AppStrings;

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

        //initialise search resultListView list
        mResultListView = (ListView) findViewById(R.id.result_list);
        mResultListView.setOnItemClickListener(mClickOnSearchResult);

        IFactory factory = ((SortMyStuffApp) getApplication()).getFactory();
        mPresenter = new SearchPresenter(factory.getDataManager(), this, factory.getSchedulerProvider());
        mPresenter.start();
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
        searchView.setOnQueryTextListener(mQueryTextListener);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
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

    @Override
    public void showResultList(List<IAsset> resultList) {
        SearchListAdapter adapter = new SearchListAdapter(resultList, getApplicationContext());
        mResultListView.setAdapter(adapter);
    }

    @Override
    public void turnToAssetPage(String assetId) {
        Intent goToAsset = new Intent(this, SwipeActivity.class);
        goToAsset.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        goToAsset.putExtra(AppStrings.INTENT_ASSET_ID, assetId);
        startActivity(goToAsset);
        finish();
    }

    @Override
    public void showMessage(String message) {
        Toast msg = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        msg.setGravity(Gravity.CENTER, 0, 0);
        msg.show();
    }

    @Override
    public void showSearchError(Throwable exception) {
        //TODO: to be implemented
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        //TODO: to be implemented
    }

    /**
     * Registers the presenter to this View.
     *
     * @param presenter the presenter
     */
    @Override
    public void setPresenter(ISearchPresenter presenter) {
        this.mPresenter = presenter;
    }

    //region PRIVATE STUFF

    //region LISTENERS

    AdapterView.OnItemClickListener mClickOnSearchResult = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            IAsset clickedAsset = (IAsset) parent.getItemAtPosition(position);
            turnToAssetPage(clickedAsset.getId());
        }
    };

    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            mPresenter.loadResult(query);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mPresenter.loadResult(newText);
            return true;
        }
    };

    //endregion

    private ISearchPresenter mPresenter;
    private ListView mResultListView;

    //endregion

}
