package by.grodno.bus.map;


import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

import by.grodno.bus.API;
import by.grodno.bus.ErrorHelper;
import by.grodno.bus.NetManager;
import by.grodno.bus.bo.ContentValuesItem;
import by.grodno.bus.bo.RouteGPSItem;
import by.grodno.bus.bo.RouteNodeItem;
import by.grodno.bus.bo.StopGPSItem;
import by.grodno.bus.db.DBContract;
import by.grodno.bus.db.Provider;

public class MapController {
    private Context mContext;
    private AtomicInteger mThreadsDone;
    private OnInitdoneListener mListener;

    protected interface OnInitdoneListener{
        void onDone();
    }

    MapController(Context context, OnInitdoneListener onInitdoneListener) {
        mContext = context;
        mListener = onInitdoneListener;
        mThreadsDone = new AtomicInteger(0);
    }

    protected void fillRoutes() {
        final Handler h = new Handler();
        new NetManager().request(API.ROUTES_URL, mContext, new NetManager.ResponseProcessor() {
            @Override
            public void onResponse(final String response) {
                if (!TextUtils.isEmpty(response)) {
                    fillGPSData(h, new RouteGPSItem(), Provider.ROUTES_GPS_CONTENT_URI, response);
                }
            }
        });
    }

    protected void fillStops() {
        final Handler h = new Handler();
        new NetManager().request(API.STATIONS_URL, mContext, new NetManager.ResponseProcessor() {
            @Override
            public void onResponse(final String response) {
                if (!TextUtils.isEmpty(response)) {
                    fillGPSData(h, new StopGPSItem(), Provider.STOPS_GPS_CONTENT_URI, response);
                }
            }
        });
    }

    private void fillGPSData(final Handler handler, final ContentValuesItem item, final Uri itemUri, final String response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray a = new JSONArray(response);
                    ContentValues contentValues[] = new ContentValues[a.length()];
                    for (int i = 0; i < a.length(); i++) {
                        JSONObject jo = a.optJSONObject(i);
                        ContentValues cv = new ContentValues();
                        contentValues[i] = cv;
                        item.fillContentValues(jo, cv);
                    }
                    mContext.getContentResolver().delete(itemUri, null, null);
                    mContext.getContentResolver().bulkInsert(itemUri, contentValues);
                    if (mThreadsDone.incrementAndGet() == 2) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onDone();
                            }
                        });
                    }
                } catch (Exception e) {
                    ErrorHelper.processError(mContext, e, handler);
                }
            }
        }).start();
    }

    private void fillRouteNodes(final Handler handler,
                                final ContentValuesItem item,
                                final String response,
                                final int selectedRouteId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray a = new JSONArray(response);
                    ContentValues contentValues[] = new ContentValues[a.length()];
                    for (int i = 0; i < a.length(); i++) {
                        JSONObject jo = a.optJSONObject(i);
                        ContentValues cv = new ContentValues();
                        contentValues[i] = cv;
                        item.fillContentValues(jo, cv);
                        cv.put(DBContract.MapRouteNodesColumns.ROUTEID, selectedRouteId);
                    }
                    mContext.getContentResolver().delete(Provider.ROUTE_NODES_GPS_CONTENT_URI, null, null);
                    mContext.getContentResolver().bulkInsert(Provider.ROUTE_NODES_GPS_CONTENT_URI, contentValues);
                } catch (Exception e) {
                    ErrorHelper.processError(mContext, e, handler);
                }
            }
        }).start();
    }

    protected void fillRouteNodes(final int routeId) {
        final Handler h = new Handler();
        new NetManager().request(API.getRouteNodesUrl(routeId), mContext, new NetManager.ResponseProcessor() {
            @Override
            public void onResponse(final String response) {
                if (!TextUtils.isEmpty(response)) {
                    fillRouteNodes(h, new RouteNodeItem(), response, routeId);
                }
            }
        });
    }

}
