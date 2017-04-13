package com.louisnard.augmentedreality.model.objects;

import android.database.Cursor;
import android.location.Location;

import com.louisnard.augmentedreality.model.database.DbContract;

/**
 * Class that holds a point and its coordinates.
 *
 * @author Alexandre Louisnard
 */

public class Point {

    // Point id
    private long mId;

    // Name
    private String mName;

    // Coordinates in degrees
    private Location mLocation;

    // Constants
    // The Earth mean radius in meters
    public static double EARTH_RADIUS = 6371000;

    // Static helper methods
    /**
     * Helper method that calculates the great-circle distance (in meters) over the earth’s surface associated to a latitude or longitude difference (in degrees).
     * @param degrees the latitude or longitude difference (in degrees).
     * @return the distance (in meters).
     */
    public static int degreesToMeters(double degrees) {
        return (int) Math.abs(degrees * 2 * Math.PI * EARTH_RADIUS / 360);
    }

    /**
     * Helper method that calculates the latitude or longitude difference (in degrees) associated to a great-circle distance (in meters) over the earth’s surface.
     * @param distance the distance (in meters).
     * @return the latitude or longitude difference (in degrees).
     */
    public static double metersToDegrees(int distance) {
        return distance * 360 / (2 * Math.PI * EARTH_RADIUS);
    }

    /**
     * Returns a valid latitude value in degrees comprised between -90° and 90° using modulo.
     * @param latitude the latitude value to correct.
     * @return the valid latitude value.
     */
    public static double getValidLatitude(double latitude) {
        double l = latitude % 360;
        if (l >= -90 && l <= 90) {
            return l;
        } else if (l > 90 && l < 180) {
            return 90 - l % 90;
        } else if ((l > 180 && l < 270) || (l < -180 && l > -270)) {
            return -l % 90;
        } else if (l > 270 && l < 360) {
            return -90 + l % 90;
        } else if (l < -90 && l > -180) {
            return -90 - l % 90;
        } else if (l < -270 && l > -360) {
            return 90 + l % 90;
        } else if (l == 180 || l == -180) {
            return 0;
        } else if (l == 270) {
            return -90;
        } else if (l == -270) {
            return 90;
        } else {
            return 0;
        }
    }

    /**
     * Returns a valid longitude value in degrees comprised between -180° and 180° using modulo.
     * @param longitude the longitude value to correct.
     * @return the valid longitude value.
     */
    public static double getValidLongitude(double longitude) {
        double l = longitude % 360;
        if (l >= -180 && l <= 180) {
            return l;
        } else if (l > 180 && l < 360) {
            return -180 + l % 180;
        } else if (l < -180 && l > -360) {
            return 180 + l % 180;
        } else {
            return 0;
        }
    }

    // Constructors
    /**
     * Constructs a new instance of {@link Point} from coordinates.
     * @param longitude the longitude in degrees.
     * @param longitude the longitude in degrees.
     * @param altitude the altitude in meters.
     * @param name the name.
     */
    public Point(String name, double latitude, double longitude, int altitude) {
        mName = name;
        mLocation = new Location("");
        mLocation.setLatitude(getValidLatitude(latitude));
        mLocation.setLongitude(getValidLongitude(longitude));
        mLocation.setAltitude(altitude);
    }

    /**
     * Constructs a new instance of {@link Point} from a {@link Location} object.
     * @param name the name.
     * @param location the {@link Location}.
     */
    public Point(String name, Location location) {
        mName = name;
        mLocation = location;
    }

    /**
     * Constructs a new instance of {@link Point} from a {@link Cursor}.
     * @param cursor the {@link Cursor} to read the data from.
     */
    public Point(Cursor cursor) {
        if (cursor != null) {
            mId = cursor.getLong((cursor.getColumnIndex(DbContract.PointsColumns._ID)));
            mName = cursor.getString(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_NAME));
            mLocation = new Location("");
            mLocation.setLatitude(cursor.getDouble(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_LATITUDE)));
            mLocation.setLongitude(cursor.getDouble(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_LONGITUDE)));
            mLocation.setAltitude(cursor.getInt(cursor.getColumnIndex(DbContract.PointsColumns.COLUMN_ELEVATION)));
        }
    }

    // Getters
    /**
     * Gets this {@link Point} id.
     * @return the id.
     */
    public long getId() {
        return mId;
    }

    /**
     * Gets this {@link Point} name.
     * @return the name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets this {@link Point} {@link Location}.
     * @return the {@link Location}.
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Gets this {@link Point} latitude in degrees.
     * @return the latitude in degrees, comprised between -90° and 90°.
     */
    public double getLatitude() {
        return mLocation.getLatitude();
    }

    /**
     * Gets this {@link Point} longitude in degrees.
     * @return the longitude in degrees, comprised between -180° and 180°.
     */
    public double getLongitude() {
        return mLocation.getLongitude();
    }

    /**
     * Gets this {@link Point} elevation above the sea level in meters.
     * @return the elevation in meters.
     */
    public int getAltitude() {
        return (int) mLocation.getAltitude();
    }
    
    // Setters
    /**
     * Sets this {@link Point} name.
     * @param name the name.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Sets this {@link Point} {@link Location}.
     * @param location the {@link Location}.
     */
    public void setLocation(Location location) {
        mLocation = location;
    }

    /**
     * Sets this {@link Point} latitude in degrees.
     * @param latitude the latitude in degrees.
     */
    public void setLatitude(double latitude) {
        mLocation.setLatitude(getValidLatitude(latitude));
    }

    /**
     * Sets this {@link Point} longitude in degrees.
     * @param longitude the latitude in degrees.
     */
    public void setLongitude(double longitude) {
        mLocation.setLongitude(getValidLongitude(longitude));
    }

    /**
     * Sets this {@link Point} altitude above the sea level in meters.
     * @param altitude the altitude in meters.
     */
    public void setElevation(int altitude) {
        mLocation.setAltitude(altitude);
    }

    // Calculations
    /**
     * Returns the approximate distance in meters between this {@link Point} and the given {@link Point}.
     * Distance is defined using the WGS84 ellipsoid.
     * @param point the destination {@link Point}.
     * @return the distance (in meters).
     */
    public int distanceTo(Point point) {
        return (int) mLocation.distanceTo(point.getLocation());
    }

    /**
     * Returns the approximate distance in meters between this {@link Point} and the given {@link Location}.
     * Distance is defined using the WGS84 ellipsoid.
     * @param location the destination {@link Location}.
     * @return the distance (in meters).
     */
    public int distanceTo(Location location) {
        return (int) mLocation.distanceTo(location);
    }

    /**
     * Returns the approximate azimuth in degrees East of true North when traveling along the shortest path from this {@link Point} to the given {@link Point}.
     * The shortest path is defined using the WGS84 ellipsoid. Locations that are (nearly) antipodal may produce meaningless results.
     * @param point the destination {@link Point}.
     * @return the azimuth (in degrees), taken clockwise from north, from 0° to 360°.
     */
    public float azimuthTo(Point point) {
        float azimuth = getLocation().bearingTo(point.getLocation());
        if (azimuth >= 0 && azimuth < 360) {
            return azimuth;
        } else if (azimuth < 0 && azimuth >= -180) {
            return 360 + azimuth;
        } else {
            return 0;
        }
    }
}
