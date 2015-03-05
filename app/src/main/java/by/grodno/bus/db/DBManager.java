package by.grodno.bus.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class DBManager {

    public static final String DBNAME = "busschedule.db";
    public static final String STOP_NAME = "name";
    public static final String STOP_ID = "id";

    public static final String BUS_NAME = "name";
    public static final String BUS_DIRECTION = "direction";
    public static final String BUS_ID = "id";
    public static final String SCHEDULE_TIME = "time";
    public static final String MINUTES = "minutes";


    private Context mContext;
    private SQLiteDatabase mdb;


    public DBManager(Context context) {
        mContext = context;
    }

    public void checkUpdateExists(UpdateListener listener) {
        DBUpdater updater = new DBUpdater(mContext);
        updater.checkUpdateExists(listener);
    }

    public void updateDB(UpdateListener listener, boolean silent) {
        DBUpdater updater = new DBUpdater(mContext);
        updater.updateDB(listener, silent);
    }

    public static String getDBfileName(Context context) {
        return context.getDatabasePath(DBManager.DBNAME).getAbsolutePath();
    }


    public boolean dbExists() {
        if (mdb == null) {
            openDB();
        }
        return mdb != null && mdb.isOpen();
    }

    public void openDB() {
        File file = new File(getDBfileName(mContext));
        if (!file.exists()) {
            return;
        }
        mdb = SQLiteDatabase.openDatabase(getDBfileName(mContext), null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS
                        | SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    public void close() {
        if (mdb != null && mdb.isOpen()) {
            mdb.close();
        }
    }

    public Cursor rawQuery(String sql) {
        return mdb.rawQuery(sql, null);
    }

    public static String getStopsSQL() {
        return "select  " + STOP_NAME + ", " + STOP_ID + "  from stops order by " + STOP_NAME;
    }

    public static String getRoutesSQL() {
        return " select " + BUS_NAME + ", " + BUS_ID + " from buses group by " + BUS_NAME +
                " order by length(" + BUS_NAME + ")," + BUS_NAME;
    }

    public static String getStopRoutesSQL(String dayName1, String dayName2, String time, String idStop) {
        return "select " + "buses.[name]," + "buses.[direction]," + "[schedule].[time] as [time]," +
                "case when substr([schedule].[time],1,2) < '04' then " +
                "                (24 + cast(substr([schedule].[time],1,2) as int))*60 + " +
                "                cast(substr([schedule].[time],4,2) as int) " +
                "                 else                 " +
                "                cast(substr([schedule].[time],1,2) as int)*60 + " +
                "                cast(substr([schedule].[time],4,2) as int) " +
                "                  end as [" + MINUTES + "],  " +
                "case when [schedule].[time] < '04.00' then 1 else 0 end as [pn], " +
                "min(case when [schedule].[time] < '04.00' then 1 else 0 end) as [minpn] " +
                "from [rlbusstops] " + "join buses buses on buses.[id]=[rlbusstops].[idbus]" +
                "join [schedule] on [schedule].[idbus]=buses.[id] and [schedule].[idstop]=[rlbusstops].[idstop]" +
                String.format("where ([rlbusstops].idstop = %s) and ", idStop) +
                String.format("[schedule].[day] in ('%s','%s') ", dayName1, dayName2) +
                String.format(" and (([schedule].[time]>'%s') or([schedule].[time]<'04.00')) ", time) +
                " group by " + " buses.[name], " + " buses.[direction] " + " order by length(buses.[name]), buses.name";
    }

    public static String getRouteStopsSQL(String dayName1, String dayName2, String time, String idBus) {
        return " select " +
                " stops.[name], " +
                " [schedule].[time] as [time], " +
                " case when substr([schedule].[time],1,2) < '04' then  " +
                " (24 + cast(substr([schedule].[time],1,2) as int))*60 +  cast(substr([schedule].[time],4,2) as int)" +
                " else  cast(substr([schedule].[time],1,2) as int)*60 + cast(substr([schedule].[time],4,2) as int) " +
                " end as [minutes], " +
                " case when [schedule].[time] < '04.00' then 1 else 0 end as [pn], " +
                " min(case when [schedule].[time] < '04.00' then 1 else 0 end) as [minpn] " +
                " from [rlbusstops] " +
                " join buses buses on buses.[id]=[rlbusstops].[idbus] " +
                "join [schedule] on [schedule].[idbus]=buses.[id] " +
                " and [schedule].[idstop]=[rlbusstops].[idstop] " +
                " join[stops] on stops.[id]=[rlbusstops].[idstop] " +
                String.format(" where([rlbusstops].[idbus]=%s) and [schedule].[day]in('%s', '%s') ", idBus, dayName1, dayName2) +
                String.format(" and(([schedule].[time] > '%s')or([schedule].[time]<'04.00')) ", time) +
                " group by stops.[name] " +
                " order by rlbusstops.[nomOrder] ";
    }


    public Cursor getStops() {
        if (!mdb.isOpen()) {
            return null;
        }
        String sql = "select  trim(replace(name,\" (конечная)\",\"\")) as name "
                + " from stops group by  trim(replace(name,\" (конечная)\",\"\"))";
        Cursor cr = mdb.rawQuery(sql, null);
        cr.moveToFirst();
        return cr;
    }

    public int getRouteDirCount(String busName) {
        String sql = "select count(*)  from buses where name=\"" + busName
                + "\"";
        Cursor cr = mdb.rawQuery(sql, null);
        cr.moveToFirst();
        int c = cr.getInt(0);
        cr.close();
        return c;
    }

    public String getRouteChild(String routeName, int dirPos) {
        String sql = "select direction from buses where name=" + "\""
                + routeName + "\"";
        Cursor cr = mdb.rawQuery(sql, null);
        cr.moveToPosition(dirPos);
        String s = cr.getString(0);
        cr.close();
        return s;
    }


}
