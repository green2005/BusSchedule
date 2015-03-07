package by.grodno.bus.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import by.grodno.bus.BusApplication;
import by.grodno.bus.CalendarHelper;
import by.grodno.bus.R;
import by.grodno.bus.adapters.RouteStopsAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class RouteStopsFragment extends Fragment {
    private Cursor mCursor;
    private String mBusId;

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
        initList(listView);
        return view;
    }

    private void initList(final ListView listView) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        DBManager dbManager = ((BusApplication) activity.getApplication()).getDBManager();
        String day1 = CalendarHelper.getDay1(activity);
        String day2 = CalendarHelper.getDay2(activity);
        String time = CalendarHelper.getTime();
        String sql = DBManager.getRouteStopsSQL(day1, day2, time, mBusId);
        new QueryHelper(dbManager).rawQuery(sql, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                mCursor = cursor;
                RouteStopsAdapter adapter = new RouteStopsAdapter(mCursor, activity);
                listView.setOnItemClickListener(adapter);
                listView.setAdapter(adapter);

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
}
