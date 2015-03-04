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
import by.grodno.bus.adapters.StopRoutesAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class StopRoutesFragment extends Fragment {
    private DBManager mDBManager;
    private String mStopName;
    private String mStopId;
    private Cursor mCursor;

    public static Fragment getNewFragment(Bundle args) {
        Fragment fragment = new StopRoutesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stop_routes, null);
        mStopId = getArguments().getString(DBManager.STOP_ID);
        mStopName = getArguments().getString(DBManager.STOP_NAME);
        mDBManager = ((BusApplication) getActivity().getApplication()).getDBManager();
        ListView listView = (ListView) view.findViewById(R.id.list);
        initList(listView);
        return view;
    }

    private void initList(final ListView listView) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        String query = DBManager.getStopRoutesSQL(CalendarHelper.getDay1(activity),
                CalendarHelper.getDay2(activity),
                CalendarHelper.getTime(),
                mStopId
        );
        new QueryHelper(mDBManager).rawQuery(query, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                mCursor = cursor;
                BaseAdapter stopRoutesAdapter = new StopRoutesAdapter(activity, cursor);
                listView.setAdapter(stopRoutesAdapter);
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
