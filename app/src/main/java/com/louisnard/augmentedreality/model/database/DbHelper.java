package com.louisnard.augmentedreality.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.louisnard.augmentedreality.model.objects.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing the {@link SQLiteDatabase}.
 *
 * @author Alexandre Louisnard
 */

public class DbHelper extends SQLiteOpenHelper {

    // Database information
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton pattern
    private static DbHelper sInstance;

    // SQL requests
    private static final String SQL_CREATE_TABLE_POINTS = "CREATE TABLE " + DbContract.PointsColumns.TABLE_NAME
            + " (" + DbContract.PointsColumns._ID + " INTEGER PRIMARY KEY,"
            + DbContract.PointsColumns.COLUMN_NAME + " TEXT,"
            + DbContract.PointsColumns.COLUMN_LATITUDE + " REAL,"
            + DbContract.PointsColumns.COLUMN_LONGITUDE + " REAL,"
            + DbContract.PointsColumns.COLUMN_ELEVATION + " REAL)";

    /**
     * Constructs a new instance of {@link DbHelper}.
     * Private constructor to prevent accidental instantiation.
     * @param context the {@link Context} to use to open or create the database.
     */
    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Initializes if necessary and returns the singleton instance of {@link DbHelper}.
     * @param applicationContext the application context to avoid leaking an activity context.
     * @return the singleton instance of {@link DbHelper}.
     */
    public static synchronized DbHelper getInstance(Context applicationContext) {
        if (sInstance == null) {
            sInstance = new DbHelper(applicationContext.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_POINTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do for the moment
    }

    /**
     * Static method that returns the database name.
     * @return the database name.
     */
    public static String getDbName() {
        return DATABASE_NAME;
    }

    /**
     * Adds the specified {@link Point} to the {@link SQLiteDatabase}.
     * @param point
     * @return
     */
    public long addPoint(Point point) {
        final ContentValues values = new ContentValues();
        values.put(DbContract.PointsColumns.COLUMN_NAME, point.getName());
        values.put(DbContract.PointsColumns.COLUMN_LATITUDE, point.getLatitude());
        values.put(DbContract.PointsColumns.COLUMN_LONGITUDE, point.getLongitude());
        values.put(DbContract.PointsColumns.COLUMN_ELEVATION, point.getElevation());
        final long result = getWritableDatabase().insert(DbContract.PointsColumns.TABLE_NAME, null, values);
        getWritableDatabase().close();
        return result;
    }

    public List<Point> getPointsAround(Point point, long distance) {
        List<Point> points = new ArrayList<Point>();
        // TODO
        return points;
    }
}