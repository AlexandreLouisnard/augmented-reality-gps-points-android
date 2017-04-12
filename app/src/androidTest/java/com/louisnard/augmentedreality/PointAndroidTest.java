package com.louisnard.augmentedreality;

import android.location.Location;

import com.louisnard.augmentedreality.model.objects.Point;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Android integration testing for the {@link Point} class.
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
        a.setElevation(1046);
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
     * Tests {@link Point#distanceTo(Point)} against values from the website http://www.movable-type.co.uk/scripts/latlong.html
     */
    @Test
    public void test_calculateDistanceWith() {
        Point a;
        Point b;
        double distance;

        a = new Point("Point 0", 0, 0, 0);
        b = new Point("Pôle Nord", 90, 0, 0);
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 4;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 245);
        b = new Point("Pôle Nord", 90, 0, 0);
        distance = 4982000;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Point 0", 0, 0, 0);
        b = new Point("Pôle Sud", -90, 0, 0);
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 4;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Point 0", 0, 0, 0);
        b = new Point("Point 0", 360, 360, 0);
        distance = 0;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * Point.EARTH_RADIUS);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Point 0", 0, 0, 0);
        b = new Point("Antipodes", 0, 180, 0);
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 2;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Point 0", 0, 0, 0);
        b = new Point("Antipodes", 0, -180, 0);
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 2;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 220);
        b = new Point("Point 0", 0, 0, 0);
        distance = 5057000;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 245);
        b = new Point("Pied de l'Developer's home", 45.1916626, 5.7385538, 220);
        distance = 0;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 245);
        b = new Point("Sous l'immeuble du développeur", 45.1916626, 5.7385538, -100);
        distance = 0;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 245);
        b = new Point("Mont Rachais", 45.2417, 5.7436, 1046);
        distance = 5578;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 245);
        b = new Point("Mont Blanc", 45.8326, 6.8652, 4810);
        distance = 113100;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 245);
        b = new Point("Developer's home", 45.1916626, 5.7385538, 245);
        distance = 0;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * Point.EARTH_RADIUS);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);

        a = new Point("Developer's home :-)", 45.1916626, 5.7385538, 245);
        b = new Point("Antipodes", -48.028459, 179.421415, 245);
        distance = 2 * Math.PI * Point.EARTH_RADIUS / 2;
        assertEquals(distance, a.distanceTo(b), ERROR_TOLERANCE * distance);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 0);
    }
}
