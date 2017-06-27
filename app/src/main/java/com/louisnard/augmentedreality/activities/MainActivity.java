package com.louisnard.augmentedreality.activities;

import android.graphics.Camera;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.fragments.AugmentedRealityFragment;
import com.louisnard.augmentedreality.fragments.CameraPreviewFragment;

/**
 * Main activity showing a {@link android.support.v4.app.Fragment}.<br>
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
            transaction.replace(R.id.fragment, new CameraPreviewFragment());
            transaction.commit();
        }
    }
}
