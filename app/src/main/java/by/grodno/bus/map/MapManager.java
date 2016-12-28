package by.grodno.bus.map;


import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.API;
import by.grodno.bus.R;
import by.grodno.bus.TrackingParams;
import by.grodno.bus.db.CursorHelper;
import by.grodno.bus.db.DBContract;
import by.grodno.bus.db.Provider;
import by.grodno.bus.service.TrackingService;

public class MapManager {
    private static final int ROUTE_LINE_WIDTH = 4;

    private Context mContext;
    private GoogleMap mMap;
    private Intent mTrackingService;
    private Bundle mExtras;
    private List<Marker> mBusMarkers;

    private Polyline mPolyLine = null;
    private int mSelectedRouteId = 0;
    private MapController mMapController;

    public MapManager(Bundle extras, Context context, GoogleMap googleMap) {
        mContext = context;
        mMap = googleMap;
        mExtras = extras;
        mBusMarkers = new ArrayList<>();
        initMap();
        mMapController = new MapController(context, new MapController.OnInitdoneListener() {
            @Override
            public void onDone() {
                startTracking();
            }
        });
        mMapController.fillRoutes();
        mMapController.fillStops();
    }

    public void stopTracking() {
        mBusMarkers.clear();
        Intent intent = new Intent(mContext, TrackingService.class);
        mContext.stopService(intent);
    }

    public void changeExtras(Bundle extras) {
        stopTracking();
        mExtras = extras;
        startTracking();
    }

    public void startTracking() {
        mMap.clear();
        mSelectedRouteId = 0;
        mTrackingService = new Intent(mContext, TrackingService.class);
        mTrackingService.putExtras(mExtras);
        mContext.startService(mTrackingService);
        Handler h = new Handler();
        mContext.getContentResolver().registerContentObserver(Provider.BUS_GPS_CONTENT_URI, false, new ContentObserver(h) {
            @Override
            public void onChange(boolean selfChange) {
                drawTrackingPoints();
            }
        });
    }

    private void drawTrackingPoints() {
        final List<MarkerOptions> optionsList = new ArrayList<>();
        final Handler h = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                setBusPoints(optionsList);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        drawBusPoints(optionsList);
                    }
                });
            }
        }).start();
    }

    private void drawBusPoints(List<MarkerOptions> optionsList) {
        for (Marker m : mBusMarkers) {
            m.remove();
        }
        mBusMarkers.clear();
        for (MarkerOptions options : optionsList) {
            Marker point;
            point = mMap.addMarker(options);
            point.setTag(point.getTitle());
            mBusMarkers.add(point);
            point.setVisible(true);
        }
    }

    public void onMarkerClick(final Marker marker) {
        class MObserver extends ContentObserver {
            private MObserver(Handler handler) {
                super(handler);
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                mContext.getContentResolver().unregisterContentObserver(this);
                drawRoute();
            }
        }

        if (mPolyLine != null) {
            mPolyLine.remove();
        }
        int rid = Integer.parseInt((String) marker.getTag());
        if (rid == mSelectedRouteId) {
            mSelectedRouteId = 0;
            return;
        }
        mSelectedRouteId = rid;
        Handler h = new Handler();
        final ContentObserver observer = new MObserver(h);
        mContext.getContentResolver().registerContentObserver(Provider.ROUTE_NODES_GPS_CONTENT_URI, false, observer);
        mMapController.fillRouteNodes(mSelectedRouteId);
    }

    private void drawRoute() {
        PolylineOptions options = new PolylineOptions();
        Cursor cr = mContext.getContentResolver().query(Provider.ROUTE_NODES_GPS_CONTENT_URI, null, null, null, null);
        if (cr == null) {
            return;
        }
        try {
            cr.moveToFirst();
            while (!cr.isAfterLast()) {
                LatLng point = new LatLng(
                        CursorHelper.getDouble(cr, DBContract.MapRouteNodesColumns.LAT),
                        CursorHelper.getDouble(cr, DBContract.MapRouteNodesColumns.LON)
                );
                options.add(point);
                cr.moveToNext();
            }
            options.width(ROUTE_LINE_WIDTH);
            options.color(Color.MAGENTA);
            mPolyLine = mMap.addPolyline(options);

        } finally {
            cr.close();
        }
    }

    private void setBusPoints(List<MarkerOptions> optionsList) {
        //Bitmap busBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_bus2).copy(Bitmap.Config.ARGB_8888, true);
       // Bitmap miniBusBmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_marsh2).copy(Bitmap.Config.ARGB_8888, true);

        Cursor cr = mContext.getContentResolver().query(Provider.BUS_GPS_CONTENT_URI, null, null, null, null);
        if (cr == null) {
            return;
        }
        try {
            cr.moveToFirst();
            while (!cr.isAfterLast()) {
                String num = CursorHelper.getString(cr, DBContract.MapBusCoordsColumns.NUM);
                int rotation = CursorHelper.getInt(cr, DBContract.MapBusCoordsColumns.DIR);
                LatLng busCoords = new LatLng(
                        CursorHelper.getDouble(cr, DBContract.MapBusCoordsColumns.LAT),
                        CursorHelper.getDouble(cr, DBContract.MapBusCoordsColumns.LON)
                );
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(CursorHelper.getInt(cr, DBContract.MapBusCoordsColumns.RID) + "");
                Bitmap mbmp;
                if (CursorHelper.getString(cr, DBContract.MapBusCoordsColumns.FTYPE).equalsIgnoreCase(TrackingParams.BUS_TYPE_KEY)) {
                    mbmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_bus2).copy(Bitmap.Config.ARGB_8888, true);
                } else {
                    mbmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_marsh2).copy(Bitmap.Config.ARGB_8888, true);
                }
                Canvas canvas = new Canvas(mbmp);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.rgb(61, 61, 61));
                paint.setTextSize((int) (28));
                Rect bounds = new Rect();
                paint.getTextBounds(num, 0, num.length(), bounds);
                int x = mbmp.getWidth() / 2 - bounds.width() / 2;//(mbmp.getWidth() - bounds.width())/2;
                int y = (int) ((mbmp.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));// (mbmp.getHeight() + bounds.height())/2;

                canvas.rotate(-rotation, mbmp.getWidth() / 2, mbmp.getHeight() / 2);

                canvas.drawText(num, x, y, paint);

                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mbmp));
                markerOptions.position(busCoords);

                markerOptions.rotation(rotation);
                optionsList.add(markerOptions);
                cr.moveToNext();
            }
        } finally {
            cr.close();
        }
    }

    private void initMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng latLng = new LatLng(API.CITY_LAT, API.CITY_LON);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mMap.animateCamera(cameraUpdate);
    }


}
