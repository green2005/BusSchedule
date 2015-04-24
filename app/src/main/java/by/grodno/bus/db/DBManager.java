package by.grodno.bus.db;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import java.util.List;

import by.grodno.bus.fragments.RoutesFragment;

public class DBManager {

    public static final String DBNAME = "schedule.db";

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
    //private SQLiteDatabase mdb;
    private FavouritiesDBHelper mFavouritiesDBHelper;
    private ScheduleDBHelper mScheduleDBHelper;


    public DBManager(Context context) {
        mContext = context;
        mFavouritiesDBHelper = new FavouritiesDBHelper(context);
        mScheduleDBHelper = new ScheduleDBHelper(context, null);
    }

    public void updateDB(UpdateListener listener, boolean silent, Context context) {
        DBUpdater updater = new DBUpdater();
        updater.updateDB(listener, silent, context, mScheduleDBHelper.dbExists());// dbExists());
    }

    public static String getDBfileName(Context context) {
        return context.getDatabasePath(DBManager.DBNAME).getAbsolutePath();
    }


    public boolean dbExists() {
        return mScheduleDBHelper.dbExists();
//        if (mdb == null) {
//            openDB();
//        }
//        return mdb != null && mdb.isOpen();
    }

    public void openDB() {
        mScheduleDBHelper.openDB();
//        File file = new File(getDBfileName(mContext));
//        if (!file.exists()) {
//            return;
//        }
//        mdb = SQLiteDatabase.openDatabase(getDBfileName(mContext), null,
//                SQLiteDatabase.NO_LOCALIZED_COLLATORS
//                        | SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    public void close() {
        if (mScheduleDBHelper != null){ // (mdb != null && mdb.isOpen()) {
             mScheduleDBHelper.close();//mdb.close();
        }
        mFavouritiesDBHelper.close();
    }

    public Cursor rawQuery(String sql) {
        return mScheduleDBHelper.rawQuery(sql); //mdb.rawQuery(sql, null);
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

    public static String getStopRoutesSQL(List<String> dayNames, String time, String idStop) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean fStep = true;
        for (String day : dayNames) {
            if (fStep) {
                stringBuilder.append(String.format("'%s'", day));
                fStep = false;
            } else {
                stringBuilder.append(String.format(",'%s'", day));
            }
        }

        return "select \n" +
                "  name,  \n" +
                "  direction,  \n" +
                "  idbus,\n" +
                "  idstop,  \n" +
                "  tr, " +
                "  cast(substr([times].[time],1,2) as int)*60 +                 \n" +
                "   cast(substr([times].[time],4,2) as int)  as [minutes],  \n" +
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
                "  buses.tr " +
                "    from [rlbusstops] \n" +
                "    join buses buses on buses.[id]=[rlbusstops].[idbus]\n" +
                "   join [schedule] on [schedule].[idbus]=buses.[id] \n" +
                "   and [schedule].[idstop]=[rlbusstops].[idstop]\n" +
                String.format("   where ([rlbusstops].idstop = %s) and [schedule].[day] in (%s)  \n", idStop, stringBuilder.toString()) +
                String.format("   and (([schedule].[time]>'%s') or([schedule].[time]<'04.00'))  \n", time) +
                "   group by  buses.[name],  buses.[direction]  order by length(buses.[name]), \n" +
                "   \n" +
                "   buses.name\n" +
                "   ) times";
    }

