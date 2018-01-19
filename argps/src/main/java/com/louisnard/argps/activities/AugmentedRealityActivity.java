package com.louisnard.argps.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.louisnard.argps.R;
import com.louisnard.argps.fragments.AugmentedRealityFragment;

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
        setContentView(R.layout.activity_toolbar_simple_fragment);

        // Set toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set fragment
        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, new AugmentedRealityFragment());
            transaction.commit();
        }
    }

    // Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_augmented_reality_activity, menu);
        return true;
    }

    // Options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
