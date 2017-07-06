package com.louisnard.augmentedreality.model.services;

import android.util.Log;

import com.louisnard.augmentedreality.BuildConfig;
import com.louisnard.augmentedreality.fragments.AugmentedRealityFragment;
import com.louisnard.augmentedreality.model.objects.Point;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Helper class that performs operations related to {@link Point}.
 *
 * @author Alexandre Louisnard
 */

public class PointService {

    // Tag
    private static final String TAG = PointService.class.getSimpleName();

    // Constants
    // The Earth mean radius in meters
    public static final double EARTH_RADIUS = 6371000;

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
    /**
     * Calculates the relative azimuth of each {@link Point} from {@param points} as seen from {@param originPoint} (which is for instance the user location).<br>
     * Returns a {@link SortedMap <>} mapping:<br>
     * - As key: each point azimuth, as seen from {@param originPoint}.<br>
     * - As value: each {@link Point} from {@param points}.<br>
     * The {@link SortedMap<>} is sorted by key value (which means by point azimuth).
     * @param originPoint the {@link Point} from which to calculate the relative azimuths of the other points. For instance, the user location.
     * @param points the {@link List <Point>} to sort by relative azimuth.
     * @return the {@link SortedMap<>} of points sorted by azimuth as seen from {@param originPoint}, and using azimuth values as keys.
     */
    public static SortedMap<Float, Point> sortPointsByRelativeAzimuth(Point originPoint, List<Point> points) {
        SortedMap<Float, Point> pointsSortedMap = new TreeMap<>();
        for (Point p : points) {
            pointsSortedMap.put(originPoint.azimuthTo(p), p);
        }
        return pointsSortedMap;
    }

    /**
     * Parses a GPX file {@link InputStream} and returns the {@link List<Point>} that it contains.
     * @param inputStream the {@link InputStream} of the GPX file.
     * @return the {@link List<Point>} contained in the GPX file.
     */
    public static List<Point> parseGpx(InputStream inputStream) {
        // TODO: check that the GPX file is valid
        // TODO: parse GPX file
        List<Point> pointsList = new ArrayList<>();
        try {
            final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(inputStream, null);

            int eventType = xpp.getEventType();

            if (eventType != XmlPullParser.START_DOCUMENT) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Invalid GPX file");
                return null;
            }
            eventType = xpp.next();
            if (eventType != XmlPullParser.START_TAG || xpp.getName().compareTo("gpx") != 0) { // TODO: make case insensitive
                if (BuildConfig.DEBUG) Log.d(TAG, "Invalid GPX file");
                return null;
            }
            eventType = xpp.next();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d(TAG, "Start document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    Log.d(TAG, "Start tag "+xpp.getName());
                } else if(eventType == XmlPullParser.END_TAG) {
                    Log.d(TAG, "End tag "+xpp.getName());
                } else if(eventType == XmlPullParser.TEXT) {
                    Log.d(TAG, "Text "+xpp.getText());
                }
                eventType = xpp.next();
            }
            Log.d(TAG, "End document");
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return pointsList;
    }
}
