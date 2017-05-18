package com.louisnard.augmentedreality;

import android.location.Location;

import com.louisnard.augmentedreality.mock.MockPoint;
import com.louisnard.augmentedreality.model.objects.Point;
import com.louisnard.augmentedreality.model.services.PointService;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Android integration testing for the {@link Point} class.<br>
 *
 * @author Alexandre Louisnard
 */

public class PointAndroidTest {

    // Error tolerance for calculations
    private final static double ERROR_TOLERANCE = 0.03;

    // Constructor, getters and setters testing
    /**
     * Tests {@link Point} constructors, getters and setters.
     */
    @Test
    public void test_constructor_getters_setters() {
        Point a;
        Location location = new Location("");
        location.setLatitude(45.1916626);
        location.setLongitude(5.7385538);
        location.setAltitude(220);
        a = new Point("Developer's home :-)", location);
        assertEquals("Developer's home :-)", a.getName());
        assertEquals(45.1916626, a.getLatitude(), 0);
        assertEquals(5.7385538, a.getLongitude(), 0);
        assertEquals(220, a.getAltitude(), 0);
        a.setName("Mont Rachais");
        a.setLatitude(45.2417);
        a.setLongitude(5.7436);
        a.setAltitude(1046);
        assertEquals("Mont Rachais", a.getName());
        assertEquals(45.2417, a.getLatitude(), 0);
        assertEquals(5.7436, a.getLongitude(), 0);
        assertEquals(1046, a.getAltitude(), 0);
        a = new Point("Nulle part", -89, -179, 12);
        assertEquals("Nulle part", a.getName());
        assertEquals(-89, a.getLatitude(), 0);
        assertEquals(-179, a.getLongitude(), 0);
        assertEquals(12, a.getAltitude(), 0);
    }

