package by.grodno.bus;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import by.grodno.bus.fragments.FavouritesFragment;
import by.grodno.bus.fragments.RoutesFragment;
import by.grodno.bus.fragments.StopsFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        TabItem[] items = TabItem.values();
        TabItem item = items[position];
        switch (item){
            case BUSES:{
                Bundle b = new Bundle();
                b.putInt(RoutesFragment.TRANSPORTKIND, RoutesFragment.TransportKind.BUS.ordinal());
                return RoutesFragment.newInstance(b);
            }
            case TROLL:{
                Bundle b = new Bundle();
                b.putInt(RoutesFragment.TRANSPORTKIND, RoutesFragment.TransportKind.TROLLEYBUS.ordinal());
                return RoutesFragment.newInstance(b);
            }
            case STOPS:{
                return StopsFragment.newInstance(null);
            }
            case FAVOURITIES:{
                return FavouritesFragment.newInstance(null);
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return TabItem.values().length;
    }
}
