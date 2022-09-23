/*
 * Licensed Materials - Property of IBM
 *
 * 5747-SM3
 *
 * (C) Copyright IBM Corp. 1999, 2012 All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *
 */
package com.jovistar.caltxt.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.jovistar.caltxt.activity.CaltxtPager;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.bo.XPlc;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.bo.XRul;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

//import com.jovistar.caltxt.bo.XLoc;
//import com.jovistar.caltxt.service.LocationReceiver;

/**
 * <code>Persistence</code> deals with interacting with the database to persist
 * {@link ConnectionMqtt} objects so created clients survive, the destruction of the
 * singleton {@link Connections} object.
 */
public class Persistence extends SQLiteOpenHelper implements BaseColumns {

    private static final String TAG = "Persistence";

    private static Persistence instance = null;//single instance for Application
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase;

    /**
     * The version of the database
     **/
    public static final int DATABASE_VERSION = 1;

    private static final long MAX_XRSP_ENTRIES = 10;
    /**
     * The name of the database file
     **/
    public static final String DATABASE_NAME = "caltxt.db";
    /**
     * The name of the caltxt table
     **/
    public static final String TABLE_XCTX = "xctx";
    public static final String TABLE_XMOB = "xmob";
    public static final String TABLE_XQRP = "xQrp";//quick response
    public static final String TABLE_XBLO = "xBlo";//blocked numbers (not in contacts)
    public static final String TABLE_XPLC = "xPlc";//cell id : status tagging ('at home', 'at work')
    public static final String TABLE_XRUL = "xRul";//cell id : status tagging ('at home', 'at work')
    public static final String TABLE_XLOC = "xLoc";//android location object
    public static final String TABLE_PLACES = "places";//place which can be tagged

    /**
     * Table column for XCtx
     **/
    public static final String COLUMN_NAME_CALLER = "name_caller";
    public static final String COLUMN_NAME_CALLEE = "name_callee";
    public static final String COLUMN_NUMBER_CALLER = "number_caller";
    public static final String COLUMN_NUMBER2_CALLER = "number2_caller";//for XCTX
    public static final String COLUMN_NUMBER_CALLEE = "number_callee";
    public static final String COLUMN_NUMBER2_CALLEE = "number2_callee";//for XMOB
    public static final String COLUMN_CONTEXT_CALLER = "context_caller";
    public static final String COLUMN_OCCUPATION_CALLER = "occupation_caller";
    public static final String COLUMN_CITY_CALLER = "city_caller";
    public static final String COLUMN_ICON_URL = "iconurl";
    public static final String COLUMN_TOD = "time_of_day";
    public static final String COLUMN_TOD_RECV = "time_of_day_recv";
    public static final String COLUMN_TOD_START = "time_of_day_start";
    public static final String COLUMN_TOD_END = "time_of_day_end";
    public static final String COLUMN_CALL_STATE = "callstate";
    public static final String COLUMN_ACK = "ack";
    public static final String COLUMN_REMOTE_PERSISTENCE_ID = "rpid";//remote persistence id
    public static final String COLUMN_CALL_OPTIONS = "pri";
    public static final String COLUMN_UNAME_CALLEE = "uname";
    public static final String COLUMN_HEADLINE_CALLEE = "headline";
    public static final String COLUMN_STATUS_CALLEE = "status";
    public static final String COLUMN_TOD_CHG = "time_of_day_chg";
    public static final String COLUMN_QRP_AUTO = "qrp_auto_response_end_time";
    public static final String COLUMN_NAME_QRP = "qrp_response_candidate";//one of the quick response
    public static final String COLUMN_NUMBER_BLOCKED = "blo_number_blocked";//blocked number or prefix
    public static final String COLUMN_NUMBER_BLOCKED_NAME = "blo_number_name";//blocked number name
    public static final String COLUMN_CELLID = "cellid";//cell id (mcc, mnc combination for gsm; cellid for cdma)
    public static final String COLUMN_RULE_EVENT = "rule_event";//status change, incoming call, incoming text
    public static final String COLUMN_RULE_EVENT_VALUE = "rule_event_value";//status changed to "at Work", "at Home"
    public static final String COLUMN_RULE_EVENT_FROM = "rule_event_from";
    public static final String COLUMN_RULE_EVENT_TIMESTAMP = "rule_event_timestamp";
    public static final String COLUMN_RULE_ACTION = "rule_action";//send, call, block
    public static final String COLUMN_RULE_ACTION_WHEN = "rule_action_when";
    public static final String COLUMN_RULE_ACTION_VALUE = "rule_action_value";//send, call "I am home"
    public static final String COLUMN_RULE_ACTION_FOR = "rule_action_for";//send, call "919953693002"
    public static final String COLUMN_RULE_ENABLED = "rule_enabled";//rule is enabled?
    public static final String COLUMN_RULE_ACTION_REPEAT = "rule_action_repeat";//once, daily, hourly
    public static final String COLUMN_RULE_ACTION_ALWAYSASK = "rule_action_alwaysask";//always ask or auto
    public static final String COLUMN_LOCATION_NAME = "location_name";
    public static final String COLUMN_LOCATION_LAT = "location_latitude";
    public static final String COLUMN_LOCATION_LON = "location_longitude";
    public static final String COLUMN_LOCATION_ACCURACY = "location_accuracy";
    public static final String COLUMN_LOCATION_BEARING = "location_bearing";
    public static final String COLUMN_LOCATION_PROVIDER = "location_provider";
    public static final String COLUMN_LOCATION_TIMESTAMP = "location_timestamp";
    public static final String COLUMN_LOCATION_SPEED = "location_speed";
    public static final String COLUMN_CONTEXT_ETC = "caltxt_etc";
    public static final String COLUMN_ACK_ETC = "ack_etc";
    public static final String COLUMN_NET_TYPE = "network_type";
    public static final String COLUMN_PLACE_NAME = "place_name";

    /** Table column for XCtt **/

    // sql lite data types
    /**
     * Text type for SQLite
     **/
    private static final String TEXT_TYPE = " TEXT";
    /**
     * Int type for SQLite
     **/
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    /**
     * Comma separator
     **/
    private static final String COMMA_SEP = ",";

    /**
     * Create tables query
     **/
    private static final String SQL_CREATE_ENTRIES_XCTX =

            "CREATE TABLE if not exists " + TABLE_XCTX + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NAME_CALLER + TEXT_TYPE + COMMA_SEP
                    + COLUMN_NAME_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_NUMBER_CALLER + TEXT_TYPE + COMMA_SEP
                    + COLUMN_NUMBER2_CALLER + TEXT_TYPE + COMMA_SEP
                    + COLUMN_NUMBER_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_CONTEXT_CALLER + TEXT_TYPE + COMMA_SEP
                    + COLUMN_CONTEXT_ETC + TEXT_TYPE + COMMA_SEP
                    + COLUMN_OCCUPATION_CALLER + TEXT_TYPE + COMMA_SEP
                    + COLUMN_CITY_CALLER + TEXT_TYPE + COMMA_SEP
                    + COLUMN_ICON_URL + TEXT_TYPE + COMMA_SEP
                    + COLUMN_TOD_RECV + INT_TYPE + COMMA_SEP
                    + COLUMN_TOD_START + INT_TYPE + COMMA_SEP
                    + COLUMN_TOD_END + INT_TYPE + COMMA_SEP
                    + COLUMN_CALL_STATE + INT_TYPE + COMMA_SEP
                    + COLUMN_ACK + TEXT_TYPE + COMMA_SEP
                    + COLUMN_ACK_ETC + TEXT_TYPE + COMMA_SEP
                    + COLUMN_REMOTE_PERSISTENCE_ID + INT_TYPE + COMMA_SEP
                    + COLUMN_CALL_OPTIONS + INT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_XMOB =

            "CREATE TABLE if not exists " + TABLE_XMOB + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NAME_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_NUMBER_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_NUMBER2_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_UNAME_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_HEADLINE_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_STATUS_CALLEE + INT_TYPE + COMMA_SEP
                    + COLUMN_TOD_CHG + INT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_XQRP =

            "CREATE TABLE if not exists " + TABLE_XQRP + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NAME_QRP + TEXT_TYPE + COMMA_SEP
                    + COLUMN_QRP_AUTO + INT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_XBLO =

            "CREATE TABLE if not exists " + TABLE_XBLO + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NUMBER_BLOCKED + TEXT_TYPE + COMMA_SEP
                    + COLUMN_NUMBER_BLOCKED_NAME + TEXT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_XPLC =

            "CREATE TABLE if not exists " + TABLE_XPLC + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_STATUS_CALLEE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_TOD + INT_TYPE + COMMA_SEP
                    + COLUMN_NET_TYPE + INT_TYPE + COMMA_SEP
                    + COLUMN_CELLID + TEXT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_PLACES =

            "CREATE TABLE if not exists " + TABLE_PLACES + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_PLACE_NAME + TEXT_TYPE + COMMA_SEP
                    + COLUMN_CELLID + TEXT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_XLOC =

            "CREATE TABLE if not exists " + TABLE_XLOC + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_LOCATION_NAME + TEXT_TYPE + COMMA_SEP
                    + COLUMN_LOCATION_LAT + REAL_TYPE + COMMA_SEP
                    + COLUMN_LOCATION_LON + REAL_TYPE + COMMA_SEP
                    + COLUMN_LOCATION_BEARING + REAL_TYPE + COMMA_SEP
                    + COLUMN_LOCATION_SPEED + REAL_TYPE + COMMA_SEP
                    + COLUMN_LOCATION_PROVIDER + TEXT_TYPE + COMMA_SEP
                    + COLUMN_LOCATION_ACCURACY + REAL_TYPE + COMMA_SEP
                    + COLUMN_LOCATION_TIMESTAMP + INT_TYPE + ")";

    private static final String SQL_CREATE_ENTRIES_XRUL =

