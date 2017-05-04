package nz.ac.aut.comp705.sortmystuff.ui.contents;


import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.Asset;


public class AssetsAdapter extends ArrayAdapter<Asset> {

    private int resourceId;

    public AssetsAdapter(@NonNull Context context, @LayoutRes int textViewResourceId, @NonNull List<Asset> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Asset asset = getItem(position);

        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.assetImage = (ImageView) view.findViewById(R.id.asset_image);
            viewHolder.assetName = (TextView) view.findViewById(R.id.asset_name);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.asset_checkbox);

            //setCheckboxVisibility(asset, viewHolder);

            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();

            //setCheckboxVisibility(asset, viewHolder);
        }

        viewHolder.assetImage.setImageResource(R.drawable.folder_icon);
        viewHolder.assetName.setText(asset.getName());

        Log.i("getView", "getView.");

        return view;
    }

//    private void setCheckboxVisibility(Asset asset, ViewHolder viewHolder) {
//        if (asset.isInEditMode() == false)
//            viewHolder.checkBox.setVisibility(View.GONE);
//        else
//            viewHolder.checkBox.setVisibility(View.VISIBLE);
//    }

    class ViewHolder {
        ImageView assetImage;
        TextView assetName;
        CheckBox checkBox;
    }
}
