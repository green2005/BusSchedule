package by.grodno.bus.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;


public class ScheduleDBHelper extends SQLiteOpenHelper {
    private static int VERSION = 1;
    private SQLiteDatabase mDb;
    private Context mContext;

    public ScheduleDBHelper(Context context,  SQLiteDatabase.CursorFactory factory) {
        super(context, DBManager.DBNAME, factory, VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onOpen(SQLiteDatabase database) {
        if (!database.isOpen()) {
            mDb = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS |
                    SQLiteDatabase.CREATE_IF_NECESSARY);
        }
    }

    public Cursor rawQuery(String sql){
        if (mDb == null){
            mDb = getReadableDatabase();
        }
        return mDb.rawQuery(sql, null);
    }

    public boolean dbExists() {
        if (mDb == null) {
            openDB();
        }
        return mDb != null && mDb.isOpen();
    }

    public void openDB() {
        File file = new File(DBManager.getDBfileName(mContext));
        if (!file.exists()) {
            return;
        }
        mDb = SQLiteDatabase.openDatabase(DBManager.getDBfileName(mContext), null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS
                        | SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
