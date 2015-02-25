package by.grodno.bus.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class DBManager {

    public static final String DBNAME = "busschedule.db";
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
        File f = context.getDatabasePath(DBManager.DBNAME);
        return f.getAbsolutePath();
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

    public Cursor getStops() {
        if (!mdb.isOpen()) {
            return null;
        }
        String sql = "select  trim(replace(name,\" (конечная)\",\"\"))"
                + " from stops group by  trim(replace(name,\" (конечная)\",\"\"))";
        Cursor cr = mdb.rawQuery(sql, null);
        cr.moveToFirst();
        return cr;
    }
}
