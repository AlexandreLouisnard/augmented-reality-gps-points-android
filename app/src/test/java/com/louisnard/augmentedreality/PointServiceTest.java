package com.louisnard.augmentedreality;

import com.louisnard.augmentedreality.model.services.PointService;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Unit testing for the {@link PointService} class.
 *
 * @author Alexandre Louisnard
 */

public class PointServiceTest {

    // Error tolerance for calculations
    private final static double ERROR_TOLERANCE = 0.001;

    // Static methods testing
    /**
     * Tests static methods {@link PointService#degreesToMeters(double)} and {@link PointService#metersToDegrees(int)}.
     */
    @Test
    public void test_degreesToMeters_metersToDegrees() {
        float angle = 0;
        int distance = 0;
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        // 1° of latitude or longitude is worth around 111 km
        angle = 1;
        distance = 111100;
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -1;
        distance = 111100;
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 2;
        distance = 222200;
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 90;
        distance = (int) (2 * Math.PI * PointService.EARTH_RADIUS / 4);
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 180;
        distance = (int) (2 * Math.PI * PointService.EARTH_RADIUS / 2);
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 360;
        distance = (int) (2 * Math.PI * PointService.EARTH_RADIUS);
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -90;
        distance = (int) (2 * Math.PI * PointService.EARTH_RADIUS / 4);
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -180;
        distance = (int) (2 * Math.PI * PointService.EARTH_RADIUS / 2);
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -360;
        distance = (int) (2 * Math.PI * PointService.EARTH_RADIUS);
        assertEquals(distance, PointService.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle, PointService.metersToDegrees(distance), ERROR_TOLERANCE * distance);
    }

    /**
     * Tests static methods {@link PointService#getValidLatitude(double)}.
     */
    @Test
    public void test_getValidLatitude() {
        assertEquals(12.123456789, PointService.getValidLatitude(12.123456789), 0);
        assertEquals(0, PointService.getValidLatitude(0), 0);
        assertEquals(90, PointService.getValidLatitude(90), 0);
        assertEquals(-90, PointService.getValidLatitude(-90), 0);
        assertEquals(0, PointService.getValidLatitude(180), 0);
        assertEquals(0, PointService.getValidLatitude(-180), 0);
        assertEquals(-90, PointService.getValidLatitude(270), 0);
        assertEquals(90, PointService.getValidLatitude(-270), 0);
        assertEquals(0, PointService.getValidLatitude(360), 0);
        assertEquals(0, PointService.getValidLatitude(-360), 0);
        assertEquals(1, PointService.getValidLatitude(1), 0);
        assertEquals(89, PointService.getValidLatitude(91), 0);
        assertEquals(1, PointService.getValidLatitude(179), 0);
        assertEquals(-1, PointService.getValidLatitude(181), 0);
        assertEquals(-89, PointService.getValidLatitude(269), 0);
        assertEquals(-89, PointService.getValidLatitude(271), 0);
        assertEquals(-1, PointService.getValidLatitude(359), 0);
        assertEquals(1, PointService.getValidLatitude(361), 0);
        assertEquals(-1, PointService.getValidLatitude(-1), 0);
        assertEquals(-89, PointService.getValidLatitude(-91), 0);
        assertEquals(-1, PointService.getValidLatitude(-179), 0);
        assertEquals(1, PointService.getValidLatitude(-181), 0);
        assertEquals(89, PointService.getValidLatitude(-269), 0);
        assertEquals(89, PointService.getValidLatitude(-271), 0);
        assertEquals(1, PointService.getValidLatitude(-359), 0);
        assertEquals(-1, PointService.getValidLatitude(-361), 0);
        assertEquals(0, PointService.getValidLatitude(3600), 0);
        assertEquals(50.5, PointService.getValidLatitude(3650.5), 0);
        assertEquals(-50, PointService.getValidLatitude(3550), 0);
    }

    /**
     * Tests static methods {@link PointService#getValidLongitude(double)}.
     */
    @Test
    public void test_getValidLongitude() {
        assertEquals(123.456789, PointService.getValidLongitude(123.456789), 0);
        assertEquals(0, PointService.getValidLongitude(0), 0);
        assertEquals(90, PointService.getValidLongitude(90), 0);
        assertEquals(-90, PointService.getValidLongitude(-90), 0);
        assertEquals(180, PointService.getValidLongitude(180), 0);
        assertEquals(-180, PointService.getValidLongitude(-180), 0);
        assertEquals(-90, PointService.getValidLongitude(270), 0);
        assertEquals(90, PointService.getValidLongitude(-270), 0);
        assertEquals(0, PointService.getValidLongitude(360), 0);
        assertEquals(0, PointService.getValidLongitude(-360), 0);
        assertEquals(-179, PointService.getValidLongitude(181), 0);
        assertEquals(-1, PointService.getValidLongitude(359), 0);
        assertEquals(1, PointService.getValidLongitude(361), 0);
        assertEquals(179, PointService.getValidLongitude(-181), 0);
        assertEquals(-1, PointService.getValidLongitude(-361), 0);
        assertEquals(0, PointService.getValidLongitude(3600), 0);
        assertEquals(180, PointService.getValidLongitude(3780), 0);
        assertEquals(-179, PointService.getValidLongitude(3781), 0);
        assertEquals(-50, PointService.getValidLongitude(3550), 0);
    }
}
