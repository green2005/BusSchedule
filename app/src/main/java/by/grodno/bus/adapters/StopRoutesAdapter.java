package by.grodno.bus.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;

public class StopRoutesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private Cursor mCursor;


    public StopRoutesAdapter(Context context, Cursor c) {
        // super(context, c, autoRequery);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mCursor = c;
    }

  /*  @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.stoproutesitem, null);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.routeNameView);
        TextView tvTime1 = (TextView) view.findViewById(R.id.time1View);
        TextView tvTime2 = (TextView) view.findViewById(R.id.time2View);
        tvName.setText(cursor.getString(cursor.getColumnIndex(DBManager.BUS_NAME)));
        tvTime1.setText(cursor.getString(cursor.getColumnIndex(DBManager.SCHEDULE_TIME)));
        int sqlMinutes = cursor.getInt(cursor.getColumnIndex(DBManager.MINUTES));
        int minutes = CalendarHelper.getMinutes();
        int diff = sqlMinutes - minutes;
        String s = null;
        if (diff > 60) {
            int mins = diff % 60;
            int hours = diff / 60;
            s = String.valueOf(hours) + " " + context.getString(R.string.hour) +
                    String.valueOf(mins) + " " + context.getString(R.string.minute);
        } else {
            s = String.valueOf(diff) + " " + context.getString(R.string.minute);
        }
        tvTime2.setText(s);
    }*/

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
        //tvName.setText(mCursor.getString(mCursor.getColumnIndex(DBManager.BUS_NAME)));
        String name = mCursor.getString(mCursor.getColumnIndex(DBManager.BUS_NAME)) +
                " " + mCursor.getString(mCursor.getColumnIndex(DBManager.BUS_DIRECTION));
        tvName.setText(name);

        tvTime1.setText(mCursor.getString(mCursor.getColumnIndex(DBManager.SCHEDULE_TIME)));
        int sqlMinutes = mCursor.getInt(mCursor.getColumnIndex(DBManager.MINUTES));
        int minutes = CalendarHelper.getMinutes();
        int diff = sqlMinutes - minutes;
        String s;
        if (diff > 60) {
            int mins = diff % 60;
            int hours = diff / 60;
            s = String.valueOf(hours) + " " + mContext.getString(R.string.hour) +
                    String.valueOf(mins) + " " + mContext.getString(R.string.minute);
        } else {
            s = String.valueOf(diff) + " " + mContext.getString(R.string.minute);
        }
        if (!TextUtils.isEmpty(s)) {
            s = mContext.getResources().getString(R.string.ina) + " " + s;
        }
        tvTime2.setText(s);
        return view;
    }
}
