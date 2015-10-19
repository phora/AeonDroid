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
    public  static final String ALERT_LABEL   = "label";
    public  static final String ALERT_ENABLED   = "enabled";
    private static final String ALERTS_CREATE = "CREATE TABLE " + TABLE_ALERTS +
            " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ALERT_LABEL + " TEXT NOT NULL, "
            + ALERT_ENABLED + "INT)";

    private static final String TABLE_ALERT_STEPS = "alert_steps";
    // can be a link to a file, will attempt to display data inline
    // if image or plaintext that isn't a URL
    public  static final String STEP_LINK = "uri";
    // an image stored in the database
    public  static final String STEP_IMAGE = "image";
    // text stored in the database
    public  static final String STEP_DESCRIPTION = "description";
    // any color that's needed for reference in the step
    public  static final String STEP_COLOR = "color";
    public  static final String STEP_REPITITIONS = "reps";
    private static final String ALERT_STEPS_CREATE = "CREATE TABLE "+TABLE_ALERT_STEPS+
            " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + STEP_LINK + " TEXT, "
            + STEP_IMAGE + " TEXT, "
            + STEP_DESCRIPTION + " TEXT, "
            + STEP_COLOR + " INT, "
            + STEP_REPITITIONS + " INT)";

    private static final String TABLE_LINKED_STEPS = "linked_steps";
    public  static final String LINKED_ALERT          = "_alert_id";
    public  static final String LINKED_STEP = "_step_id";
    public  static final String LINKED_STEP_ORDER = "step_order";
    private static final String LINKED_STEPS_CREATE = "CREATE TABLE "+TABLE_LINKED_STEPS+
            " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LINKED_ALERT + " INTEGER NOT NULL, "
            + LINKED_STEP + " INTEGER NOT NULL, "
            + LINKED_STEP_ORDER + " INTEGER)";

    private static final String TABLE_LINKED_TRIGGERS = "linked_triggers";
    public  static final String LINKED_TRIGGER        = "_trigger_id";
    private static final String LINKED_TRIGGERS_CREATE = "CREATE TABLE " + TABLE_LINKED_TRIGGERS +
            " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LINKED_ALERT + " INTEGER NOT NULL, "
            + LINKED_TRIGGER + " INTEGER NOT NULL)";

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

        //sqLiteDatabase.execSQL(ALERTS_CREATE);
        //sqLiteDatabase.execSQL(LINKED_TRIGGERS_CREATE);
        //sqLiteDatabase.execSQL(ALERT_STEPS_CREATE);
        //sqLiteDatabase.execSQL(LINKED_STEPS_CREATE);
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

    /* ALERT+STEP FUNCTIONS */
    public Cursor allAlerts() {
        return getReadableDatabase().query(TABLE_ALERTS, null, null, null,
                null, null, null, null);
    }

    public Cursor triggersForAlert(Long alertId) {
        String selectTriggers = String.format("SELECT * FROM  %1$s " +
                "JOIN %2$s " +
                "ON %2$s.%3$s=%1$s.%4$s " +
                "WHERE %2$s.%5$s=?",
                TABLE_ALERT_TRIGGERS, TABLE_LINKED_TRIGGERS,
                LINKED_TRIGGER, COLUMN_ID, LINKED_ALERT);
        return getReadableDatabase().rawQuery(selectTriggers, new String[]{String.valueOf(alertId)});
    }

    public Cursor stepsForAlert(Long alertId) {
        String selectSteps = String.format("SELECT *, %2$s.%5$s FROM  %1$s " +
                        "JOIN %2$s " +
                        "ON %2$s.%3$s=%1$s.%4$s " +
                        "WHERE %2$s.%5$s=?",
                TABLE_ALERT_TRIGGERS, TABLE_LINKED_STEPS,
                LINKED_STEP, COLUMN_ID, LINKED_ALERT);
        return getReadableDatabase().rawQuery(selectSteps, new String[]{String.valueOf(alertId)});
    }

    public long createAlert(String label) {
        ContentValues cv = new ContentValues();
        cv.put(ALERT_LABEL, label);
        return getWritableDatabase().insert(TABLE_ALERTS, null, cv);
    }

    public void renameAlert(long alertId, String label) {
        String   whereClause = COLUMN_ID+" = ?";
        String[] whereArgs = new String[]{String.valueOf(alertId)};

        ContentValues cv = new ContentValues();
        cv.put(ALERT_LABEL, label);

        getWritableDatabase().update(TABLE_ALERTS, cv, whereClause, whereArgs);
    }

    public long createAlertStep(String uri, String imageUri, String description, Integer color,
                                Integer repetitions) {
        ContentValues cv = new ContentValues();

        cv.put(STEP_LINK, uri);
        cv.put(STEP_IMAGE, imageUri);
        cv.put(STEP_DESCRIPTION, description);
        cv.put(STEP_COLOR, color);
        cv.put(STEP_REPITITIONS, repetitions);

        return getWritableDatabase().insert(TABLE_ALERT_STEPS, null, cv);
    }

    public void updateAlertStep(long stepId, String uri, String imageUri, String description,
                                Integer color, Integer repetitions) {
        String   whereClause = COLUMN_ID+" = ?";
        String[] whereArgs = new String[]{String.valueOf(stepId)};

        ContentValues cv = new ContentValues();

        cv.put(STEP_LINK, uri);
        cv.put(STEP_IMAGE, imageUri);
        cv.put(STEP_DESCRIPTION, description);
        cv.put(STEP_COLOR, color);
        cv.put(STEP_REPITITIONS, repetitions);

        getWritableDatabase().update(TABLE_ALERT_STEPS, cv, whereClause, whereArgs);
    }

    public long linkAlertStep(long alertId, long stepId, Integer stepOrder) {
        ContentValues cv = new ContentValues();
        cv.put(LINKED_ALERT, alertId);
        cv.put(LINKED_STEP, stepId);
        cv.put(LINKED_STEP_ORDER, stepOrder);
        return getWritableDatabase().insert(TABLE_LINKED_STEPS, null, cv);
    }

    public void unlinkAlertStep(long stepAlertPairId) {
        String   whereClause = COLUMN_ID+" = ?";
        String[] whereArgs = new String[]{String.valueOf(stepAlertPairId)};

        getWritableDatabase().delete(TABLE_LINKED_STEPS, whereClause, whereArgs);
    }
    /* /ALERT+STEP FUNCTIONS */

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
        if (oldVersion < 3) {
            sqLiteDatabase.execSQL(ALERTS_CREATE);
            sqLiteDatabase.execSQL(LINKED_TRIGGERS_CREATE);
            sqLiteDatabase.execSQL(ALERT_STEPS_CREATE);
            sqLiteDatabase.execSQL(LINKED_STEPS_CREATE);
        }
    }
}
