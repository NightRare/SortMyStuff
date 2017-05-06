package nz.ac.aut.comp705.sortmystuff.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.Detail;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;

public class DetailActivity extends AppCompatActivity implements IDetailView{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        details = (ListView) findViewById(R.id.detail_list);

        // Create the presenter
        dataManager = ((SortMyStuffApp) getApplication()).getFactory().getDataManager();
        presenter = new DetailPresenter(dataManager, this, this);
        setPresenter(presenter);
        presenter.start();

        addDetilButton = (FloatingActionButton) findViewById(R.id.fab);
        addDetilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { presenter.showDialogBox(view);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param presenter the presenter
     */
    @Override
    public void setPresenter(IDetailPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * {@inheritDoc}
     *
     * @param detailList
     */
    @Override
    public void showDetails(List<Detail> detailList){
        details.setAdapter(new DetailAdapter(this, android.R.layout.two_line_list_item, detailList));
    }

    /**
     * {@inheritDoc}
     *
     * @param message
     */
    @Override
    public void showMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG);
    }


    //*****PRIVATE STUFF*****//
    private IDataManager dataManager;
    private IDetailPresenter presenter;
    private ListView details;
    private FloatingActionButton addDetilButton;

    /**
     * Inner class to create an Array Adapter
     * according to the format required for the detail list
     */
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
