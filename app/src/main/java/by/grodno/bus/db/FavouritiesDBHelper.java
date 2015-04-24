package by.grodno.bus.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FavouritiesDBHelper extends SQLiteOpenHelper {

    public interface FavoritiesListener {
        public void onGetFavourities(List<FavouritiesItem> favouritiesItems);

        public void onGetCursor(Cursor cursor);
    }


    public static final String TABLE_NAME = "favourities";
    public static final String STOP_NAME = "stopName";
    public static final String BUS_NAME = "busName";
    public static final String DIRECTION_NAME = "directionName";
    public static final String TR = "tr";

    private static final String CREATE_DB_SQL = " create table " + TABLE_NAME + "(" +
            " _id INTEGER PRIMARY KEY , " +
            TR + " varchar(10) , " +
            STOP_NAME + " varchar(255), " +
            BUS_NAME + " varchar(255), " +
            DIRECTION_NAME + " varchar(255) " +
            ")";
    private static final String DB_NAME = "favourities.db";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDb;


    public FavouritiesDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onOpen(SQLiteDatabase database) {
        if (!database.isOpen()) {
            mDb = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS |
                    SQLiteDatabase.CREATE_IF_NECESSARY);
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void appendFavouritiesRecord(final FavouritiesItem item) {
        if (mDb == null) {
            mDb = getWritableDatabase();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mDb != null && mDb.isOpen()) {
                    ContentValues cv = new ContentValues();
                    cv.put(BUS_NAME, item.getBusName());
                    cv.put(STOP_NAME, item.getStopName());
                    cv.put(TR, item.getTr());
                    cv.put(DIRECTION_NAME, item.getDirectionName());
                    mDb.beginTransaction();
                    try {
                        mDb.insert(TABLE_NAME, null, cv);
                    } finally {
                        mDb.setTransactionSuccessful();
                        mDb.endTransaction();
                    }
                }
            }
        }).start();
    }

    public void removeFavouritiesRecord(final FavouritiesItem item) {
        if (mDb == null) {
            mDb = getWritableDatabase();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String sql;
                if (TextUtils.isEmpty(item.getBusName())) {
                    sql = "delete from " + TABLE_NAME + " where " + BUS_NAME + " is NULL AND " + STOP_NAME +
                            "=\"" + item.getStopName() + "\"";
                } else {
                    sql = "delete from " + TABLE_NAME + " where " + BUS_NAME + " = " + "\"" + item.getBusName() + "\"" + " AND " + STOP_NAME +
                            "=\"" + item.getStopName() + "\"";
                }
                if (mDb != null && mDb.isOpen()) {
                    mDb.execSQL(sql);
                }
            }
        }).start();
    }

    public void rawQuery(final String sql, final FavoritiesListener listener) {
        if (mDb == null) {
            mDb = getWritableDatabase();
        }

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mDb != null && mDb.isOpen()) {
                }
                final Cursor cursor = mDb.rawQuery(sql, null);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onGetCursor(cursor);
                    }
                });
            }
        }).start();
    }

    public void getFavourities(final FavoritiesListener listener) {
        if (mDb == null) {
            mDb = getWritableDatabase();
        }

        final Handler handler = new Handler();
        final List<FavouritiesItem> favouritiesItems = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sql = "select " +
                        STOP_NAME + " , " +
                        BUS_NAME + " , " +
                        DIRECTION_NAME + " , " +
                        TR + " from " + TABLE_NAME;
                Cursor cr = mDb.rawQuery(sql, null);
                try {
                    FavouritiesDBHelper.this.fillFavouritiesItems(cr, favouritiesItems);
                } finally {
                    cr.close();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onGetFavourities(favouritiesItems);
                    }
                });
            }
        }).start();
    }

    private void fillFavouritiesItems(Cursor cr, List<FavouritiesItem> favouritiesItems) {
        cr.moveToPosition(0);
        while (!cr.isAfterLast()) {
            FavouritiesItem item = new FavouritiesItem();
            if (!TextUtils.isEmpty(cr.getString(cr.getColumnIndex(BUS_NAME)))) {
                item.setBusName(cr.getString(cr.getColumnIndex(BUS_NAME)));
            }

            if (!TextUtils.isEmpty(cr.getString(cr.getColumnIndex(STOP_NAME)))) {
                item.setStopName(cr.getString(cr.getColumnIndex(STOP_NAME)));
            }

            if (!TextUtils.isEmpty(cr.getString(cr.getColumnIndex(TR)))) {
                item.setTr(cr.getString(cr.getColumnIndex(TR)));
            }
            if (!TextUtils.isEmpty(cr.getString(cr.getColumnIndex(DIRECTION_NAME)))) {
                item.setDirectionName(cr.getString(cr.getColumnIndex(DIRECTION_NAME)));
            }
            favouritiesItems.add(item);
            cr.moveToNext();
        }
    }

    public void close() {
        if (mDb != null && mDb.isOpen()) {
            mDb.close();
        }
    }
}
