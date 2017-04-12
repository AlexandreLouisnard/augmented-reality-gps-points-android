package com.louisnard.augmentedreality.model.objects.mock;



import com.louisnard.augmentedreality.model.objects.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides mock data for debug use only.
 *
 * @author Alexandre Louisnard
 */
public class Mock {

    /**
     * Provides {@link Point} mock data for debug use only.
     */
    public static class MockPoint {
        private static List<Point> mPoints = new ArrayList<>(Arrays.asList(new Point("Fort de La Bastille", 45.1987f, 5.7253f, 476),
                new Point("Mont Rachais", 45.2417f, 5.7436f, 1046),
                new Point("Mont Jalla", 45.2041f, 5.7242f, 635),
                new Point("Developer's home :-)", 45.1916626f, 5.7385538f, 250),
                new Point("Moucherotte", 45.1472f, 5.6382f, 1901),
                new Point("Mont Blanc", 45.8326f, 6.8652f, 4810),
                new Point("Pôle Nord", 90, 0, 0),
                new Point("Pôle Sud", -90, 0, 0)
        ));

        /**
         * Returns a list of mock points.
         *
         * @return {@link List<Point>} the list of mock points.
         */
        public static List<Point> getPoints() {
            return mPoints;
        }
    }
}
