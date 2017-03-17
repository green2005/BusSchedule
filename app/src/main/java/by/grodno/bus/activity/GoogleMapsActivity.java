package by.grodno.bus.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import by.grodno.bus.R;
import by.grodno.bus.map.GPSTracker;
import by.grodno.bus.map.MapManager;

public class GoogleMapsActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener {
    public static final String ERROR_ACTION = "by.grodno.bus.ERR";
    public static final String ERROR_MSG = "error_msg";


    private GoogleMap mMap;
    private MapManager mMapManager;
    private ErrorReciever mReciever;

    private class ErrorReciever extends BroadcastReceiver {
        private Context mContext;

        ErrorReciever(Context context) {
            mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.error);
            if (b != null) {
                builder.setMessage(b.getString(ERROR_MSG));
            }
            builder.show();
        }
    }


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
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {


                    }
                });
                mMapManager = new MapManager(getIntent().getExtras(), GoogleMapsActivity.this, mMap);

            }
        };
        mapFragment.getMapAsync(onMapReadyCallback);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        final ImageButton chooseBuses = (ImageButton) findViewById(R.id.bus_on_map);
        chooseBuses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseBuses();
            }
        });

        final ImageButton showLocation = (ImageButton) findViewById(R.id.me_on_map);
        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(GoogleMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(GoogleMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    GPSTracker tracker = new GPSTracker(GoogleMapsActivity.this);
                    tracker.getPosition(new GPSTracker.GPSTrackerListener() {
                        @Override
                        public void onChange(double lat, double lng) {
                            if (mMapManager != null) {
                                mMapManager.drawMeOnMap(lat, lng);
                            }
                        }
                    });
                }
            }
        });

        final ImageButton routes_on_map = (ImageButton) findViewById(R.id.routes_on_map);
        routes_on_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMapManager != null) {
                    mMapManager.drawRoutes(0);
                }
            }
        });

       /* final ImageButton stops_on_route = (ImageButton) findViewById(R.id.stop_on_map);
        stops_on_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapManager.setAddTrackStops(true, 0);
                mMapManager.stopTracking();
                mMapManager.startTracking();
            }
        });*/
        mReciever = new ErrorReciever(this);
        registerReceiver(mReciever, new IntentFilter(ERROR_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReciever);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (outState == null) {
            outState = new Bundle();
        }
        outState.putAll(mMapManager.getExtras());
        mMapManager.stopTracking(true);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        mMapManager = new MapManager(savedInstanceState, GoogleMapsActivity.this, mMap);
        //super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMapManager!=null) {
            mMapManager.stopTracking(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMapManager != null) {
            mMapManager.startTracking(true);
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
            return true;
        } else if (item.getItemId() == R.id.actionNavigate) {

            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseBuses() {
        Intent intent = new Intent(GoogleMapsActivity.this, BusNameChoiceActivity.class);
        intent.putExtras(mMapManager.getExtras());
        startActivityForResult(intent, BusNameChoiceActivity.KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (requestCode == BusNameChoiceActivity.KEY)) {
            mMapManager.changeExtras(data.getExtras());
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        mMapManager.onMarkerClick(marker);
        return true;
    }
}
