package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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

public class ContentsActivity extends AppCompatActivity
        implements IContentsView, View.OnClickListener {

    //region Activity METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contents_act);

        // register Floating ActionButton
        fab = (FloatingActionButton) findViewById(R.id.addAssetButton);

        initView();

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        // clicking on name on toolbar


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
        switch (item.getItemId()) {
            case R.id.selection_mode_button:
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessage(message);
                break;
            default:
                break;

        }
        if (presenter.selectOptionItem(item))
            return true;
        return super.onOptionsItemSelected(item);
    }


    //endregion

    //region IContentsView METHODS

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
        adapter = new AssetListAdapter(assets, getApplicationContext(), false);
        showCheckbox = false;
        index.setAdapter(adapter);
        assetList = new ArrayList<>();
        assetList = assets;
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

    //region PRIVATE STUFF


    private IContentsPresenter presenter;

    // UI Components
    private FloatingActionButton fab;

    private Toolbar toolbar;
    private TextView pathBarRoot;
    private RecyclerView pathBar;

    private ListView index;
    private Button select_btn, selectAll_btn, selectNone_btn, cancel_btn;
    private AssetListAdapter adapter;
    private CheckBox checkBox;
    private int checkedCount;
    private static List<Asset> assetList;
    private Boolean showCheckbox = false;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1)
                enableEditMode();
            if (msg.what == 0)
                quitEditMode();
        }
    };

    private void initView() {

        View view = LayoutInflater.from(this).inflate(R.layout.assets_layout, null);
        checkBox = (CheckBox) view.findViewById(R.id.asset_checkbox);

        // list view for index
        index = (ListView) findViewById(R.id.index_list);

        cancel_btn = (Button) findViewById(R.id.cancel_button);
//        select_btn = (Button) findViewById(R.id.select_button);
        selectAll_btn = (Button) findViewById(R.id.select_all_button);
        selectNone_btn = (Button) findViewById(R.id.select_none_button);

        if (showCheckbox) {
            selectNone_btn.setVisibility(View.VISIBLE);
            selectAll_btn.setVisibility(View.VISIBLE);
            cancel_btn.setVisibility(View.VISIBLE);
        }
        else {
            selectNone_btn.setVisibility(View.GONE);
            selectAll_btn.setVisibility(View.GONE);
            cancel_btn.setVisibility(View.GONE);
        }

//        select_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
        selectAll_btn.setOnClickListener(this);
        selectNone_btn.setOnClickListener(this);

    }

    private void enableEditMode() {
        adapter = new AssetListAdapter(assetList, getApplicationContext(), true);
        index.setAdapter(adapter);
        showCheckbox = true;
        selectNone_btn.setVisibility(View.VISIBLE);
        selectAll_btn.setVisibility(View.VISIBLE);
        cancel_btn.setVisibility(View.VISIBLE);
//        select_btn.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);

    }

    private void quitEditMode() {
        adapter = new AssetListAdapter(assetList, getApplicationContext(), false);
        index.setAdapter(adapter);
        showCheckbox = false;
        selectNone_btn.setVisibility(View.GONE);
        selectAll_btn.setVisibility(View.GONE);
        cancel_btn.setVisibility(View.GONE);
//        select_btn.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
    }


    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";

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
                // show a success message
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

                if (showCheckbox) {
                    AssetListAdapter.ViewHolder holder = (AssetListAdapter.ViewHolder) view.getTag();
                    holder.checkbox.toggle();
                    AssetListAdapter.getSelectStatusMap().put(position, holder.checkbox.isChecked());
                }
                else {
                    //fetches the selected asset in the list
                    Asset a = (Asset) parent.getItemAtPosition(position);
                    //sets the selected asset's ID as the current asset (to be viewed)
                    presenter.setCurrentAssetId(a.getId());
                    presenter.loadCurrentContents(false);
                }

            }
        });

        index.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessage(message);
                return true;
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


    @Override
    public void onClick(View v) {
        int btn_id = v.getId();
        Message message = Message.obtain();

        switch (btn_id) {
            case R.id.cancel_button:
                message.what = 0;
                handler.sendMessage(message);
                break;

            case R.id.select_all_button:
                selectAll();
                Toast.makeText(this, assetList.size() + " items", Toast.LENGTH_SHORT).show();
                break;

            case R.id.select_none_button:
                selectNone();
                Toast.makeText(this, "0 items", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void selectAll() {
        for (int i = 0; i < assetList.size(); i++) {
            AssetListAdapter.getSelectStatusMap().put(i, true);
        }
        checkedCount = assetList.size();
        adapter.notifyDataSetChanged();
    }

    private void selectNone() {
        for (int i = 0; i < assetList.size(); i++) {
            AssetListAdapter.getSelectStatusMap().put(i, false);
        }
        checkedCount = assetList.size();
        adapter.notifyDataSetChanged();
    }

    //endregion
}


