package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

/**
 * Created by Yuan on 2017/4/28.
 */

public class ContentsActivity extends AppCompatActivity implements IContentsView {

    private IContentsPresenter presenter;

    // UI Components
    private FloatingActionButton fab;
    private ListView index;
    private Toolbar toolbar;
    private TextView pathBarRoot;
    private RecyclerView pathBarPath;

    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_view);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        // clicking on name on toolbar

        // register Floating ActionButton
        fab = (FloatingActionButton) findViewById(R.id.addAssetButton);

        // list view
        index = (ListView) findViewById(R.id.index_list);
        // clicking on an asset in the list

        pathBarRoot = (TextView) findViewById(R.id.pathbar_root);
        pathBarPath = (RecyclerView) findViewById(R.id.pathbar_pathview);

        // register all the listeners
        registerListeners();

        // Create the presenter
        IDataManager dm = ((SortMyStuffApp) getApplication()).getFactory().getDataManager();
        IContentsPresenter p = new ContentsPresenter(dm, this, this);
        setPresenter(p);

        // Load previously saved state, if available
        if (savedInstanceState != null) {
            String currentAssetId = savedInstanceState.getString(CURRENT_ASSET_ID);
            presenter.setCurrentAssetId(currentAssetId);
        }

        // start the presenter
        presenter.start();
    }

    @Override
    public void setPresenter(IContentsPresenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public void showAssetTitle(String name) {
        setTitle(name);
    }

    @Override
    public void showAssetContents(List<Asset> assets) {
        ArrayAdapter<Asset> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, assets);
        index.setAdapter(arrayAdapter);
    }

    @Override
    public void showAddDialog() {
        getAddAssetDialogBuilder().create().show();
    }

    @Override
    public void showMessageOnScreen(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG);
    }

    @Override
    public void showPath(List<Asset> assets) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_ASSET_ID, presenter.getCurrentAssetId());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_index_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(presenter.selectOptionItem(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Deprecated
    @Override
    public void showRootAssetList() {
        // deprecated methods
    }

    @Deprecated
    @Override
    public void showContainerAsset(Asset asset) {
        // deprecated methods
    }

    @Deprecated
    @Override
    public void showAssetList(String assetID) {
        // deprecated methods
    }

    private AlertDialog.Builder getAddAssetDialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Asset");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get user input and add input as asset
                presenter.addAsset(input.getText().toString());
                //show a success message
                showMessageOnScreen("Successfully added " + input.getText().toString());
            }
        });
        //creates the Cancel button and what happens when clicked
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        return builder;
    }

    private void registerListeners() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setCurrentAssetIdToContainer();
                presenter.loadCurrentContents(false);
            }
        });

        index.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //fetches the selected asset in the list
                Asset a = (Asset) parent.getItemAtPosition(position);
                //sets the selected asset's ID as the current asset (to be viewed)
                presenter.setCurrentAssetId(a.getId());
                presenter.loadCurrentContents(false);
            }
        });

        pathBarRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setCurrentAssetIdToRoot();
                presenter.loadCurrentContents(false);
            }
        });

    }
}
