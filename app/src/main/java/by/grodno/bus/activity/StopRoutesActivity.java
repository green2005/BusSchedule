package by.grodno.bus.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import by.grodno.bus.R;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.FavouritiesItem;
import by.grodno.bus.fragments.StopRoutesFragment;

public class StopRoutesActivity extends DetailActivity {
    private String mStopName;
    private String mStopId;

    @Override
    protected Fragment getFragment(Bundle bundle) {
        return StopRoutesFragment.getNewFragment(bundle);
    }

    @Override
    protected String getSQLDropDownDays() {
        return DBManager.getDaysByStopId(mStopId);
    }

    @Override
    protected void setBundle(Bundle bundle) {
        mStopName = bundle.getString(DBManager.STOP_NAME);
        mStopId = bundle.getString(DBManager.STOP_ID);
        setTitle(mStopName);
    }

    @Override
    protected FavouritiesItem getFavouritiesItem() {
        FavouritiesItem item = new FavouritiesItem();
        item.setStopName(mStopName);
        return item;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menupref, menu);
        final MenuItem addToFav = menu.findItem(R.id.action_addtopref);
        final MenuItem delFromFav = menu.findItem(R.id.action_delfrompref);
        initFavouritiesItems(addToFav, delFromFav);
        return true;
    }

}