package by.grodno.bus.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import by.grodno.bus.fragments.RoutesFragment;

public class DBManager {

    public static final String DBNAME = "busschedule.db";
    public static final String STOP_NAME = "name";
    public static final String STOP_ID = "idstop";

    public static final String BUS_NAME = "name";
    public static final String BUS_DIRECTION = "direction";
    public static final String BUS_ID = "idbus";
    public static final String SCHEDULE_TIME = "time";
    public static final String MINUTES = "minutes";
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";

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
        return "select  " + STOP_NAME + ", id as [" + STOP_ID + "]  from stops order by " + STOP_NAME;
    }

    public static String getRoutesSQL(RoutesFragment.TransportKind transportKind) {
        switch (transportKind) {
            case TROLLEYBUS:
                return " select " + BUS_NAME + ", id as [" + BUS_ID + "] from buses where tr = 1 group by " + BUS_NAME +
                        " order by length(" + BUS_NAME + ")," + BUS_NAME;
            case BUS:
                return " select " + BUS_NAME + ", id as [" + BUS_ID + "] from buses where tr = 0 group by " + BUS_NAME +
                        " order by length(" + BUS_NAME + ")," + BUS_NAME;
        }
        return null;
    }

    public static String getStopRoutesSQL(String dayName1, String dayName2, String time, String idStop) {
        return "select " + "buses.[name]," + "buses.[direction]," + "[schedule].[time] as [time]," +
                " buses.id as [" + BUS_ID + "], " + idStop + " as [" + STOP_ID + "], " +
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
                " min(case when [schedule].[time] < '04.00' then 1 else 0 end) as [minpn], " +
                " stops.[id] as [" + STOP_ID + "], " +
                " buses.[id] as [" + BUS_ID + "] " +
                " from [rlbusstops] " +
                " join buses buses on buses.[id]=[rlbusstops].[idbus] " +
                " left join [schedule] on [schedule].[idbus]=buses.[id] " +
                "       and [schedule].[idstop]=[rlbusstops].[idstop] " +
                String.format("       and [schedule].[day]in('%s', '%s') ", dayName1, dayName2) +
                String.format(" and(([schedule].[time] > '%s')or([schedule].[time]<'04.00')) ", time) +
                " join[stops] on stops.[id]=[rlbusstops].[idstop] " +
                String.format(" where ([rlbusstops].[idbus]=%s)  ", idBus) +
                " group by stops.[name] " +
                " order by rlbusstops.[nomOrder] ";
    }

    public static String getBusStopSQL(String dayName1, String dayName2, String idBus, String idStop) {
        return " select  " +
                "  [schedule].[" + SCHEDULE_TIME + "] as [" + SCHEDULE_TIME + "], " +
                "    subStr([schedule].[time],1,2) as [" + HOUR + "], " +
                " subStr([schedule].[time],4,2) as [" + MINUTE + "]" +
                "  from [schedule] " +
                String.format("  where([schedule].[idbus]=%s) and [schedule].[day]in('%s', '%s') ", idBus, dayName1, dayName2) +
                String.format("     and ([schedule].[idstop]=%s) ", idStop) +
                " order by   replace(replace(schedule.time, '00.','24.'), '01.','25.') ";
    }


    public int getRouteDirCount(String busName, RoutesFragment.TransportKind kind) {
        String sql = null;
        switch (kind) {
            case BUS: {
                sql = "select count(*)  from buses where " + BUS_NAME + "=\"" + busName
                        + "\" and tr = 0";
                break;
            }
            case TROLLEYBUS: {
                sql = "select count(*)  from buses where " + BUS_NAME + "=\"" + busName
                        + "\" and tr = 1";
                break;
            }
        }
        Cursor cr = mdb.rawQuery(sql, null);
        cr.moveToFirst();
        int c = cr.getInt(0);
        cr.close();
        return c;
    }

    public static String getRouteChildSQL(String routeName, RoutesFragment.TransportKind kind) {
        switch (kind) {
            case TROLLEYBUS: {
                return "select direction, id as [" + BUS_ID + "] from buses where name=" + "\""
                        + routeName + "\" and tr = 1";
            }
            case BUS: {
                return "select direction, id as [" + BUS_ID + "] from buses where name=" + "\""
                        + routeName + "\" and tr = 0";
            }
        }
        return null;
    }
}