            "CREATE TABLE if not exists " + TABLE_XRUL + " (" + _ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_RULE_ENABLED + INT_TYPE + COMMA_SEP
                    + COLUMN_RULE_EVENT + TEXT_TYPE + COMMA_SEP
                    + COLUMN_RULE_EVENT_FROM + TEXT_TYPE + COMMA_SEP
                    + COLUMN_RULE_EVENT_VALUE + TEXT_TYPE + COMMA_SEP
                    + COLUMN_RULE_EVENT_TIMESTAMP + INT_TYPE + COMMA_SEP
                    + COLUMN_RULE_ACTION + TEXT_TYPE + COMMA_SEP
                    + COLUMN_RULE_ACTION_WHEN + INT_TYPE + COMMA_SEP
                    + COLUMN_RULE_ACTION_FOR + TEXT_TYPE + COMMA_SEP
                    + COLUMN_RULE_ACTION_REPEAT + TEXT_TYPE + COMMA_SEP
                    + COLUMN_RULE_ACTION_ALWAYSASK + INT_TYPE + COMMA_SEP
                    + COLUMN_RULE_ACTION_VALUE + TEXT_TYPE + ")";

    /**
     * Delete tables entry
     **/
    private static final String SQL_DELETE_ENTRIES_XCTX = "DROP TABLE IF EXISTS "
            + TABLE_XCTX;
    private static final String SQL_DELETE_ENTRIES_XMOB = "DROP TABLE IF EXISTS "
            + TABLE_XMOB;
    private static final String SQL_DELETE_ENTRIES_XQRP = "DROP TABLE IF EXISTS "
            + TABLE_XQRP;
    private static final String SQL_DELETE_ENTRIES_XBLO = "DROP TABLE IF EXISTS "
            + TABLE_XBLO;
    private static final String SQL_DELETE_ENTRIES_XPLC = "DROP TABLE IF EXISTS "
            + TABLE_XPLC;
    private static final String SQL_DELETE_ENTRIES_PLACES = "DROP TABLE IF EXISTS "
            + TABLE_PLACES;
    private static final String SQL_DELETE_ENTRIES_PALCES = "DROP TABLE IF EXISTS "
            + TABLE_PLACES;
    private static final String SQL_DELETE_ENTRIES_XRUL = "DROP TABLE IF EXISTS "
            + TABLE_XRUL;
    private static final String SQL_DELETE_ENTRIES_XLOC = "DROP TABLE IF EXISTS "
            + TABLE_XLOC;

    /**
     * Get last record id
     **/
    private static final String SQL_MAX_RECORD_ID_XCTX = "SELECT * FROM "
            + TABLE_XCTX + " WHERE " + _ID + " = (SELECT MAX(" + _ID
            + ") FROM " + TABLE_XCTX + ")";

    Context context;

    /**
     * Creates the persistence object passing it a context
     *
     * @param context Context that the application is running in
     */
    private Persistence(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        SQLiteDatabase db = openDatabase();
        db.execSQL(SQL_CREATE_ENTRIES_XCTX);
        db.execSQL(SQL_CREATE_ENTRIES_XMOB);
        db.execSQL(SQL_CREATE_ENTRIES_XQRP);
        db.execSQL(SQL_CREATE_ENTRIES_XBLO);
        db.execSQL(SQL_CREATE_ENTRIES_XPLC);
        db.execSQL(SQL_CREATE_ENTRIES_PLACES);
        db.execSQL(SQL_CREATE_ENTRIES_XRUL);
        db.execSQL(SQL_CREATE_ENTRIES_XLOC);
    }

    public static synchronized Persistence getInstance(Context context) {
        if (instance == null)
            instance = new Persistence(context);
        return instance;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
     * .SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_XCTX);
        db.execSQL(SQL_CREATE_ENTRIES_XMOB);
        db.execSQL(SQL_CREATE_ENTRIES_XQRP);
        db.execSQL(SQL_CREATE_ENTRIES_XBLO);
        db.execSQL(SQL_CREATE_ENTRIES_XPLC);
        db.execSQL(SQL_CREATE_ENTRIES_PLACES);
        db.execSQL(SQL_CREATE_ENTRIES_XRUL);
        db.execSQL(SQL_CREATE_ENTRIES_XLOC);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
     * .SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_XCTX);
        db.execSQL(SQL_DELETE_ENTRIES_XMOB);
        db.execSQL(SQL_DELETE_ENTRIES_XQRP);
        db.execSQL(SQL_DELETE_ENTRIES_XBLO);
        db.execSQL(SQL_DELETE_ENTRIES_XPLC);
        db.execSQL(SQL_DELETE_ENTRIES_PLACES);
        db.execSQL(SQL_DELETE_ENTRIES_XRUL);
        db.execSQL(SQL_DELETE_ENTRIES_XLOC);
        onCreate(db);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onDowngrade(android.database
     * .sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = getWritableDatabase();
        }
        return mDatabase;
    }

    private synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();
        }
    }

    public synchronized void clearXCTX() {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XCTX, null, null);
        closeDatabase();
    }

    public synchronized void clearXBLO() {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XBLO, null, null);
        closeDatabase();
    }

    public synchronized void clearXPLC() {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XPLC, null, null);
        closeDatabase();
    }

    public synchronized void clearPlaces() {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_PLACES, null, null);
        closeDatabase();
    }

    public synchronized void clearXRUL() {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XRUL, null, null);
        closeDatabase();
    }

    public synchronized void clearXMOB() {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XMOB, null, null);
        closeDatabase();
    }

    public synchronized long insertXQRP(String qresponse) {
        if (getCountXQRP() > MAX_XRSP_ENTRIES) {
//			Notify.toast(Globals.getCustomAppContext(), 
//					Globals.getCustomAppContext().getString(R.string.warn_maxsize_xqrsp), 
//					Toast.LENGTH_SHORT);
            return -1;
        }

        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long newRowId = -1;

        // put the column values object
        values.put(COLUMN_NAME_QRP, qresponse);
        values.put(COLUMN_QRP_AUTO, 0);

        // insert the values into the tables, returns the ID for the row
        newRowId = db.insert(TABLE_XQRP, null, values);
        if (newRowId == -1) {
            Log.e(TAG, TABLE_XQRP + " newRowId:" + newRowId
                    + ", failed for qresponse:" + qresponse);
        }
        closeDatabase(); // close the db then deal with the result of the query
        return newRowId;
    }

    public synchronized long insertXBLO(String number, String name) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long newRowId = -1;

        // put the column values object
        values.put(COLUMN_NUMBER_BLOCKED, number);
        values.put(COLUMN_NUMBER_BLOCKED_NAME, name);

        // insert the values into the tables, returns the ID for the row
        newRowId = db.insert(TABLE_XBLO, null, values);
        if (newRowId == -1) {
            Log.e(TAG, TABLE_XBLO + " newRowId:" + newRowId
                    + ", failed for qresponse:" + number);
        }
        closeDatabase(); // close the db then deal with the result of the query
        return newRowId;
    }

    public synchronized long insertXPLC(String status, String cellid, int type) {
        if (cellid == null || cellid.length() == 0)
            return 0;

        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long newRowId = -1;

        // put the column values object
        values.put(COLUMN_CELLID, cellid);
        values.put(COLUMN_STATUS_CALLEE, status);
        values.put(COLUMN_TOD, Calendar.getInstance().getTimeInMillis());
        values.put(COLUMN_NET_TYPE, type);

        if (getPlaceForCellId(cellid) == null) {
            // insert the values into the tables, returns the ID for the row
            newRowId = db.insert(TABLE_XPLC, null, values);
            if (newRowId == -1) {
                Log.e(TAG, TABLE_XPLC + " newRowId:" + newRowId
                        + ", failed for qresponse:" + status);
            }
        } else {
            setStatusForCellId(cellid, status);
        }
        closeDatabase(); // close the db then deal with the result of the query
        return newRowId;
    }

    public synchronized long insertPlace(String name) {
        if (name == null || name.length() == 0)
            return 0;

        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long newRowId = -1;

        // put the column values object
        values.put(COLUMN_PLACE_NAME, name);
        values.put(COLUMN_CELLID, "");

        // insert the values into the tables, returns the ID for the row
        newRowId = db.insert(TABLE_PLACES, null, values);
        if (newRowId == -1) {
            Log.e(TAG, TABLE_PLACES + " newRowId:" + newRowId
                    + ", failed for qresponse:" + name);
        }

        closeDatabase(); // close the db then deal with the result of the query
        return newRowId;
    }

    /*
        public synchronized long insertXLOC(XLoc o) {
            if(o.getName()==null || o.getName().length()==0)
                return 0;

            SQLiteDatabase db = openDatabase();
            ContentValues values = new ContentValues();
            long newRowId = -1;

            // put the column values object
            values.put(COLUMN_LOCATION_NAME, o.getName());
            values.put(COLUMN_LOCATION_PROVIDER, o.getProvider());
            values.put(COLUMN_LOCATION_ACCURACY, o.getAccuracy());
            values.put(COLUMN_LOCATION_BEARING, o.getBearing());
            values.put(COLUMN_LOCATION_LAT, o.getLatitude());
            values.put(COLUMN_LOCATION_LON, o.getLongitude());
            values.put(COLUMN_LOCATION_PROVIDER, o.getProvider());
            values.put(COLUMN_LOCATION_SPEED, o.getSpeed());
            values.put(COLUMN_LOCATION_TIMESTAMP, o.getTimestamp());

            // insert the values into the tables, returns the ID for the row
            newRowId = db.insert(TABLE_XLOC, null, values);
            if (newRowId == -1) {
                Log.e(TAG, TABLE_XLOC +" newRowId:"+newRowId
                        +", failed for qresponse:"+o.getName());
            }

            closeDatabase(); // close the db then deal with the result of the query
            return newRowId;
        }
    */
    public synchronized long insertXRUL(XRul rul) {

        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long newRowId = -1;

        // put the column values object
        values.put(COLUMN_RULE_ENABLED, 0);//default disabled
        values.put(COLUMN_RULE_ACTION, rul.getAction());
        values.put(COLUMN_RULE_ACTION_VALUE, rul.getActionValue());
        values.put(COLUMN_RULE_ACTION_REPEAT, rul.getActionRepeat());
        values.put(COLUMN_RULE_ACTION_ALWAYSASK, 1);//default always ask
        values.put(COLUMN_RULE_ACTION_FOR, rul.getActionFor());
        values.put(COLUMN_RULE_ACTION_WHEN, rul.getActionWhen());
        values.put(COLUMN_RULE_EVENT, rul.getEvent());
        values.put(COLUMN_RULE_EVENT_VALUE, rul.getEventValue());
        values.put(COLUMN_RULE_EVENT_FROM, rul.getEventFrom());
        values.put(COLUMN_RULE_EVENT_TIMESTAMP, rul.getEventTimestamp());

        // insert the values into the tables, returns the ID for the row
        newRowId = db.insert(TABLE_XRUL, null, values);
        if (newRowId == -1) {
            Log.e(TAG, TABLE_XRUL + " newRowId:" + newRowId
                    + ", failed for qresponse:" + rul);
        }

        closeDatabase(); // close the db then deal with the result of the query
        return newRowId;
    }

    public XRul getXRul(long pid) {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] columns = {_ID, COLUMN_RULE_ENABLED,
                COLUMN_RULE_EVENT, COLUMN_RULE_EVENT_VALUE, COLUMN_RULE_EVENT_FROM, COLUMN_RULE_EVENT_TIMESTAMP,
                COLUMN_RULE_ACTION, COLUMN_RULE_ACTION_VALUE, COLUMN_RULE_ACTION_REPEAT, COLUMN_RULE_ACTION_ALWAYSASK, COLUMN_RULE_ACTION_FOR, COLUMN_RULE_ACTION_WHEN};
        String selection = _ID + "=\"" + pid + "\"";

        Cursor c = db.query(TABLE_XRUL, columns, selection, null, null, null,
                sort + " DESC");

        XRul rul = null;
        if (c.getCount() == 1) {
            c.moveToNext();

            rul = new XRul();
            rul.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
            rul.setAction(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION)));
            rul.setActionValue(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_VALUE)));
            rul.setActionRepeat(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_REPEAT)));
            rul.setActionFor(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_FOR)));
            rul.setActionWhen(c.getLong(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_WHEN)));
            rul.setEvent(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT)));
            rul.setEventValue(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT_VALUE)));
            rul.setEventFrom(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT_FROM)));
            rul.setEventTimestamp(c.getLong(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT_TIMESTAMP)));
            rul.setEnabled(c.getInt(c.getColumnIndexOrThrow(COLUMN_RULE_ENABLED)) != 0);
            rul.setAlwaysAsk(c.getInt(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_ALWAYSASK)) != 0);

