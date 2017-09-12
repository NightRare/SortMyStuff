package nz.ac.aut.comp705.sortmystuff.ui.details;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.ui.swipe.SwipeActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment implements IDetailsView{

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance() {
        DetailsFragment fragment = new DetailsFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        activity = (SwipeActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.details_frag, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        details = (ListView) rootView.findViewById(R.id.detail_list);

        details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Detail detail = (Detail) parent.getItemAtPosition(position);
                presenter.showDialogBox(view, detail);
            }
        });

        presenter.start();
    }

    @Override
    public void setPresenter(IDetailsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showDetails(List<Detail> detailList) {
        details.setAdapter(new DetailAdapter(activity, R.layout.details_two_lines_list, detailList));
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG);
    }

    //region PRIVATE STUFF

    private SwipeActivity activity;
    private IDetailsPresenter presenter;
    private ListView details;

    private View rootView;

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

        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                v = inflater.inflate(layoutResourceId, parent, false);
            }

            final Detail item = detailList.get(position);

            TextView labelView = (TextView) v.findViewById(android.R.id.text1);
            TextView textFieldView = (TextView) v.findViewById(android.R.id.text2);
            ImageView imageFieldView = (ImageView) v.findViewById(R.id.details_asset_image);
            labelView.setText(item.getLabel());

            if (item.getType().equals(DetailType.Date) || item.getType().equals(DetailType.Text)) {
                textFieldView.setText((String) item.getField());
                imageFieldView.setImageBitmap(null);

            } else if (item.getLabel().equals(CategoryType.BasicDetail.PHOTO)
                    && item.getType().equals(DetailType.Image)) {
                //The label "Photo" and its screen space is hidden
                imageFieldView.setImageBitmap((Bitmap) item.getField());
                textFieldView.setText(null);
                labelView.setText(null);

                //Set a click listener on asset image to launch camera
                imageFieldView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.takePhoto();
                    }
                });

                //Set a long-click listener on asset image to delete photo
                imageFieldView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                        dialog.setTitle("Remove Photo");
                        dialog.setMessage("Are you sure of removing this photo?");
                        dialog.setCancelable(true);
                        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                presenter.resetImage((ImageDetail)item);
                            }
                        });
                        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(activity,
                                        "Removing photo cancelled.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.show();

                        return true;
                    }
                });
            }

            return v;
        }
    }

    //endregion
}
