package com.louisnard.augmentedreality;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Main activity showing a {@link android.support.v4.app.Fragment}.
 *
 * @author Alexandre Louisnard
 */

public class MainActivity extends AppCompatActivity {

    // Tag
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_main);

        // Set fragment
        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, new MainFragment());
            transaction.commit();
        }

    }
}
