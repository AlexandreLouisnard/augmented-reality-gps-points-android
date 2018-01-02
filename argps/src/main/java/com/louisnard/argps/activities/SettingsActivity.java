package com.louisnard.argps.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.louisnard.argps.R;
import com.louisnard.argps.fragments.SettingsFragment;

/**
 * Augmented reality {@link AppCompatActivity} showing a {@link SettingsFragment}.<br>
 *
 * @author Alexandre Louisnard
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * On back button pressed listener interface.
     */
    public interface BackButtonListener {
        /**
         * Callback method invoked when the back button is pressed.
         * @return <b>false</b> to prevent back navigation. <b>true</b> to allow it.
         */
        public boolean onBackPressed();
    }

    // Tag
    private static final String TAG = SettingsActivity.class.getSimpleName();

    private BackButtonListener mBackButtonListener;

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
            transaction.replace(R.id.fragment, new SettingsFragment());
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (mBackButtonListener != null && !mBackButtonListener.onBackPressed()) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Sets the {@link BackButtonListener} for this activity.
     * @param listener the {@link BackButtonListener}.
     */
    public void setOnBackPressedListener(BackButtonListener listener) {
        mBackButtonListener = listener;
    }
}
