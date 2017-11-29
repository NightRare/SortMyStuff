package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Adapter class for the recycler view of path bar.
 *
 * @author Yuan
 */

public class PathBarAdapter extends
        RecyclerView.Adapter<PathBarAdapter.ViewHolder> {

    public PathBarAdapter(Context context, List<IAsset> assets,
                          IContentsView.ViewListeners viewListeners) {
        mInflater = LayoutInflater.from(checkNotNull(context, "The context cannot be null."));
        mAssets = checkNotNull(assets, "The assets cannot be null.");
        mViewListeners = checkNotNull(viewListeners, "The viewListeners cannot be null.");
    }

    //region RecyclerView.Adapter methods

    @Override
    public PathBarAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contents_pathbar, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        StringBuffer sb = new StringBuffer();

        if(position == 0)
            sb.append("  ");
        else
            sb.append(" ⟩  ");

        sb.append(mAssets.get(position).getName());

        holder.nameView.setText(sb.toString());

        final String assetId = mAssets.get(position).getId();
        holder.nameView.setOnClickListener(v -> mViewListeners.onPathbarItemClick(assetId));
    }


    @Override
    public int getItemCount() {
        return mAssets.size();
    }

    /**
     * A ViewHolder class for {@link PathBarAdapter}.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.pathbar_asset_name);
        }
    }

    //endregion

    //region PRIVATE STUFF

    private LayoutInflater mInflater;
    private List<IAsset> mAssets;
    private IContentsView.ViewListeners mViewListeners;

    //endregion


}
