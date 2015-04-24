package by.grodno.bus.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.FavouritiesItem;
import by.grodno.bus.db.QueryHelper;
import by.grodno.bus.fragments.RouteStopsFragment;

public class RouteStopsActivity extends DetailActivity {
    private String mBusId;

    @Override
    protected Fragment getFragment(Bundle bundle) {
        return RouteStopsFragment.getNewFragment(bundle);
    }

    @Override
    protected String getSQLDropDownDays() {
        return DBManager.getDaysByBusId(mBusId);
    }

    @Override
    protected void setBundle(Bundle bundle) {
        mBusId = bundle.getString(DBManager.BUS_ID);
        final DBManager manager = getDBManager();
        final String sql = DBManager.getRoute(mBusId);
        new QueryHelper(manager).rawQuery(sql, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                cursor.moveToFirst();
                setTitle(cursor.getString(0) + ", " + cursor.getString(1));
                cursor.close();
            }
        });
    }

    @Override
    protected FavouritiesItem getFavouritiesItem() {
        return null;
    }

}
