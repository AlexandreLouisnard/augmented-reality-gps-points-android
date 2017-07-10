package com.louisnard.augmentedreality.model.database;

import android.provider.BaseColumns;

/**
 * Contract class that defines the tables for the database.<br>
 *
 * @author Alexandre Louisnard
 */
public final class ARDbContract {

    // Private constructor to prevent accidental instantiation
    private ARDbContract() {}

    /**
     * Columns for the points table.
     */
    public static final class PointsColumns implements BaseColumns {
        public static final String TABLE_NAME = "points";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ALTITUDE = "altitude";
    }
}
