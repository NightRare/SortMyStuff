//package nz.ac.aut.comp705.sortmystuff;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.View;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ListView;
//
//import nz.ac.aut.comp705.sortmystuff.data.Asset;
//
//public class IndexView extends AppCompatActivity {
//
//    final Context context = this;
//    //set index as the existing index_list id in content_index_view
//    ListView index;
//    //creates a dummy Asset to start
//    Asset dummyRoot = Asset.loadDummyAssets();
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_index_view);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        index = (ListView)findViewById(R.id.index_list);
//
//        //creates an array adapter to load an array of asset names
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                this, android.R.layout.simple_selectable_list_item,
//                dummyRoot.getContentNames());
//        //links arrayAdapter to the index for viewing
//        index.setAdapter(arrayAdapter);
//
//        //the "fab" is the Add button
//       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addAssetButton);
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
//                        dummyRoot.addContent(inputName);
//                        //workaround on list not updating
//                        ArrayAdapter<String> updateOnAdd = new ArrayAdapter<String>(
//                                context, android.R.layout.simple_selectable_list_item,
//                                dummyRoot.getContentNames());
//                        index.setAdapter(updateOnAdd);
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
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_index_view, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//}
