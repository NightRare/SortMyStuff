package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import nz.ac.aut.comp705.sortmystuff.ui.detail.DetailActivity;

/**
 * Created by Yuan on 2017/4/28.
 */

public class ContentsActivity extends AppCompatActivity implements IContentsView {

    private IContentsPresenter presenter;

    // UI Components
    private AlertDialog.Builder addDialogBuilder;
    private FloatingActionButton fab;
    ListView index;
    Toolbar toolbar;
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
        IContentsPresenter p = new ContentsPresenter(dm, this);
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
    public void showRootAssetList() {
        presenter.setCurrentAssetIdToRoot();

    }

    @Override
    public void showContainerAsset() {

    }

    @Override
    public void showAssetList(Asset container) {
        ArrayAdapter<Asset> arrayAdapter = new ArrayAdapter<Asset>(
                this, android.R.layout.simple_list_item_1,
                presenter.loadContents());
        index.setAdapter(arrayAdapter);
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
        if (addDialogBuilder == null)
            initDialogBuilder();

        //create an input area
        final EditText input = new EditText(this);
        addDialogBuilder.setView(input);

        //creates the Save button and what happens when clicked
        addDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // get user input and add input as asset
                presenter.addAsset(input.getText().toString());
                //show a success message
                showMessageOnScreen("Successfully added " + input.getText().toString());
            }
        });

        addDialogBuilder.create().show();
    }

    @Override
    public void showMessageOnScreen(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_ASSET_ID, presenter.getCurrentAssetId());
        super.onSaveInstanceState(outState);
    }

    private void initDialogBuilder() {
        addDialogBuilder = new AlertDialog.Builder(this);
        addDialogBuilder.setTitle("Add Asset");

        //creates the Cancel button and what happens when clicked
        addDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_index_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //action button stuff
        if (id == R.id.action_settings && !presenter.isRootCurrentAsset()) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("AssetID", presenter.getCurrentAssetId());
            startActivity(intent);
            return true;
        } else {
            Toast.makeText(this, "Root has no detail",Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
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
