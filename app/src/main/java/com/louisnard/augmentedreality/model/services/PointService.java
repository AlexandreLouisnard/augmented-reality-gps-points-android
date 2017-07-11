package com.louisnard.augmentedreality.model.services;

import android.os.AsyncTask;
import android.util.Log;

import com.louisnard.augmentedreality.BuildConfig;
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

    // Singleton pattern
    private static PointService sInstance;

    // Constants
    // The Earth mean radius in meters
    public static final double EARTH_RADIUS = 6371000;

    public interface GpxParserListener {
        void onGpxParsed(List<Point> parsedPoints);
    }

    /**
     * Initializes if necessary and returns the singleton instance of {@link PointService}.
     * @return the singleton instance of {@link PointService}.
     */
    public static synchronized PointService getInstance() {
        if (sInstance == null) {
            sInstance = new PointService();
        }
        return sInstance;
    }

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
     * @param listener the {@link GpxParserListener} to notify when parsing has completed.
     * @return the {@link List<Point>} contained in the GPX file or <b>null</b> if the file is invalid or empty.
     */
    public void parseGpxAsynchronously(InputStream inputStream, GpxParserListener listener) {
        final GpxParser gpxParser = new GpxParser(inputStream, listener);
        gpxParser.execute();
    }

    /**
     * GPX parser.
     */
    private class GpxParser extends AsyncTask<Void, Void, Void> {

        // GPX input stram
        private InputStream mInputStream;
        // Parsed points list
        private List<Point> mPointsList;
        // Listener
        private GpxParserListener mListener;

        /**
         * Parses a GPX file {@link InputStream} and generates the {@link List<Point>} that it contains.
         * @param inputStream the {@link InputStream} of the GPX file.
         * @param listener the {@link GpxParserListener} to notify when parsing has completed.
         */
        public GpxParser(InputStream inputStream, GpxParserListener listener) {
            mInputStream = inputStream;
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Initialize XmlPullParser
                final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                final XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(mInputStream, null);

                // Ensure this is a GPX file
                int eventType = xpp.getEventType();
                if (eventType != XmlPullParser.START_DOCUMENT) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Invalid GPX file");
                    return null;
                }
                eventType = xpp.next();
                if (eventType != XmlPullParser.START_TAG || !xpp.getName().equalsIgnoreCase("gpx")) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "Invalid GPX file");
                    return null;
                }

                // Parse points
                mPointsList = new ArrayList<>();
                eventType = xpp.next();
                Point temporaryPoint = null;
                String currentTag = null;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG
                            && xpp.getName().equalsIgnoreCase("wpt")) {
                        // <wpt>: create a new Point
                        temporaryPoint = new Point();
                        temporaryPoint.setLatitude(Double.parseDouble(xpp.getAttributeValue(null, "lat")));
                        temporaryPoint.setLongitude(Double.parseDouble(xpp.getAttributeValue(null, "lon")));
                    } else if (eventType == XmlPullParser.END_TAG
                            && xpp.getName().equalsIgnoreCase("wpt")) {
                        // </wpt>: add the new Point to the list
                        if (temporaryPoint != null && temporaryPoint.isValid()) {
                            mPointsList.add(temporaryPoint);
                        }
                        temporaryPoint = null;
                    } else if (eventType == XmlPullParser.START_TAG
                            && (xpp.getName().equalsIgnoreCase("name") || xpp.getName().equalsIgnoreCase("ele") || xpp.getName().equalsIgnoreCase("desc"))) {
                        // <name> or <ele> or <desc>
                        currentTag = xpp.getName();
                    } else if (eventType == XmlPullParser.END_TAG
                            && (xpp.getName().equalsIgnoreCase("name") || xpp.getName().equalsIgnoreCase("ele") || xpp.getName().equalsIgnoreCase("desc"))) {
                        // </name> or </ele> or </desc>
                        currentTag = null;
                    } else if (eventType == XmlPullParser.TEXT) {
                        // Text node
                        if (currentTag != null && temporaryPoint != null) {
                            if (currentTag.equals("name")) {
                                temporaryPoint.setName(xpp.getText());
                            } else if (currentTag.equals("ele")) {
                                temporaryPoint.setAltitude((int) Double.parseDouble(xpp.getText()));
                            } else if (currentTag.equals("desc")) {
                                temporaryPoint.setDescription(xpp.getText());
                            }
                        }
                    }
                    eventType = xpp.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mListener.onGpxParsed(mPointsList);
        }
    }
}
