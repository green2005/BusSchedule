package by.grodno.bus.fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.List;

import by.grodno.bus.BusNamesAdapter;
import by.grodno.bus.NamesSource;
import by.grodno.bus.R;
import by.grodno.bus.TrackingParams;
import by.grodno.bus.db.DBContract;
import by.grodno.bus.db.Provider;


public class BusNamesFragment extends Fragment implements NamesSource {
    private TrackingParams mParams;
    private String mTransportType;
    BusNamesAdapter mAdapter;


    public static BusNamesFragment getBusNamesFragment(Bundle params) {
        BusNamesFragment fragment = new BusNamesFragment();
        fragment.setParams(params);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus_names_choice, null);
        initView(view);
        return view;
    }

    private void setParams(Bundle params) {
        if (params.containsKey(TrackingParams.KEY)) {
            mParams = params.getParcelable(TrackingParams.KEY);
        } else {
            mParams = null;
        }
        mTransportType = params.getString(TrackingParams.TRANSPORT_TYPE_KEY);
    }

    private void initView(View v) {
        final GridView gv = (GridView) v.findViewById(R.id.gridView);
        final Handler h = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Cursor cr = getContext().getContentResolver().query(Provider.ROUTES_GPS_CONTENT_URI,
                        new String[]{DBContract.MapRoutesColumns.NUM},
                        DBContract.MapRoutesColumns.TYPE + " = " + "\"" + mTransportType + "\"" + " group by " + DBContract.MapRoutesColumns.NUM +
                                " order by length(" + DBContract.MapRoutesColumns.NUM + "), " + DBContract.MapRoutesColumns.NUM,
                        null,
                        null
                );

                h.post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new BusNamesAdapter(cr, getActivity(), mParams);
                        gv.setAdapter(mAdapter);
                    }
                });
            }
        }).start();
    }


    @Override
    public void fillNames(List<String> busNames, List<String> busTypes) {
        mAdapter.fillChosenNames(busNames, busTypes, mTransportType);
    }
}
