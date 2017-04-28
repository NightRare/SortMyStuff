package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.Asset;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

/**
 * Created by Yuan on 2017/4/28.
 */

public class ContentsActivityAlter extends AppCompatActivity implements IContentsView {

    private IContentsPresenter presenter;

    // UI Components
    private AlertDialog.Builder addDialogBuilder;
    private FloatingActionButton fab;
    ListView index;

    private static final String CURRENT_ASSET_ID = "CURRENT_ASSET_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_view);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        // list view
        index = (ListView)findViewById(R.id.index_list);

        // register Floating ActionButton
        fab = (FloatingActionButton) findViewById(R.id.addAssetButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        // Create the presenter
        IDataManager dm = ((SortMyStuffApp)getApplication()).getFactory().getDataManager();
        IContentsPresenter p = new ContentsPresenterAlter(dm, this);
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
    public void showContainerAsset(Asset asset) {
        setTitle(asset.getName());
    }

    @Override
    public void showAssetList(String assetID) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,
                presenter.loadContents(presenter.getCurrentAssetId()));
        index.setAdapter(arrayAdapter);

    }

    @Override
    public void showAddDialog() {
        if(addDialogBuilder == null)
            initDialogBuilder();

        //create an input area
        final EditText input = new EditText(this);
        addDialogBuilder.setView(input);

        //creates the Save button and what happens when clicked
        addDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // get user input and add input as asset
                presenter.addAsset(input.getText().toString(), presenter.getCurrentAssetId());
                showAssetList(presenter.getCurrentAssetId());
            }
        });

        addDialogBuilder.create().show();
    }

    @Override
    public void showMessageOnScreen(View view, CharSequence msg, int length) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
        addDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {dialog.cancel();}
        });
    }
}
