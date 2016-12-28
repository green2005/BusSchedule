package by.grodno.bus;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.fragments.BusNamesFragment;

public class BusNamesPagerAdapter extends FragmentPagerAdapter implements NamesSource{
    private Bundle mParams;
    private List<NamesSource> mSources;


    public BusNamesPagerAdapter(FragmentManager fm, Bundle params) {
        super(fm);
        mParams = params;
        mSources = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            mParams.putString(TrackingParams.TRANSPORT_TYPE_KEY, TrackingParams.BUS_TYPE_KEY);
        } else if (position == 1) {
            mParams.putString(TrackingParams.TRANSPORT_TYPE_KEY, TrackingParams.MINI_BUS_TYPE_KEY);
        }
        BusNamesFragment fragment = BusNamesFragment.getBusNamesFragment(mParams);
        mSources.add(fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public void fillNames(List<String> busNames, List<String> busTypes) {
        for (NamesSource source:mSources){
            source.fillNames(busNames, busTypes);
        }
    }
}