//			IFTTT.getRules().put(rul.getPersistenceId(), rul);
        }

        c.close();
        closeDatabase();

        return rul;
    }

    /*
        public XLoc getXLoc(String name) {
            String sort = _ID;
            SQLiteDatabase db = openDatabase();

            // columns to return
            String[] columns = { _ID, COLUMN_LOCATION_NAME,
                    COLUMN_LOCATION_LAT, COLUMN_LOCATION_LON, COLUMN_LOCATION_ACCURACY, COLUMN_LOCATION_BEARING,
                    COLUMN_LOCATION_PROVIDER, COLUMN_LOCATION_TIMESTAMP, COLUMN_LOCATION_SPEED };
            String selection = COLUMN_LOCATION_NAME+"=\""+name+"\"";

            Cursor c = db.query(TABLE_XLOC, columns, selection, null, null, null,
                    sort+" DESC");

            XLoc loc = null;
            if(c.getCount()==1) {
                c.moveToNext();

                loc = new XLoc();
                loc.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
                loc.setAccuracy(c.getFloat(c.getColumnIndexOrThrow(COLUMN_LOCATION_ACCURACY)));
                loc.setBearing(c.getFloat(c.getColumnIndexOrThrow(COLUMN_LOCATION_BEARING)));
                loc.setLatitude(c.getDouble(c.getColumnIndexOrThrow(COLUMN_LOCATION_LAT)));
                loc.setLogitude(c.getDouble(c.getColumnIndexOrThrow(COLUMN_LOCATION_LON)));
                loc.setName(c.getString(c.getColumnIndexOrThrow(COLUMN_LOCATION_NAME)));
                loc.setProvider(c.getString(c.getColumnIndexOrThrow(COLUMN_LOCATION_PROVIDER)));
                loc.setSpeed(c.getFloat(c.getColumnIndexOrThrow(COLUMN_LOCATION_SPEED)));
                loc.setTimestamp(c.getLong(c.getColumnIndexOrThrow(COLUMN_LOCATION_TIMESTAMP)));

            }

            c.close();
            closeDatabase();

            return loc;
        }
    */
    public XPlc getPlaceForNetworkId(String netid) {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] columns = {_ID, COLUMN_STATUS_CALLEE, COLUMN_CELLID, COLUMN_NET_TYPE, COLUMN_TOD};
        String selection = COLUMN_CELLID + " LIKE \"" + netid + "%\"";

        Cursor c = db.query(TABLE_XPLC, columns, selection, null, null, null,
                sort + " DESC");

        if (c.getCount() == 1) {
            // if exactly one place is found, return it
            c.moveToNext();

            XPlc plc = new XPlc();
            plc.setCellId(c.getString(c.getColumnIndexOrThrow(COLUMN_CELLID)));
            plc.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
            plc.setStatus(c.getString(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE)));
            plc.setTime(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD)));
            plc.setType(c.getInt(c.getColumnIndexOrThrow(COLUMN_NET_TYPE)));

            return plc;
        } else if (c.getCount() > 1) {
            // if more than one place found, it means more than one place is inside this network id
            // cant decide which one to return, therefore return null
        } else {
        }

        c.close();
        closeDatabase();

        return null;
    }

    public XPlc getPlaceForCellId(String cellid) {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] columns = {_ID, COLUMN_STATUS_CALLEE, COLUMN_CELLID, COLUMN_NET_TYPE, COLUMN_TOD};
        String selection = COLUMN_CELLID + "=\"" + cellid + "\"";

        Cursor c = db.query(TABLE_XPLC, columns, selection, null, null, null,
                sort + " DESC");

        if (c.getCount() == 1) {
            c.moveToNext();

            XPlc plc = new XPlc();
            plc.setCellId(c.getString(c.getColumnIndexOrThrow(COLUMN_CELLID)));
            plc.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
            plc.setStatus(c.getString(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE)));
            plc.setTime(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD)));
            plc.setType(c.getInt(c.getColumnIndexOrThrow(COLUMN_NET_TYPE)));

            return plc;
        } else if (c.getCount() > 1) {
        } else {
        }

        c.close();
        closeDatabase();

        return null;
    }

    public int getCellNetworkCountForStatus(String status, String network_id) {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] columns = {_ID, COLUMN_STATUS_CALLEE, COLUMN_CELLID, COLUMN_NET_TYPE, COLUMN_TOD};
        String selection = COLUMN_STATUS_CALLEE + "=\"" + status + "\" AND " + COLUMN_CELLID + " LIKE \"" + network_id + "%\"";

        Cursor c = db.query(TABLE_XPLC, columns, selection, null, null, null,
                sort + " DESC");
        int count = c.getCount();

        c.close();
        closeDatabase();

        return count;
    }

    public int getCellIdCountForStatus(String status) {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] columns = {_ID, COLUMN_STATUS_CALLEE, COLUMN_CELLID, COLUMN_NET_TYPE, COLUMN_TOD};
        String selection = COLUMN_NET_TYPE + "=\"" + XPlc.NETWORK_TYPE_CELL + "\" AND "
                + COLUMN_STATUS_CALLEE + "=\"" + status + "\"";

        Cursor c = db.query(TABLE_XPLC, columns, selection, null, null, null,
                sort + " DESC");
        int count = c.getCount();

//		String cellid = null;
/*		for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
				break;
			}

			if(c.getInt(c.getColumnIndexOrThrow(COLUMN_NET_TYPE))==XPlc.NETWORK_TYPE_CELL)
				count++;
		}
*/
        c.close();
        closeDatabase();

        return count;
    }

    public int getWiFiCellIdCountForStatus(String status) {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] columns = {_ID, COLUMN_STATUS_CALLEE, COLUMN_CELLID, COLUMN_NET_TYPE, COLUMN_TOD};
        String selection = COLUMN_NET_TYPE + "=\"" + XPlc.NETWORK_TYPE_WIFI + "\" AND "
                + COLUMN_STATUS_CALLEE + "=\"" + status + "\"";

        Cursor c = db.query(TABLE_XPLC, columns, selection, null, null, null,
                sort + " DESC");
        int count = c.getCount();
//		String cellid = null;
/*		for (int i = 0; i < c.getCount(); i++) {
			if (!c.moveToNext()) {
				break;
			}

			if(c.getInt(c.getColumnIndexOrThrow(COLUMN_NET_TYPE))==XPlc.NETWORK_TYPE_WIFI)
				count++;

//			cellid = c.getString(c.getColumnIndexOrThrow(COLUMN_CELLID));
//			try{
				//gsm cell id is numeric
//				Float.parseFloat(cellid);
//			}catch(NumberFormatException nfe){
//				count++;
//			}
		}
*/
        c.close();
        closeDatabase();

        return count;
    }

    public ArrayList<String> getAllPlaces() {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();
        ArrayList<String> places = new ArrayList<>();

        // columns to return
        String[] columns = {_ID, COLUMN_PLACE_NAME, COLUMN_CELLID};

        Cursor c = db.query(TABLE_PLACES, columns, null, null, null, null,
                sort + " DESC");

        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
                break;
            }
//            String name = c.getString(c.getColumnIndexOrThrow(COLUMN_PLACE_NAME));
            // store it in the list
