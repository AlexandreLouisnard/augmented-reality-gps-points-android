package com.louisnard.augmentedreality.mock;

import com.louisnard.augmentedreality.model.objects.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class that provides {@link Point} mock data.<br>
 *
 * @author Alexandre Louisnard
 */
public class MockPoint {

    // Points
    public static final Point mZeroPoint = new Point("Zero", 0, 0, 0);
    public static final Point mZeroAntipodesPoint = new Point("Zero antipodes", 0, -180, 0);
    public static final Point mNorthPolePoint = new Point("North Pole", 90, 0, 0);
    public static final Point mSouthPolePoint = new Point("South Pole", -90, 0, 0);
    public static final Point mDevelopersHomePoint = new Point("Developer's home :-)", 45.1916626, 5.7385538, 250);
    public static final Point mDevelopersHomeBasementPoint = new Point("Developer's home basement :-)", 45.1916626, 5.7385538, 220);
    public static final Point mDevelopersHomeRooftopPoint = new Point("Developer's home rooftop :-)", 45.1916626, 5.7385538, 320);
    public static final Point mDevelopersHomeAntipodesPoint = new Point("Developer's home antipodes", -45.191663, -174.261446, 245);
    public static final Point mDevelopersWorkplacePoint = new Point("Developer's workplace", 45.1927864, 5.712356, 220);
    public static final Point mEastOfDevelopersWorkplacePoint = new Point("East of developer's workplace", 45.1927864, 5.722356, 220);
    public static final Point mBastillePoint = new Point("Fort de La Bastille", 45.1987, 5.7253, 476);
    public static final Point mRachaisPoint = new Point("Mont Rachais", 45.2417, 5.7436, 1046);
    public static final Point mJallaPoint = new Point("Mont Jalla", 45.2041, 5.7242, 635);
    public static final Point mMoucherottePoint = new Point("Moucherotte", 45.1472, 5.6382, 1901);
    public static final Point mMontBlancPoint = new Point("Mont Blanc", 45.8326, 6.8652, 4810);
    public static final Point mPopocateptlPoint = new Point("Popocatépetl", 19.0224, -98.6279, 5426);

    // Points list
    private static final List<Point> mPoints = new ArrayList<>(Arrays.asList(
            mZeroPoint,
            mZeroAntipodesPoint,
            mNorthPolePoint,
            mSouthPolePoint,
            mDevelopersHomePoint,
            mDevelopersHomeBasementPoint,
            mDevelopersHomeRooftopPoint,
            mDevelopersHomeAntipodesPoint,
            mDevelopersWorkplacePoint,
            mEastOfDevelopersWorkplacePoint,
            mBastillePoint,
            mRachaisPoint,
            mJallaPoint,
            mMoucherottePoint,
            mMontBlancPoint,
            mPopocateptlPoint
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
