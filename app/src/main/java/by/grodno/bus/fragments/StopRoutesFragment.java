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

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.BusApplication;
import by.grodno.bus.CalendarHelper;
import by.grodno.bus.OnDateChangedListener;
import by.grodno.bus.R;
import by.grodno.bus.adapters.StopRoutesAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class StopRoutesFragment extends Fragment implements OnDateChangedListener {
    private DBManager mDBManager;
    private String mStopId;
    private Cursor mCursor;
    private StopRoutesAdapter mStopRoutesAdapter;

    public static Fragment getNewFragment(Bundle args) {
        Fragment fragment = new StopRoutesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, null);
        mStopId = getArguments().getString(DBManager.STOP_ID);
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
        List<String> days = new ArrayList<>();
        days.add(CalendarHelper.getDay1(activity));
        days.add(CalendarHelper.getDay2(activity));
        String query = DBManager.getStopRoutesSQL(
                days,
                CalendarHelper.getTime(),
                mStopId
        );
        new QueryHelper(mDBManager).rawQuery(query, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                mCursor = cursor;
                mStopRoutesAdapter = new StopRoutesAdapter(activity, cursor);
                listView.setOnItemClickListener(mStopRoutesAdapter);
                listView.setAdapter(mStopRoutesAdapter);
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
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        List<String> days = CalendarHelper.getDaySynonims(day, activity);
        String query = DBManager.getStopRoutesSQL(
                days,
                CalendarHelper.getTime(),
                mStopId
        );
        new QueryHelper(mDBManager).rawQuery(query, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                mCursor = cursor;
                mStopRoutesAdapter.swapCursor(mCursor);
                mStopRoutesAdapter.notifyDataSetChanged();
            }
        });
    }
}
