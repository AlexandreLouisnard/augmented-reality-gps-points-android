package com.louisnard.augmentedreality.model.objects;

import android.database.Cursor;

import com.louisnard.augmentedreality.model.database.DbContract;

/**
 * Class that holds a point and its coordinates.
 *
 * @author Alexandre Louisnard
 */

public class Point {

    // Name
    private String mName;

    // Coordinates in degrees
    private float mLatitude;
    private float mLongitude;
    private int mElevation;

    // Constants
    // The Earth mean radius in meters
    private static double EARTH_RADIUS = 6371000;

    // Static helper methods
    /**
     * Helper method that calculates the great-circle distance (in meters) over the earth’s surface associated to a latitude or longitude difference (in degrees).
     * @param degrees the latitude or longitude difference (in degrees).
     * @return the distance (in meters).
     */
    public static long degreesToMeters(float degrees) {
        return (long) Math.abs(degrees % 360 * 2 * Math.PI * EARTH_RADIUS / 360);
    }

    /**
     * Helper method that calculates the latitude or longitude difference (in degrees) associated to a great-circle distance (in meters) over the earth’s surface.
     * @param distance the distance (in meters).
     * @return the latitude or longitude difference (in degrees).
     */
    public static float metersToDegrees(long distance) {
        return (float) ((distance * 360 / (2 * Math.PI * EARTH_RADIUS)) % 360);
    }

    // Constructors
    /**
     * Constructs a new instance of {@link Point}.
     * @param latitude the latitude in degrees.
     * @param longitude the longitude in degrees.
     * @param elevation the elevation in meters.
     * @param name the name.
     */
    public Point(String name, float latitude, float longitude, int elevation) {
        mName = name;
        mLatitude = latitude % 360;
        mLongitude = longitude % 360;
        mElevation = elevation;
    }

    /**
     * Constructs a new instance of {@link Point} from a {@link Cursor}.
     * @param cursor the {@link Cursor} to read the data from.
     */
    public Point(Cursor cursor) {
        if (cursor != null) {
            mName = cursor.getString(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_NAME));
            mLatitude = (cursor.getFloat(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_LATITUDE))) % 360;
            mLongitude = (cursor.getFloat(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_LONGITUDE))) % 360;
            mElevation = cursor.getInt(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_ELEVATION));
        }
    }

    // Getters
    public String getName() {
        return mName;
    }

    /**
     * Gets this {@link Point} latitude in degrees.
     * @return the latitude in degrees.
     */
    public float getLatitude() {
        return mLatitude;
    }

    /**
     * Gets this {@link Point} longitude in degrees.
     * @return the longitude in degrees.
     */
    public float getLongitude() {
        return mLongitude;
    }

    /**
     * Gets this {@link Point} elevation above the sea level in meters.
     * @return the elevation in meters.
     */
    public int getElevation() {
        return mElevation;
    }
    
    // Setters
    public void setName(String name) {
        mName = name;
    }

    /**
     * Sets this {@link Point} latitude in degrees.
     * @param latitude the latitude in degrees.
     */
    public void setLatitude(float latitude) {
        mLatitude = latitude % 360;
    }

    /**
     * Sets this {@link Point} longitude in degrees.
     * @param longitude the latitude in degrees.
     */
    public void setLongitude(float longitude) {
        mLongitude = longitude % 360;
    }

    /**
     * Sets this {@link Point} elevation above the sea level in meters.
     * @param elevation the elevation in meters.
     */
    public void setElevation(int elevation) {
        mElevation = elevation;
    }

    // Calculations
    /**
     * Calculates the great-circle distance (in meters) over the earth’s surface between this {@link Point} and the specified {@link Point} parameter.
     * That is, the shortest distance "as-the-crow-flies" between the points (ignoring any hills they fly over, of course!)
     * Uses the Haversine formula.
     * @param point the {@link Point} to calculate the distance with.
     * @param ignoreHeightDifference <b>true</b> to ignore the height difference in the calculation. <b>false</b> to take it into account.
     * @return the distance (in meters).
     */
    public long calculateDistanceWith(Point point, boolean ignoreHeightDifference) {
        float lat1 = getLatitude();
        float lon1 = getLongitude();
        int el1 = getElevation();
        float lat2 = point.getLatitude();
        float lon2 = point.getLongitude();
        int el2 = point.getElevation();

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double heightDifference = el1 - el2;

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        if (!ignoreHeightDifference) {
            distance = Math.sqrt(Math.pow(distance, 2) + Math.pow(heightDifference, 2));
        }

        return (long) distance;
    }

    /**
     * Calculates the azimuth (in degrees) between a meridian and the smooth curve connecting this {@link Point} and the specified {@link Point} parameter.
     * The azimuth is the angle at which a smooth curve crosses a meridian, taken clockwise from north. The North Pole has an azimuth of 0º from every other point on the globe.
     * @param point the {@link Point} to calculate the azimuth with.
     * @return the azimuth (in degrees), taken clockwise from north, from 0° to 360°.
     */
    public float calculateAzimuthOf(Point point) {
        float azimuth = 0;
        // TODO
        return azimuth;
    }
}