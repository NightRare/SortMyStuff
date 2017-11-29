package nz.ac.aut.comp705.sortmystuff.ui.adding;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;

import static com.google.common.base.Preconditions.checkNotNull;

public class DetailListAdapter extends
        RecyclerView.Adapter<DetailListAdapter.ViewHolder> {

    public DetailListAdapter(
            Context context,
            List<IDetail> details,
            DetailItemListener itemListeners) {
        mInflater = LayoutInflater.from(checkNotNull(context, "The context cannot be null."));
        checkNotNull(details, "The assets cannot be null.");
        mDetails = excludeNonTextDetail(details);
        mItemListeners = itemListeners;
    }

    @Override
    public DetailListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.details_label_field_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.mLabelView.setText(mDetails.get(position).getLabel());

        String field = mDetails.get(position).getField().toString();
        holder.mFieldView.setText(field == null ? "" : field);

        holder.itemView.setOnClickListener(v ->
                mItemListeners.onClickItem(v, mDetails.get(position)));
    }


    @Override
    public int getItemCount() {
        return mDetails.size();
    }

    public void replaceData(List<IDetail> data) {
        mDetails = excludeNonTextDetail(data);
        notifyDataSetChanged();
    }

    public List<IDetail> getItems() {
        return mDetails;
    }

    /**
     * A ViewHolder class for {@link DetailListAdapter}.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mLabelView;
        private TextView mFieldView;

        private ViewHolder(View itemView) {
            super(itemView);
            mLabelView = (TextView) itemView.findViewById(R.id.aa_detail_label);
            mFieldView = (TextView) itemView.findViewById(R.id.aa_detail_field);
        }
    }

    public interface DetailItemListener {
        void onClickItem(View view, IDetail item);

    }

    //region PRIVATE STUFF

    private List<IDetail> excludeNonTextDetail(List<IDetail> details) {
        List<IDetail> output = new ArrayList<>();
        for (IDetail detail : checkNotNull(details)) {
            DetailType type = detail.getType();
            if (!type.equals(DetailType.Text) &&
                    !type.equals(DetailType.Date))
                continue;

            output.add(detail);
        }
        return output;
    }

    private LayoutInflater mInflater;
    private List<IDetail> mDetails;
    private DetailItemListener mItemListeners;

    //endregion


}
