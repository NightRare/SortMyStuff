package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;

/**
 * The Adapter class for the list view of assets list.
 * Created by Jing on 5/05/17.
 */

public class AssetListAdapter extends BaseAdapter {

    public AssetListAdapter(List<Asset> list, Context context, Boolean showCheckbox) {
        this(list, context, showCheckbox, new ArrayList<Asset>());
    }

    public AssetListAdapter(List<Asset> list, Context context, Boolean showCheckbox
            , List<Asset> movingAssets) {
        Preconditions.checkNotNull(list);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(movingAssets);

        this.assetList = list;
        this.context = context;
        this.showCheckbox = showCheckbox;
        this.movingAssets = movingAssets;
        inflater = LayoutInflater.from(context);
        selectStatusMap = new HashMap<>();
        initSelectionStatus();
    }

    private void initSelectionStatus() {
        for (int i = 0; i < assetList.size(); i++) {
            selectStatusMap.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return assetList.size();
    }

    @Override
    public Object getItem(int position) {
        return assetList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.contents_assets_layout, null);

            holder.imageView = (ImageView) convertView.findViewById(R.id.contents_asset_image);
            holder.textView = (TextView) convertView.findViewById(R.id.asset_name);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.asset_checkbox);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        Asset asset = assetList.get(position);

        if(showCheckbox)
            holder.checkbox.setVisibility(View.VISIBLE);
        else
            holder.checkbox.setVisibility(View.GONE);

        Bitmap photo = asset.getPhoto();
        if (photo == null)
            holder.imageView.setImageResource(R.drawable.default_square);
        else
            holder.imageView.setImageBitmap(photo);

        holder.textView.setText(asset.getName());
        holder.checkbox.setChecked(selectStatusMap.get(position));

        //gray the asset name if it is in moving list
        if(movingAssets.contains(assetList.get(position))) {
            holder.textView.setTextColor(ContextCompat.getColor(context, R.color.light_grey));
        }
        return convertView;
    }

    public boolean isCheckboxShowed() {
        return showCheckbox;
    }

    public HashMap<Integer, Boolean> getSelectStatusMap() {
        return selectStatusMap;
    }

    public Map<Integer, Asset> getSelectedAssets() {
        Map<Integer, Asset> selectedAssets = new HashMap<>();
        for (Map.Entry<Integer, Boolean> e : selectStatusMap.entrySet()) {
            if (e.getValue()) {
                selectedAssets.put(e.getKey(), assetList.get(e.getKey()));
            }
        }
        return selectedAssets;
    }

    //region PRIVATE STUFF

    private List<Asset> assetList;
    private List<Asset> movingAssets;

    //Whether the checkbox is selected.
    private HashMap<Integer, Boolean> selectStatusMap;
    private LayoutInflater inflater;
    private Boolean showCheckbox;
    private Context context;

    //endregion

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
        CheckBox checkbox;
    }
}
