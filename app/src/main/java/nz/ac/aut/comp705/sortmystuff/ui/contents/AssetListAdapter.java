package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;

/**
 * The Adapter class for the list view of assets list.
 * Created by Jing on 5/05/17.
 */

public class AssetListAdapter extends BaseAdapter {

    private static List<Asset> assetList;

    private static List<Asset> selectedAssetList;

    //Whether the checkbox is selected.
    private static HashMap<Integer, Boolean> selectStatusMap;
    private Context context;
    private LayoutInflater inflater;
    private Boolean showCheckbox;

    public AssetListAdapter(List<Asset> list, Context context, Boolean showCheckbox) {
        this.context = context;
        this.assetList = list;
        selectedAssetList = new ArrayList<>();
        inflater = LayoutInflater.from(context);
        selectStatusMap = new HashMap<>();
        this.showCheckbox = showCheckbox;
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

            holder.imageView = (ImageView) convertView.findViewById(R.id.asset_image);
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

        holder.imageView.setImageResource(R.drawable.folder_icon);
        holder.textView.setText(asset.getName());
        holder.checkbox.setChecked(selectStatusMap.get(position));

        return convertView;
    }

    public boolean isCheckboxShowed() {
        return showCheckbox;
    }

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
        CheckBox checkbox;
    }

    public static HashMap<Integer, Boolean> getSelectStatusMap() {
        return selectStatusMap;
    }

    public static List<Asset> getSelectedAssetList() {
        Set<Map.Entry<Integer, Boolean>> mapSet = selectStatusMap.entrySet();
        for (Map.Entry<Integer, Boolean> i : mapSet) {
            if (i.getValue())
                selectedAssetList.add(assetList.get(i.getKey()));
        }
        return selectedAssetList;
    }


}
