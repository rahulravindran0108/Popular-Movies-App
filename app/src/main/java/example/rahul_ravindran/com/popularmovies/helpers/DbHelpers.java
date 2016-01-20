package example.rahul_ravindran.com.popularmovies.helpers;

import android.database.Cursor;

/**
 * Created by rahulravindran on 18/01/16.
 */
public final class DbHelpers {

    public static final int BOOLEAN_TRUE = 1;
    public static final int BOOLEAN_FALSE = 0;

    public static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    public static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }

    public static long getLong(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
    }

    public  static double getDouble(Cursor cursor, String columnName) {
        return cursor.getDouble(cursor.getColumnIndexOrThrow(columnName));
    }

    public static boolean getBoolean(Cursor cursor, String columnName) {
        return getInt(cursor, columnName) == BOOLEAN_TRUE;
    }

    private DbHelpers() {
        throw new AssertionError("no Instances");
    }

}
