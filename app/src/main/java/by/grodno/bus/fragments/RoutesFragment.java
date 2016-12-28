package by.grodno.bus.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import java.util.HashSet;
import java.util.Set;

import by.grodno.bus.BusApplication;
import by.grodno.bus.R;
import by.grodno.bus.adapters.RouteAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class RoutesFragment extends Fragment {
    public enum TransportKind {
        BUS,
        TROLLEYBUS
    }

    private Cursor mGroupCursor;
    private ExpandableListView mListView;

    public static final String TRANSPORTKIND = "transportkind";
    private static final String POSITION = "position";
    private static final String ROUTES = "routes";
    private static final String EXPANDED = "expanded";

    private TransportKind mKind;


    public static Fragment newInstance(Bundle args) {
        RoutesFragment fragment = new RoutesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGroupCursor != null) {
            mGroupCursor.close();
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
    }


    @Override
    public void onDestroyView() {
        saveState();
        super.onDestroyView();
    }

    private void saveState() {
        Activity activity = getActivity();
        if (activity == null || mListView == null) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(getSaveKey(), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(POSITION, mListView.getFirstVisiblePosition());
        Set<String> expanded = new HashSet<>();
        for (int i = 0; i < mListView.getCount(); i++) {
            if (mListView.isGroupExpanded(i)) {
                expanded.add(String.valueOf(i));
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet(EXPANDED, expanded);
        }
        editor.apply();
    }

    private void restoreState() {
        Activity activity = getActivity();
        if (activity == null || mListView == null) {
            return;
        }
        if (mGroupCursor == null || mGroupCursor.isClosed()) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(getSaveKey(), Activity.MODE_PRIVATE);
        int pos = prefs.getInt(POSITION, 0);
        scrollToPos(pos);
        Set<String> expanded = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            expanded = prefs.getStringSet(EXPANDED, null);
        }
        if (expanded != null) {
            for (String s : expanded) {
                int groupNo = Integer.parseInt(s);
                if ((groupNo >= 0) && (groupNo < mGroupCursor.getCount())) {
                    mListView.expandGroup(groupNo);
                }
            }
        }
    }

    private String getSaveKey() {
        if (mKind == null) {
            return ROUTES + TransportKind.BUS.toString();
        } else {
            return ROUTES + mKind.toString();
        }
    }

    private void scrollToPos(int pos) {
        if (pos > mListView.getCount() || pos < 0) {
            return;
        }
        mListView.setSelection(pos);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, null);
        Bundle b = getArguments();

        if (b != null) {
            int i = b.getInt(TRANSPORTKIND, TransportKind.BUS.ordinal());
            if (i == TransportKind.BUS.ordinal()) {
                mKind = TransportKind.BUS;
            } else if (i == TransportKind.TROLLEYBUS.ordinal()) {
                mKind = TransportKind.TROLLEYBUS;
            } else {
                mKind = TransportKind.BUS;
            }
        }
        initList(view);
        return view;
    }

    private void initList(View fragmentView) {
        final ActionBarActivity activity = (ActionBarActivity) getActivity();
        if (activity == null) {
            return;
        }

        final ActionBar mBar = activity.getSupportActionBar();


        mListView = (ExpandableListView) fragmentView.findViewById(R.id.routesList);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        final DBManager dbManager = ((BusApplication) activity.getApplication()).getDBManager();
        new QueryHelper(dbManager).rawQuery(DBManager.getRoutesSQL(mKind), new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                RouteAdapter adapter = new RouteAdapter(activity, cursor, dbManager, mKind);
                mListView.setOnChildClickListener(adapter);
                mListView.setAdapter(adapter);
                mGroupCursor = cursor;
                restoreState();
            }
        });
    }
}