    public static String getStopsLikeSQL(String name) {
        String stopName1 = name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1);
        String stopName2 = name.toLowerCase();
        String stopName3 = name.toUpperCase();
        return "select name, _id, id from stops where name like \"%" + stopName1 + "%\" or name like \"%" + stopName2 + "%\""
                + " or name like \"%" + stopName3 + "%\"";
    }

    public static String getRouteStopsSQL(String dayName1, String dayName2, String time, String idBus) {
        return "select  \n" +
                "  name ,   " +
                "   case when times.time>\"24.\" then Replace(REPLACE(times.[time], \"24.\",\"00.\"),\"25.\",\"01.\") else times.time end as time,   \n" +
                "    cast(substr(times.[time],1,2) as int)*60 + cast(substr([times].[time],4,2) as int) as [minutes],  \n" +
                "idstop , " +
                "  tr, " +
                "idbus " +
                "\n" +
                "from(\n" +
                "     select  \n" +
                "     stops.[name],  \n" +
                "     min([schedule].[time]) as [time],  \n" +
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

    public static String getBusStopName(String busId, String stopId) {
        return "select \n" +
                "buses.name as [busname], \n" +
                "stops.name as [stopname], \n" +
                "buses.tr as [tr], " +
                "buses.direction as [direction] " +
                "from rlBusStops \n" +
                "join buses on buses.id=rlbusstops.idbus\n" +
                "join stops on stops.id = rlbusstops.idstop\n" +
                "\n" +
                String.format("where rlbusstops.idbus=%s and rlbusstops.idstop=%s", busId, stopId);
    }

    public static String getRoute(String busId) {
        return "select buses.name, buses.direction" +
                " from buses where buses.id = " + busId;
    }

    public static String getBusStopSQL(String dayName1, String dayName2, String idBus, String idStop) {
        return " select  " +
                "  [schedule].[" + SCHEDULE_TIME + "] as [" + SCHEDULE_TIME + "], " +
                "    subStr([schedule].[time],1,2) as [" + HOUR + "], " +
                " subStr([schedule].[time],4,2) as [" + MINUTE + "], " +
                " cast(substr([schedule].[time],1,2) as int)*60 + cast(substr([schedule].[time],4,2) as int) as [" + MINUTES + "]" +
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
        Cursor cr = rawQuery(sql);
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

    public void addToFavourities(FavouritiesItem item) {
        mFavouritiesDBHelper.appendFavouritiesRecord(item);
    }

    public void removeFavourities(FavouritiesItem item) {
        mFavouritiesDBHelper.removeFavouritiesRecord(item);
    }

    public void getIsInFavourities(String busName, String stopName, String tr, FavouritiesDBHelper.FavoritiesListener listener) {
        String sql;
        if (TextUtils.isEmpty(busName)) {
            sql = "select " + FavouritiesDBHelper.STOP_NAME +
                    " from " + FavouritiesDBHelper.TABLE_NAME +
                    " where " + FavouritiesDBHelper.STOP_NAME + " = \"" + stopName + "\" and "
                    + FavouritiesDBHelper.BUS_NAME + " is null ";
        } else {
            sql = "select " + FavouritiesDBHelper.STOP_NAME +
                    " from " + FavouritiesDBHelper.TABLE_NAME +
                    " where " + FavouritiesDBHelper.STOP_NAME + " = \"" + stopName + "\" and " +
                    " " + FavouritiesDBHelper.BUS_NAME + " = \"" + busName + "\" and " +
                    " " + FavouritiesDBHelper.TR + " = \"" + tr + "\"";
        }
        mFavouritiesDBHelper.rawQuery(sql, listener);
    }

    public static String getDaysByStopId(String stopId) {
        return "select day  from schedule \n" +
                "where idstop = " + stopId + "\n" +
                "group by day \n" +
                "order by \n" +
                "case \n" +
                "  when day='Рабочий' then 0   \n" +
                "  when day='Понедельник' then 0   \n" +
                "  when day='Вторник' then 1   \n" +
                "  when day='Среда' then 2   \n" +
                "  when day='Четверг' then 3   \n" +
                "  when day='Пятница' then 4   \n" +
                "  when day='Суббота' then 5   \n" +
                "  when day='Воскресенье' then 6\n" +
                "  when day='Выходной' then 7   \n" +
                "   end\n";
    }

    public static String getDaysByBusId(String busId) {
        return "select day from schedule \n" +
                "where idbus=" + busId + "\n" +
                "group by day \n" +
                "order by \n" +
                "case \n" +
                "  when day='Рабочий' then 0   \n" +
                "  when day='Понедельник' then 0   \n" +
                "  when day='Вторник' then 1   \n" +
                "  when day='Среда' then 2   \n" +
                "  when day='Четверг' then 3   \n" +
                "  when day='Пятница' then 4   \n" +
                "  when day='Суббота' then 5   \n" +
                "  when day='Воскресенье' then 6\n" +
                "  when day='Выходной' then 7   \n" +
                "   end\n";
    }

    public void getFavourities(FavouritiesDBHelper.FavoritiesListener favoritiesListener) {
        mFavouritiesDBHelper.getFavourities(favoritiesListener);
    }
}
