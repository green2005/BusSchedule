package by.grodno.bus.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import by.grodno.bus.BusApplication;
import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;

public class StopsFragment extends Fragment {

    public static Fragment newInstance(Bundle params) {
        Fragment fragment = new StopsFragment();
        fragment.setArguments(params);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stops, null);
        TextView textView = (TextView)view.findViewById(R.id.stops);
        textView.setText(getSomeTextFromDB());
        return view;
    }

    private String getSomeTextFromDB(){
        DBManager mDBManager = ((BusApplication) getActivity().getApplication()).getDBManager();
        if (mDBManager == null) {
            return null;
        }
        Cursor cursor = mDBManager.getStops();
        try {
            cursor.moveToFirst();
            return cursor.getString(0);
        }finally {
            cursor.close();
        }
    }
}
