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
     * Tests static method {@link Point#degreesToMeters(float)}
     */
    @Test
    public void test_degreesToMeters_metersToDegrees() {
        float angle = 0;
        long distance = 0;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 90;
        distance = 10010000;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 180;
        distance = 20020000;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 3600;
        distance = 0;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -450;
        distance = 10010000;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -3780;
        distance = 20020000;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = -1;
        distance = 111200;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);

        angle = 362;
        distance = 222400;
        assertEquals(distance, Point.degreesToMeters(angle), ERROR_TOLERANCE * distance);
        assertEquals(angle % 360, Point.metersToDegrees(distance), ERROR_TOLERANCE * distance);
    }

    // Constructor, getters and setters testing
    /**
     * Tests {@link Point} constructors, getters and setters.
     */
    @Test
    public void test_constructor_getters_setters() {
        final Point a = new Point("Appartement du développeur :-)", 360 + 45.1916626f, 720 + 5.7385538f, 220);
        assertEquals("Appartement du développeur :-)", a.getName());
        assertEquals(45.1916626f, a.getLatitude(), ERROR_TOLERANCE * 45.1916626);
        assertEquals(5.7385538f, a.getLongitude(), ERROR_TOLERANCE * 5.7385538);
        assertEquals(220, a.getElevation());
        a.setName("Mont Rachais");
        a.setLatitude(45.2417f);
        a.setLongitude(5.7436f);
        a.setElevation(1046);
        assertEquals("Mont Rachais", a.getName());
        assertEquals(45.2417f, a.getLatitude(), 0);
        assertEquals(5.7436f, a.getLongitude(), 0);
        assertEquals(1046, a.getElevation());
        a.setName("Nulle part");
        a.setLatitude(-360.5f - 89.5f);
        a.setLongitude(3610.2f);
        a.setElevation(-50);
        assertEquals("Nulle part", a.getName());
        assertEquals(-90f, a.getLatitude(), 0);
        assertEquals(10.2f, a.getLongitude(), ERROR_TOLERANCE * 10.2f);
        assertEquals(-50, a.getElevation());
    }

    // Calculation methods testing
    /**
     * Tests {@link Point#calculateDistanceWith(Point, boolean)} against values from the website http://www.movable-type.co.uk/scripts/latlong.html
     */
    @Test
    public void test_calculateDistanceWith() {
        Point a;
        Point b;
        // Distance without taking into account height difference
        long flatDistance;
        // Distance taking into account height difference
        long distance;

        // Tests without height differences
        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Pôle Nord", 90f, 0f, 0);
        flatDistance = 10010000;
        distance = 10010000;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Pôle Sud", -90f, 0f, 0);
        flatDistance = 10010000;
        distance = 10010000;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Point 0", 360f, 360f, 0);
        flatDistance = 0;
        distance = 0;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Antipodes", 180f, 0f, 0);
        flatDistance = 20020000;
        distance = 20020000;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Antipodes", 0f, -180f, 0);
        flatDistance = 20020000;
        distance = 20020000;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 220);
        b = new Point("Point 0", 0f, 0f, 0);
        flatDistance = 5057000;
        distance = 5057000;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Pied de l'appartement du développeur", 45.1916626f, 5.7385538f, 220);
        flatDistance = 0;
        distance = 25;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Sous l'immeuble du développeur", 45.1916626f, 5.7385538f, -100);
        flatDistance = 0;
        distance = 345;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Pôle Nord", 90f, 0f, 0);
        flatDistance = 4982000;
        distance = 4982000;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Mont Rachais", 45.2417f, 5.7436f, 1046);
        flatDistance = 5578;
        distance = 5635;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Mont Blanc", 45.8326f, 6.8652f, 4810);
        flatDistance = 113100;
        distance = 113166;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Appartement du développeur", 45.1916626f + 360, 5.7385538f - 360, 245);
        flatDistance = 0;
        distance = 0;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), 1);
        assertEquals(distance, a.calculateDistanceWith(b, false), 1);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Antipodes", 45.1916626f - 180, 5.7385538f, 245);
        flatDistance = 20020000;
        distance = 20020000;
        assertEquals(flatDistance, a.calculateDistanceWith(b, true), flatDistance*ERROR_TOLERANCE);
        assertEquals(distance, a.calculateDistanceWith(b, false), distance*ERROR_TOLERANCE);
        assertEquals(a.calculateDistanceWith(b, true), b.calculateDistanceWith(a, true));
        assertEquals(a.calculateDistanceWith(b, false), b.calculateDistanceWith(a, false));
    }
}
