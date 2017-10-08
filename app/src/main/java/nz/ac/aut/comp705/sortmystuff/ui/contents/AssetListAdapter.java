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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * The Adapter class for the list view of assets list.
 * @author Jing
 */

public class AssetListAdapter extends BaseAdapter {

    public AssetListAdapter(List<IAsset> list, Context context, Boolean showCheckbox
            , List<IAsset> movingAssets) {
        mAssetList = checkNotNull(list, "The list cannot be null.");
        mContext = checkNotNull(context, "The context cannot be null.");
        mShowCheckbox = checkNotNull(showCheckbox, "The showCheckbox cannot be null.");
        mMovingAssets = checkNotNull(movingAssets, "The movingAssets cannot be null.");
        mInflater = LayoutInflater.from(context);
        mSelectStatusMap = new HashMap<>();
        initSelectionStatus();
    }

    private void initSelectionStatus() {
        for (int i = 0; i < mAssetList.size(); i++) {
            mSelectStatusMap.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return mAssetList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAssetList.get(position);
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
            convertView = mInflater.inflate(R.layout.contents_assets_layout, null);

            holder.imageView = (ImageView) convertView.findViewById(R.id.contents_asset_image);
            holder.textView = (TextView) convertView.findViewById(R.id.asset_name);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.asset_checkbox);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        IAsset asset = mAssetList.get(position);

        if(mShowCheckbox)
            holder.checkbox.setVisibility(View.VISIBLE);
        else
            holder.checkbox.setVisibility(View.GONE);

        Bitmap thumbnail = asset.getThumbnail();
        if (thumbnail == null)
            holder.imageView.setImageResource(R.drawable.default_square);
        else
            holder.imageView.setImageBitmap(thumbnail);

        holder.textView.setText(asset.getName());
        holder.checkbox.setChecked(mSelectStatusMap.get(position));

        //gray the asset name if it is in moving list
        if(mMovingAssets.contains(mAssetList.get(position))) {
            holder.textView.setTextColor(ContextCompat.getColor(mContext, R.color.light_grey));
        }
        return convertView;
    }

    public boolean isCheckboxShowed() {
        return mShowCheckbox;
    }

    public HashMap<Integer, Boolean> getmSelectStatusMap() {
        return mSelectStatusMap;
    }

    public Map<Integer, IAsset> getSelectedAssets() {
        Map<Integer, IAsset> selectedAssets = new HashMap<>();
        for (Map.Entry<Integer, Boolean> e : mSelectStatusMap.entrySet()) {
            if (e.getValue()) {
                selectedAssets.put(e.getKey(), mAssetList.get(e.getKey()));
            }
        }
        return selectedAssets;
    }

    public class ViewHolder {
        ImageView imageView;
        TextView textView;
        CheckBox checkbox;
    }

    //region PRIVATE STUFF

    private List<IAsset> mAssetList;
    private List<IAsset> mMovingAssets;

    //Whether the checkbox is selected.
    private HashMap<Integer, Boolean> mSelectStatusMap;
    private LayoutInflater mInflater;
    private Boolean mShowCheckbox;
    private Context mContext;

    //endregion

}
