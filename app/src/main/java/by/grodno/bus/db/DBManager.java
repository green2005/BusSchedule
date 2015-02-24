package by.grodno.bus.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;

public class DBManager {

    public static final String DBNAME = "busschedule.db";
    private static final String UPDATE_DATE = "update_date";
    private Context mContext;
    private SQLiteDatabase mdb;


    public DBManager(Context context) {
        mContext = context;
    }

    private String getUpdateDate() {
        SharedPreferences prefs = mContext.getSharedPreferences(UPDATE_DATE, Context.MODE_PRIVATE);
        return prefs.getString(UPDATE_DATE, null);
    }

    private void setUpdateDate(String updateDate) {
        SharedPreferences prefs = mContext.getSharedPreferences(UPDATE_DATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(UPDATE_DATE, updateDate);
        editor.apply();
    }

    public void checkUpdateExists(UpdateListener listener) {
        DBUpdater updater = new DBUpdater(mContext);
        updater.checkUpdateExists(listener, getUpdateDate());
    }

    public void updateDB(UpdateListener listener) {
        DBUpdater updater = new DBUpdater(mContext);
        updater.updateDB(listener, getUpdateDate());
    }

    public static String getDBfileName(Context context) {
        File f = context.getDatabasePath(DBManager.DBNAME);
        return f.getAbsolutePath();
    }


    public boolean dbExists() {
        if (mdb == null) {
            openDB();
        }
        if (mdb != null) {
            return mdb.isOpen();
        } else {
            return false;
        }
    }

    public void openDB() {
        File file = new File(getDBfileName(mContext));
        if (file.exists()){
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

}
