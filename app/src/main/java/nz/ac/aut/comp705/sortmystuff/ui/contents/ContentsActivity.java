//package nz.ac.aut.comp705.sortmystuff.ui.contents;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ListView;
//
//import nz.ac.aut.comp705.sortmystuff.R;
//import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
//import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
//
//public class ContentsActivity extends AppCompatActivity implements IContentsView{
//
//    private IContentsPresenter presenter;
//    ListView index;
//    String currentAssetID;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_index_view);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
//        setSupportActionBar(toolbar);
//
//        index = (ListView)findViewById(R.id.index_list);
//        IDataManager dm = ((SortMyStuffApp)getApplication()).getFactory().getDataManager();
//        IContentsPresenter p = new ContentsPresenter(dm, this);
//        setPresenter(p);
//        p.start();
//        currentAssetID = p.getRoot();
//        setTitle("Home");
//        showAssetList(currentAssetID);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addAssetButton);
//        clickAddButton(fab,this);
//    }
//
//    private void clickAddButton(FloatingActionButton fab, final Context context){
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //create a dialog box
//                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//                dialogBuilder.setTitle("Add Asset");
//                //create an input area
//                final EditText input = new EditText(context);
//                dialogBuilder.setView(input);
//                //creates the Save button and what happens when clicked
//                dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog,int id) {
//                        // get user input and add input as asset
//                        String inputName = input.getText().toString();
//
//                        presenter.addAsset(inputName, currentAssetID);
//                        showAssetList(currentAssetID);
//                    }
//                });
//                //creates the Cancel button and what happens when clicked
//                dialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog,int id) {dialog.cancel();}
//                });
//                //show the created dialog box
//                AlertDialog dialog = dialogBuilder.create();
//                //shows the dialog box upon click of addAssetButton
//                dialog.show();
//            }
//        });
//    }
//
//    @Override
//    public void setPresenter(IContentsPresenter presenter) {
//        this.presenter = presenter;
//    }
//
//    @Override
//    public void showAssetList(String assetID) {
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                this, android.R.layout.simple_list_item_1,
//                presenter.loadContents(currentAssetID));
//        index.setAdapter(arrayAdapter);
//
//    }
//
//    @Override
//    public void showMessageOnScreen(View view, CharSequence msg, int length) {
//        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
//    }
//
//
//}
