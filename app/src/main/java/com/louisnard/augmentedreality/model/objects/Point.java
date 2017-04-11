package com.louisnard.augmentedreality.model.objects;

import android.database.Cursor;

import com.louisnard.augmentedreality.model.database.DbContract;

/**
 * Class that holds a point.
 *
 * @author Alexandre Louisnard
 */

public class Point {

    // Name
    private String mName;

    // Coordinates
    private float mLatitude;
    private float mLongitude;
    private float mElevation;

    /**
     * Constructs a new instance of {@link Point}.
     * @param latitude the latitude.
     * @param longitude the longitude.
     * @param elevation the elevation.
     * @param name the name.
     */
    public Point(String name, float latitude, float longitude, float elevation) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mElevation = elevation;
    }

    /**
     * Constructs a new instance of {@link Point} from a {@link Cursor}.
     * @param cursor the {@link Cursor} to read the data from.
     */
    public Point(Cursor cursor) {
        if (cursor != null) {
            mName = cursor.getString(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_NAME));
            mLatitude = cursor.getFloat(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_LATITUDE));
            mLongitude = cursor.getFloat(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_LONGITUDE));
            mElevation = cursor.getFloat(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_ELEVATION));
        }
    }

    // Getters
    public String getName() {
        return mName;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public float getElevation() {
        return mElevation;
    }
    
    // Setters
    public void setName(String name) {
        mName = name;
    }

    public void setLatitude(float latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(float longitude) {
        mLongitude = longitude;
    }

    public void setElevation(float elevation) {
        mElevation = elevation;
    }

    // Distance calculation
    public long getHorizontalDistanceWith(Point point) {
        long distance = 0;
        // TODO
        return distance;
    }

    public long get3DDistanceWith(Point point) {
        long distance = 0;
        // TODO
        return distance;
    }

    public float getAzimuthOf(Point point) {
        float azimuth = 0;
        // TODO
        return azimuth;
    }
}
