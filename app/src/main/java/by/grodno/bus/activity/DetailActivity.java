package by.grodno.bus.activity;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.BusApplication;
import by.grodno.bus.CalendarHelper;
import by.grodno.bus.OnDateChangedListener;
import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.FavouritiesDBHelper;
import by.grodno.bus.db.FavouritiesItem;
import by.grodno.bus.db.QueryHelper;

public abstract class DetailActivity extends ActionBarActivity {

    private Fragment mFragment;
    private MenuItem mDelFromFavItem;
    private MenuItem mAddToFavItem;

    protected abstract Fragment getFragment(Bundle bundle);
    protected abstract String getSQLDropDownDays();
    protected abstract void setBundle(Bundle bundle);
    protected abstract FavouritiesItem getFavouritiesItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle bundle = getIntent().getExtras();
        setBundle(bundle);
        mFragment = getFragment(bundle); //StopRoutesFragment.getNewFragment(bundle);
        if (mFragment == null){
            throw new IllegalArgumentException("Fragment is null");
        }
        ft.replace(R.id.container, mFragment);
        ft.commit();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initDaysPopup();
    }

    protected void initFavouritiesItems(MenuItem delFromFavItem, MenuItem addToFavItem){
        mDelFromFavItem = delFromFavItem;
        mAddToFavItem = addToFavItem;
        mAddToFavItem.setIcon(android.R.drawable.star_big_on);
        mDelFromFavItem.setIcon(android.R.drawable.star_big_off);

        FavouritiesItem item = getFavouritiesItem();
        getDBManager().getIsInFavourities(item.getBusName(), item.getStopName(), item.getTr(), new FavouritiesDBHelper.FavoritiesListener() {
            @Override
            public void onGetFavourities(List<FavouritiesItem> favouritiesItems) {
            }

            @Override
            public void onGetCursor(Cursor cursor) {
                if (cursor.getCount() == 0) {
                    mAddToFavItem.setVisible(true);
                    mDelFromFavItem.setVisible(false);
                } else {
                    mAddToFavItem.setVisible(false);
                    mDelFromFavItem.setVisible(true);
                }
                cursor.close();
            }
        });

        mAddToFavItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addToFav();
                mDelFromFavItem.setVisible(true);
                mAddToFavItem.setVisible(false);
                return false;
            }
        });

        mDelFromFavItem.setVisible(false);
        mDelFromFavItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                delFromFav();
                mDelFromFavItem.setVisible(false);
                mAddToFavItem.setVisible(true);
                return false;
            }
        });
    }

    private void addToFav(){
        FavouritiesItem item = getFavouritiesItem();
        if (item != null) {
            getDBManager().addToFavourities(getFavouritiesItem());
        }
    }

    private void delFromFav(){
        FavouritiesItem item = getFavouritiesItem();
        if (item != null) {
            getDBManager().addToFavourities(getFavouritiesItem());
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected DBManager getDBManager(){
        return ((BusApplication) getApplication()).getDBManager();
    }

    private void initDaysPopup() {
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        final List<String> daysList = new ArrayList<>();
        new QueryHelper(getDBManager()).rawQuery(getSQLDropDownDays(), new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                String day1 = CalendarHelper.getDay1(getApplicationContext());
                String day2 = CalendarHelper.getDay2(getApplicationContext());
                int selPos = -1;
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        daysList.add(cursor.getString(0));
                        if (selPos == -1) {
                            if ((day1.equals(cursor.getString(0))) || (day2.equals(cursor.getString(0)))) {
                                selPos = daysList.size() - 1;
                            }
                        }
                        cursor.moveToNext();
                    }
                } finally {
                    cursor.close();
                }

                ActionBar.OnNavigationListener onNavigationListener = new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                        String day = daysList.get(itemPosition);
                        ((OnDateChangedListener) mFragment).onChange(day);
                        return true;
                    }
                };

                Context context = getSupportActionBar().getThemedContext();
                SpinnerAdapter spinnerAdapter = new ArrayAdapter(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        daysList);
                getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, onNavigationListener);
                if (selPos != -1) {
                    getSupportActionBar().setSelectedNavigationItem(selPos);
                }
            }
        });
    }



}
