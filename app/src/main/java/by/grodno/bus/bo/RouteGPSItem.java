package by.grodno.bus.bo;

import android.content.ContentValues;

import org.json.JSONObject;

import by.grodno.bus.db.DBContract;

public class RouteGPSItem implements ContentValuesItem {
//    {"id":116,"name":"1","type":"А","num":"1","fromst":"Академическая","fromstid":183,"tost":"Мыс Анна","tostid":299}

    public RouteGPSItem() {
    }

    @Override
    public void fillContentValues(JSONObject jo, ContentValues values) throws Exception {
        if (jo == null) {
            throw new Exception("Empty JSONObject");
        }

        if (values == null) {
            throw new Exception("Empty ContentValues");
        }

        values.put(DBContract.MapRoutesColumns.FID, jo.optInt("id"));
        values.put(DBContract.MapRoutesColumns.NAME, jo.optString("name"));
        values.put(DBContract.MapRoutesColumns.TYPE, jo.optString("type"));
        values.put(DBContract.MapRoutesColumns.NUM, jo.optString("num"));
        values.put(DBContract.MapRoutesColumns.FROMST, jo.optString("fromst"));
        values.put(DBContract.MapRoutesColumns.FROMSTID, jo.optInt("fromstid"));
        values.put(DBContract.MapRoutesColumns.TOST, jo.optString("tost"));
        values.put(DBContract.MapRoutesColumns.TOSTID, jo.optInt("tostid"));
    }
}
