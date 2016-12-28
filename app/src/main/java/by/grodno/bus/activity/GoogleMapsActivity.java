package by.grodno.bus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import by.grodno.bus.map.MapManager;
import by.grodno.bus.R;

public class GoogleMapsActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private MapManager mMapManager;
    private Bundle mExtras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setOnMarkerClickListener(GoogleMapsActivity.this);
                mExtras = getIntent().getExtras();
                mMapManager = new MapManager(mExtras, GoogleMapsActivity.this, mMap);
            }
        };
        mapFragment.getMapAsync(onMapReadyCallback);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (outState == null) {
            outState = new Bundle();
        }
        outState.putAll(mExtras);
        mMapManager.stopTracking();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        mExtras = savedInstanceState;
        mMapManager = new MapManager(mExtras, GoogleMapsActivity.this, mMap);
        //super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapManager.stopTracking();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMapManager != null) {
            mMapManager.startTracking();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionChooseBusNames) {
            Intent intent = new Intent(GoogleMapsActivity.this, BusNameChoiceActivity.class);
            intent.putExtras(mExtras);
            startActivityForResult(intent, BusNameChoiceActivity.KEY);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (requestCode == BusNameChoiceActivity.KEY)) {
            mExtras = data.getExtras();
            mMapManager.changeExtras(mExtras);
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        mMapManager.onMarkerClick(marker);
        return true;
    }
}
