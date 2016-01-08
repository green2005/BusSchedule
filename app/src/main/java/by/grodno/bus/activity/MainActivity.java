package by.grodno.bus.activity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import by.grodno.bus.BusApplication;
import by.grodno.bus.ErrorHelper;
import by.grodno.bus.R;
import by.grodno.bus.TabItem;
import by.grodno.bus.TabsPagerAdapter;
import by.grodno.bus.adapters.SearchAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.DBUpdater;
import by.grodno.bus.db.QueryHelper;
import by.grodno.bus.db.UpdateListener;

public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.TabListener {
    private ViewPager mViewPager;
    private Cursor mSearchCursor;
    private DBManager mDBManager;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mDBManager = ((BusApplication) getApplication()).getDBManager();
        if (mDBManager.dbExists()) {
            mDBManager.openDB();    
            initTabs();
            updateDB(true);
        } else {
            updateDB(false);
        }
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_LONG).show();
        }
    }

    private void updateDB(final boolean silent) {
        if ((!mDBManager.dbExists()) || (DBUpdater.needCheckUpdate(this))) {
            DBUpdater.setCheckDate(this);

            UpdateListener listener = new UpdateListener() {
                @Override
                public void onError(String error) {
                    if (!silent) {
                        ErrorHelper.showErrorDialog(error, MainActivity.this, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onError(int errorResId) {
                    if (!silent) {
                        ErrorHelper.showErrorDialog(errorResId, MainActivity.this, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onSuccess(String updatedDate) {
                    if (!silent) {
                        if (!TextUtils.isEmpty(updatedDate)) {
                            mDBManager.openDB();
                            initTabs();
                        } else {
                            finish();
                        }
                    } else {
                        if (!TextUtils.isEmpty(updatedDate)) {
                            Toast.makeText(MainActivity.this, R.string.scheduleUpdated, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            };
            mDBManager.updateDB(listener, silent, this);
        }
    }


    private void initTabs() {
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);



        ActionBar.Tab tab;
        String firstItem = getFirstItem();
        TabItem items[];
        items = TabItem.values();
        if (!TextUtils.isEmpty(firstItem)) {
            switch (firstItem) {
                case "1": {
                    break;
                }
                case "2": {
                    items[0] = TabItem.TROLL;
                    items[1] = TabItem.BUSES;
                    items[2] = TabItem.STOPS;
                    items[3] = TabItem.FAVOURITIES;
                    break;
                }
                case "3": {
                    items[0] = TabItem.STOPS;
                    items[1] = TabItem.BUSES;
                    items[2] = TabItem.TROLL;
                    items[3] = TabItem.FAVOURITIES;
                    break;
                }
                case "4": {
                    items[0] = TabItem.FAVOURITIES;
                    items[1] = TabItem.BUSES;
                    items[2] = TabItem.TROLL;
                    items[3] = TabItem.STOPS;
                    break;
                }
            }
        }
        for (TabItem item : items) {
            tab = mActionBar.newTab();
            tab.setIcon(item.getIcon());
            tab.setTabListener(this);
            mActionBar.addTab(tab);
        }
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), items);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private String getFirstItem() {
        Context appContext = getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(DBUpdater.getPreferencesFileName(appContext), Context.MODE_PRIVATE);
        String firstItemKey = getResources().getString(R.string.firstItemKey);
        return prefs.getString(firstItemKey, "");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if (mDBManager != null) {
        //    mDBManager.close();
        //    ((BusApplication)getApplication()).setDBManager(null);
        // }
        if (mSearchCursor != null && !mSearchCursor.isClosed()) {
            mSearchCursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, PrefActivity.class);
                startActivity(intent);
                return true;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            searchItem.setVisible(false);
        } else {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setQueryHint(
                    getResources().getString(R.string.search_hint));

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setIconifiedByDefault(true);
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int i) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int i) {
                    if (mSearchCursor != null && !mSearchCursor.isClosed()) {
                        mSearchCursor.moveToPosition(i);
                        String stopName = mSearchCursor.getString(mSearchCursor.getColumnIndex(DBManager.STOP_NAME));
                        String stopId = mSearchCursor.getString(mSearchCursor.getColumnIndex("_id"));
                        Intent intent = new Intent(MainActivity.this, StopRoutesActivity.class);
                        intent.putExtra(DBManager.STOP_NAME, stopName);
                        intent.putExtra(DBManager.STOP_ID, stopId);
                        MainActivity.this.startActivity(intent);
                    }
                    return false;
                }
            });


            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    // Toast.makeText(MainActivity.this, "Здесь будет поиск", Toast.LENGTH_SHORT).show();


                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    loadSuggestions(s, menu);
                    return true;
                }
            });
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadSuggestions(String query, final Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (TextUtils.isEmpty(query) || query.length() <= 2) {
                return;
            }
            String sql = DBManager.getStopsLikeSQL(query);
            new QueryHelper(mDBManager).rawQuery(sql, new QueryHelper.QueryListener() {
                @Override
                public void onQueryCompleted(Cursor cursor) {
                    final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
                    if (cursor.getCount() > 0) {
                        mSearchCursor = cursor;
                        search.setIconified(false);
                        search.setSuggestionsAdapter(new SearchAdapter(MainActivity.this, cursor));
                    } else {
                        if (mSearchCursor != null && !mSearchCursor.isClosed()) {
                            mSearchCursor.close();
                        }
                        search.setSuggestionsAdapter(null);
                    }
                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}
