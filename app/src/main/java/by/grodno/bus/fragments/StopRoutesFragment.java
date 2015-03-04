package by.grodno.bus.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import by.grodno.bus.BusApplication;
import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;

public class StopRoutesFragment extends Fragment {
    private DBManager mDBManager;
    private String mStopName;
    private String mStopId;

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
        mDBManager = ((BusApplication)getActivity().getApplication()).getDBManager();
        ListView listView = (ListView) view.findViewById(R.id.list);
        initList(listView);
        return view;
    }

    private void initList(ListView listView) {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        Date now = new Date();
        String strTime = sdfTime.format(now);
        String query = DBManager.getStopRoutesSQL();

    }
}
