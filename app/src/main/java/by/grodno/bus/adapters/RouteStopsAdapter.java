package by.grodno.bus.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;
import by.grodno.bus.activity.BusStopActivity;
import by.grodno.bus.db.DBManager;

public class RouteStopsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{
    private Cursor mCursor;
    private Context mContext;
    private LayoutInflater mInflater;

    public RouteStopsAdapter(Cursor cursor, Context context ){
        mCursor = cursor;
        mContext = context;
        mInflater = LayoutInflater.from(context);
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
        View cnView = convertView;
        if (cnView == null){
            cnView =  mInflater.inflate(R.layout.stoproutesitem, null);
        }
        TextView tvTitle = (TextView)cnView.findViewById(R.id.routeNameView);
        TextView tvTime1 = (TextView)cnView.findViewById(R.id.time1View);
        TextView tvTime2 = (TextView)cnView.findViewById(R.id.time2View);
        String name = mCursor.getString(mCursor.getColumnIndex(DBManager.STOP_NAME));
        tvTitle.setText(name);
        tvTime1.setText(mCursor.getString(mCursor.getColumnIndex(DBManager.SCHEDULE_TIME)));
        int sqlMinutes = mCursor.getInt(mCursor.getColumnIndex(DBManager.MINUTES));
        tvTime2.setText(CalendarHelper.getTimeDiff(mContext, sqlMinutes));
        return cnView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCursor.moveToPosition(position);
        String stopId = mCursor.getString(mCursor.getColumnIndex(DBManager.STOP_ID));
        String busId = mCursor.getString(mCursor.getColumnIndex(DBManager.BUS_ID));
        Intent intent = new Intent(mContext, BusStopActivity.class);
        intent.putExtra(DBManager.STOP_ID, stopId);
        intent.putExtra(DBManager.BUS_ID, busId);
        mContext.startActivity(intent);
    }
}
