package by.grodno.bus.fragments;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.grodno.bus.BusApplication;
import by.grodno.bus.CalendarHelper;
import by.grodno.bus.OnDateChangedListener;
import by.grodno.bus.R;
import by.grodno.bus.adapters.BusStopAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class BusStopFragment extends Fragment implements OnDateChangedListener {
    private String mBusId;
    private String mStopId;
    private ListView mListView;
    private TextView mTimeView;
    private TextView mInaTimeView;
    private String mNearestTime = "";
    private String mNearestHour ;
    private String mInaTime;
    private DBManager mDBManager;


    @Override
    public void onChange(String day) {
        String query = DBManager.getBusStopSQL(day, "", mBusId, mStopId);
        new QueryHelper(mDBManager).rawQuery(query, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                mNearestTime = "";
                processQueryResult(cursor, mListView);

            }
        });
    }



    public static Fragment getNewFragment(Bundle args) {
        Fragment fragment = new BusStopFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_busstop, null);
        mDBManager = ((BusApplication)getActivity().getApplication()).getDBManager();
        mListView = (ListView) view.findViewById(R.id.list);
        mTimeView = (TextView) view.findViewById(R.id.tvNextBusTime);
        mInaTimeView = (TextView) view.findViewById(R.id.tvNextBusInATime);
        Bundle args = getArguments();
        if (args != null) {
            mBusId = args.getString(DBManager.BUS_ID);
            mStopId = args.getString(DBManager.STOP_ID);
        }
        initList(mListView);
        return view;
    }

    private void initList(final ListView listView) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
         if ((TextUtils.isEmpty(mBusId)) || (TextUtils.isEmpty(mStopId))) {
            return;
        }
        String day1 = CalendarHelper.getDay1(activity);
        String day2 = CalendarHelper.getDay2(activity);
        String query = DBManager.getBusStopSQL(day1, day2, mBusId, mStopId);
        new QueryHelper(mDBManager).rawQuery(query, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                processQueryResult(cursor, listView);
            }
        });
    }

    private void processQueryResult(final Cursor cursor, final ListView listView) {
        final Handler handler = new Handler();
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        final Map<String, List<String>> timesMap = new HashMap<>();
        final List<String> hours = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                cursor.moveToFirst();
                try {
                    String hour = null;
                    ArrayList<String> timesList = null;
                    String now = CalendarHelper.now();
                    while (!cursor.isAfterLast()) {
                        if (TextUtils.isEmpty(mNearestTime)) {
                            if (cursor.getString(cursor.getColumnIndex(DBManager.SCHEDULE_TIME)).compareTo(now) >= 0) {
                                mNearestTime = cursor.getString(cursor.getColumnIndex(DBManager.SCHEDULE_TIME));
                                int sqlMinutes = cursor.getInt(cursor.getColumnIndex(DBManager.MINUTES));
                                mInaTime = CalendarHelper.getTimeDiff(activity, sqlMinutes);
                                mNearestHour = cursor.getString(cursor.getColumnIndex(DBManager.HOUR));
                            }
                        }
                        if (!cursor.getString(cursor.getColumnIndex(DBManager.HOUR)).equals(hour)) {
                            hour = cursor.getString(cursor.getColumnIndex(DBManager.HOUR));
                            timesList = new ArrayList<>();
                            timesMap.put(hour, timesList);
                            hours.add(hour);
                        }
                        timesList.add(cursor.getString(cursor.getColumnIndex(DBManager.MINUTE)));
                        cursor.moveToNext();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(mNearestTime)) {
                                mTimeView.setText(mNearestTime);
                                Activity activity = getActivity();
                                if (activity != null) {
                                    mInaTimeView.setText(mInaTime);
                                }
                            }
                            BaseAdapter adapter = new BusStopAdapter(activity, timesMap, hours, mNearestHour);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            int ix = hours.indexOf(mNearestHour);
                            if (ix > -1) {
                                scrollToPos(ix, listView);
                            } else {
                                if (CalendarHelper.getHour().compareTo("23.") > 0) {
                                    scrollToPos(hours.size(), listView);
                                }
                            }
                        }
                    });
                } finally {
                    cursor.close();
                }
            }
        }).start();
    }

    private void scrollToPos(int pos, ListView listView) {
        if (pos > listView.getCount() || pos < 0) {
            return;
        }
        listView.setSelection(pos);
    }
}
