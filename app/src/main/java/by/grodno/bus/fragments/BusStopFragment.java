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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.grodno.bus.BusApplication;
import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;
import by.grodno.bus.adapters.BusStopAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class BusStopFragment extends Fragment {
    private String mBusId;
    private String mStopId;

    public static Fragment getNewFragment(Bundle args) {
        Fragment fragment = new BusStopFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, null);
        ListView listView = (ListView) view.findViewById(R.id.list);
        Bundle args = getArguments();
        if (args != null) {
            mBusId = args.getString(DBManager.BUS_ID);
            mStopId = args.getString(DBManager.STOP_ID);
        }
        initList(listView);
        return view;
    }

    private void initList(final ListView listView) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        DBManager dbManager = ((BusApplication) activity.getApplication()).getDBManager();
        if ((TextUtils.isEmpty(mBusId)) || (TextUtils.isEmpty(mStopId))) {
            return;
        }
        String day1 = CalendarHelper.getDay1(activity);
        String day2 = CalendarHelper.getDay2(activity);
        String query = DBManager.getBusStopSQL(day1, day2, mBusId, mStopId);
        new QueryHelper(dbManager).rawQuery(query, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                processQueryResult(cursor, listView);
            }
        });
    }

    private void processQueryResult(final Cursor cursor, final ListView listView) {
        final Handler handler = new Handler();
        final Activity activity = getActivity();
        if (activity == null){
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
                    while (!cursor.isAfterLast()) {
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
                            BaseAdapter adapter = new BusStopAdapter(activity, timesMap, hours);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } finally {
                    cursor.close();
                }
            }
        }).start();
    }

}