//            CaltxtStatus plc = new CaltxtStatus(name, name, Addressbook.getInstance(context).getStatusResourceIDByName(name));

            places.add(0, c.getString(c.getColumnIndexOrThrow(COLUMN_PLACE_NAME)));
            Log.d(TAG, " place : "+places.get(i));
        }

        c.close();
        closeDatabase();
        return places;
    }

    public ArrayList<XPlc> getAllXPLC() {
        String sort = COLUMN_STATUS_CALLEE;
        SQLiteDatabase db = openDatabase();
        ArrayList<XPlc> places = new ArrayList<>();

        // columns to return
        String[] columns = {_ID, COLUMN_STATUS_CALLEE, COLUMN_CELLID, COLUMN_NET_TYPE, COLUMN_TOD};
//		String selection = COLUMN_STATUS_CALLEE+"=\""+status+"\"";

        Cursor c = db.query(TABLE_XPLC, columns, null, null, null, null,
                sort + " DESC");

//		CallManager.getInstance().cellids.clear();

        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
                break;
            }
            // store it in the list
            XPlc plc = new XPlc();
            plc.setCellId(c.getString(c.getColumnIndexOrThrow(COLUMN_CELLID)));
            plc.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
            plc.setStatus(c.getString(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE)));
            plc.setTime(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD)));
            plc.setType(c.getInt(c.getColumnIndexOrThrow(COLUMN_NET_TYPE)));

//			CallManager.getInstance().cellids.put(c.getString(c.getColumnIndexOrThrow(COLUMN_CELLID)), plc);
            places.add(0, plc);
//			Log.d(TAG, "getAllXPLC "+plc.getStatus());
        }

        c.close();
        closeDatabase();
        return places;
    }

    /*
        public void getAllXLoc() {
            String sort = COLUMN_LOCATION_NAME;
            SQLiteDatabase db = openDatabase();

            // columns to return
            String[] columns = { _ID, COLUMN_LOCATION_NAME, COLUMN_LOCATION_LAT, COLUMN_LOCATION_LON,
                    COLUMN_LOCATION_BEARING, COLUMN_LOCATION_SPEED, COLUMN_LOCATION_ACCURACY, COLUMN_LOCATION_NAME,
                    COLUMN_LOCATION_PROVIDER };
    //		String selection = COLUMN_STATUS_CALLEE+"=\""+status+"\"";

            Cursor c = db.query(TABLE_XLOC, columns, null, null, null, null,
                    sort+" DESC");

            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) {
                    break;
                }
                // store it in the list
                XLoc loc = new XLoc();
                loc.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
                loc.setAccuracy(c.getFloat(c.getColumnIndexOrThrow(COLUMN_LOCATION_ACCURACY)));
                loc.setBearing(c.getFloat(c.getColumnIndexOrThrow(COLUMN_LOCATION_BEARING)));
                loc.setLatitude(c.getDouble(c.getColumnIndexOrThrow(COLUMN_LOCATION_LAT)));
                loc.setLogitude(c.getDouble(c.getColumnIndexOrThrow(COLUMN_LOCATION_LON)));
                loc.setName(c.getString(c.getColumnIndexOrThrow(COLUMN_LOCATION_NAME)));
                loc.setProvider(c.getString(c.getColumnIndexOrThrow(COLUMN_LOCATION_PROVIDER)));
                loc.setSpeed(c.getFloat(c.getColumnIndexOrThrow(COLUMN_LOCATION_SPEED)));
                loc.setTimestamp(c.getLong(c.getColumnIndexOrThrow(COLUMN_LOCATION_TIMESTAMP)));

                LocationReceiver.nameLocations.put(loc.getName(), loc);
            }

            c.close();
            closeDatabase();
        }
    */
    public ArrayList<XRul> getAllXRUL() {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] columns = {_ID, COLUMN_RULE_ENABLED,
                COLUMN_RULE_EVENT, COLUMN_RULE_EVENT_VALUE, COLUMN_RULE_EVENT_FROM, COLUMN_RULE_EVENT_TIMESTAMP,
                COLUMN_RULE_ACTION, COLUMN_RULE_ACTION_VALUE, COLUMN_RULE_ACTION_REPEAT, COLUMN_RULE_ACTION_ALWAYSASK, COLUMN_RULE_ACTION_FOR, COLUMN_RULE_ACTION_WHEN};
//		String selection = COLUMN_STATUS_CALLEE+"=\""+status+"\"";

        Cursor c = db.query(TABLE_XRUL, columns, null, null, null, null,
                sort + " DESC");

//		IFTTT.getRules().clear();
        ArrayList<XRul> rules = new ArrayList<XRul>();

        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
                break;
            }
            // store it in the list
            XRul rul = new XRul();
            rul.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
            rul.setAction(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION)));
            rul.setActionValue(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_VALUE)));
            rul.setActionRepeat(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_REPEAT)));
            rul.setActionFor(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_FOR)));
            rul.setActionWhen(c.getLong(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_WHEN)));
            rul.setEvent(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT)));
            rul.setEventValue(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT_VALUE)));
            rul.setEventFrom(c.getString(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT_FROM)));
            rul.setEventTimestamp(c.getLong(c.getColumnIndexOrThrow(COLUMN_RULE_EVENT_TIMESTAMP)));
            rul.setEnabled(c.getInt(c.getColumnIndexOrThrow(COLUMN_RULE_ENABLED)) != 0);
            rul.setAlwaysAsk(c.getInt(c.getColumnIndexOrThrow(COLUMN_RULE_ACTION_ALWAYSASK)) != 0);

//			IFTTT.getRules().put(rul.getPersistenceId(), rul);
            rules.add(rul);
        }

        c.close();
        closeDatabase();
        return rules;
    }

    public XQrp getQuickAutoResponse() {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] qrpColumns = {_ID, COLUMN_NAME_QRP, COLUMN_QRP_AUTO};
        String qrpSelection = COLUMN_QRP_AUTO + ">0";

        Cursor c = db.query(TABLE_XQRP, qrpColumns, qrpSelection, null, null, null,
                sort + " DESC");

        XQrp qrp = null;
        if (c.getCount() == 1) {
            c.moveToNext();
            qrp = new XQrp(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_QRP)),
                    c.getLong(c.getColumnIndexOrThrow(COLUMN_QRP_AUTO)),
                    c.getInt(c.getColumnIndexOrThrow(_ID)));
        } else if (c.getCount() > 1) {
//			Notify.toast(Globals.getCustomAppContext(), 
//					Globals.getCustomAppContext().getString(R.string.warn_maxsize_xqrsp_autoresponse), 
//					Toast.LENGTH_SHORT);
        } else {
			/*Notify.toast(Globals.getCustomAppContext(), 
					Globals.getCustomAppContext().getString(R.string.warn_maxsize_xqrsp_noautoresponse), 
					Toast.LENGTH_SHORT);*/
        }

        c.close();
        closeDatabase();

        return qrp;
    }

    /*
        public synchronized void setQuickResponseAsAuto(String qresponse, long endtm) {
            SQLiteDatabase db = openDatabase();
            ContentValues values = new ContentValues();
            long noOfRowsUpdated = 0;

            // put the column values object
    //		values.put(COLUMN_NAME_QRP, qresponse);
            values.put(COLUMN_QRP_AUTO, endtm);

            // update the values into the tables, returns the number of rows updated (must be always 1)
            noOfRowsUpdated = db.update(TABLE_XQRP, values, COLUMN_NAME_QRP + "=?",
                    new String[] { qresponse });
            if (noOfRowsUpdated != 1) {
                Log.e(TAG, "COLUMN_CALL_OPTIONS, U TBL:"+TABLE_XQRP+", noOfRowsUpdated:"+noOfRowsUpdated
                        +", failed for qresponse:"+qresponse);
            }
            closeDatabase(); // close the db then deal with the result of the query
        }
    */
    public synchronized void setQuickResponseAsAuto(int id, long endtm) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
