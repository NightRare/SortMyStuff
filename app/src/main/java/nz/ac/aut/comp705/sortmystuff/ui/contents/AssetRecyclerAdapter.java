package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

public class AssetRecyclerAdapter extends
        RecyclerView.Adapter<AssetRecyclerAdapter.ViewHolder> {

    public AssetRecyclerAdapter(
            @NonNull Context context,
            @NonNull List<IAsset> assets,
            @NonNull IContentsView.ViewListeners itemListener) {
        mContext = checkNotNull(context);
        mAssets = checkNotNull(assets);
        mItemListener = itemListener;
        mSelectedAssets = new LinkedHashSet<>();
        mViewMode = ContentsViewMode.Default;
    }

    @Override
    public AssetRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contents_assets_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AssetRecyclerAdapter.ViewHolder holder, int position) {
        IAsset asset = mAssets.get(position);

        if (asset.getThumbnail() != null)
            holder.mPhotoView.setImageBitmap(asset.getThumbnail());

        holder.mNameView.setText(asset.getName());
        holder.mCheckBox.setVisibility(mViewMode.equals(ContentsViewMode.Selection) ?
                View.VISIBLE : View.GONE);
        holder.mMoreOptionsView.setVisibility(mViewMode.equals(ContentsViewMode.Default) ?
            View.VISIBLE : View.GONE);
        holder.mCheckBox.setChecked(mSelectedAssets.contains(mAssets.get(position).getId()));

        //gray the asset name if it is in moving list
        boolean beingMoved = mViewMode.equals(ContentsViewMode.Moving) &&
                mSelectedAssets.contains(mAssets.get(position).getId());
        if (beingMoved) {
            holder.mNameView.setTextColor(ContextCompat.getColor(mContext, R.color.light_grey));
        } else {
            holder.mNameView.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }

        holder.itemView.setOnClickListener(v -> {
            if (mViewMode.equals(ContentsViewMode.Selection)) {
                toggleCheckBox(holder.mCheckBox, position);

            } else {
                if (beingMoved) return;
                mItemListener.onContentAssetClick(mAssets.get(position));
            }
        });

        holder.itemView.setOnLongClickListener(v ->
                mViewMode.equals(ContentsViewMode.Default) &&
                mItemListener.onContentAssetLongClick());

        holder.mMoreOptionsView.setOnClickListener(v ->
                initialiseMoreOptionsMenu(holder, position).show());

    }

    private void toggleCheckBox(CheckBox checkBox, int position) {
        checkBox.toggle();
        if (checkBox.isChecked())
            mSelectedAssets.add(mAssets.get(position).getId());
        else
            mSelectedAssets.remove(mAssets.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return mAssets.size();
    }

    @NonNull
    public List<String> getSelectedAssets() {
        return new ArrayList<>(mSelectedAssets);
    }

    public void replaceData(
            @NonNull List<IAsset> assets,
            @NonNull ContentsViewMode viewMode) {
        mAssets.clear();
        mAssets.addAll(checkNotNull(assets));
        mViewMode = checkNotNull(viewMode);

        if(viewMode.equals(ContentsViewMode.Default))
            mSelectedAssets.clear();
        notifyDataSetChanged();
    }

//    private void switchViewMode (ContentsViewMode viewMode) {
//        switch (viewMode) {
//            case Default:
//                mSelectedAssets.clear();
//                mMovingAssets.clear();
//                break;
//            case Moving:
//                mMovingAssets.addAll(mSelectedAssets);
//                mSelectedAssets.clear();
//                break;
//        }
//    }

    public void clearSelectedAssets() {
        mSelectedAssets.clear();
        notifyDataSetChanged();
    }

    public void selectAllAssets() {
        Observable.from(mAssets)
                .map(IAsset::getId)
                .toList()
                .subscribe(ids -> {
                    mSelectedAssets.addAll(ids);
                    notifyDataSetChanged();
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mPhotoView;
        private TextView mNameView;
        private ImageView mMoreOptionsView;
        private CheckBox mCheckBox;

        private ViewHolder(View itemView) {
            super(itemView);

            mPhotoView = (ImageView) itemView.findViewById(R.id.contents_asset_image);
            mNameView = (TextView) itemView.findViewById(R.id.asset_name);
            mMoreOptionsView = (ImageView) itemView.findViewById(R.id.asset_more_options);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.asset_checkbox);
        }
    }

    //region PRIVATE STUFF

    private PopupMenu initialiseMoreOptionsMenu(AssetRecyclerAdapter.ViewHolder holder, int position) {
        PopupMenu menu = new PopupMenu(mContext, holder.mMoreOptionsView);
        menu.inflate(R.menu.assets_menu);
        menu.setOnMenuItemClickListener(item ->
                mItemListener.onAssetMoreOptionsClick(mAssets.get(position), item));
        return menu;
    }

    @NonNull
    private final Context mContext;

    @NonNull
    private final List<IAsset> mAssets;

    @NonNull
    private final IContentsView.ViewListeners mItemListener;

    @NonNull
    private final Set<String> mSelectedAssets;

    @NonNull
    private ContentsViewMode mViewMode;



    //endregion
}
