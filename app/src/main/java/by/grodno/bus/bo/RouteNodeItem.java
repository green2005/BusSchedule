package by.grodno.bus.bo;


import android.content.ContentValues;

import org.json.JSONObject;

import java.security.InvalidParameterException;

import by.grodno.bus.API;
import by.grodno.bus.db.DBContract;

public class RouteNodeItem implements ContentValuesItem {
    @Override
    public void fillContentValues(JSONObject jo, ContentValues values) throws Exception {
        if (jo == null) {
            throw new InvalidParameterException("jsonObject is null");
        }
        double d = jo.optDouble("lat") / API.GPS_DIVIDER;
        values.put(DBContract.MapRouteNodesColumns.LAT, d);
        d = jo.optDouble("lng") / API.GPS_DIVIDER;
        values.put(DBContract.MapRouteNodesColumns.LON, d);
    }
}