//		values.put(_ID, id);
        values.put(COLUMN_QRP_AUTO, endtm);

        // update the values into the tables, returns the number of rows updated (must be always 1)
        noOfRowsUpdated = db.update(TABLE_XQRP, values, _ID + "=?",
                new String[]{Long.toString(id)});
        if (noOfRowsUpdated != 1) {
            Log.e(TAG, "COLUMN_CALL_OPTIONS, U TBL:" + TABLE_XQRP + ", noOfRowsUpdated:" + noOfRowsUpdated
                    + ", failed for qresponse id:" + id);
        } else {
            XQrp qrp = getQuickAutoResponse();
            if (qrp != null) {
                CaltxtPager.updateNotifications(context);
				/*String when = DateUtils.getRelativeDateTimeString(Globals.getCustomAppContext(), endtm, 
						DateUtils.DAY_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
				if(RebootService.getConnection().isConnected()) {
			    	Notify.notify_caltxt_autoresponse_set_connected(qrp.getQuickResponseValue(), when);
				} else {
			    	Notify.notify_caltxt_autoresponse_set_disconnected(qrp.getQuickResponseValue(), when);
				}*/
            }
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    public synchronized void setStatusForCellId(String cellid, String status) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
        values.put(COLUMN_STATUS_CALLEE, status);
        values.put(COLUMN_TOD, Calendar.getInstance().getTimeInMillis());

        // update the values into the tables, returns the number of rows updated (must be always 1)
        noOfRowsUpdated = db.update(TABLE_XPLC, values, COLUMN_CELLID + "=?",
                new String[]{cellid});
        if (noOfRowsUpdated != 1) {
            Log.e(TAG, "COLUMN_CALL_OPTIONS, U TBL:" + TABLE_XPLC + ", noOfRowsUpdated:" + noOfRowsUpdated
                    + ", failed for cellid :" + cellid);
        } else if (noOfRowsUpdated == 1) {
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    public synchronized void setXRulEnabled(long pid, boolean enabled) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
        values.put(COLUMN_RULE_ENABLED, enabled == true ? 1 : 0);

        // update the values into the tables, returns the number of rows updated (must be always 1)
        noOfRowsUpdated = db.update(TABLE_XRUL, values, _ID + "=?",
                new String[]{Long.toString(pid)});
        if (noOfRowsUpdated != 1) {
            Log.e(TAG, "COLUMN_CALL_OPTIONS, U TBL:" + TABLE_XRUL + ", noOfRowsUpdated:" + noOfRowsUpdated
                    + ", failed for pid :" + pid);
        } else if (noOfRowsUpdated == 1) {
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    public synchronized void resetAutoResponse() {//reset to no set as auto response
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
        values.put(COLUMN_QRP_AUTO, 0);

        noOfRowsUpdated = db.update(TABLE_XQRP, values, null, null);//update all rows
        if (noOfRowsUpdated == 0) {
            Log.e(TAG, "COLUMN_CALL_OPTIONS, U TBL:" + TABLE_XQRP + ", noOfRowsUpdated:" + noOfRowsUpdated
                    + ", none reset");
        } else {
            Notify.notify_caltxt_autoresponse_cancel(context);
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    public synchronized void deleteXQRP(String qresponse) {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XQRP, COLUMN_NAME_QRP + "=?",
                new String[]{qresponse});
        closeDatabase();
    }

    public synchronized void deleteXBLO(String number) {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XBLO, COLUMN_NUMBER_BLOCKED + "=?",
                new String[]{number});
        closeDatabase();
    }

    public synchronized void deleteXPLC(String cellid) {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XPLC, COLUMN_CELLID + "=?",
                new String[]{cellid});
        Log.d(TAG, "deleteXPLC " + cellid);
        closeDatabase();
    }

    public synchronized void deletePlace(String name) {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_PLACES, COLUMN_PLACE_NAME + "=?",
                new String[]{name});
        Log.d(TAG, "deletePlace " + name);
        closeDatabase();
    }

    public synchronized void deleteAllXPLCForStatus(String status) {
        SQLiteDatabase db = openDatabase();

        int c = db.delete(TABLE_XPLC, COLUMN_STATUS_CALLEE + "=?",
                new String[]{status});
        Log.d(TAG, "deleteAllXPLCForStatus " + c);
        closeDatabase();
    }

    public synchronized void deleteXRul(long pid) {
        SQLiteDatabase db = openDatabase();

        db.delete(TABLE_XRUL, _ID + "=?",
                new String[]{Long.toString(pid)});
        closeDatabase();
    }

    public synchronized long getCountXQRP() {
        SQLiteDatabase db = openDatabase();

        long count = DatabaseUtils.queryNumEntries(db, TABLE_XQRP);
        closeDatabase();
        return count;
    }

    public synchronized long getCountXMOB() {
        SQLiteDatabase db = openDatabase();

        long count = DatabaseUtils.queryNumEntries(db, TABLE_XMOB);
        closeDatabase();
        return count;
    }

    public synchronized long getCountXPLC() {
        SQLiteDatabase db = openDatabase();

        long count = DatabaseUtils.queryNumEntries(db, TABLE_XPLC);
        closeDatabase();
        return count;
    }

    public synchronized long getCountPlaces() {
        SQLiteDatabase db = openDatabase();

        long count = DatabaseUtils.queryNumEntries(db, TABLE_PLACES);
        closeDatabase();
        return count;
    }

    public synchronized long getCountXRUL() {
        SQLiteDatabase db = openDatabase();

        long count = DatabaseUtils.queryNumEntries(db, TABLE_XRUL);
        closeDatabase();
        return count;
    }

    public synchronized long getCountXCTX() {
        SQLiteDatabase db = openDatabase();

        long count = DatabaseUtils.queryNumEntries(db, TABLE_XCTX);
        closeDatabase();
        return count;
    }

    /*
        public synchronized long getCountXMOBRegistered() {
            SQLiteDatabase db = openDatabase();

            // columns to return
            String[] qrpColumns = { _ID };
            String qrpSelection = COLUMN_STATUS_CALLEE+"!=2";

            Cursor c = db.query(TABLE_XMOB, qrpColumns, qrpSelection, null, null, null, null);

            long count = c.getCount();

            closeDatabase();
            Log.e(TAG, "getCountXMOBRegistered, count:"+count);
            return count;
        }
    */
    public synchronized ArrayList<XQrp> restoreXQRP() {
        // how to sort the data being returned
        String sort = _ID;
        SQLiteDatabase db = openDatabase();
        ArrayList<XQrp> list = null;

        // columns to return
        String[] qrpColumns = {_ID, COLUMN_NAME_QRP, COLUMN_QRP_AUTO};

        Cursor c = db.query(TABLE_XQRP, qrpColumns, null, null, null, null,
                sort + " DESC");
        list = new ArrayList<XQrp>(c.getCount());
        XQrp obj = null;
        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
                break;
//					throw new PersistenceException(
//							"Failed restoring XCtx - count: " + c.getCount()
//									+ "loop iteration: " + i);
            }
            // get data from cursor
            obj = new XQrp(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_QRP)),
                    c.getLong(c.getColumnIndexOrThrow(COLUMN_QRP_AUTO)),
                    c.getInt(c.getColumnIndexOrThrow(_ID)));
            // store it in the list
            list.add(obj);
        }
        c.close();

        // close the cursor now we are finished with it
        closeDatabase();
        return list;
    }

    public synchronized ArrayList<IDTObject> getAllBlockedNumbers() {
        // how to sort the data being returned
        String sort = _ID;
        SQLiteDatabase db = openDatabase();
        ArrayList<IDTObject> list = null;

        // columns to return
        String[] bloColumns = {_ID, COLUMN_NUMBER_BLOCKED, COLUMN_NUMBER_BLOCKED_NAME};

        Cursor c = db.query(TABLE_XBLO, bloColumns, null, null, null, null,
                sort + " DESC");
        list = new ArrayList<IDTObject>(c.getCount());
        XMob obj = null;
        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) {
                break;
            }
            // get data from cursor
            obj = new XMob();
            obj.setNumber(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_BLOCKED))/*, CaltxtApp.getMyCountryCode()*/);
            obj.setName(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_BLOCKED_NAME)));
            obj.setUsername(XMob.toFQMN(obj.getNumber(), Addressbook.getInstance(context).getMyCountryCode()));
            obj.setBlocked();
            // store it in the list
            list.add(obj);
        }
        c.close();

        // close the cursor now we are finished with it
        closeDatabase();
        return list;
    }

    public synchronized void updateXQRP(String qrp, int rid) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
        values.put(COLUMN_NAME_QRP, qrp);

        // update the values into the tables, returns the number of rows updated (must be always 1)
        noOfRowsUpdated = db.update(TABLE_XQRP, values, _ID + "=?",
                new String[]{Long.toString(rid)});
        if (noOfRowsUpdated != 1) {
            Log.e(TAG, "updateXQRP, noOfRowsUpdated:" + noOfRowsUpdated + ", object:" + qrp);
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    /**
     * Persist a ConnectionMqtt to the database
     *
     * @param connection the connection to persist
     * @throws PersistenceException If storing the data fails
     */
    public synchronized long insert(IDTObject oo) /*throws PersistenceException */ {

        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long newRowId = -1;

        if (oo.getCName().equals("XCtx")) {
            XCtx o = (XCtx) oo;
            // put the column values object
            values.put(COLUMN_NAME_CALLER, o.getNameCaller());
            values.put(COLUMN_NUMBER_CALLER, o.getUsernameCaller());
            if (o.getUsername2Caller() != null || o.getUsername2Caller().length() > 0)
                values.put(COLUMN_NUMBER2_CALLER, o.getUsername2Caller());
            values.put(COLUMN_OCCUPATION_CALLER, o.getOccupation());
            values.put(COLUMN_CONTEXT_CALLER, o.getCaltxt());
            values.put(COLUMN_CONTEXT_ETC, o.getCaltxtEtc());
            values.put(COLUMN_CITY_CALLER, o.getCity());
            values.put(COLUMN_NAME_CALLEE, o.getNameCallee());
            values.put(COLUMN_NUMBER_CALLEE, o.getNumberCallee());
            values.put(COLUMN_ICON_URL, o.getIcon());
            values.put(COLUMN_TOD_RECV, o.getRecvToD());
            values.put(COLUMN_TOD_START, o.getStartToD());
            values.put(COLUMN_TOD_END, o.getEndToD());
            values.put(COLUMN_CALL_STATE, o.getCallState());
            values.put(COLUMN_ACK, o.getAck());
            values.put(COLUMN_ACK_ETC, o.getAckEtc());
            values.put(COLUMN_REMOTE_PERSISTENCE_ID, o.getRemotePersistenceId());
            values.put(COLUMN_CALL_OPTIONS, o.getCallOptions());

            // insert the values into the tables, returns the ID for the row
            newRowId = db.insert(TABLE_XCTX, null, values);
            if (newRowId == -1) {
                Log.e(TAG, "insert, newRowId:" + newRowId + ", object:" + o);
//				throw new PersistenceException("Failed to persist caltxt: "
//						+ o.toString());
            }
        } else if (oo.getCName().equals("XMob")) {
            XMob o = (XMob) oo;
            // put the column values object
            values.put(COLUMN_NAME_CALLEE, o.getName());
            values.put(COLUMN_NUMBER_CALLEE, o.getNumber());
            values.put(COLUMN_NUMBER2_CALLEE, o.getNumber2());
            values.put(COLUMN_UNAME_CALLEE, o.getUsername());
            values.put(COLUMN_HEADLINE_CALLEE, o.getHeadline());
            values.put(COLUMN_STATUS_CALLEE, o.getStatus());
            values.put(COLUMN_TOD_CHG, o.getModified());

            // insert the values into the tables, returns the ID for the row
            newRowId = db.insert(TABLE_XMOB, null, values);
            if (newRowId == -1) {
                Log.e(TAG, "insert, newRowId:" + newRowId + ", object:" + o);
//				throw new PersistenceException("Failed to persist XMob: "
//						+ o.toString());
            }
        }
        closeDatabase(); // close the db then deal with the result of the query
        return newRowId;
    }

    public synchronized void updateContactName(String username, String name) /*throws PersistenceException */ {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
        values.put(COLUMN_NAME_CALLEE, name);

        // update the values into the tables, returns the number of rows updated (must be always 1)
        noOfRowsUpdated = db.update(TABLE_XMOB, values, COLUMN_UNAME_CALLEE + "=?",
                new String[]{username});
        if (noOfRowsUpdated != 1) {
            Log.e(TAG, "updateContactName, noOfRowsUpdated:" + noOfRowsUpdated + ", object:" + username);
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    public synchronized void updateContactHeadline(String username, String headline) /*throws PersistenceException */ {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        // put the column values object
        values.put(COLUMN_HEADLINE_CALLEE, headline);

        // update the values into the tables, returns the number of rows updated (must be always 1)
        noOfRowsUpdated = db.update(TABLE_XMOB, values, COLUMN_UNAME_CALLEE + "=?",
                new String[]{username});
        if (noOfRowsUpdated != 1) {
            Log.e(TAG, "updateContactHeadline, noOfRowsUpdated:" + noOfRowsUpdated + ", object:" + username);
        }
        closeDatabase(); // close the db then deal with the result of the query
    }

    public synchronized long update(IDTObject oo) /*throws PersistenceException */ {

        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        long noOfRowsUpdated = 0;

        if (oo.getCName().equals("XCtx")) {
            XCtx o = (XCtx) oo;
            // put the column values object
            values.put(COLUMN_NAME_CALLER, o.getNameCaller());
            values.put(COLUMN_NUMBER_CALLER, o.getUsernameCaller());
            if (o.getUsername2Caller() != null || o.getUsername2Caller().length() > 0)
                values.put(COLUMN_NUMBER2_CALLER, o.getUsername2Caller());
            values.put(COLUMN_OCCUPATION_CALLER, o.getOccupation());
            values.put(COLUMN_CONTEXT_CALLER, o.getCaltxt());
            values.put(COLUMN_CONTEXT_ETC, o.getCaltxtEtc());
            values.put(COLUMN_CITY_CALLER, o.getCity());
            values.put(COLUMN_NAME_CALLEE, o.getNameCallee());
            values.put(COLUMN_NUMBER_CALLEE, o.getNumberCallee());
            values.put(COLUMN_ICON_URL, o.getIcon());
            values.put(COLUMN_TOD_RECV, o.getRecvToD());
            values.put(COLUMN_TOD_START, o.getStartToD());
            values.put(COLUMN_TOD_END, o.getEndToD());
            values.put(COLUMN_CALL_STATE, o.getCallState());
            values.put(COLUMN_ACK, o.getAck());
            values.put(COLUMN_ACK_ETC, o.getAckEtc());
            values.put(COLUMN_REMOTE_PERSISTENCE_ID, o.getRemotePersistenceId());
            values.put(COLUMN_CALL_OPTIONS, o.getCallOptions());

            // insert the values into the tables, returns the ID for the row
            noOfRowsUpdated = db.update(TABLE_XCTX, values, _ID + "=?",
                    new String[]{String.valueOf(o.getPersistenceId())});
            if (noOfRowsUpdated != 1) {
                Log.e(TAG, "update, noOfRowsUpdated:" + noOfRowsUpdated + ", object:" + o);
//				throw new PersistenceException("Failed to update caltxt: "
//						+ o.toString());
            }
        } else if (oo.getCName().equals("XMob")) {
            XMob o = (XMob) oo;
            // put the column values object
            values.put(COLUMN_NAME_CALLEE, o.getName());
            values.put(COLUMN_NUMBER_CALLEE, o.getNumber());
            values.put(COLUMN_NUMBER2_CALLEE, o.getNumber2());
            values.put(COLUMN_UNAME_CALLEE, o.getUsername());
            values.put(COLUMN_HEADLINE_CALLEE, o.getHeadline());
            values.put(COLUMN_STATUS_CALLEE, o.getStatus());
            if (o.getModified() > 0) {//change only if new time given (update via MQTT update)
                values.put(COLUMN_TOD_CHG, o.getModified());
            }

            // update the values into the tables, returns the number of rows updated (must be always 1)
            noOfRowsUpdated = db.update(TABLE_XMOB, values, /*_ID*/COLUMN_UNAME_CALLEE + "=?",
                    new String[]{String.valueOf(o.getUsername())});
            if (noOfRowsUpdated != 1) {
                Log.e(TAG, "update, noOfRowsUpdated:" + noOfRowsUpdated + ", object:" + o);
//				throw new PersistenceException("Failed to update caltxt: "
//						+ o.toString());
            }
		/*} else if (oo.getCName().equals("XLoc")) {
			XLoc o = (XLoc) oo;
			// put the column values object
			values.put(COLUMN_LOCATION_NAME, o.getName());
			values.put(COLUMN_LOCATION_PROVIDER, o.getProvider());
			values.put(COLUMN_LOCATION_ACCURACY, o.getAccuracy());
			values.put(COLUMN_LOCATION_BEARING, o.getBearing());
			values.put(COLUMN_LOCATION_LAT, o.getLatitude());
			values.put(COLUMN_LOCATION_LON, o.getLongitude());
			values.put(COLUMN_LOCATION_PROVIDER, o.getProvider());
			values.put(COLUMN_LOCATION_SPEED, o.getSpeed());
			values.put(COLUMN_LOCATION_TIMESTAMP, o.getTimestamp());

			// update the values into the tables, returns the number of rows updated (must be always 1)
			noOfRowsUpdated = db.update(TABLE_XLOC, values, COLUMN_LOCATION_NAME + "=?",
					new String[] { String.valueOf(o.getName())});
			if (noOfRowsUpdated != 1) {
				Log.e(TAG, "update, noOfRowsUpdated:"+noOfRowsUpdated +", object:"+o);
//				throw new PersistenceException("Failed to update caltxt: "
//						+ o.toString());
			}*/
        } else if (oo.getCName().equals("XRul")) {
            XRul o = (XRul) oo;
            // put the column values object
            values.put(COLUMN_RULE_ACTION, o.getAction());
            values.put(COLUMN_RULE_ACTION_FOR, o.getActionFor());
            values.put(COLUMN_RULE_ACTION_VALUE, o.getActionValue());
            values.put(COLUMN_RULE_ACTION_REPEAT, o.getActionRepeat());
            values.put(COLUMN_RULE_ACTION_WHEN, o.getActionWhen());
            values.put(COLUMN_RULE_EVENT, o.getEvent());
            values.put(COLUMN_RULE_EVENT_FROM, o.getEventFrom());
            values.put(COLUMN_RULE_EVENT_VALUE, o.getEventValue());
            values.put(COLUMN_RULE_EVENT_TIMESTAMP, o.getEventTimestamp());
            values.put(COLUMN_RULE_ENABLED, o.isEnabled() == true ? 1 : 0);
            values.put(COLUMN_RULE_ACTION_ALWAYSASK, o.isAlwaysAsk() == true ? 1 : 0);

            // update the values into the tables, returns the number of rows updated (must be always 1)
            noOfRowsUpdated = db.update(TABLE_XRUL, values, _ID + "=?",
                    new String[]{Long.toString(o.getPersistenceId())});
            if (noOfRowsUpdated != 1) {
                Log.e(TAG, "update, noOfRowsUpdated:" + noOfRowsUpdated + ", object:" + o);
//				throw new PersistenceException("Failed to update caltxt: "
//						+ o.toString());
            }
        }
        closeDatabase(); // close the db then deal with the result of the query
        return noOfRowsUpdated;
    }

    /* merge this XCTX object and all XCTX objects in database within 'duration' section from
     * the 'time'
     */
    public synchronized long mergeWithLatestXCTX(XCtx ctx, long duration) /*throws PersistenceException */ {
        Log.i(TAG, "mergeWithLatestXCTX ctx " + ctx);

        SQLiteDatabase db = openDatabase();
        // columns to return
        String[] ctxColumns = {_ID, COLUMN_NAME_CALLER, COLUMN_NAME_CALLEE,
                COLUMN_NUMBER_CALLER, COLUMN_NUMBER2_CALLER, COLUMN_NUMBER_CALLEE,
                COLUMN_CONTEXT_CALLER, COLUMN_OCCUPATION_CALLER,
                COLUMN_CITY_CALLER, COLUMN_ICON_URL, COLUMN_CONTEXT_ETC, COLUMN_TOD_RECV, COLUMN_TOD_START,
                COLUMN_TOD_END, COLUMN_CALL_STATE, COLUMN_ACK, COLUMN_ACK_ETC, COLUMN_REMOTE_PERSISTENCE_ID, COLUMN_CALL_OPTIONS};
        long nearEnd = ctx.getRecvToD() - duration;
        long farEnd = ctx.getRecvToD() + duration;
        String ctxSelection = /*"MAX("+*/COLUMN_TOD_RECV + " BETWEEN " + nearEnd + " AND " + farEnd/*+")"*/;

		/* commented. put condition instead in the while loop below to match the caller
		with call entry found in database
		ctxSelection = COLUMN_NUMBER_CALLER + " IN ("+ctx.getUsernameCaller()
				+ (ctx.getUsername2Caller().length()==0?"":("," + ctx.getUsername2Caller()))+ ")"
				+ " AND " + ctxSelection;*/

        Cursor c = db.query(TABLE_XCTX, ctxColumns, ctxSelection, null, null, null,
                COLUMN_TOD_RECV + " ASC");
        long pid = 0;

        Log.v(TAG, "mergeWithLatestXCTX ctxSelection, " + ctxSelection + ", c.getCount()" + c.getCount());

        if (c.getCount() == 0) {
            Log.e(TAG, "mergeWithLatestXCTX ctxSelection, NO SELECTION!!!");
			/*ctx.setCallState(XCtx.IN_CALL_MISSED);
			 if(ctx.getCaltxt().length()>0) {
				 Notify.notify_caltxt_missed_call(ctx.getNameCaller(),
    				ctx.getCaltxt(), ctx.getRecvToD());
			 }
			pid = insert(ctx);*/
        } else {
            while (c.moveToNext()) {
                String caller_number = c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLER));
                caller_number = XMob.toFQMN(caller_number, Addressbook.getMyCountryCode());

                Log.v(TAG, "mergeWithLatestXCTX moveToNext, " + caller_number);

                // if caller does not match the found call entry, skip
                if (!caller_number.equals(ctx.getUsernameCaller()) && !caller_number.equals(ctx.getUsername2Caller()))
                    continue;

                pid = c.getInt(c.getColumnIndexOrThrow(_ID));
                XCtx lctx = (XCtx) get(pid, "XCtx");

                Log.v(TAG, "mergeWithLatestXCTX lctx, " + lctx);

                // merge remote pid & ack (19-FEB-17)
                if (lctx.getRemotePersistenceId() == -1) {
                    lctx.setRemotePersistenceId(ctx.getRemotePersistenceId());
                } else {
                    Log.e(TAG, "mergeWithLatestXCTX error, rpid already filled " + lctx.getRemotePersistenceId());
                    continue;
                }

                // ack must be updated if local ack null
                if (lctx.getAck().length() == 0) {
                    lctx.setAck(ctx.getAck());
                } else {
                    Log.e(TAG, "mergeWithLatestXCTX, ack already filled " + lctx.getAck());
                }

                // caltxt must be updated if local caltxt null
                if (lctx.getCaltxt().length() == 0) {
                    lctx.setCaltxt(ctx.getCaltxt());
                } else {
                    Log.e(TAG, "mergeWithLatestXCTX, caltxt already filled " + lctx.getCaltxt());
                }

                update(lctx);
                Log.v(TAG, "mergeWithLatestXCTX, update " + lctx + ", caller_number" + caller_number + ".");

//			ctx.setPersistenceId(pid);
//			ctx.setCaltxt(lctx.getCaltxt());
//			update(ctx);
            }
        }
        c.close();
        return pid;
    }

    /**
     * Recreates connection objects based upon information stored in the
     * database
     *
     * @param context Context for creating {@link ConnectionMqtt} objects
     * @return list of connections that have been restored
     * @throws PersistenceException if restoring connections fails, this is thrown
     */
    public synchronized ArrayList<IDTObject> restore(String classname) /*throws PersistenceException*/ {
        // how to sort the data being returned
//		String sort = COLUMN_TOD_RECV;
        SQLiteDatabase db = openDatabase();
        ArrayList<IDTObject> list = null;

        if (classname.equals("XCtx")) {
            // columns to return
            String[] ctxColumns = {_ID, COLUMN_NAME_CALLER, COLUMN_NAME_CALLEE,
                    COLUMN_NUMBER_CALLER, COLUMN_NUMBER2_CALLER, COLUMN_NUMBER_CALLEE,
                    COLUMN_CONTEXT_CALLER, COLUMN_OCCUPATION_CALLER,
                    COLUMN_CITY_CALLER, COLUMN_ICON_URL, COLUMN_CONTEXT_ETC, COLUMN_TOD_RECV, COLUMN_TOD_START,
                    COLUMN_TOD_END, COLUMN_CALL_STATE, COLUMN_ACK, COLUMN_ACK_ETC, COLUMN_REMOTE_PERSISTENCE_ID, COLUMN_CALL_OPTIONS};

            Cursor c = db.query(TABLE_XCTX, ctxColumns, null, null, null, null,
                    COLUMN_TOD_RECV + " DESC");
            list = new ArrayList<IDTObject>(c.getCount());
            XCtx obj = null;
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) {
                    break;
//					throw new PersistenceException(
//							"Failed restoring XCtx - count: " + c.getCount()
//									+ "loop iteration: " + i);
                }
                // get data from cursor
                obj = new XCtx();
                obj.setNameCaller(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_CALLER)));
                obj.setNameCallee(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_CALLEE)));
                obj.setUsernameCaller(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLER)));
                try {
                    obj.setUsername2Caller(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER2_CALLER)));
                } catch (IllegalArgumentException e) {
                }
                obj.setNumberCallee(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLEE)));
                obj.setCaltxt(c.getString(c.getColumnIndexOrThrow(COLUMN_CONTEXT_CALLER)));
                obj.setCaltxtEtc(c.getString(c.getColumnIndexOrThrow(COLUMN_CONTEXT_ETC)));
                obj.setOccupation(c.getString(c.getColumnIndexOrThrow(COLUMN_OCCUPATION_CALLER)));
                obj.setCity(c.getString(c.getColumnIndexOrThrow(COLUMN_CITY_CALLER)));
                obj.setIcon(c.getString(c.getColumnIndexOrThrow(COLUMN_ICON_URL)));
                obj.setRecvToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_RECV)));
                obj.setStartToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_START)));
                obj.setEndToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_END)));
                obj.setCallState(c.getShort(c.getColumnIndexOrThrow(COLUMN_CALL_STATE)));
                obj.setAck(c.getString(c.getColumnIndexOrThrow(COLUMN_ACK)));
                obj.setAckEtc(c.getString(c.getColumnIndexOrThrow(COLUMN_ACK_ETC)));
                obj.setRemotePersistenceId(c.getInt(c.getColumnIndexOrThrow(COLUMN_REMOTE_PERSISTENCE_ID)));
                obj.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
                obj.setCallOptions(c.getShort(c.getColumnIndexOrThrow(COLUMN_CALL_OPTIONS)));
                // store it in the list
                list.add(obj);
                // sometimes getProfile returns XMob object with zero length username and XCtx object get disturbed without caller username
                // therefore below update was tried to confirm this fact. This code is commented now 12112019
