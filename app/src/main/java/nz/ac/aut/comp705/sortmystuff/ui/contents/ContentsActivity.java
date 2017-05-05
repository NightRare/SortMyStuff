package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

/**
 * The Activity class for "Contents View" (a.k.a. Index Page) where the contained assets of the
 * container asset will be displayed and ready for interactions. It is also the implementation class
 * of {@link IContentsView}.
 *
 * @author Yuan
 */

public class ContentsActivity extends AppCompatActivity implements IContentsView {

    //region Activity methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contents_act);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        // clicking on name on toolbar

        // register Floating ActionButton
        fab = (FloatingActionButton) findViewById(R.id.addAssetButton);

        assetListView = (LinearLayout) findViewById(R.id.action_buttons);
        cancel_button = (Button) findViewById(R.id.cancel_button);

        // list view
        index = (ListView) findViewById(R.id.index_list);
        // clicking on an asset in the list

        initPathBar();

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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_ASSET_ID, presenter.getCurrentAssetId());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contents_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (presenter.selectOptionItem(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region IContentsView methods

    /**
     * {@inheritDoc}
     *
     * @param presenter the presenter
     */
    @Override
    public void setPresenter(IContentsPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * {@inheritDoc}
     *
     * @param name the name of the asset
     */
    @Override
    public void showAssetTitle(String name) {
        setTitle(name);
    }

    /**
     * {@inheritDoc}
     *
     * @param assets the assets
     */
    @Override
    public void showAssetContents(List<Asset> assets) {
        arrayAdapter = new AssetsAdapter(
                this, R.layout.assets_layout, assets);
        index.setAdapter(arrayAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAddDialog() {
        getAddAssetDialogBuilder().create().show();
    }

    /**
     * {@inheritDoc}
     *
     * @param message the message
     */
    @Override
    public void showMessageOnScreen(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

    /**
     * {@inheritDoc}
     *
     * @param assets the list of parent assets, excluding Root asset.
     */
    @Override
    public void showPath(List<Asset> assets) {
        PathBarAdapter pba = new PathBarAdapter(this, assets, presenter);
        pathBar.setAdapter(pba);
    }

    //endregion

    //region Private stuff

    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";

    private IContentsPresenter presenter;

    //region UI Components
    private FloatingActionButton fab;
    private ListView index;
    private Toolbar toolbar;
    private TextView pathBarRoot;
    private RecyclerView pathBar;
    private AssetsAdapter arrayAdapter;
    private LinearLayout assetListView;
    private Button cancel_button;
    //endregion

    /**
     * Gets the DialogBuilder of "add asset dialog".
     *
     * @return
     */
    private AlertDialog.Builder getAddAssetDialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Asset");

        final EditText input = new EditText(this);
        input.setSingleLine();
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

    /**
     * Initialises path bar.
     */
    private void initPathBar() {
        pathBarRoot = (TextView) findViewById(R.id.pathbar_root);
        pathBar = (RecyclerView) findViewById(R.id.pathbar_pathview);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        llm.setStackFromEnd(true);
        pathBar.setLayoutManager(llm);
    }

    /**
     * Registers the listeners to UI components.
     */
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

        index.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                presenter.enableEditMode(view);
                fab.setVisibility(View.GONE);
                assetListView.setVisibility(View.VISIBLE);

                return true;
            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assetListView.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
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

    //endregion
}


