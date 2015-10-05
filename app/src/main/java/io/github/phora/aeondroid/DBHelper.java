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
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "aeondroid.db";
    public static final String COLUMN_ID = "_id";

    private static final String TABLE_ORBS  = "orbs";
    public  static final String ORB_DEGREE  = "degree";
    public  static final String ORB_VALUE   = "value";
    public  static final String ORB_VISIBLE = "visible";
    private static final String ORBS_CREATE = "CREATE TABLE " + TABLE_ORBS +
        " ( " + COLUMN_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + ORB_DEGREE  + " INTEGER NOT NULL UNIQUE, "
        + ORB_VALUE   + " REAL NOT NULL, "
        + ORB_VISIBLE + " INT)";

    private static final String TABLE_ALERTS = "alerts";
    public  static final String ALERT_TYPE   = "alert_type";

    public enum AlertType {
        LED,     //fields: color, interval
        TEXT,    //fields: text
        NOTI,    //fields: ticker, text
        VIBRATE, //fields: durations, repeat @ index
        RING     //fields: ringtone
    }

    private static final String TABLE_LINKED_TRIGGERS = "linked_triggers";
    public  static final String LINKED_ALERT          = "_alert_id";
    public  static final String LINKED_TRIGGER        = "_trigger_id";

    private static final String TABLE_ALERT_TRIGGERS = "alert_triggers";
    public  static final String ATRIGGER_TYPE = "atrigger_type";
    //expected types are documented in AlertTriggerType
    public  static final String ATRIGGER_ARG1 = "arg1";
    public  static final String ATRIGGER_ARG2 = "arg2";
    public  static final String ATRIGGER_SPECIFICITY = "specificity";
    public  static final String ATRIGGER_ENABLED = "enabled";
    public  static final String ATRIGGER_CREATE = "CREATE TABLE " + TABLE_ALERT_TRIGGERS +
            " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ATRIGGER_TYPE + " INTEGER NOT NULL, "
            + ATRIGGER_ARG1 + " NUMERIC, "
            + ATRIGGER_ARG2 + " NUMERIC, "
            + ATRIGGER_SPECIFICITY + " NUMERIC, "
            + ATRIGGER_ENABLED + " INT)";

    private static final String TABLE_SUBTRIGGERS = "subtriggers";
    //the id of the trigger group from alert_triggers
    public  static final String SUBTRIGGER_GID    = "_group_id";
    //the id of the trigger that's part of the group from alert_triggers
    public  static final String SUBTRIGGER_TID    = "_trigger_id";
    public  static final String SUBTRIGGERS_CREATE = "CREATE TABLE " + TABLE_SUBTRIGGERS +
            " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SUBTRIGGER_GID + " INTEGER NOT NULL, "
            + SUBTRIGGER_TID + " INTEGER NOT NULL)";

    public static synchronized DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(ORBS_CREATE);

        for (int i=0; i<11; i++) {
            ContentValues cv = new ContentValues();
            cv.put(ORB_DEGREE,  AspectConfig.ASPECT_VALUES[i]);
            cv.put(ORB_VALUE,   AspectConfig.DEFAULT_ORBS[i]);
            cv.put(ORB_VISIBLE, AspectConfig.DEFAULT_VISIBILITY[i]);

            sqLiteDatabase.insert(TABLE_ORBS, null, cv);
        }

        sqLiteDatabase.execSQL(ATRIGGER_CREATE);
        sqLiteDatabase.execSQL(SUBTRIGGERS_CREATE);
    }

    /* ORB FUNCTIONS */
    public void batchUpdateOrbs(SparseArray<AspectConfig> orbConfig) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            for (int i = 0; i < orbConfig.size(); i++) {
                ContentValues cv = new ContentValues();
                int degree = orbConfig.keyAt(i);
                double orbValue = orbConfig.valueAt(i).getOrb();
                boolean visible = orbConfig.valueAt(i).isShown();
                cv.put(ORB_VALUE, orbValue);
                cv.put(ORB_VISIBLE, visible);
                sqLiteDatabase.update(TABLE_ORBS, cv, ORB_DEGREE+" = ?", new String[]{String.valueOf(degree)});
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void updateOrb(int degree, double orbValue, boolean visible) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ORB_VALUE, orbValue);
        cv.put(ORB_VISIBLE, visible);
        sqLiteDatabase.update(TABLE_ORBS, cv, ORB_DEGREE + " = ?", new String[]{String.valueOf(degree)});
    }

    public SparseArray<AspectConfig> getOrbs() {
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
    /* /ORB FUNCTIONS */

    /* ALERT TRIGGER FUNCTIONS */
    public Cursor getSubtriggers(long triggerId) {
        String TREE_SELECT = String.format("SELECT %1$s.%3$s, %4$s, %5$s, %6$s, %7$s, %8$s " +
                "FROM %1$s " +
                "JOIN %2$s " +
                "ON %1$s.%3$s=%2$s.%9$s " +
                "WHERE %2$s.%8$s=?",
                TABLE_ALERT_TRIGGERS, TABLE_SUBTRIGGERS, COLUMN_ID,
                ATRIGGER_TYPE, ATRIGGER_ARG1, ATRIGGER_ARG2, ATRIGGER_SPECIFICITY,
                SUBTRIGGER_GID, SUBTRIGGER_TID);
        return getReadableDatabase().rawQuery(TREE_SELECT, new String[]{String.valueOf(triggerId)});
    }

    public Cursor getAllEnabledTriggers() {
        String   whereClause = ATRIGGER_ENABLED+" = ?";
        String[] whereArgs = {"1"};
        return getReadableDatabase().query(TABLE_ALERT_TRIGGERS, null, whereClause, whereArgs,
                null, null, null, null);
    }

    public Cursor getAllTriggerGroups() {
        String   whereClause = ATRIGGER_TYPE+" = ?";
        // this looks unnecessarily verbose since ATRIGGER_GROUP is 0, but this is just
        // to make it more future proof
        String[] whereArgs = {String.valueOf(AlertTriggerType.ATRIGGER_GROUP.attToInt())};
        return getReadableDatabase().query(TABLE_ALERT_TRIGGERS, null, whereClause, whereArgs,
                null, null, null, null);
    }

    public Cursor getAllTriggers() {
        return getReadableDatabase().query(TABLE_ALERT_TRIGGERS, null, null, null,
                null, null, null, null);
    }

    public Long createTrigger(AlertTriggerType att, Long argument1, Double argument2, Long specificity, boolean enabled) {
        int attInt = att.attToInt();

        ContentValues cv = new ContentValues();
        cv.put(ATRIGGER_TYPE, attInt);
        cv.put(ATRIGGER_ARG1, argument1);
        cv.put(ATRIGGER_ARG2, argument2);
        cv.put(ATRIGGER_SPECIFICITY, specificity);
        cv.put(ATRIGGER_ENABLED, enabled);
        return getWritableDatabase().insert(TABLE_ALERT_TRIGGERS, null, cv);
    }

    public void addTriggerToGroup(long groupId, long alertTriggerId) {
        ContentValues cv = new ContentValues();
        cv.put(SUBTRIGGER_GID, groupId);
        cv.put(SUBTRIGGER_TID, alertTriggerId);

        getWritableDatabase().insert(TABLE_SUBTRIGGERS, null, cv);
    }

    public void addTriggersToGroup(long groupId, Long... alertTriggerIds) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.beginTransaction();
        try {
            for (Long alertTriggerId: alertTriggerIds) {
                ContentValues cv = new ContentValues();
                cv.put(SUBTRIGGER_GID, groupId);
                cv.put(SUBTRIGGER_TID, alertTriggerId);
                sqLiteDatabase.insert(TABLE_SUBTRIGGERS, null, cv);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public void setTriggerEnabled(long alertTriggerId, boolean enabled) {
        ContentValues cv = new ContentValues();
        cv.put(ATRIGGER_ENABLED, enabled);

        String   whereClause = COLUMN_ID+" = ?";
        String[] whereArgs   = new String[]{String.valueOf(alertTriggerId)};

        getWritableDatabase().update(TABLE_ALERT_TRIGGERS, cv, whereClause, whereArgs);
    }

    public void updateTriggerParams(long alertTriggerId, Long argument1, Double argument2, Long specificity, boolean enabled) {
        ContentValues cv = new ContentValues();
        cv.put(ATRIGGER_ARG1, argument1);
        cv.put(ATRIGGER_ARG2, argument2);
        cv.put(ATRIGGER_SPECIFICITY, specificity);
        cv.put(ATRIGGER_ENABLED, enabled);

        String   whereClause = COLUMN_ID+" = ?";
        String[] whereArgs   = new String[]{String.valueOf(alertTriggerId)};

        getWritableDatabase().update(TABLE_ALERT_TRIGGERS, cv, whereClause, whereArgs);
    }

    public void removeSubtriggers(long groupId) {
        String whereClause = SUBTRIGGER_GID+" = ?";
        String[] whereArgs = new String[]{String.valueOf(groupId)};
        getWritableDatabase().delete(TABLE_SUBTRIGGERS, whereClause, whereArgs);
    }

    public void removeSubtrigger(long groupId, long alertTriggerId) {
        String whereClause = SUBTRIGGER_GID+" = ? AND "+SUBTRIGGER_TID+" = ?";
        String[] whereArgs = new String[]{String.valueOf(groupId), String.valueOf(alertTriggerId)};
        getWritableDatabase().delete(TABLE_SUBTRIGGERS, whereClause, whereArgs);
    }

    public void deleteTrigger(long alertTriggerId) {
        String   whereClause = COLUMN_ID+" = ?";
        String[] whereArgs   = new String[]{String.valueOf(alertTriggerId)};

        getWritableDatabase().delete(TABLE_ALERT_TRIGGERS, whereClause, whereArgs);
    }

    public void deleteTriggers(Long... alertTriggerIds) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        int count = alertTriggerIds.length;
        String whereClause = String.format("%1$s in (%2$s)", COLUMN_ID, makePlaceholders(count));
        String[] whereArgs = new String[count];
        String[] fields = {COLUMN_ID, ATRIGGER_TYPE};

        String groupWhereClause = SUBTRIGGER_GID+" = ?";

        for (int i = 0; i < count; i++) {
            whereArgs[i]=String.valueOf(alertTriggerIds[i]);
        }

        sqLiteDatabase.beginTransaction();
        try {
            Cursor cursor = sqLiteDatabase.query(TABLE_ALERT_TRIGGERS, fields, whereClause, whereArgs,
                    null, null, null, null);

            while (cursor.moveToNext()) {
                AlertTriggerType att = AlertTriggerType.intToATT(cursor.getInt(cursor.getColumnIndex(ATRIGGER_TYPE)));
                if (att == AlertTriggerType.ATRIGGER_GROUP) {
                    long gid = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    String[] groupWhereArgs = {String.valueOf(gid)};
                    sqLiteDatabase.delete(TABLE_SUBTRIGGERS, groupWhereClause, groupWhereArgs);
                }
            }
            cursor.close();
            sqLiteDatabase.delete(TABLE_ALERT_TRIGGERS, whereClause, whereArgs);
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    /* /ALERT TRIGGER FUNCTIONS */

    public static String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(ATRIGGER_CREATE);
            sqLiteDatabase.execSQL(SUBTRIGGERS_CREATE);
        }
    }
}
