package by.grodno.bus.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class Provider extends ContentProvider {
    public static final String AUTHORITY = "by.grodno.bus";
    public static final String BUS_GPS_PATH = "bus_gps";


    public static final String STOPS_GPS_PATH = "stops_gps";
    public static final String ROUTES_GPS_PATH = "routes_gps";
    public static final String ROUTE_NODES_PATH = "route_nodes";

    public static final Uri BUS_GPS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + BUS_GPS_PATH);

    public static final Uri ROUTES_GPS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ROUTES_GPS_PATH);

    public static final Uri ROUTE_NODES_GPS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ROUTE_NODES_PATH);

    public static final Uri STOPS_GPS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + STOPS_GPS_PATH);

    public static final String COORDS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + BUS_GPS_PATH;

    public static final String CONTACT_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + BUS_GPS_PATH;


    public static final int URI_BUS_GPS = 1;
    static final int URI_BUS_GPS_ID = 2;
    public static final int URI_ROUTES_GPS = 3;
    static final int URI_ROUTES_GPS_ID = 4;
    public static final int URI_STOPS_GPS = 5;
    static final int URI_STOPS_GPS_ID = 6;

    public static final int URI_ROUTE_NODES = 7;
    static final int URI_ROUTE_NODES_ID = 8;

    private final static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, BUS_GPS_PATH, URI_BUS_GPS);
        sUriMatcher.addURI(AUTHORITY, BUS_GPS_PATH + "/#", URI_BUS_GPS_ID);

        sUriMatcher.addURI(AUTHORITY, STOPS_GPS_PATH, URI_STOPS_GPS);
        sUriMatcher.addURI(AUTHORITY, STOPS_GPS_PATH + "/#", URI_STOPS_GPS_ID);

        sUriMatcher.addURI(AUTHORITY, ROUTES_GPS_PATH, URI_ROUTES_GPS);
        sUriMatcher.addURI(AUTHORITY, ROUTES_GPS_PATH + "/#", URI_ROUTES_GPS_ID);

        sUriMatcher.addURI(AUTHORITY, ROUTE_NODES_PATH, URI_ROUTE_NODES);
        sUriMatcher.addURI(AUTHORITY, ROUTE_NODES_PATH + "/#", URI_ROUTE_NODES_ID);
    }

    private DBHelper mDBHelper;

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext().getApplicationContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        String tableName = getTableNameByUri(uri);
        switch (sUriMatcher.match(uri)) {
            case URI_ROUTE_NODES_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = DBContract.MapRouteNodesColumns.ID + " = " + id;
                } else {
                    selection = selection + " AND " + DBContract.MapRouteNodesColumns.ID + " = " + id;
                }
                break;
            }
            case URI_STOPS_GPS_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = DBContract.MapStopCoordsColumns.ID + " = " + id;
                } else {
                    selection = selection + " AND " + DBContract.MapStopCoordsColumns.ID + " = " + id;
                }
                break;
            }
            case URI_ROUTES_GPS_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = DBContract.MapRoutesColumns.ID + " = " + id;
                } else {
                    selection = selection + " AND " + DBContract.MapRoutesColumns.ID + " = " + id;
                }
                break;
            }
            case URI_BUS_GPS_ID: {
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = DBContract.MapBusCoordsColumns.ID + " = " + id;
                } else {
                    selection = selection + " AND " + DBContract.MapBusCoordsColumns.ID + " = " + id;
                }
                break;
            }
        }
        cursor = mDBHelper.getReadableDatabase().query(tableName,
                projection, selection, selectionArgs, "", "", "");
        cursor.setNotificationUri(getContext().getApplicationContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Uri resultUri = null;
        String tableName = getTableNameByUri(uri);
        long rowID = db.insert(tableName, null, contentValues);
        ContentUris.withAppendedId(uri, rowID);
        return resultUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        String tableName = getTableNameByUri(uri);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.beginTransaction();
        for (ContentValues cv : values) {
            try {
                long i = db.insertOrThrow(tableName, null, cv);
                Log.d("", i + "");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        db.setTransactionSuccessful();
        db.endTransaction();
        getContext().getApplicationContext().getContentResolver().notifyChange(uri, null);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        String tableName = getTableNameByUri(uri);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        return db.delete(tableName, s, strings);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    private String getTableNameByUri(Uri uri) {
        String tableName = "";
        switch (sUriMatcher.match(uri)) {
            case URI_BUS_GPS_ID: {
                tableName = DBContract.MapBusCoordsColumns.TABLE_NAME;
                break;
            }
            case URI_ROUTES_GPS_ID: {
                tableName = DBContract.MapRoutesColumns.TABLE_NAME;
                break;
            }
            case URI_STOPS_GPS_ID: {
                tableName = DBContract.MapStopCoordsColumns.TABLE_NAME;
                break;
            }
            case URI_BUS_GPS: {
                tableName = DBContract.MapBusCoordsColumns.TABLE_NAME;
                break;
            }
            case URI_ROUTES_GPS: {
                tableName = DBContract.MapRoutesColumns.TABLE_NAME;
                break;
            }
            case URI_STOPS_GPS: {
                tableName = DBContract.MapStopCoordsColumns.TABLE_NAME;
                break;
            }
            case URI_ROUTE_NODES: {
                tableName = DBContract.MapRouteNodesColumns.TABLE_NAME;
                break;
            }
            case URI_ROUTE_NODES_ID: {
                tableName = DBContract.MapRouteNodesColumns.TABLE_NAME;
                break;
            }
        }
        return tableName;
    }

}
