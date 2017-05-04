package nz.ac.aut.comp705.sortmystuff.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

public class DetailActivity extends AppCompatActivity implements IDetailView{

    IDetailPresenter presenter;
    String currentAsset;
    ListView details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_act);

        // Create the presenter
        IDataManager dm = ((SortMyStuffApp) getApplication()).getFactory().getDataManager();
        IDetailPresenter p = new DetailPresenter(dm, this);
        setPresenter(p);

        Intent intent = getIntent();
        p.setCurrentAsset(intent.getStringExtra("AssetID"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(p.getCurrentAssetName());

        details = (ListView) findViewById(R.id.detail_list);
        showDetails(p.loadDetails());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDetailDialog(view);
            }
        });
    }

    @Override
    public void setPresenter(IDetailPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showDetails(List<Detail> detailList){
        details.setAdapter(new DetailAdapter(this, android.R.layout.two_line_list_item, detailList));
    }

    @Override
    public void showAddDetailDialog(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
        dialog.setTitle("Add Detail for "+presenter.getCurrentAssetName());

        Context context = view.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText labelText = new EditText(context);
        labelText.setHint("Label");
        layout.addView(labelText);

        final EditText fieldText = new EditText(context);
        fieldText.setHint("Field");
        layout.addView(fieldText);

        dialog.setView(layout).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.addDetail(labelText.getText().toString(),fieldText.getText().toString());
                showDetails(presenter.loadDetails());
                showMessage("Added " + labelText.getText().toString() + "detail");
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.create().show();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG);
    }


    private class DetailAdapter extends ArrayAdapter<Detail> {

        private Context context;
        private int layoutResourceId;
        private List<Detail> detailList = null;

        private DetailAdapter(Context context, int layoutResourceId, List<Detail> detailList) {
            super(context, layoutResourceId, detailList);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.detailList = detailList;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                v = inflater.inflate(layoutResourceId, parent, false);
            }

            Detail item = detailList.get(position);
            TextView label = (TextView) v.findViewById(android.R.id.text1);
            label.setText(item.getLabel());
            TextView field = (TextView) v.findViewById(android.R.id.text2);
            field.setText((String)item.getField());
            return v;
        }
    }

}
