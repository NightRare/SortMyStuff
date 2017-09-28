package nz.ac.aut.comp705.sortmystuff.ui.details;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.CategoryType;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;

import static com.google.common.base.Preconditions.checkNotNull;

public class DetailsAdapter extends ArrayAdapter<IDetail> {

    public DetailsAdapter(Context context, List<IDetail> detailsList,
                           IDetailsView.ViewListeners viewListeners) {
        super(context, R.layout.details_two_lines_list, detailsList);
        mContext = checkNotNull(context, "The context cannot be null.");
        mDetailList = checkNotNull(detailsList, "The context cannot be null.");
        mViewListeners = viewListeners;
    }

    @Override
    public int getCount() {
        return mDetailList.size();
    }

    @Nullable
    @Override
    public IDetail getItem(int position) {
        return mDetailList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final IDetail item = mDetailList.get(position);
        DetailType type = item.getType();
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

        if (type.equals(DetailType.Image)) {
            // TODO: to be completed later, now only display the photo of the asset, any other image detail will not be displayed
            if (!item.getLabel().equals(CategoryType.BasicDetail.PHOTO)) {
                v = inflater.inflate(DUMMY_LAYOUT, parent, false);
            } else {
                v = inflater.inflate(R.layout.details_asset_photo, parent, false);
                ImageView imageFieldView = (ImageView) v.findViewById(R.id.details_photo);

                imageFieldView.setImageBitmap((Bitmap) item.getField());

                //Set a click listener on asset image to launch camera
                imageFieldView.setOnClickListener(v1 -> mViewListeners.onImageClick(item));

                //Set a long-click listener on asset image to delete photo
                imageFieldView.setOnLongClickListener(v12 -> {
                    mViewListeners.onImageLongClick((IDetail<Bitmap>) item);
                    return true;
                });
            }
        } else if (type.equals(DetailType.Date) || type.equals(DetailType.Text)) {
            v = inflater.inflate(R.layout.details_two_lines_list, parent, false);
            TextView labelView = (TextView) v.findViewById(R.id.detail_label);
            TextView textFieldView = (TextView) v.findViewById(R.id.detail_field);

            labelView.setText(item.getLabel());
            textFieldView.setText((String) item.getField());
        }

        return v;
    }

    public void replaceData(List<IDetail> data) {
        mDetailList = checkNotNull(data);
        notifyDataSetChanged();
    }

    //region PRIVATE STUFF

    // TODO: to be removed later; used as a placeholder of other image details
    private static final int DUMMY_LAYOUT = R.layout.details_dummy_item;

    private Context mContext;
    private List<IDetail> mDetailList;
    private IDetailsView.ViewListeners mViewListeners;

    //endregion
}
