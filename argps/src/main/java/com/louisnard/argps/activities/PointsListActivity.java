package com.louisnard.argps.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.louisnard.argps.R;
import com.louisnard.argps.fragments.PointsListFragment;
import com.louisnard.argps.model.objects.Point;

/**
 * Points list {@link AppCompatActivity} showing the list of {@link Point}s in the database.<br>
 *
 * @author Alexandre Louisnard
 */

public class PointsListActivity extends AppCompatActivity {

    // Tag
    private static final String TAG = PointsListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_toolbar_simple_fragment);

        // Set toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set fragment
        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, new PointsListFragment());
            transaction.commit();
        }
    }
}
