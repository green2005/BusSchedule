package by.grodno.bus.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.HashSet;
import java.util.Set;

import by.grodno.bus.BusApplication;
import by.grodno.bus.R;
import by.grodno.bus.adapters.RouteAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;

public class RoutesFragment extends Fragment {
    private Cursor mGroupCursor;
    private ExpandableListView mListView;

    private static final String POSITION = "position";
    private static final String ROUTES = "routes";
    private static final String EXPANDED = "expanded";

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
        if (activity == null) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(ROUTES, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(POSITION, mListView.getFirstVisiblePosition());
        Set<String> expanded = new HashSet<>();
        for (int i = 0; i < mListView.getCount(); i++) {
            if (mListView.isGroupExpanded(i)) {
                expanded.add(String.valueOf(i));
            }
        }
        editor.putStringSet(EXPANDED, expanded);
        editor.apply();
    }

    private void restoreState() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(ROUTES, Activity.MODE_PRIVATE);
        int pos = prefs.getInt(POSITION, 0);
        scrollToPos(pos);
        Set<String> expanded = prefs.getStringSet(EXPANDED, null);
        if (expanded != null) {
            for (String s : expanded) {
                mListView.expandGroup(Integer.parseInt(s));
            }
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
        initList(view);
        return view;
    }

    private void initList(View fragmentView) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        mListView = (ExpandableListView) fragmentView.findViewById(R.id.routesList);

        final DBManager dbManager = ((BusApplication) activity.getApplication()).getDBManager();
        new QueryHelper(dbManager).rawQuery(DBManager.getRoutesSQL(), new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                RouteAdapter adapter = new RouteAdapter(activity, cursor, dbManager);
                mListView.setOnChildClickListener(adapter);
                mListView.setAdapter(adapter);
                mGroupCursor = cursor;
                restoreState();
            }
        });
    }
}
