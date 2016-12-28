package by.grodno.bus.bo;


import android.content.ContentValues;

import org.json.JSONObject;

import by.grodno.bus.API;
import by.grodno.bus.db.DBContract;

public class StopGPSItem implements ContentValuesItem {
    private ContentValues mValues;
//{"id":120,"name":"1-я Речка","descr":"в сторону ж\/д вокзала","lat":43132818,"lng":131899441,"type":"0"}

    public StopGPSItem() {
    }

    @Override
    public void fillContentValues(JSONObject jo, ContentValues values) throws Exception {
        if (jo == null) {
            throw new Exception("empty JSONObject");
        }

        if (values == null) {
            throw new Exception("empty ContentValues");
        }

        values.put(DBContract.MapStopCoordsColumns.FID, jo.optInt("id"));
        values.put(DBContract.MapStopCoordsColumns.NAME, jo.optString("name"));
        values.put(DBContract.MapStopCoordsColumns.DESCRIPTION, jo.optString("descr"));
        double d = jo.optDouble("lat");
        d = d / API.GPS_DIVIDER;
        values.put(DBContract.MapStopCoordsColumns.LAT, d);
        d = jo.optDouble("lng");
        d = d / API.GPS_DIVIDER;
        values.put(DBContract.MapStopCoordsColumns.LON, d);
    }
}
