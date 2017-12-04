package nz.ac.aut.comp705.sortmystuff.ui.adding;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailForm;
import nz.ac.aut.comp705.sortmystuff.data.models.DetailType;
import nz.ac.aut.comp705.sortmystuff.data.models.IDetail;

import static android.text.InputType.TYPE_DATETIME_VARIATION_DATE;
import static com.google.common.base.Preconditions.checkNotNull;

public class DetailListAdapter extends
        RecyclerView.Adapter<DetailListAdapter.ViewHolder> {

    public DetailListAdapter(
            Context context,
            List<DetailForm> details) {
        mInflater = LayoutInflater.from(checkNotNull(context, "The context cannot be null."));
        checkNotNull(details, "The assets cannot be null.");
        mDetails = excludeNonTextDetail(details);
    }

    @Override
    public DetailListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.details_label_field_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        DetailForm detail = mDetails.get(position);
        holder.mLabelView.setText(detail.getLabel());

        String field = detail.getField().toString();
        holder.mFieldView.setText(field);

        if (detail.getType().equals(DetailType.Date))
            holder.mFieldView.setInputType(TYPE_DATETIME_VARIATION_DATE);
        else
            holder.mFieldView.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

        holder.mFieldView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                detail.setField(s.toString());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mDetails.size();
    }


    public void replaceData(List<DetailForm> data) {
        mDetails.clear();
        mDetails.addAll(excludeNonTextDetail(data));
        notifyDataSetChanged();
    }

    public List<DetailForm> getItems() {
        return new ArrayList<>(mDetails);
    }

    public List<IDetail> getItemsAsIDetail() {
        return new ArrayList<>(mDetails);
    }

    /**
     * A ViewHolder class for {@link DetailListAdapter}.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mLabelView;
        private EditText mFieldView;

        private ViewHolder(View itemView) {
            super(itemView);
            mLabelView = (TextView) itemView.findViewById(R.id.aa_detail_label);
            mFieldView = (EditText) itemView.findViewById(R.id.aa_detail_text_field);
        }
    }

    //region PRIVATE STUFF

    private List<DetailForm> excludeNonTextDetail(List<DetailForm> details) {
        List<DetailForm> output = new ArrayList<>();
        for (DetailForm detail : checkNotNull(details)) {
            DetailType type = detail.getType();
            if (!type.equals(DetailType.Text) &&
                    !type.equals(DetailType.Date))
                continue;

            output.add(detail);
        }
        return output;
    }

    private final LayoutInflater mInflater;
    private final List<DetailForm> mDetails;

    //endregion


}
