package by.grodno.bus.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import by.grodno.bus.BusApplication;
import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.QueryHelper;
import by.grodno.bus.fragments.RouteStopsFragment;

public class RouteStopsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Bundle bundle = getIntent().getExtras();

        //String busName = bundle.getString(DBManager.BUS_NAME);
        //setTitle(busName);
        setTitle(bundle);
        
        Fragment fragment = RouteStopsFragment.getNewFragment(bundle);
        ft.replace(R.id.container, fragment);
        ft.commit();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTitle(Bundle bundle){
        String busId = bundle.getString(DBManager.BUS_ID);
        DBManager dbManager = ((BusApplication) getApplication()).getDBManager();
        final String sql = DBManager.getRoute(busId);
        new QueryHelper(dbManager).rawQuery(sql, new QueryHelper.QueryListener() {
            @Override
            public void onQueryCompleted(Cursor cursor) {
                cursor.moveToFirst();
                setTitle(cursor.getString(0)+ ", " + cursor.getString(1));
            }
        });

    }
}
