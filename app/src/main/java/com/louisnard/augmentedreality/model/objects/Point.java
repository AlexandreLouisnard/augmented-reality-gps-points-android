package com.louisnard.augmentedreality.model.objects;

import android.database.Cursor;
import android.location.Location;
import android.util.SparseArray;

import com.louisnard.augmentedreality.model.database.DbContract;
import com.louisnard.augmentedreality.model.services.PointService;

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

    // Cached azimuths to other points
    private SparseArray<Float> mCachedAzimuths = new SparseArray<>();

    // Constructors
    /**
     * Constructs a new instance of {@link Point} from coordinates.
     * @param longitude the longitude in degrees.
     * @param altitude the altitude in meters.
     * @param name the name.
     */
    public Point(String name, double latitude, double longitude, int altitude) {
        mName = name;
        mLocation = new Location("");
        mLocation.setLatitude(PointService.getValidLatitude(latitude));
        mLocation.setLongitude(PointService.getValidLongitude(longitude));
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
        mLocation.setLatitude(PointService.getValidLatitude(latitude));
        mCachedAzimuths.clear();
    }

    /**
     * Sets this {@link Point} longitude in degrees.
     * @param longitude the latitude in degrees.
     */
    public void setLongitude(double longitude) {
        mLocation.setLongitude(PointService.getValidLongitude(longitude));
        mCachedAzimuths.clear();
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
     * Returns the approximate azimuth in degrees East of true North when traveling along the shortest path from this {@link Point} to the given {@link Point}.
     * The shortest path is defined using the WGS84 ellipsoid. Locations that are (nearly) antipodal may produce meaningless results.
     * @param point the destination {@link Point}.
     * @return the azimuth (in degrees), taken clockwise from north, from 0° to 360°.
     */
    public float azimuthTo(Point point) {
        // Return cached azimuth to this point, if any (to improve performance)
        if(point.getId() != 0 && mCachedAzimuths.get((int) point.getId()) != null) {
            return mCachedAzimuths.get((int) point.getId());
        // Or calculate the azimuth to this point
        } else {
            float azimuth = getLocation().bearingTo(point.getLocation());
            if (azimuth < 0 && azimuth >= -180) {
                azimuth = 360 + azimuth;
            }
            mCachedAzimuths.setValueAt((int) point.getId(), azimuth);
            return azimuth;
        }
    }
}
