package by.grodno.bus.map;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.HashSet;
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

    private Bundle mExtras;
    private List<Marker> mBusMarkers;
    private List<Polyline> mRouteLines;
    private HashSet<Integer> mRIds;
    private int mSelectedRouteId = 0;
    private MapController mMapController;
    private int[] ROUTE_COLORS = {Color.BLUE, Color.CYAN, Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.RED, Color.DKGRAY};
    private List<Marker> mStopMarkers;
    private Bitmap mbmBus;
    private Bitmap mbmMiniBus;
    private boolean mNeedSaveSettings = false;
    private static final int BUS_NO_TEXT_SIZE = 28;

    public MapManager(Bundle extras, Context context, GoogleMap googleMap) {
        mContext = context;
        mMap = googleMap;
        loadExtras(extras);
        mBusMarkers = new ArrayList<>();
        mRouteLines = new ArrayList<>();
        mStopMarkers = new ArrayList<>();
        mRIds = new HashSet<>();
        initMap();
        mMapController = new MapController(context, new MapController.OnInitdoneListener() {
            @Override
            public void onDone() {
                startTracking(true);
            }
        });
        mMapController.fillRoutes();
        mMapController.fillStops();
        mbmBus = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_bus2);
        mbmMiniBus = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_marsh2);

    }

    private void loadExtras(Bundle extras) {
        TrackingParams params = null;
        if (extras != null) {
            params = extras.getParcelable(TrackingParams.KEY);
        }

        if ((extras == null) || (params == null)) {
            mNeedSaveSettings = true;
            //we load previously selected buses in this case
            SharedPreferences prefs = mContext.getSharedPreferences(TrackingParams.KEY, Context.MODE_PRIVATE);
            String s = "";
            if (prefs != null) {
                s = prefs.getString(TrackingParams.KEY, "");
            }
            TrackingParams trackingParams = TrackingParams.fromString(s);
            mExtras = new Bundle();
            mExtras.putParcelable(TrackingParams.KEY, trackingParams);
        } else {
            mExtras = extras;
        }
    }

    public Bundle getExtras() {
        return mExtras;
    }

    private void saveExtras() {
        TrackingParams trackingParams = mExtras.getParcelable(TrackingParams.KEY);
        if ((trackingParams != null) && (mNeedSaveSettings)) {
            SharedPreferences prefs = mContext.getSharedPreferences(TrackingParams.KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(TrackingParams.KEY, trackingParams.toString());
            editor.apply();
        }
    }


    public void setAddTrackStops(boolean addStops, int busId) {
        TrackingParams params = mExtras.getParcelable(TrackingParams.KEY);
        if (params != null) {
            params.setTrackStops(addStops, busId);
            mExtras.putParcelable(TrackingParams.KEY, params);
        }
    }

    public void stopTracking(boolean clearBusMarkers) {
        if (clearBusMarkers) {
            mBusMarkers.clear();
        }
        Intent intent = new Intent(mContext, TrackingService.class);
        mContext.stopService(intent);
    }

    public void changeExtras(Bundle extras) {
        stopTracking(true);
        mExtras = extras;
        saveExtras();
        startTracking(true);
    }

    public void startTracking(boolean clearMap) {
        if (clearMap) {
            mMap.clear();
        }
        mSelectedRouteId = 0;
        Intent mTrackingService = new Intent(mContext, TrackingService.class);
        mTrackingService.putExtras(mExtras);
        mContext.startService(mTrackingService);
        Handler h = new Handler();
        mContext.getContentResolver().registerContentObserver(Provider.BUS_GPS_CONTENT_URI, false, new ContentObserver(h) {
            @Override
            public void onChange(boolean selfChange) {
                drawTrackingPoints();
            }
        });

        TrackingParams params = mExtras.getParcelable(TrackingParams.KEY);
        if (params != null) {
            if (params.getNeedTrackStops()) {
                mContext.getContentResolver().registerContentObserver(Provider.ROUTE_STOPS_GPS_CONTENT_URI, false, new ContentObserver(h) {
                    @Override
                    public void onChange(boolean selfChange) {
                        drawRouteStops();
                    }
                });
            }
        }
    }

    private void drawRouteStops() {
        Cursor cr = mContext.getContentResolver().query(Provider.ROUTE_STOPS_GPS_CONTENT_URI, null, null, null, null);
        if (cr != null) {
            clearStopMarkers();
            cr.moveToFirst();

            Bitmap mbmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bus_stop_3).copy(Bitmap.Config.ARGB_8888, true);
            while (!cr.isAfterLast()) {
                MarkerOptions options = new MarkerOptions();
                LatLng latlng = new LatLng(CursorHelper.getDouble(cr, DBContract.MapRoutesStopsColumns.LAT),
                        CursorHelper.getDouble(cr, DBContract.MapRoutesStopsColumns.LON)
                );

                options.icon(BitmapDescriptorFactory.fromBitmap(mbmp));
                options.position(latlng);
                Marker m = mMap.addMarker(options);
                MarkerInfo info = new MarkerInfo(MarkerInfo.MarkerType.STOP,
                        CursorHelper.getInt(cr, DBContract.MapRoutesStopsColumns.STOPID),
                        CursorHelper.getString(cr, DBContract.MapRoutesStopsColumns.STOP_NAME)
                );
                m.setTag(info);

                m.setTitle(CursorHelper.getString(cr, DBContract.MapRoutesStopsColumns.STOP_NAME));
                mStopMarkers.add(m);
                cr.moveToNext();
            }
            cr.close();
        }
    }

    private void drawTrackingPoints() {
        final List<MarkerOptions> optionsList = new ArrayList<>();
        final List<MarkerInfo> markerInfoList = new ArrayList<>();
        final Handler h = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                setBusPoints(optionsList, markerInfoList);
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        drawBusPoints(optionsList, markerInfoList);
                    }
                });
            }
        }).start();
    }

    private void drawBusPoints(List<MarkerOptions> optionsList, List<MarkerInfo> markerInfoList) {
        clearBusMarkers();
        for (int i = 0; i < optionsList.size(); i++) {
            MarkerOptions options = optionsList.get(i);
            MarkerInfo info = markerInfoList.get(i);
            Marker point;
            point = mMap.addMarker(options);
            point.setTag(info);
            mBusMarkers.add(point);
            point.setVisible(true);
        }
    }

    public void onMarkerClick(final Marker marker) {
        if (marker != null && marker.getTag() != null) {
            MarkerInfo info = (MarkerInfo) marker.getTag();
            if (info.getmMarkerType() == MarkerInfo.MarkerType.BUS) {
                clearRouteLines();
                clearStopMarkers();
                //  clearBusMarkers();
                int rid = info.getRid();
                drawRoutes(rid);
                setAddTrackStops(true, info.getId());
                stopTracking(false);
                startTracking(false);


            } else if (info.getmMarkerType() == MarkerInfo.MarkerType.STOP) {
                marker.showInfoWindow();
            }
        }
    }

    public void drawMeOnMap(double lat, double lon) {
        if ((lat > 0) && (lon > 0)) {
            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(lat, lon));

            Bitmap mbmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.me_dot).copy(Bitmap.Config.ARGB_8888, true);
            options.icon(BitmapDescriptorFactory.fromBitmap(mbmp));

            mMap.addMarker(options);

            LatLng pos = new LatLng(lat, lon);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pos, 15);
            mMap.animateCamera(update);
        }
    }

    private void clearRouteLines() {
        for (Polyline line : mRouteLines) {
            line.remove();
        }
        mRouteLines.clear();
    }

    private void clearStopMarkers() {
        for (Marker m : mStopMarkers) {
            m.remove();
        }
        mStopMarkers.clear();
    }

    private void clearBusMarkers() {
        for (Marker m : mBusMarkers) {
            m.remove();
        }
        mBusMarkers.clear();
    }

    public void drawRoutes(int rid) {
        if (rid == 0) {
            clearRouteLines();
            mRIds.clear();
            for (Marker marker : mBusMarkers) {
                MarkerInfo info = (MarkerInfo) marker.getTag();
                mRIds.add(info.getRid());
            }
            if (mRIds.size() > 0) {
                mSelectedRouteId = (Integer) mRIds.toArray()[0];
            }
        } else {
            mSelectedRouteId = rid;
        }

        class MObserver extends ContentObserver {
            private MObserver(Handler handler) {
                super(handler);
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                mContext.getContentResolver().unregisterContentObserver(this);
                drawRoute();
                mRIds.remove(mSelectedRouteId);
                if (mRIds.size() > 0) {
                    mSelectedRouteId = (Integer) mRIds.toArray()[0];
                    drawRoutes(mSelectedRouteId);
                }
            }
        }

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

            int i = mRIds.size();
            if (i > ROUTE_COLORS.length - 1) {
                i = i / ROUTE_COLORS.length - 1;
            }
            options.color(ROUTE_COLORS[i]);
            mRouteLines.add(mMap.addPolyline(options));
        } finally {
            cr.close();
        }
    }

    private void setBusPoints(List<MarkerOptions> optionsList, List<MarkerInfo> markerInfoList) {
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
                MarkerInfo markerInfo = new MarkerInfo(
                        MarkerInfo.MarkerType.BUS,
                        CursorHelper.getInt(cr, DBContract.MapBusCoordsColumns.FID),
                        CursorHelper.getString(cr, DBContract.MapBusCoordsColumns.NUM)
                );
                markerInfo.setRid(CursorHelper.getInt(cr, DBContract.MapBusCoordsColumns.RID));

                markerInfoList.add(markerInfo);
                markerOptions.title(CursorHelper.getInt(cr, DBContract.MapBusCoordsColumns.RID) + "");
                Bitmap mbmp;
                if (CursorHelper.getString(cr, DBContract.MapBusCoordsColumns.FTYPE).equalsIgnoreCase(TrackingParams.BUS_TYPE_KEY)) {
                    mbmp = mbmBus.copy(Bitmap.Config.ARGB_8888, true);
                } else {
                    mbmp = mbmMiniBus.copy(Bitmap.Config.ARGB_8888, true);
                }
                Canvas canvas = new Canvas(mbmp);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.rgb(61, 61, 61));
                paint.setTextSize((int) (BUS_NO_TEXT_SIZE));
                Rect bounds = new Rect();
                paint.getTextBounds(num, 0, num.length(), bounds);
                int x = mbmp.getWidth() / 2 - bounds.width() / 2;
                int y = (int) ((mbmp.getHeight() / 2) -  (paint.descent() + paint.ascent()) / 2) ;

                canvas.rotate(-rotation, mbmp.getWidth() / 2, mbmp.getHeight() / 2);
                canvas.drawText(num, x, y , paint);
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
