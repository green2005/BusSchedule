package by.grodno.bus.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "coords.db";
    private static final int DB_VERSION = 4;


    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);


    }

    public DBHelper(Context context) {
        super(context, DBNAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "create table " + DBContract.MapBusCoordsColumns.TABLE_NAME + " ( " +
                        " _id integer primary key AUTOINCREMENT, " + //DBContract.MapBusCoordsColumns.ID +" INTEGER PRIMARY KEY   AUTOINCREMENT, " +
                        DBContract.MapBusCoordsColumns.LAT + " REAL, " +
                        DBContract.MapBusCoordsColumns.LON + " REAL, " +
                        DBContract.MapBusCoordsColumns.NUM + " TEXT, " +
                        DBContract.MapBusCoordsColumns.FID + " INTEGER , " +
                        DBContract.MapBusCoordsColumns.RID + " INTEGER , " +
                        DBContract.MapBusCoordsColumns.FTYPE + " TEXT, " +
                        DBContract.MapBusCoordsColumns.DIR + " INTEGER) ";
        db.execSQL(sql);

        sql =
                "create table " + DBContract.MapRoutesColumns.TABLE_NAME + " ( " +
                        DBContract.MapRoutesColumns.ID + " integer primary key AUTOINCREMENT, " +
                        DBContract.MapRoutesColumns.FID + " integer, " +
                        DBContract.MapRoutesColumns.FROMST + " text, " +
                        DBContract.MapRoutesColumns.FROMSTID + " INTEGER, " +
                        DBContract.MapRoutesColumns.NAME + " text, " +
                        DBContract.MapRoutesColumns.NUM + " text, " +
                        DBContract.MapRoutesColumns.TOST + " text, " +
                        DBContract.MapRoutesColumns.TOSTID + " integer, " +
                        DBContract.MapRoutesColumns.TYPE + " text) ";
        db.execSQL(sql);

        sql = "create table " + DBContract.MapStopCoordsColumns.TABLE_NAME + " ( " +
                DBContract.MapStopCoordsColumns.ID + " integer primary key  AUTOINCREMENT, " +
                DBContract.MapStopCoordsColumns.DESCRIPTION + " text, " +
                DBContract.MapStopCoordsColumns.NAME + " text, " +
                DBContract.MapStopCoordsColumns.FID + " integer, " +
                DBContract.MapStopCoordsColumns.LAT + " REAL, " +
                DBContract.MapStopCoordsColumns.LON + " REAL " +
                " )";
        db.execSQL(sql);

        sql = "create table " + DBContract.MapRouteNodesColumns.TABLE_NAME + " ( " +
                DBContract.MapRouteNodesColumns.ID + " integer primary key AUTOINCREMENT, " +
                DBContract.MapRouteNodesColumns.ROUTEID + " integer, " +
                DBContract.MapRouteNodesColumns.LAT + " REAL, " +
                DBContract.MapRouteNodesColumns.LON + " REAL " +
                " )";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + DBContract.MapBusCoordsColumns.TABLE_NAME);
        db.execSQL("drop table if exists " + DBContract.MapRoutesColumns.TABLE_NAME);
        db.execSQL("drop table if exists " + DBContract.MapStopCoordsColumns.TABLE_NAME);
        db.execSQL("drop table if exists " + DBContract.MapRouteNodesColumns.TABLE_NAME);
        onCreate(db);
    }
}
