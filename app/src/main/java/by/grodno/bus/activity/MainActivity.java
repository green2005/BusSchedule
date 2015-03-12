package by.grodno.bus.activity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import by.grodno.bus.BusApplication;
import by.grodno.bus.ErrorHelper;
import by.grodno.bus.R;
import by.grodno.bus.TabItem;
import by.grodno.bus.TabsPagerAdapter;
import by.grodno.bus.db.DBManager;
import by.grodno.bus.db.UpdateListener;

public class MainActivity extends ActionBarActivity implements android.support.v7.app.ActionBar.TabListener {
    private ViewPager mViewPager;
    private DBManager mDBManager;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mDBManager = new DBManager(this);
        ((BusApplication) getApplication()).setDBManager(mDBManager);
        if (!mDBManager.dbExists()) {
            UpdateListener listener = new UpdateListener() {
                @Override
                public void onError(String error) {
                    ErrorHelper.showErrorDialog(error, MainActivity.this, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }

                @Override
                public void onError(int errorResId) {
                    ErrorHelper.showErrorDialog(errorResId, MainActivity.this, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }

                @Override
                public void onSuccess(String updatedDate) {
                    if (!TextUtils.isEmpty(updatedDate)) {
                        mDBManager.openDB();
                        initTabs();
                    } else {
                        finish();
                    }
                }
            };
            mDBManager.updateDB(listener, false);
        } else {
            initTabs();
        }

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_LONG).show();//doMySearch(query);
        }
    }

    private void initTabs() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        android.support.v7.app.ActionBar.Tab tab;
        TabItem items[] = TabItem.values();
        for (TabItem item : items) {
            tab = actionBar.newTab();
            tab.setText(item.getText());
            tab.setIcon(item.getIcon());
            tab.setTabListener(this);
            actionBar.addTab(tab);
        }
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBManager.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(
                getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast.makeText(MainActivity.this, "Здесь будет поиск", Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });
        return true;
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
