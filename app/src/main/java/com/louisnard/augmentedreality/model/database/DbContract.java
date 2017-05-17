package com.louisnard.augmentedreality.model.database;

import android.provider.BaseColumns;

/**
 * Contract class that defines the tables for the database.
 *
 * @author Alexandre Louisnard
 */

public final class DbContract {

    // Private constructor to prevent accidental instantiation
    private DbContract() {}

    // Inner class that defines the table contents.
    public static class PointsColumns implements BaseColumns {
        public static final String TABLE_NAME = "points";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ALTITUDE = "altitude";
    }
}
