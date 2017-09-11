package nz.ac.aut.comp705.sortmystuff.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.List;

import nz.ac.aut.comp705.sortmystuff.R;
import nz.ac.aut.comp705.sortmystuff.data.models.Asset;

/**
 * Created by YuanY on 2017/9/11.
 */

public class SearchListAdapter extends BaseAdapter{

    public SearchListAdapter(List<Asset> resultList, Context context) {
        Preconditions.checkNotNull(resultList);
        Preconditions.checkNotNull(context);

        this.resultList = resultList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultList.get(position);
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
            convertView = inflater.inflate(R.layout.search_result_layout, null);

            holder.titleView = (TextView) convertView.findViewById(R.id.search_result_title);
            holder.categoriesView = (TextView) convertView.findViewById(R.id.search_result_categories);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Asset result = resultList.get(position);

        holder.titleView.setText(result.getName());
        // TODO: It now only displays one category type according to the data structure
        holder.categoriesView.setText(result.getCategoryType().toString());

        return convertView;
    }

    //region PRIVATE STUFF

    private List<Asset> resultList;
    private LayoutInflater inflater;
    private Context context;

    //endregion

    public class ViewHolder {
        TextView titleView;
        TextView categoriesView;
    }
}
