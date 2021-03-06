package by.grodno.bus.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;
import by.grodno.bus.activity.BusStopActivity;
import by.grodno.bus.db.DBManager;

public class StopRoutesAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private LayoutInflater mInflater;
    private Context mContext;
    private Cursor mCursor;

    public StopRoutesAdapter(Context context, Cursor c) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mCursor = c;
    }

    public void swapCursor(Cursor cursor){
        if (cursor != null){
            Cursor prevCursor = mCursor;
            mCursor = cursor;
            prevCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        mCursor.moveToPosition(position);
        return mCursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mCursor.moveToPosition(position);
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.stoproutesitem, null);
        }
        TextView tvName = (TextView) view.findViewById(R.id.routeNameView);
        TextView tvTime1 = (TextView) view.findViewById(R.id.time1View);
        TextView tvTime2 = (TextView) view.findViewById(R.id.time2View);
        ImageView imageView = (ImageView) view.findViewById(R.id.trans_kind);
        if ("0".equals(mCursor.getString(mCursor.getColumnIndex(DBManager.TRANSPORT_KIND)))) {
            imageView.setImageResource(R.drawable.bus);
        } else {
            imageView.setImageResource(R.drawable.trolleybus);
        }
        String name = mCursor.getString(mCursor.getColumnIndex(DBManager.BUS_NAME)) +
                " " + mCursor.getString(mCursor.getColumnIndex(DBManager.BUS_DIRECTION));
        tvName.setText(name);
        tvTime1.setText(mCursor.getString(mCursor.getColumnIndex(DBManager.SCHEDULE_TIME)));
        int sqlMinutes = mCursor.getInt(mCursor.getColumnIndex(DBManager.MINUTES));
        tvTime2.setText(CalendarHelper.getTimeDiff(mContext, sqlMinutes));
        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCursor.moveToPosition(position);
        String stopId = mCursor.getString(mCursor.getColumnIndex(DBManager.STOP_ID));
        String busId = mCursor.getString(mCursor.getColumnIndex(DBManager.BUS_ID));
        Intent intent = new Intent(mContext, BusStopActivity.class);
        intent.putExtra(DBManager.BUS_ID, busId);
        intent.putExtra(DBManager.STOP_ID, stopId);
        mContext.startActivity(intent);
    }
}
