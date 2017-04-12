package com.louisnard.augmentedreality;

import com.louisnard.augmentedreality.model.objects.Point;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Unit testing for the {@link Point} class.
 *
 * @author Alexandre Louisnard
 */

public class PointTest {

    // Error tolerance for calculations
    private final static float ERROR_TOLERANCE = 0.001f;
    
    // Static methods testing
    /**
     * Tests static methods {@link Point#degreesToMeters(double)} and {@link Point#metersToDegrees(double)}.
     */
    @Test
    public void test_degreesToMeters_metersToDegrees() {
        double angle = 0;
        double distance = 0;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 90;
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 4;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 180;
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 2;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 360;
        distance = 2 * Math.PI * Point.EARTH_RADIUS;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -90;
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 4;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -180;
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 2;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -360;
        distance = 2 * Math.PI * Point.EARTH_RADIUS;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 1;
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 360;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -1;
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 360;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);
    }

    @Test
    public void test_getValidLatitude() {
        assertEquals(12.123456789, Point.getValidLatitude(12.123456789), 0);
        assertEquals(0, Point.getValidLatitude(0), 0);
        assertEquals(90, Point.getValidLatitude(90), 0);
        assertEquals(-90, Point.getValidLatitude(-90), 0);
        assertEquals(0, Point.getValidLatitude(180), 0);
        assertEquals(0, Point.getValidLatitude(-180), 0);
        assertEquals(-90, Point.getValidLatitude(270), 0);
        assertEquals(90, Point.getValidLatitude(-270), 0);
        assertEquals(0, Point.getValidLatitude(360), 0);
        assertEquals(0, Point.getValidLatitude(-360), 0);
        assertEquals(1, Point.getValidLatitude(1), 0);
        assertEquals(89, Point.getValidLatitude(91), 0);
        assertEquals(1, Point.getValidLatitude(179), 0);
        assertEquals(-1, Point.getValidLatitude(181), 0);
        assertEquals(-89, Point.getValidLatitude(269), 0);
        assertEquals(-89, Point.getValidLatitude(271), 0);
        assertEquals(-1, Point.getValidLatitude(359), 0);
        assertEquals(1, Point.getValidLatitude(361), 0);
        assertEquals(-1, Point.getValidLatitude(-1), 0);
        assertEquals(-89, Point.getValidLatitude(-91), 0);
        assertEquals(-1, Point.getValidLatitude(-179), 0);
        assertEquals(1, Point.getValidLatitude(-181), 0);
        assertEquals(89, Point.getValidLatitude(-269), 0);
        assertEquals(89, Point.getValidLatitude(-271), 0);
        assertEquals(1, Point.getValidLatitude(-359), 0);
        assertEquals(-1, Point.getValidLatitude(-361), 0);
        assertEquals(0, Point.getValidLatitude(3600), 0);
        assertEquals(50.5, Point.getValidLatitude(3650.5), 0);
        assertEquals(-50, Point.getValidLatitude(3550), 0);
    }

    @Test
    public void test_getValidLongitude() {
        assertEquals(123.456789, Point.getValidLongitude(123.456789), 0);
        assertEquals(0, Point.getValidLongitude(0), 0);
        assertEquals(90, Point.getValidLongitude(90), 0);
        assertEquals(-90, Point.getValidLongitude(-90), 0);
        assertEquals(180, Point.getValidLongitude(180), 0);
        assertEquals(-180, Point.getValidLongitude(-180), 0);
        assertEquals(-90, Point.getValidLongitude(270), 0);
        assertEquals(90, Point.getValidLongitude(-270), 0);
        assertEquals(0, Point.getValidLongitude(360), 0);
        assertEquals(0, Point.getValidLongitude(-360), 0);
        assertEquals(-179, Point.getValidLongitude(181), 0);
        assertEquals(-1, Point.getValidLongitude(359), 0);
        assertEquals(1, Point.getValidLongitude(361), 0);
        assertEquals(179, Point.getValidLongitude(-181), 0);
        assertEquals(-1, Point.getValidLongitude(-361), 0);
        assertEquals(0, Point.getValidLongitude(3600), 0);
        assertEquals(180, Point.getValidLongitude(3780), 0);
        assertEquals(-179, Point.getValidLongitude(3781), 0);
        assertEquals(-50, Point.getValidLongitude(3550), 0);
    }
}
