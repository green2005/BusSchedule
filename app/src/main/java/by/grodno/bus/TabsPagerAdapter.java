package by.grodno.bus;


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
            case ROUTES:{
                Fragment fragment = RoutesFragment.newInstance(null);
                return fragment;
            }
            case STOPS:{
                Fragment fragment = StopsFragment.newInstance(null);
                return fragment;
            }
            case FAVOURITIES:{
                Fragment fragment = FavouritesFragment.newInstance(null);
                return fragment;
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return TabItem.values().length;
    }
}
