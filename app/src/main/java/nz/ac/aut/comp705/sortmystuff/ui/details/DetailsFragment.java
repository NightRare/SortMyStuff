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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.ImageDetail;
import nz.ac.aut.comp705.sortmystuff.data.models.TextDetail;
import nz.ac.aut.comp705.sortmystuff.ui.swipe.SwipeActivity;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;

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

        details = (ListView) rootView.findViewById(R.id.details_list);

        details.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Detail detail = (Detail) parent.getItemAtPosition(position);
                if (detail.getType().equals(DetailType.Text))
                    getEditTextDetailDialog(view, (TextDetail) detail).show();
                else return;
            }
        });

        presenter.subscribe();
    }

    @Override
    public void setPresenter(IDetailsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showDetails(List<Detail> detailList) {
        details.setAdapter(new DetailAdapter(activity, R.layout.details_two_lines_list,
                R.layout.details_asset_photo, detailList));
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
     * Setup the dialog box for adding details
     * that enables two single line inputs for
     * detail label and field, and has a
     * functional save and cancel button
     * @param v
     * @return dialog
     */
    private AlertDialog.Builder getEditTextDetailDialog(View v, final TextDetail detail){
        AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
        dialog.setTitle(detail.getLabel());
        //dialog box setup
        Context context = v.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        //field text configuration
        final EditText fieldText = createEditText(context,layout);
        fieldText.setText(detail.getField());
        //button setup
        dialog.setView(layout)
                .setPositiveButton(R.string.edit_detail_confirm_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.updateTextDetail(detail, fieldText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.cancel();
                    }
                });
        return dialog;
    }

    /**
     * Creates an editable text area for the dialog box
     * @param context
     * @param layout
     * @return editText
     */
    private EditText createEditText(Context context, LinearLayout layout){
        final EditText editText = new EditText(context);
        editText.setSingleLine();
        editText.setInputType(TYPE_TEXT_FLAG_CAP_SENTENCES);
        layout.addView(editText);
        return editText;
    }

    /**
     * Inner class to create an Array Adapter
     * according to the format required for the detail list
     */
    private class DetailAdapter extends ArrayAdapter<Detail> {

        // TODO: to be removed later; used as a placeholder of other image details
        private static final int DUMMY_LAYOUT = R.layout.details_dummy_item;

        private Context context;
        private int twoLinesLayoutId;
        private int photoLayoutId;
        private List<Detail> detailList = null;

        private DetailAdapter(Context context, int twoLinesLayoutId, int photoLayoutId, List<Detail> detailList) {
            super(context, twoLinesLayoutId, detailList);
            this.context = context;
            this.twoLinesLayoutId = twoLinesLayoutId;
            this.photoLayoutId = photoLayoutId;
            this.detailList = detailList;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final Detail item = detailList.get(position);
            DetailType type = item.getType();
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            if(type.equals(DetailType.Image)) {
                // TODO: to be completed later, now only display the photo of the asset, any other image detail will not be displayed
                if(!item.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
                    v = inflater.inflate(DUMMY_LAYOUT, parent, false);
                }
                else {
                    v = inflater.inflate(photoLayoutId, parent, false);
                    ImageView imageFieldView = (ImageView) v.findViewById(R.id.details_photo);

                    imageFieldView.setImageBitmap((Bitmap) item.getField());

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
                            dialog.setTitle(R.string.photo_remove_dialog_title);
                            dialog.setMessage("Are you sure of removing this photo?");
                            dialog.setCancelable(true);
                            dialog.setPositiveButton(R.string.photo_remove_confirm_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    presenter.resetImage((ImageDetail)item);
                                }
                            });
                            dialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
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
            }
            else if (type.equals(DetailType.Date) || type.equals(DetailType.Text)) {
                v = inflater.inflate(twoLinesLayoutId, parent, false);
                TextView labelView = (TextView) v.findViewById(R.id.detail_label);
                TextView textFieldView = (TextView) v.findViewById(R.id.detail_field);

                labelView.setText(item.getLabel());
                textFieldView.setText((String) item.getField());
            }

            return v;
        }
    }

    //endregion
}