//                Log.i(TAG, "restore " + obj.toString());
//                if(obj.getUsernameCaller().length()==0) {
//                    obj.setUsernameCaller(Addressbook.getMyProfile().getUsername());
//                    update(obj);
//                    Log.i(TAG, "restore setUsernameCaller " + obj.toString());
//                }
            }
            c.close();
        } else if (classname.equals("XMob")) {
            // columns to return
            String[] mobColumns = {_ID, COLUMN_NAME_CALLEE,
                    COLUMN_NUMBER_CALLEE, COLUMN_NUMBER2_CALLEE, COLUMN_UNAME_CALLEE,
                    COLUMN_HEADLINE_CALLEE, COLUMN_STATUS_CALLEE, COLUMN_TOD_CHG};

            Cursor c = db.query(TABLE_XMOB, mobColumns, null, null, null, null,
                    COLUMN_NAME_CALLEE + " ASC");
            list = new ArrayList<IDTObject>(c.getCount());
            XMob obj = null;
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) {
                    break;
//					throw new PersistenceException(
//							"Failed restoring XMob - count: " + c.getCount()
//									+ "loop iteration: " + i);
                }
                // get data from cursor
                obj = new XMob();
                obj.setName(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_CALLEE)));
                obj.setNumber(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLEE)));
                obj.setNumber2(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER2_CALLEE)));
                obj.setUsername(c.getString(c.getColumnIndexOrThrow(COLUMN_UNAME_CALLEE)));
                obj.setHeadline(c.getString(c.getColumnIndexOrThrow(COLUMN_HEADLINE_CALLEE)));
                obj.setStatus(c.getShort(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE)));
