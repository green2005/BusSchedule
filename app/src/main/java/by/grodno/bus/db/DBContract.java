package by.grodno.bus.db;


import android.provider.BaseColumns;

public final class DBContract {

    public static final class MapBusCoordsColumns implements BaseColumns {
        public static final String LAT = "lat";
        public static final String LON = "lon";
        public static final String DIR = "dir";
        public static final String NUM = "num";

        public static final String FID = "fid";
        public static final String RID = "rid";
        public static final String FTYPE = "type_";

        public static final String ID = BaseColumns._ID;
        public static final String TABLE_NAME = "bus_coords";
    }

    public static final class MapStopCoordsColumns implements BaseColumns{
        public static final String ID = BaseColumns._ID;
        public static final String LAT = "lat";
        public static final String LON = "lon";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String FID = "id";
        public static final String TABLE_NAME = "stop_coords";
    }

    public static final class MapRouteNodesColumns implements  BaseColumns{
        public static final String ID = BaseColumns._ID;
        public static final String LAT = "lat";
        public static final String LON = "lon";
        public static final String ROUTEID = "routeId";

        public static final String TABLE_NAME = "route_nodes";
    }


    public static final class MapRoutesColumns implements BaseColumns{
        public static final String ID = BaseColumns._ID;
        public static final String FID = "id";
        public static final String NUM = "num";
        public static final String NAME = "name";
        public static final String TYPE = "typeName";
        public static final String FROMST = "fromst";
        public static final String FROMSTID = "fromstid";
        public static final String TOST = "tost";
        public static final String TOSTID = "tostid";
        public static final String TABLE_NAME = "routes";
    }
}

