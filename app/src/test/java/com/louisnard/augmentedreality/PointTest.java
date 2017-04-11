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
    /**
     * Tests {@link Point#getDistanceWith(Point, boolean)} against values from the website http://www.movable-type.co.uk/scripts/latlong.html
     */
    @Test
    public void testGetDistanceWith() {
        Point a;
        Point b;
        // Distance without taking into account height difference
        long flatDistance;
        // Distance taking into account height difference
        long distance;
        // Error tolerance
        final float precision = 0.001f;

        // Tests without height differences
        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Pôle Nord", 90f, 0f, 0);
        flatDistance = 10010000;
        distance = 10010000;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Pôle Sud", -90f, 0f, 0);
        flatDistance = 10010000;
        distance = 10010000;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Point 0", 360f, 360f, 0);
        flatDistance = 0;
        distance = 0;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Antipodes", 180f, 0f, 0);
        flatDistance = 20020000;
        distance = 20020000;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Point 0", 0f, 0f, 0);
        b = new Point("Antipodes", 0f, -180f, 0);
        flatDistance = 20020000;
        distance = 20020000;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 220);
        b = new Point("Point 0", 0f, 0f, 0);
        flatDistance = 5057000;
        distance = 5057000;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Pied de l'appartement du développeur", 45.1916626f, 5.7385538f, 220);
        flatDistance = 0;
        distance = 25;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Sous l'immeuble du développeur", 45.1916626f, 5.7385538f, -100);
        flatDistance = 0;
        distance = 345;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Pôle Nord", 90f, 0f, 0);
        flatDistance = 4982000;
        distance = 4982000;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Mont Rachais", 45.2417f, 5.7436f, 1046);
        flatDistance = 5578;
        distance = 5635;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));

        a = new Point("Appartement du développeur :-)", 45.1916626f, 5.7385538f, 245);
        b = new Point("Mont Blanc", 45.8326f, 6.8652f, 4810);
        flatDistance = 113100;
        distance = 113166;
        assertEquals(flatDistance, a.getDistanceWith(b, true), flatDistance*precision);
        assertEquals(distance, a.getDistanceWith(b, false), distance*precision);
        assertEquals(a.getDistanceWith(b, true), b.getDistanceWith(a, true));
        assertEquals(a.getDistanceWith(b, false), b.getDistanceWith(a, false));
    }
}