//				if(c.getShort(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE))!=XMob.STATUS_UNREGISTERED)
//					obj.setStatus(XMob.STATUS_OFFLINE);//always set it from peer
                if (obj.isRegistered())
                    obj.setStatusOffline();//always set it from peer
                obj.setModified(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_CHG)));
                obj.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
                // store it in the list
                if (obj.isRegistered()/* || obj.isBlocked()*/) {
                    list.add(0, obj);
                } else {
                    list.add(obj);
                }
            }
            c.close();
        }

        // close the cursor now we are finished with it
        closeDatabase();
        return list;
    }

    /**
     * Deletes a connection from the database
     *
     * @param connection The connection to delete from the database
     */
    public synchronized void deleteXCTX(long persistId) {
        SQLiteDatabase db = openDatabase();

//		if(oo.getCName().equals("XCtx") && oo.getPersistenceId()!=-1) {
//			XCtx o = (XCtx)oo;
        db.delete(TABLE_XCTX, _ID + "=?",
                new String[]{ /*String.valueOf(o.getPersistenceId())*/Long.toString(persistId)});
//		}
        closeDatabase();
        // don't care if it failed, means it's not in the db therefore no need
        // to delete

    }

    public synchronized void deleteXMob(String uname) {
        SQLiteDatabase db = openDatabase();

        int i = db.delete(TABLE_XMOB, COLUMN_UNAME_CALLEE + "=?",
                new String[]{uname});

        closeDatabase();
        Log.i(TAG, "deleteXMob count," + i);
        // don't care if it failed, means it's not in the db therefore no need
        // to delete

    }

    public boolean hasXMOBByUsername(String username) {
        SQLiteDatabase db = openDatabase();
        return DatabaseUtils.longForQuery(db,
                "select count(*) from " + TABLE_XMOB + " where uname=? limit 1", new String[]{username}) > 0;
    }

    public ArrayList<IDTObject> getXCTXByRemotePID(long rpid, String caller) {
        SQLiteDatabase db = openDatabase();
        String sort = _ID;
        ArrayList<IDTObject> list = null;

        // columns to return
        String[] ctxColumns = {_ID, COLUMN_NAME_CALLER, COLUMN_NAME_CALLEE,
                COLUMN_NUMBER_CALLER, COLUMN_NUMBER2_CALLER, COLUMN_NUMBER_CALLEE,
                COLUMN_CONTEXT_CALLER, COLUMN_OCCUPATION_CALLER,
                COLUMN_CITY_CALLER, COLUMN_ICON_URL, COLUMN_CONTEXT_ETC, COLUMN_TOD_RECV, COLUMN_TOD_START,
                COLUMN_TOD_END, COLUMN_CALL_STATE, COLUMN_ACK, COLUMN_ACK_ETC, COLUMN_REMOTE_PERSISTENCE_ID, COLUMN_CALL_OPTIONS};
        String ctxSelection = COLUMN_REMOTE_PERSISTENCE_ID + "=" + rpid
                + " and " + COLUMN_NUMBER_CALLER + "=" + caller;

        Cursor c = db.query(TABLE_XCTX, ctxColumns, ctxSelection, null, null, null,
                sort + " DESC");
        list = new ArrayList<IDTObject>(c.getCount());
        XCtx obj = null;
        for (int i = 0; i < c.getCount(); i++) {
            if (!c.moveToNext()) { // move to the next item throw persistence exception, if it fails
                break;
//					throw new PersistenceException(
//							"Failed restoring XCtx - count: " + c.getCount()
//									+ "loop iteration: " + i);
            }
            // get data from cursor
            obj = new XCtx();
            obj.setNameCaller(c.getString(c
                    .getColumnIndexOrThrow(COLUMN_NAME_CALLER)));
            obj.setNameCallee(c.getString(c
                    .getColumnIndexOrThrow(COLUMN_NAME_CALLEE)));
            obj.setUsernameCaller(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLER)));
            try {
                obj.setUsername2Caller(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER2_CALLER)));
            } catch (IllegalArgumentException e) {
            }
            obj.setNumberCallee(c.getString(c
                    .getColumnIndexOrThrow(COLUMN_NUMBER_CALLEE)));
            obj.setCaltxt(c.getString(c.getColumnIndexOrThrow(COLUMN_CONTEXT_CALLER)));
            obj.setCaltxtEtc(c.getString(c.getColumnIndexOrThrow(COLUMN_CONTEXT_ETC)));
            obj.setOccupation(c.getString(c
                    .getColumnIndexOrThrow(COLUMN_OCCUPATION_CALLER)));
            obj.setCity(c.getString(c
                    .getColumnIndexOrThrow(COLUMN_CITY_CALLER)));
            obj.setIcon(c.getString(c
                    .getColumnIndexOrThrow(COLUMN_ICON_URL)));
            obj.setRecvToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_RECV)));
            obj.setStartToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_START)));
            obj.setEndToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_END)));
            obj.setCallState(c.getShort(c
                    .getColumnIndexOrThrow(COLUMN_CALL_STATE)));
            obj.setAck(c.getString(c.getColumnIndexOrThrow(COLUMN_ACK)));
            obj.setAckEtc(c.getString(c.getColumnIndexOrThrow(COLUMN_ACK_ETC)));
            obj.setRemotePersistenceId(c.getInt(c.getColumnIndexOrThrow(COLUMN_REMOTE_PERSISTENCE_ID)));
            obj.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
            obj.setCallOptions(c.getShort(c.getColumnIndexOrThrow(COLUMN_CALL_OPTIONS)));
            // store it in the list
            list.add(obj);
        }
        c.close();
        return list;
    }

    public XMob getXMOBByUsername(String username) {
        String sort = _ID;
        SQLiteDatabase db = openDatabase();

        // columns to return
        String[] mobColumns = {_ID, COLUMN_NAME_CALLEE,
                COLUMN_NUMBER_CALLEE, COLUMN_NUMBER2_CALLEE, COLUMN_UNAME_CALLEE,
                COLUMN_HEADLINE_CALLEE, COLUMN_STATUS_CALLEE, COLUMN_TOD_CHG};
        String mobSelection = COLUMN_UNAME_CALLEE + "=\"" + username + "\"";
        // OR "
//				+COLUMN_NUMBER2_CALLEE+"=\""+username+"\"";

//		Log.i(TAG, "getXMOBByUsername, mobSelection "+mobSelection);

        Cursor c = db.query(TABLE_XMOB, mobColumns, mobSelection, null, null, null,
                sort + " DESC");
        XMob obj = null;
        if (c.getCount() == 1) {

            c.moveToNext();
            // get data from cursor
            obj = new XMob();
            obj.setName(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_CALLEE)));
            obj.setNumber(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLEE))/*, CaltxtApp.getMyCountryCode()*/);
            obj.setNumber2(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER2_CALLEE))/*, CaltxtApp.getMyCountryCode()*/);
            obj.setUsername(c.getString(c.getColumnIndexOrThrow(COLUMN_UNAME_CALLEE)));
            obj.setHeadline(c.getString(c.getColumnIndexOrThrow(COLUMN_HEADLINE_CALLEE)));
            obj.setStatus(c.getShort(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE)));
