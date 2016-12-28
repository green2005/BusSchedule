package by.grodno.bus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.BusNamesPagerAdapter;
import by.grodno.bus.NamesSource;
import by.grodno.bus.R;
import by.grodno.bus.TrackingParams;


public class BusNameChoiceActivity extends AppCompatActivity {
    public static final int KEY = 1;
    private NamesSource mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_names_choice);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.bus_names)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.minibus_names)));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final BusNamesPagerAdapter adapter = new BusNamesPagerAdapter(getSupportFragmentManager(), getIntent().getExtras());
        mSource = adapter;
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bus_names_choice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_choose) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            List<String> busNames = new ArrayList<>();
            List<String> busTypes = new ArrayList<>();
            mSource.fillNames(busNames, busTypes);
            TrackingParams trackingParams = new TrackingParams(busNames, busTypes, "", "");
            bundle.putParcelable(TrackingParams.KEY, trackingParams);
            intent.putExtras(bundle);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        }else
        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
