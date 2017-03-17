package by.grodno.bus.bo;


import android.content.ContentValues;

import org.json.JSONObject;

import by.grodno.bus.db.DBContract;

public class BusCoord {
    private float lat;
    private float lon;
    private int dir;
    private float speed;
    private int id;
    private String rtype;
    private String rnum;
    private int rid;

    /*
    {"id":"2112","lon":131908325,"lat":43133160,"dir":232,"speed":0,"lasttime":"20.11.2016 21:13:13",
    "gos_num":"","rid":21,"rnum":"7Т","rtype":"А","anim_key":43228431,"big_jump":"0","anim_points":[]}
     */
    public BusCoord(JSONObject jo) {
        id = jo.optInt("id");
        lon = jo.optInt("lon");
        lat = jo.optInt("lat");
        dir = jo.optInt("dir");
        rtype = jo.optString("rtype");
        rnum = jo.optString("rnum");
        rid = jo.optInt("rid");
    }

    public int getId() {
        return id;
    }


    public float getLat() {
        return (float) lat / 1000000;
    }

    public float getLon() {
        return (float) lon / 1000000;
    }

    public int getDir() {
        return dir;
    }

    public ContentValues getContentValue() {
        ContentValues cv = new ContentValues();
        // cv.put(DBContract.MapBusCoordsColumns.ID, getId());
        cv.put(DBContract.MapBusCoordsColumns.LAT, getLat());
        cv.put(DBContract.MapBusCoordsColumns.LON, getLon());
        cv.put(DBContract.MapBusCoordsColumns.DIR, getDir());

        cv.put(DBContract.MapBusCoordsColumns.FID, id);
        cv.put(DBContract.MapBusCoordsColumns.FTYPE, rtype);
        cv.put(DBContract.MapBusCoordsColumns.NUM, rnum);
        cv.put(DBContract.MapBusCoordsColumns.RID, rid);
        return cv;
    }

}
