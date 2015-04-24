package by.grodno.bus.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import by.grodno.bus.BusApplication;
import by.grodno.bus.CalendarHelper;
import by.grodno.bus.OnDateChangedListener;
import by.grodno.bus.R;
import by.grodno.bus.adapters.RouteStopsAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class RouteStopsFragment extends Fragment implements OnDateChangedListener {
    private Cursor mCursor;
    private String mBusId;
    DBManager mDbManager;
    RouteStopsAdapter mAdapter;


    public static Fragment getNewFragment(Bundle args) {
        Fragment fragment = new RouteStopsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBusId = getArguments().getString(DBManager.BUS_ID);
        View view = inflater.inflate(R.layout.fragment_detail, null);
        ListView listView = (ListView) view.findViewById(R.id.list);
        String day1 = CalendarHelper.getDay1(getActivity());
        String day2 = CalendarHelper.getDay2(getActivity());
        initList(listView, day1, day2);
        return view;
    }

    private void initList(final ListView listView, String day1, String day2) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        mDbManager = ((BusApplication) activity.getApplication()).getDBManager();
        String time = CalendarHelper.getTime();
        String sql = DBManager.getRouteStopsSQL(day1, day2, time, mBusId);
        new QueryHelper(mDbManager).rawQuery(sql, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                mCursor = cursor;
                mAdapter = new RouteStopsAdapter(mCursor, activity);
                listView.setOnItemClickListener(mAdapter);
                listView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public void onChange(String day) {
        String time = CalendarHelper.getTime();
        String sql = DBManager.getRouteStopsSQL(day, "", time, mBusId);
        new QueryHelper(mDbManager).rawQuery(sql, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                mCursor = cursor;
                mAdapter.swapCursor(mCursor);
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
