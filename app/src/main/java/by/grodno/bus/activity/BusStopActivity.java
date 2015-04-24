package by.grodno.bus.activity;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.FavouritiesItem;
import by.grodno.bus.db.QueryHelper;
import by.grodno.bus.fragments.BusStopFragment;

public class BusStopActivity extends DetailActivity {
    private String mBusId;
    private String mStopId;
    private String mStopName;
    private String mBusName;
    private String mTr;
    private String mDirection;


    @Override
    protected Fragment getFragment(Bundle bundle) {
        return BusStopFragment.getNewFragment(bundle);
    }

    @Override
    protected String getSQLDropDownDays() {
        return DBManager.getDaysByBusId(mBusId);
    }

    @Override
    protected void setBundle(Bundle bundle) {
        mBusId = bundle.getString(DBManager.BUS_ID);
        mStopId = bundle.getString(DBManager.STOP_ID);
        final String sql = DBManager.getBusStopName(mBusId, mStopId);
        DBManager dbManager = getDBManager();
        new QueryHelper(dbManager).rawQuery(sql, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                cursor.moveToFirst();
                mBusName = cursor.getString(0);
                mStopName = cursor.getString(1);
                mTr = cursor.getString(2);
                mDirection = cursor.getString(3);
                setTitle(mBusName + ", " + mStopName);
                cursor.close();
            }
        });
    }

    @Override
    protected FavouritiesItem getFavouritiesItem() {
        FavouritiesItem item = new FavouritiesItem();
        item.setBusName(mBusName);
        item.setStopName(mStopName);
        item.setTr(mTr);
        item.setDirectionName(mDirection);
        return item;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bus_stop_menu, menu);
        final MenuItem addToFav = menu.findItem(R.id.action_addtopref_item);
        final MenuItem delFromFav = menu.findItem(R.id.action_delfrompref_item);
        initFavouritiesItems(delFromFav, addToFav);

        MenuItem allBuses = menu.findItem(R.id.action_allbuses_item);
        allBuses.setIcon(R.drawable.bus);
        allBuses.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(BusStopActivity.this, StopRoutesActivity.class);
                intent.putExtra(DBManager.STOP_NAME, mStopName);
                intent.putExtra(DBManager.STOP_ID, mStopId);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }
}
