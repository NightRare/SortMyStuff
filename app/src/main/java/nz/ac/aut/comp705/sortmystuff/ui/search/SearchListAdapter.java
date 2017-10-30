package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.IAsset;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class SearchListAdapter extends BaseAdapter{

    public SearchListAdapter(List<IAsset> resultList, Context context) {

        mResultList = checkNotNull(resultList);
        mInflater = LayoutInflater.from(checkNotNull(context));
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public Object getItem(int position) {
        return mResultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.search_result_layout, null);

            holder.titleView = (TextView) convertView.findViewById(R.id.search_result_title);
            holder.categoriesView = (TextView) convertView.findViewById(R.id.search_result_categories);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        IAsset result = mResultList.get(position);

        holder.titleView.setText(result.getName());
        // TODO: It now only displays one category type according to the data structure
        holder.categoriesView.setText(result.getCategoryType().toString());

        return convertView;
    }

    //region PRIVATE STUFF

    private List<IAsset> mResultList;
    private LayoutInflater mInflater;

    //endregion

    public class ViewHolder {
        TextView titleView;
        TextView categoriesView;
    }
}
