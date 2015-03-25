package by.grodno.bus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;

public class BusStopAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private Map<String, List<String>> mTimesMap;
    private List<String> mHours;
    private String mCurrentHour = CalendarHelper.getHour();

    public BusStopAdapter(Context context, Map<String, List<String>> timesMap, List<String> hours) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mTimesMap = timesMap;
        mHours = hours;
    }

    @Override
    public int getCount() {
        return mHours.size();
    }

    @Override
    public Object getItem(int position) {
        return mHours.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cnView = convertView;
        ViewHolder holder;
        if (cnView == null) {
            cnView = mInflater.inflate(R.layout.bus_stop_item, null);
            holder = new ViewHolder();
            holder.tvHour = (TextView) cnView.findViewById(R.id.hour);
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time1));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time2));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time3));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time4));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time5));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time6));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time7));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time8));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time9));
            holder.tvItems.add((TextView) cnView.findViewById(R.id.time10));
            cnView.setTag(holder);
        } else {
            holder = (ViewHolder) cnView.getTag();
        }
        String hour = mHours.get(position);
        if (mCurrentHour.equals(hour)){
            int currentColor = mContext.getResources().getColor(android.R.color.holo_blue_light);
            cnView.setBackgroundColor(currentColor);
        } else
        {
            int color = mContext.getResources().getColor(android.R.color.transparent);
            cnView.setBackgroundColor(color);
        }
        holder.tvHour.setText(hour);
        List<String> minutes = mTimesMap.get(hour);
        int i = 0;
        for (String minute : minutes) {
            if (holder.tvItems.size() > i) {
                holder.tvItems.get(i).setText(minute);
                holder.tvItems.get(i).setVisibility(View.VISIBLE);
            }
            i++;
        }
        for (int j = i; j < 10; j++) {
            holder.tvItems.get(j).setVisibility(View.GONE);
        }
        return cnView;
    }

    private class ViewHolder {
        List<TextView> tvItems;
        TextView tvHour;

        ViewHolder() {
            tvItems = new ArrayList<>();
        }
    }
}
