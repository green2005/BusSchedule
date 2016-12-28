package by.grodno.bus.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.R;
import by.grodno.bus.TrackingParams;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.FavouritiesItem;
import by.grodno.bus.db.QueryHelper;
import by.grodno.bus.fragments.RouteStopsFragment;

public class RouteStopsActivity extends DetailActivity {
    private String mBusId;

    private String mBusName;
    private String mRouteName;
    private int mTr;

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

                //TODO get rid of cursor.getString(0)
                setTitle(cursor.getString(0) + ", " + cursor.getString(1));
                mBusName = cursor.getString(0);
                mRouteName = cursor.getString(1);
                mTr = cursor.getInt(2);
                cursor.close();
            }
        });
    }

    @Override
    protected FavouritiesItem getFavouritiesItem() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem item = menu.findItem(R.id.action_map);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mTr == 1){
                    Toast.makeText(RouteStopsActivity.this, getString(R.string.tr_are_not_tracking), Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent intent = new Intent(RouteStopsActivity.this, GoogleMapsActivity.class);
                Bundle bundle = new Bundle();

                List<String> busNames = new ArrayList<>();
                List<String> busTypes = new ArrayList<>();
                busNames.add(mBusName);
                busTypes.add(TrackingParams.BUS_TYPE_KEY);
                TrackingParams params = new TrackingParams(busNames, busTypes, "", "");
                bundle.putParcelable(TrackingParams.KEY, params);

                intent.putExtras(bundle);
                startActivity(intent);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
