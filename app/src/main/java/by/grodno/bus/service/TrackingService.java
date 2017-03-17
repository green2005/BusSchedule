package by.grodno.bus.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import by.grodno.bus.API;
import by.grodno.bus.ErrorHelper;
import by.grodno.bus.NetManager;
import by.grodno.bus.R;
import by.grodno.bus.TrackingParams;
import by.grodno.bus.activity.GoogleMapsActivity;
import by.grodno.bus.bo.BusCoord;
import by.grodno.bus.bo.ContentValuesItem;
import by.grodno.bus.bo.RouteStopItem;
import by.grodno.bus.db.CursorHelper;
import by.grodno.bus.db.DBContract;
import by.grodno.bus.db.Provider;

public class TrackingService extends Service {
    private boolean mIsRunning = false;
    private static final int REFRESH_DELAY = 3500;
    private String mTrackingUrl;
    private boolean mTrackStops;
    private List<Integer> mBusIds;
    private int mTrackingBusId = 0;


    public TrackingService() {
        mBusIds = new ArrayList<>();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (!mIsRunning) {
            mIsRunning = true;
             new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (mIsRunning) {
                            if (TextUtils.isEmpty(mTrackingUrl)) {
                                parseExtras(intent.getExtras());
                            }
                            doProcess();
                            if (mTrackStops) {
                                trackStops();
                                mTrackStops = false;
                            }
                            Thread.sleep(REFRESH_DELAY);
                        }
                    } catch (Exception e) {
                        doProcessError(e );
                        mIsRunning = false;
                    }

                }
            }).start();
        }
        return Service.START_NOT_STICKY;
    }

    private void trackStops() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getApplicationContext().getContentResolver().delete(Provider.ROUTE_STOPS_GPS_CONTENT_URI, "", null);
                    for (int vid : mBusIds) {
                        String url = API.getRouteStopsUrl(vid);
                        String jsonStr = NetManager.getHTMLFromUrl(url);
                        JSONArray array = new JSONArray(jsonStr);
                        ContentValuesItem item = new RouteStopItem();
                        ContentValues contentValues[] = new ContentValues[array.length()];
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jo = array.optJSONObject(i);
                            ContentValues cv = new ContentValues();
                            contentValues[i] = cv;
                            item.fillContentValues(jo, cv);
                            cv.put(DBContract.MapRoutesStopsColumns.BUSID, vid);
                        }
                        getApplicationContext().getContentResolver().bulkInsert(Provider.ROUTE_STOPS_GPS_CONTENT_URI, contentValues);
                    }
                } catch (Exception e) {
                    doProcessError(e);
                }
            }
        }).start();
    }


    private void doProcess() throws Exception {
        if (!TextUtils.isEmpty(mTrackingUrl)) {
            String jsonStr = NetManager.getHTMLFromUrl(mTrackingUrl);
            JSONObject jo = new JSONObject(jsonStr);
            JSONArray array = jo.optJSONArray("anims");
            if (mTrackStops) {
                mBusIds.clear();
                if (mTrackingBusId != 0) {
                    mBusIds.add(mTrackingBusId);
                }
            }

            ContentValues cv[] = new ContentValues[array.length()];
            for (int i = 0; i < array.length(); i++) {
                JSONObject coordJo = array.optJSONObject(i);
                BusCoord busCoord = new BusCoord(coordJo);
                if (mTrackStops && mTrackingBusId == 0) {
                    mBusIds.add(busCoord.getId());
                }
                cv[i] = busCoord.getContentValue();
            }
            getApplicationContext().getContentResolver().delete(Provider.BUS_GPS_CONTENT_URI, "", null);
            getApplicationContext().getContentResolver().bulkInsert(Provider.BUS_GPS_CONTENT_URI, cv);
        }
    }

    private void parseExtras(Bundle extras) {
        TrackingParams params = extras.getParcelable(TrackingParams.KEY);
        if (params == null) {
            throw new UnsupportedOperationException("Bundle is null");
        } else {
            List<Integer> ids = new ArrayList<>();
            mTrackStops = params.getNeedTrackStops();
            mTrackingBusId = params.getTrackingStopsBusId();

            for (int i = 0; i < params.getBusNames().size(); i++) {
                String busName = params.getBusNames().get(i);
                String busType = params.getBusTypes().get(i);
                String[] projection = new String[]{DBContract.MapRoutesColumns.FID,
                        DBContract.MapRoutesColumns.NAME,
                        DBContract.MapRoutesColumns.NUM
                };

                Cursor cr = getContentResolver().query(
                        Provider.ROUTES_GPS_CONTENT_URI,
                        projection,
                        DBContract.MapRoutesColumns.NUM + " = ?  and " + DBContract.MapRoutesColumns.TYPE + " = ? ",
                        new String[]{busName, busType},
                        ""
                );
                if (cr != null) {
                    cr.moveToFirst();
                    while (!cr.isAfterLast()) {
                        ids.add(CursorHelper.getInt(cr, DBContract.MapRoutesColumns.FID));
                        cr.moveToNext();
                    }
                    cr.close();
                }
            }
            mTrackingUrl = API.getRoutesUrl(ids);
        }
    }


    private void doProcessError(final Exception e) {
        Intent intent = new Intent(GoogleMapsActivity.ERROR_ACTION);
        Bundle b = new Bundle();
        if (e instanceof UnknownHostException){
            b.putString(GoogleMapsActivity.ERROR_MSG, this.getString(R.string.check_inet_connection));
        } else {
            b.putString(GoogleMapsActivity.ERROR_MSG, e.getMessage());
        }intent.putExtras(b);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        mIsRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
