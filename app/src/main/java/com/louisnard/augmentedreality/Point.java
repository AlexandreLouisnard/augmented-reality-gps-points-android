package com.louisnard.augmentedreality;

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
}
