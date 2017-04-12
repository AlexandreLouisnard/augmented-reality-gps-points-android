package com.louisnard.augmentedreality.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.louisnard.augmentedreality.DevUtils;
import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.model.Compass;
import com.louisnard.augmentedreality.model.objects.mock.Mock;
import com.louisnard.augmentedreality.model.database.DbContract;
import com.louisnard.augmentedreality.model.database.DbHelper;
import com.louisnard.augmentedreality.model.objects.Point;
import com.louisnard.augmentedreality.ui.util.AlertDialogFragment;
import com.louisnard.augmentedreality.ui.views.CompassView;

import java.util.List;

/**
 * Main fragment showing {@link Compass} data in a {@link CompassView}.
 *
 * @author Alexandre Louisnard
 */

public class MainFragment extends Fragment implements LocationListener, Compass.CompassListener {

    // Tag
    private static final String TAG = MainFragment.class.getSimpleName();

    // Views
    private CompassView mCompassView;
    private TextView mLocationTextView;

    // Location
    private LocationManager mLocationManager;
    private Location mLocation;

    // Compass
    private Compass mCompass;

    // Request codes
    private final int REQUEST_PERMISSIONS = 1;
    private final int REQUEST_ENABLE_GPS = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
            }
            Log.d(TAG, "Missing permissions.");
            return;
        }

        // Location
        mLocationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);

        // Check that GPS is enabled
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "GPS is disabled.");
            showEnableGpsAlertDialog();
        }

        // Compass
        mCompass = Compass.newInstance(getActivity(), this);
        if (mCompass == null) {
            Log.d(TAG, "The device does not have the required sensors to use a augmentedreality.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        mLocationTextView = (TextView) view.findViewById(R.id.text_view_location);
        mCompassView = (CompassView) view.findViewById(R.id.compass_view);
    }

    @Override
    public void onResume() {
        super.onResume();

        // For test use only : dump database
        DevUtils.exportDatabaseToExternalStorage(getActivity(), DbHelper.getDbName());

        // Start compass
        if (mCompass != null) mCompass.start();

        // TODO: FOR TEST USE ONLY: populate database
        final DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
        dbHelper.clearTable(DbContract.PointsColumns.TABLE_NAME);
        dbHelper.addPoints(Mock.MockPoint.getPoints());
        final List<Point> allPoints = dbHelper.getAllPoints();
        for (Point p : allPoints) {
            Log.d(TAG, "All points : " + p.getName());
        }

        Location l = new Location("");
        l.setLatitude(1);
        l.setLongitude(2);
        l.setAltitude(3);

        final Point p = new Point("123", l);
        Log.d(TAG, "p: " + p.getName() + "," + p.getLatitude() + "," + p.getLongitude() + "," + p.getAltitude());
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop compass
        if (mCompass != null) mCompass.stop();
    }

    // CompassListener interface
    @Override
    public void onAzimuthChanged(float azimuth) {
        mCompassView.updateAzimuth(azimuth);
    }

    // LocationListener interface
    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        Log.d(TAG, "LocationListener.onLocationChanged(): " + mLocation.toString());
        mLocationTextView.setText(mLocation.toString());
    }

    // LocationListener interface
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "LocationListener.onStatusChanged()");
    }

    // LocationListener interface
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "LocationListener.onProviderEnabled()");
    }

    // LocationListener interface
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "LocationListener.onProviderDisabled()");
        showEnableGpsAlertDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_GPS:
                if (resultCode == Activity.RESULT_OK) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getActivity().recreate();
            } else {
                getActivity().recreate();
            }
        }
    }

    // Display an alert dialog asking the user to enable the GPS
    private void showEnableGpsAlertDialog() {
        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(R.string.alert_dialog_title_gps, R.string.alert_dialog_message_enable_gps, android.R.string.ok, android.R.string.cancel);
        alertDialogFragment.setTargetFragment(this, REQUEST_ENABLE_GPS);
        alertDialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
    }
}
