package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.SortMyStuffApp;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.Detail;
import nz.ac.aut.comp705.sortmystuff.data.IDataManager;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;

public class DetailsActivity extends AppCompatActivity implements IDetailsView {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        details = (ListView) findViewById(R.id.detail_list);

        startPresenter();

        addDetilButton = (FloatingActionButton) findViewById(R.id.fab);
        addDetilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.showDialogBox(view);
            }
        });

    }

    /**
     * {@inheritDoc}
     *
     * @param presenter the presenter
     */
    @Override
    public void setPresenter(IDetailsPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * {@inheritDoc}
     *
     * @param detailList
     */
    @Override
    public void showDetails(List<Detail> detailList) {
        details.setAdapter(new DetailAdapter(this, R.layout.two_lines_list, detailList));
    }

    /**
     * {@inheritDoc}
     *
     * @param message
     */
    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }


    //*****PRIVATE STUFF*****//
    private IDetailsPresenter presenter;
    private ListView details;
    private FloatingActionButton addDetilButton;

    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;


    /**
     * Creates and starts the presenter
     */
    private void startPresenter() {
        IDataManager dataManager = ((SortMyStuffApp) getApplication()).getFactory().getDataManager();
        presenter = new DetailsPresenter(dataManager, this, this);
        setPresenter(presenter);
        presenter.start();
    }

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
            labelView.setText(item.getLabel());

            TextView textFieldView = (TextView) v.findViewById(android.R.id.text2);
            ImageView imageFieldView = (ImageView) v.findViewById(R.id.asset_image);

            if (item.getType().equals(DetailType.Date) || item.getType().equals(DetailType.Text)) {
                textFieldView.setText((String) item.getField());
                imageFieldView.setImageBitmap(null);

            } else if (item.getLabel().equals(CategoryType.BasicDetail.PHOTO)
                    && item.getType().equals(DetailType.Image)) {
                imageFieldView.setImageBitmap((Bitmap) item.getField());

                textFieldView.setText(null);

                imageFieldView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File outputImage = new File(getExternalCacheDir(),
                                "output_image.jpg");
                        try {
                            if (outputImage.exists()) {
                                outputImage.delete();
                            }
                            outputImage.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (Build.VERSION.SDK_INT >= 24) {
                            imageUri = FileProvider.getUriForFile(DetailsActivity.this,
                                    "com.example.cameratest.fileprovider", outputImage);
                        } else {
                            imageUri = Uri.fromFile(outputImage);
                        }

                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent, TAKE_PHOTO);
                    }
                });
            }
            return v;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO:
                    performCrop(imageUri);
                    break;
                case 2:
                    try {
                        Bitmap croppedBmp = BitmapFactory.decodeStream
                                (getContentResolver().openInputStream(imageUri));
                        presenter.updateImage(croppedBmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    }

    private void performCrop(Uri imageUri) {
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(imageUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 10);
            cropIntent.putExtra("aspectY", 6);
            cropIntent.putExtra("outputX", 1000);
            cropIntent.putExtra("outputY", 600);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cropIntent, 2);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                    "Sorry, your device doesn't support the crop action.",
                    Toast.LENGTH_SHORT).show();
        }
    }


}
