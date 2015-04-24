package by.grodno.bus.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.R;
import by.grodno.bus.db.FavouritiesItem;

public class FavouritiesAdapter extends BaseAdapter {
    public interface SelectionChangedListener {
        public void onChanged(List<Integer> selectedItems);
    }

    private LayoutInflater mInflater;
    private List<FavouritiesItem> mItems;
    private List<Integer> mSelectedItems;
    private SelectionChangedListener mListener;

    public FavouritiesAdapter(Context context, List<FavouritiesItem> items) {
        mInflater = LayoutInflater.from(context);
        mItems = items;
        mSelectedItems = new ArrayList();
    }

    public void setSelectionChangedListener(SelectionChangedListener listener){
        mListener = listener;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View cnView = convertView;
        if (cnView == null) {
            cnView = mInflater.inflate(R.layout.favitem, null);
        }
        TextView busName = (TextView) cnView.findViewById(R.id.routeName);
        TextView routeName = (TextView) cnView.findViewById(R.id.stopName);
        ImageView image = (ImageView) cnView.findViewById(R.id.trans_kind);
        CheckBox checkBox = (CheckBox) cnView.findViewById(R.id.checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mSelectedItems.add(position);
                } else {
                    int pos = mSelectedItems.indexOf(position);
                    if (pos >= 0) {
                        mSelectedItems.remove(pos);
                    }
                }
                if (mListener != null){
                    mListener.onChanged(mSelectedItems);
                }
            }
        });

        FavouritiesItem item = mItems.get(position);
        if (!TextUtils.isEmpty(item.getBusName())) {
            busName.setText(item.getBusName());
            busName.setVisibility(View.VISIBLE);
        } else {
            busName.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(item.getStopName())) {
            routeName.setVisibility(View.VISIBLE);
            routeName.setText(item.getStopName());
        } else {
            routeName.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(item.getBusName())) {
            image.setImageResource(R.drawable.stop);
        } else if ("0".equals(item.getTr())) {
            image.setImageResource(R.drawable.bus);
        } else {
            image.setImageResource(R.drawable.trolleybus);
        }
        return cnView;
    }


}
