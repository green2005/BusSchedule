package by.grodno.bus;


import android.text.TextUtils;

import java.util.List;

public class API {

    public static final int GPS_DIVIDER = 1000000;
    public static final double CITY_LAT = 53.69699478149414; //43.143810;// ;
    public static final double CITY_LON = 23.80278968811035; //131.906921 ; //23.80278968811035;

    private static final String CITY_NAME = "grodno";//"vladivostok";
    private static final String API_SOURCE = "https://bus.ap1.by";//"http://bus125.ru";//; // http://bus125.ru

    public static final String ROUTES_URL = API_SOURCE + "/php/getRoutes.php?city=" + CITY_NAME;
    public static final String STATIONS_URL = API_SOURCE + "/php/getStations.php?city=" + CITY_NAME;
    private static final String ROUTE_NODES_URL = API_SOURCE + "/php/getRouteNodes.php?city=" + CITY_NAME + "&type=0&rid=";

    private static final String TRACKING_PREFIX = API_SOURCE + "/php/getVehiclesMarkers.php?rids=";
    private static final String TRACKING_POSTFIX = "&lat0=0&lng0=0&lat1=90&lng1=180&curk=0&city=" + CITY_NAME;

    //https://bus.ap1.by/php/getVehicleForecasts.php?vid=238&type=0&city=grodno&info=01234&_=1484685670016
    private static final String STOPS_ROUTE_TRACKING = API_SOURCE + "/php/getVehicleForecasts.php?type=0&city=" + CITY_NAME + "&vid=";

    public static final String getRoutesUrl(List<Integer> busIds) {
        String ids = "";
        for (int busId : busIds) {
            if (TextUtils.isEmpty(ids)) {
                ids = busId + "-0";
            } else {
                ids = ids + "," + busId + "-0";
            }
        }
        return TRACKING_PREFIX + ids + TRACKING_POSTFIX;
    }

    public static final String getRouteStopsUrl(int vId) {
        return STOPS_ROUTE_TRACKING + vId;
    }

    public static final String getRouteNodesUrl(int busId) {
        return ROUTE_NODES_URL + busId;
    }


}
