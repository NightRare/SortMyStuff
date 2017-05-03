package nz.ac.aut.comp705.sortmystuff.ui.contents;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.Asset;

/**
 * Created by Yuan on 2017/5/4.
 */

public class PathBarAdapter extends
        RecyclerView.Adapter<PathBarAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Asset> assets;
    private IContentsPresenter presenter;

    public PathBarAdapter(Context context, List<Asset> assets, IContentsPresenter presenter) {
        this.inflater = LayoutInflater.from(context);
        this.assets = assets;
        this.presenter = presenter;
    }

    @Override
    public PathBarAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.content_index_pathbar_asset_view, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        StringBuffer sb = new StringBuffer();

        if(position == 0)
            sb.append("  ");
        else
            sb.append(" >  ");

        sb.append(assets.get(position).getName());

        holder.nameView.setText(sb.toString());

        final String assetId = assets.get(position).getId();
        holder.nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.setCurrentAssetId(assetId);
                presenter.loadCurrentContents(false);
            }
        });
    }


    @Override
    public int getItemCount() {
        return assets.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.pathbar_asset_name);
        }
    }
}
