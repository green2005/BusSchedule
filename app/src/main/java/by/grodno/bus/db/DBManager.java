package by.grodno.bus.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class DBManager {

    public static final String DBNAME = "busschedule.db";
    public static final String STOP_NAME = "name";
    public static final String STOP_ID = "id";


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

    public static final String getStopsSQL() {
        String sql = "select  "+STOP_NAME+", "+STOP_ID+"  from stops ";
        return sql;
    }

    public static final String getRoutesSQL(){
       return " select name from buses group by name order by length(name),name";
    }

    public static final String getStopRoutesSQL(String dayName1, String dayName2, String time, String idStop){
        StringBuilder builder = new StringBuilder();
        builder.append("select ");
        builder.append("buses.[name],");
        builder.append("buses.[direction],");
        builder.append("[schedule].[time] as [time],");
        builder.append("case when [schedule].[time] < '04.00' then 1 else 0 end as [pn], ");
        builder.append("min(case when [schedule].[time] < '04.00' then 1 else 0 end) as [minpn] ");
        builder.append("from [rlbusstops] ");
        builder.append("join buses buses on buses.[id]=[rlbusstops].[idbus]");
        builder.append("join [schedule] on [schedule].[idbus]=buses.[id] and [schedule].[idstop]=[rlbusstops].[idstop]");
        builder.append(String.format("where ([rlbusstops].idstop = %s) and ", idStop));
        builder.append(String.format("[schedule].[day] in ('%s','%s')) ", dayName1, dayName2));
        builder.append(String.format(" and (([schedule].[time]>'%s') or([schedule].[time]<'04.00')) ", time));
        builder.append(" group by ");
        builder.append(" buses.[name], ");
        builder.append(" buses.[direction] ");
        builder.append(" order by length(buses.[name]), buses.name" );
        return builder.toString();
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
