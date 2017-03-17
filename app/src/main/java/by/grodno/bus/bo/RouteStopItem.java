package by.grodno.bus.bo;


import android.content.ContentValues;

import org.json.JSONObject;

import by.grodno.bus.API;
import by.grodno.bus.db.DBContract;

public class RouteStopItem implements ContentValuesItem {
/*
[{"arrt":914,"stid":"312","stname":"Улица Победы","stdescr":"Автобусный парк г. Гродно","lat0":"53662082","lng0":"23831201","lat1":
"53661977","lng1":"53661977"},
{"arrt":851,"stid":"476","stname":"Дорожно-строительный трест № 6","stdescr":"Белкард","lat0":"53660846","lng0":"23830241","lat1":"53661043","lng1":"53661043"},{"arrt":759,"stid":"335","stname":"Плодоовощесервис","stdescr":"Дорожно-строительный трест № 6","lat0":"53655373","lng0":"23830853","lat1":"53655526","lng1":"53655526"},{"arrt":692,"stid":"334","stname":"Улица Томина","stdescr":"Плодоовощесервис","lat0":"53651604","lng0":"23832896","lat1":"53651779","lng1":"53651779"},{"arrt":615,"stid":"322","stname":"Магазин \"Ника\"","stdescr":"Улица Славинского","lat0":"53648577","lng0":"23839548","lat1":"53648529","lng1":"53648529"},{"arrt":515,"stid":"321","stname":"Проспект Клецкова","stdescr":"Магазин \"Ника\"","lat0":"53649337","lng0":"23844543","lat1":"53649299","lng1":"53649299"},{"arrt":345,"stid":"338","stname":"Румлево","stdescr":"Проспект Клецкова","lat0":"53656514","lng0":"23857570","lat1":"53656373","lng1":"53656373"},{"arrt":229,"stid":"340","stname":"Проспект Румлёвский","stdescr":"Румлево","lat0":"53663856","lng0":"23860483","lat1":"53663770","lng1":"53663770"},{"arrt":132,"stid":"342","stname":"Госавтоинспекция","stdescr":"Проспект Румлевский","lat0":"53670041","lng0":"23860749","lat1":"53669863","lng1":"53669863"},{"arrt":53,"stid":"343","stname":"Улица Лидская","stdescr":"Госавтоинспекция","lat0":"53671512","lng0":"23855229","lat1":"53671474","lng1":"53671474"}]
 */

    @Override
    public void fillContentValues(JSONObject jo, ContentValues values) throws Exception {
        if (jo == null) {
            throw new NullPointerException("JSONObject is null");
        }

        if (values == null) {
            throw new NullPointerException("ContentValues is null");
        }

        values.put(DBContract.MapRoutesStopsColumns.ARRT, jo.optInt("arrt"));
        values.put(DBContract.MapRoutesStopsColumns.STOPID, jo.optInt("stid"));
        values.put(DBContract.MapRoutesStopsColumns.STOP_NAME, jo.optString("stname"));
        values.put(DBContract.MapRoutesStopsColumns.DESCR, jo.optString("stdescr"));
        double d = jo.optDouble("lat0");
        d = d / API.GPS_DIVIDER;
        values.put(DBContract.MapRoutesStopsColumns.LAT, d);
        d = jo.optDouble("lng0");
        d = d / API.GPS_DIVIDER;
        values.put(DBContract.MapRoutesStopsColumns.LON, d);
    }
}