//			if(c.getShort(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE))!=XMob.STATUS_UNREGISTERED)
//				obj.setStatus(XMob.STATUS_OFFLINE);//always set it from peer
            if (obj.isRegistered())
                obj.setStatusOffline();//always set it from peer
            obj.setModified(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_CHG)));
            obj.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
        }

        c.close();
        closeDatabase();

//		Log.i(TAG, "getXMOBByUsername, obj "+obj);
        return obj;
    }

    public synchronized IDTObject get(long persistId, String classname) /*throws PersistenceException */ {
        // how to sort the data being returned
        String sort = _ID;
        SQLiteDatabase db = openDatabase();
        ArrayList<IDTObject> list = null;

        if (classname.equals("XCtx")) {
            // columns to return
            String[] ctxColumns = {_ID, COLUMN_NAME_CALLER, COLUMN_NAME_CALLEE,
                    COLUMN_NUMBER_CALLER, COLUMN_NUMBER2_CALLER, COLUMN_NUMBER_CALLEE,
                    COLUMN_CONTEXT_CALLER, COLUMN_OCCUPATION_CALLER,
                    COLUMN_CITY_CALLER, COLUMN_ICON_URL, COLUMN_CONTEXT_ETC, COLUMN_TOD_RECV, COLUMN_TOD_START,
                    COLUMN_TOD_END, COLUMN_CALL_STATE, COLUMN_ACK, COLUMN_ACK_ETC, COLUMN_REMOTE_PERSISTENCE_ID, COLUMN_CALL_OPTIONS};
            String ctxSelection = _ID + "=" + persistId;

            Cursor c = db.query(TABLE_XCTX, ctxColumns, ctxSelection, null, null, null,
                    sort + " DESC");
            list = new ArrayList<IDTObject>(c.getCount());
            XCtx obj = null;
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) { // move to the next item throw persistence exception, if it fails
                    break;
//					throw new PersistenceException(
//							"Failed restoring XCtx - count: " + c.getCount()
//									+ "loop iteration: " + i);
                }
                // get data from cursor
                obj = new XCtx();
                obj.setNameCaller(c.getString(c
                        .getColumnIndexOrThrow(COLUMN_NAME_CALLER)));
                obj.setNameCallee(c.getString(c
                        .getColumnIndexOrThrow(COLUMN_NAME_CALLEE)));
                obj.setUsernameCaller(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLER)));
                try {
                    obj.setUsername2Caller(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER2_CALLER)));
                } catch (IllegalArgumentException e) {
                }
                obj.setNumberCallee(c.getString(c
                        .getColumnIndexOrThrow(COLUMN_NUMBER_CALLEE)));
                obj.setCaltxt(c.getString(c.getColumnIndexOrThrow(COLUMN_CONTEXT_CALLER)));
                obj.setCaltxtEtc(c.getString(c.getColumnIndexOrThrow(COLUMN_CONTEXT_ETC)));
                obj.setOccupation(c.getString(c
                        .getColumnIndexOrThrow(COLUMN_OCCUPATION_CALLER)));
                obj.setCity(c.getString(c
                        .getColumnIndexOrThrow(COLUMN_CITY_CALLER)));
                obj.setIcon(c.getString(c
                        .getColumnIndexOrThrow(COLUMN_ICON_URL)));
                obj.setRecvToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_RECV)));
                obj.setStartToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_START)));
                obj.setEndToD(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_END)));
                obj.setCallState(c.getShort(c
                        .getColumnIndexOrThrow(COLUMN_CALL_STATE)));
                obj.setAck(c.getString(c.getColumnIndexOrThrow(COLUMN_ACK)));
                obj.setAckEtc(c.getString(c.getColumnIndexOrThrow(COLUMN_ACK_ETC)));
                obj.setRemotePersistenceId(c.getInt(c.getColumnIndexOrThrow(COLUMN_REMOTE_PERSISTENCE_ID)));
                obj.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
                obj.setCallOptions(c.getShort(c.getColumnIndexOrThrow(COLUMN_CALL_OPTIONS)));
                // store it in the list
                list.add(obj);
            }
            c.close();
        } else if (classname.equals("XMob")) {
            // columns to return
            String[] mobColumns = {_ID, COLUMN_NAME_CALLEE,
                    COLUMN_NUMBER_CALLEE, COLUMN_NUMBER2_CALLEE, COLUMN_UNAME_CALLEE,
                    COLUMN_HEADLINE_CALLEE, COLUMN_STATUS_CALLEE, COLUMN_TOD_CHG};
            String mobSelection = _ID + "=" + persistId;

            Cursor c = db.query(TABLE_XMOB, mobColumns, mobSelection, null, null, null,
                    sort + " DESC");
            list = new ArrayList<IDTObject>(c.getCount());
            XMob obj = null;
            for (int i = 0; i < c.getCount(); i++) {
                if (!c.moveToNext()) { // move to the next item throw persistence exception, if it fails
                    break;
//					throw new PersistenceException(
//							"Failed restoring XMob - count: " + c.getCount()
//									+ "loop iteration: " + i);
                }
                // get data from cursor
                obj = new XMob();
                obj.setName(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_CALLEE)));
                obj.setNumber(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER_CALLEE))/*, CaltxtApp.getMyCountryCode()*/);
                obj.setNumber2(c.getString(c.getColumnIndexOrThrow(COLUMN_NUMBER2_CALLEE))/*, CaltxtApp.getMyCountryCode()*/);
                obj.setUsername(c.getString(c.getColumnIndexOrThrow(COLUMN_UNAME_CALLEE)));
                obj.setHeadline(c.getString(c.getColumnIndexOrThrow(COLUMN_HEADLINE_CALLEE)));
                obj.setStatus(c.getShort(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE)));
//				if(c.getShort(c.getColumnIndexOrThrow(COLUMN_STATUS_CALLEE))!=XMob.STATUS_UNREGISTERED)
//					obj.setStatus(XMob.STATUS_OFFLINE);//always set it from peer
                if (obj.isRegistered())
                    obj.setStatusOffline();//always set it from peer
                obj.setModified(c.getLong(c.getColumnIndexOrThrow(COLUMN_TOD_CHG)));
                obj.setPersistenceId(c.getInt(c.getColumnIndexOrThrow(_ID)));
                // store it in the list
                list.add(obj);
            }
            c.close();
        }

        // close the cursor now we are finished with it
        closeDatabase();
        if (list.size() > 0)
            return list.get(0);
        else
            return null;
    }
}