    // Calculation methods testing
    /**
     * Tests {@link Point#getLocation()#distanceTo(Location)} calculation against values from the website http://www.movable-type.co.uk/scripts/latlong.html
     */
    @Test
    public void test_distanceTo() {
        Point a;
        Point b;
        double distance;

        a = MockPoint.mZeroPoint;
        b = MockPoint.mZeroPoint;
        distance = 0;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * PointService.EARTH_RADIUS);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mZeroPoint;
        b = MockPoint.mNorthPolePoint;
        distance = 2 * Math.PI * PointService.EARTH_RADIUS / 4;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mZeroPoint;
        b = MockPoint.mSouthPolePoint;
        distance = 2 * Math.PI * PointService.EARTH_RADIUS / 4;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);


        a = MockPoint.mZeroPoint;
        b = MockPoint.mZeroAntipodesPoint;
        distance = 2 * Math.PI * PointService.EARTH_RADIUS / 2;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mDevelopersHomePoint;
        distance = 0;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * PointService.EARTH_RADIUS);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mNorthPolePoint;
        distance = 4982000;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mDevelopersHomeAntipodesPoint;
        distance = 2 * Math.PI * PointService.EARTH_RADIUS / 2;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mZeroPoint;
        distance = 5057000;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mRachaisPoint;
        distance = 5578;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mMontBlancPoint;
        distance = 113100;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mNorthPolePoint;
        b = new Point("1° latitude away from North Pole", 89, 0, 0);
        distance = 111100; // 111 km
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mNorthPolePoint;
        b = new Point("100m away and 100m above from North Pole", 90 - PointService.metersToDegrees(100), 0, 100);
        distance = 100;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mZeroPoint;
        b = new Point("100 away and 100m above point zero", 0, PointService.metersToDegrees(100), 100);
        distance = 100;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = MockPoint.mZeroPoint;
        b = new Point("141.42m away from point zero", PointService.metersToDegrees(100), PointService.metersToDegrees(100), 100);
        distance = 141.42;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);
    }

    /**
     * Tests {@link Point#azimuthTo(Point)} calculation.
     */
    @Test
    public void test_azimuthTo() {
        assertEquals(0, MockPoint.mZeroPoint.azimuthTo(MockPoint.mNorthPolePoint) % 360, ERROR_TOLERANCE * 360);
        assertEquals(0, MockPoint.mDevelopersHomePoint.azimuthTo(MockPoint.mNorthPolePoint) % 360, ERROR_TOLERANCE * 360);
        assertEquals(0, MockPoint.mDevelopersHomeAntipodesPoint.azimuthTo(MockPoint.mNorthPolePoint) % 360, ERROR_TOLERANCE * 360);
        assertEquals(0, MockPoint.mRachaisPoint.azimuthTo(MockPoint.mNorthPolePoint) % 360, ERROR_TOLERANCE * 360);
        assertEquals(0, MockPoint.mPopocateptlPoint.azimuthTo(MockPoint.mNorthPolePoint) % 360, ERROR_TOLERANCE * 360);
        assertEquals(180, MockPoint.mZeroPoint.azimuthTo(MockPoint.mSouthPolePoint), ERROR_TOLERANCE * 360);
        assertEquals(180, MockPoint.mDevelopersHomePoint.azimuthTo(MockPoint.mSouthPolePoint), ERROR_TOLERANCE * 360);
        assertEquals(180, MockPoint.mDevelopersHomeAntipodesPoint.azimuthTo(MockPoint.mSouthPolePoint), ERROR_TOLERANCE * 360);
        assertEquals(180, MockPoint.mRachaisPoint.azimuthTo(MockPoint.mSouthPolePoint), ERROR_TOLERANCE * 360);
        assertEquals(180, MockPoint.mPopocateptlPoint.azimuthTo(MockPoint.mSouthPolePoint), ERROR_TOLERANCE * 360);
        assertEquals(90, MockPoint.mZeroPoint.azimuthTo(new Point("East", 0, 10, 0)), ERROR_TOLERANCE * 360);
        assertEquals(90, MockPoint.mZeroPoint.azimuthTo(new Point("East", 0, 10, 200)), ERROR_TOLERANCE * 360);
        assertEquals(90, MockPoint.mZeroPoint.azimuthTo(new Point("East", 0, 10, 200)), ERROR_TOLERANCE * 360);
        assertEquals(270, MockPoint.mZeroPoint.azimuthTo(new Point("West", 0, -10, 0)), ERROR_TOLERANCE * 360);
        assertEquals(270, MockPoint.mZeroPoint.azimuthTo(new Point("West", 0, -10, -200)), ERROR_TOLERANCE * 360);
        assertEquals(0, MockPoint.mZeroPoint.azimuthTo(new Point("North", 50, 0, 0)) % 360, ERROR_TOLERANCE * 360);
        assertEquals(180, MockPoint.mZeroPoint.azimuthTo(new Point("South", -60, 0, 6507)), ERROR_TOLERANCE * 360);
    }

    /**
     * Tests {@link Point#verticalAngleTo(Point)} calculation.
     */
    @Test
    public void test_verticalAngleTo() {
        Point a;
        Point b;
        float angle;

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mDevelopersHomePoint;
        angle = 90f;
        assertEquals(angle, a.verticalAngleTo(b), ERROR_TOLERANCE * 360);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mDevelopersHomeRooftopPoint;
        angle = 90f;
        assertEquals(angle, a.verticalAngleTo(b), ERROR_TOLERANCE * 360);
        assertEquals(-angle, b.verticalAngleTo(a), ERROR_TOLERANCE * 360);

        a = MockPoint.mDevelopersHomePoint;
        b = MockPoint.mDevelopersHomeBasementPoint;
        angle = -90f;
        assertEquals(angle, a.verticalAngleTo(b), ERROR_TOLERANCE * 360);
        assertEquals(-angle, b.verticalAngleTo(a), ERROR_TOLERANCE * 360);

        a = MockPoint.mNorthPolePoint;
        b = MockPoint.mSouthPolePoint;
        angle = 0f;
        assertEquals(angle, a.verticalAngleTo(b), ERROR_TOLERANCE * 360);
        assertEquals(-angle, b.verticalAngleTo(a), ERROR_TOLERANCE * 360);

        a = MockPoint.mZeroPoint;
        b = new Point("100m away and 100m above from point zero", PointService.metersToDegrees(100), 0, 100);
        angle = 45f;
        assertEquals(angle, a.verticalAngleTo(b), ERROR_TOLERANCE * 360);
        assertEquals(-angle, b.verticalAngleTo(a), ERROR_TOLERANCE * 360);

        a = MockPoint.mZeroPoint;
        b = new Point("141.42m away and 141m above from point zero", PointService.metersToDegrees(100), PointService.metersToDegrees(100), 141);
        angle = 45f;
        assertEquals(angle, a.verticalAngleTo(b), ERROR_TOLERANCE * 360);
        assertEquals(-angle, b.verticalAngleTo(a), ERROR_TOLERANCE * 360);
    }
}
