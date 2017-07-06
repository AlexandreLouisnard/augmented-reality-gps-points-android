package com.louisnard.augmentedreality.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.fragments.AugmentedRealityFragment;

/**
 * Augmented reality {@link AppCompatActivity} showing a {@link AugmentedRealityFragment}.<br>
 *
 * @author Alexandre Louisnard
 */
public class AugmentedRealityActivity extends AppCompatActivity {

    // Tag
    private static final String TAG = AugmentedRealityActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_simple_fragment);

        // Set fragment
        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, new AugmentedRealityFragment());
            transaction.commit();
        }
    }
}
