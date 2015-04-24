package by.grodno.bus.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import by.grodno.bus.BusApplication;
import by.grodno.bus.R;
import by.grodno.bus.activity.StopRoutesActivity;
import by.grodno.bus.adapters.StopsAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;
import by.grodno.indexableListView.IndexableListView;


public class StopsFragment extends Fragment {
    private Cursor mCursor;
    private IndexableListView mListView;

    private static final String STOPS = "stops";
    private static final String POSITION = "position";


    public static Fragment newInstance(Bundle params) {
        Fragment fragment = new StopsFragment();
        fragment.setArguments(params);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops, null);
        initList(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveState();
    }

    private void saveState() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(STOPS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(POSITION, mListView.getFirstVisiblePosition());
        editor.apply();
    }

    private void restoreState() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(STOPS, Activity.MODE_PRIVATE);
        int pos = prefs.getInt(POSITION, 0);
        scrollToPos(pos);
    }

    private void scrollToPos(int pos) {
        if (pos > mListView.getCount() || pos < 0) {
            return;
        }
        mListView.setSelection(pos);
    }

    private void initList(View fragmentView) {
        DBManager mDBManager = ((BusApplication) getActivity().getApplication()).getDBManager();
        mListView = (IndexableListView) fragmentView.findViewById(R.id.stopslist);
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (mDBManager != null) {
            new QueryHelper(mDBManager).rawQuery(DBManager.getStopsSQL(), new QueryHelper.QueryListener() {
                @Override
                public void onQueryCompleted(Cursor cursor) {
                    mCursor = cursor;
                    StopsAdapter adapter = new StopsAdapter(activity, cursor);
                    mListView.setAdapter(adapter);
                    mListView.setFastScrollEnabled(true);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mCursor.moveToPosition(position);
                            String stopName = mCursor.getString(mCursor.getColumnIndex(DBManager.STOP_NAME));
                            String stopId = mCursor.getString(mCursor.getColumnIndex(DBManager.STOP_ID));
                            Intent intent = new Intent(activity, StopRoutesActivity.class);
                            intent.putExtra(DBManager.STOP_NAME, stopName);
                            intent.putExtra(DBManager.STOP_ID, stopId);
                            activity.startActivity(intent);
                        }
                    });
                    restoreState();
                }
            });
        }
    }
}
