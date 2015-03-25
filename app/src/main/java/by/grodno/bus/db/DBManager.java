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
    public static final String TRANSPORT_KIND = "tr";

    private Context mContext;
    private SQLiteDatabase mdb;


    public DBManager(Context context) {
        mContext = context;
    }

    public void updateDB(UpdateListener listener, boolean silent, Context context) {
        DBUpdater updater = new DBUpdater();
        updater.updateDB(listener, silent, context);
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
       return "select \n" +
               "  name,  \n" +
               "  direction,  \n" +
               "  idbus,\n" +
               "  idstop,  \n" +
               "  minutes,  \n" +
               "  tr, "+
               "case when times.time>\"24.\" then Replace(REPLACE(times.[time], \"24.\",\"00.\"),\"25.\",\"01.\") else times.time end as time  \n" +
               "\n" +
               "from\n" +
               "\n" +
               "( select \n" +
               "  buses.[name],\n" +
               "  buses.[direction],\n" +
               "  min([schedule].[time]) as [time], \n" +
               "  buses.id as [idbus], \n" +
               String.format("  %s as [idstop], \n", idStop) +
               "  cast(substr([schedule].[time],1,2) as int)*60 +                 \n" +
               "   cast(substr([schedule].[time],4,2) as int)  as [minutes],  \n" +
               "  buses.tr "+
               "    from [rlbusstops] \n" +
               "    join buses buses on buses.[id]=[rlbusstops].[idbus]\n" +
               "   join [schedule] on [schedule].[idbus]=buses.[id] \n" +
               "   and [schedule].[idstop]=[rlbusstops].[idstop]\n" +
               String.format("   where ([rlbusstops].idstop = %s) and [schedule].[day] in ('%s','%s')  \n", idStop, dayName1, dayName2) +
               String.format("   and (([schedule].[time]>'%s') or([schedule].[time]<'04.00'))  \n",time ) +
               "   group by  buses.[name],  buses.[direction]  order by length(buses.[name]), \n" +
               "   \n" +
               "   buses.name\n" +
               "   ) times";
    }

    public static String getRouteStopsSQL(String dayName1, String dayName2, String time, String idBus) {
        return "select  \n" +
                "  name ,   " +
                "   case when times.time>\"24.\" then Replace(REPLACE(times.[time], \"24.\",\"00.\"),\"25.\",\"01.\") else times.time end as time,   \n" +
                " minutes,  " +
                "idstop , " +
                "  tr, "+
                "idbus " +
                "\n" +
                "from(\n" +
                "     select  \n" +
                "     stops.[name],  \n" +
                "     min([schedule].[time]) as [time],  \n" +
                "    cast(substr([schedule].[time],1,2) as int)*60 + cast(substr([schedule].[time],4,2) as int) as [minutes],  \n" +
                "     stops.[id] as [idstop], \n" +
                "      buses.[id] as [idbus],  \n" +
                "     buses.tr " +
                "     from [rlbusstops]  \n" +
                "     join buses buses on buses.[id]=[rlbusstops].[idbus]  \n" +
                "     left join [schedule] on [schedule].[idbus]=buses.[id]        \n" +
                "     and [schedule].[idstop]=[rlbusstops].[idstop]        \n" +
                String.format(" and [schedule].[day]in('%s', '%s')  and(([schedule].[time] > '%s')or([schedule].[time]<'04.00'))  \n", dayName1, dayName2, time) +
                String.format("     join[stops] on stops.[id]=[rlbusstops].[idstop]  where ([rlbusstops].[idbus]=%s)   \n", idBus) +
                "     group by stops.[name]  order by rlbusstops.[nomOrder]\n" +
                " ) times ";
    }

    public static String getBusStopName(String busId, String stopId){
        return "select \n" +
                "buses.name as [busname],\n" +
                "stops.name as [stopname]\n" +
                "from rlBusStops \n" +
                "join buses on buses.id=rlbusstops.idbus\n" +
                "join stops on stops.id = rlbusstops.idstop\n" +
                "\n" +
                String.format("where rlbusstops.idbus=%s and rlbusstops.idstop=%s",busId,stopId);
    }

    public static String getRoute(String busId){
        return "select buses.name, buses.direction" +
                " from buses where buses.id = " +busId;
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
