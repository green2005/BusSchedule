package by.grodno.bus.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import by.grodno.bus.API;
import by.grodno.bus.bo.BusCoord;
import by.grodno.bus.ErrorHelper;
import by.grodno.bus.NetManager;
import by.grodno.bus.TrackingParams;
import by.grodno.bus.db.CursorHelper;
import by.grodno.bus.db.DBContract;
import by.grodno.bus.db.Provider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrackingService extends Service {
    private boolean mIsRunning = false;
    private static final int REFRESH_DELAY = 3000;
    private String mTrackingUrl;


    public TrackingService() {
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
                            Thread.sleep(REFRESH_DELAY);
                        }
                    } catch (Exception e) {
                        doProcessError(e);
                    }

                }
            }).start();
        }
        return Service.START_NOT_STICKY;
    }

    private void doProcess() throws Exception {
        if (!TextUtils.isEmpty(mTrackingUrl)) {
            String jsonStr = NetManager.getHTMLFromUrl(mTrackingUrl);
            JSONObject jo = new JSONObject(jsonStr);
            JSONArray array = jo.optJSONArray("anims");
            ContentValues cv[] = new ContentValues[array.length()];
            for (int i = 0; i < array.length(); i++) {
                JSONObject coordJo = array.optJSONObject(i);
                BusCoord busCoord = new BusCoord(coordJo);
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
                cr.moveToFirst();
                while (!cr.isAfterLast()) {
                    ids.add(CursorHelper.getInt(cr, DBContract.MapRoutesColumns.FID));
                    cr.moveToNext();
                }
                cr.close();
            }
            mTrackingUrl = API.getRoutesUrl(ids);
        }
    }


    private void doProcessError(Exception e) {
        ErrorHelper.showErrorDialog(e.getMessage(), getBaseContext(), null);
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
