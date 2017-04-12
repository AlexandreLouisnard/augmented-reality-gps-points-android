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
}
