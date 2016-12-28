package by.grodno.bus.db;

import android.database.Cursor;

public class CursorHelper {
    public static final int getInt(Cursor cursor, String fieldName) {
        return cursor.getInt(cursor.getColumnIndex(fieldName));
    }

    public static final String getString(Cursor cursor, String fieldName) {
        return cursor.getString(cursor.getColumnIndex(fieldName));
    }

    public static final double getDouble(Cursor cursor, String fieldName){
        return cursor.getDouble(cursor.getColumnIndex(fieldName));
    }
}
