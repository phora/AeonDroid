package io.github.phora.aeondroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import io.github.phora.aeondroid.model.AspectConfig;

/**
 * Created by phora on 9/22/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper sInstance;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "aeondroid.db";
    public static final String COLUMN_ID = "_id";

    private static final String TABLE_ORBS  = "orbs";
    public  static final String ORB_DEGREE  = "degree";
    public  static final String ORB_NAME    = "name";
    public  static final String ORB_VALUE   = "value";
    public  static final String ORB_VISIBLE = "visible";

    private static final String TABLE_ALERTS = "alerts";
    private static final String TABLE_ALERT_TYPES = "alert_types";

    private static final String ORBS_CREATE = "CREATE TABLE " + TABLE_ORBS +
            " ( " + COLUMN_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                  + ORB_DEGREE  + " INTEGER NOT NULL UNIQUE, "
                  + ORB_NAME    + " INTEGER NOT NULL, "
                  + ORB_VALUE   + " REAL NOT NULL, "
                  + ORB_VISIBLE + " INT)";


    public static synchronized DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ORBS_CREATE);

        for (int i=0; i<11; i++) {
            ContentValues cv = new ContentValues();
            cv.put(ORB_DEGREE,  AspectConfig.ASPECT_VALUES[i]);
            cv.put(ORB_NAME,    AspectConfig.ASPECT_NAMES[i]);
            cv.put(ORB_VALUE,   AspectConfig.DEFAULT_ORBS[i]);
            cv.put(ORB_VISIBLE, AspectConfig.DEFAULT_VISIBILITY[i]);

            sqLiteDatabase.insert(TABLE_ORBS, null, cv);
        }
    }

    public Cursor getOrbsForEditing() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        return sqLiteDatabase.query(TABLE_ORBS, null, //we want all the fields
                null, null,
                null, null, null, null);
    }

    public SparseArray<AspectConfig> getOrbsForBackgroundUsage() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String[] fields = {ORB_DEGREE, ORB_VALUE, ORB_VISIBLE};

        Cursor cursor = sqLiteDatabase.query(TABLE_ORBS, fields,
                null, null,
                null, null, null, null);

        SparseArray<AspectConfig> output = new SparseArray<>(cursor.getCount());
        int colValue = -1;
        int colVisible = -1;

        while (cursor.moveToNext()) {
            if (colValue == -1) {
                colValue = cursor.getColumnIndex(ORB_VALUE);
            }
            if (colVisible == -1) {
                colVisible = cursor.getColumnIndex(ORB_VISIBLE);
            }
            double orbValue = cursor.getDouble(colValue);
            boolean orbVisible = cursor.getInt(colVisible) == 1;
            AspectConfig aspectConfig = new AspectConfig(orbVisible, orbValue);
            output.append(cursor.getInt(cursor.getColumnIndex(ORB_DEGREE)), aspectConfig);
        }
        cursor.close();

        return output;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
