package com.louisnard.augmentedreality;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private TextView mGpsLocationTextView;

    // Location
    private LocationManager mLocationManager;

    // Compass
    private Compass mCompass;

    // Dialogs
    private AlertDialogFragment mAlertDialogFragment;

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
        mLocationManager = (LocationManager) getActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);

        // Check that GPS is enabled
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "GPS is disabled.");
            showEnableGpsAlertDialog();
        }

        // Compass
        mCompass = Compass.getInstance(getActivity(), this);
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
        mGpsLocationTextView = (TextView) view.findViewById(R.id.text_view_gps_location);
        mCompassView = (CompassView) view.findViewById(R.id.compass_view);

    }

    @Override
    public void onResume() {
        super.onResume();

        // Start compass
        if (mCompass != null) mCompass.start();
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
        Log.d(TAG, "LocationListener.onLocationChanged()");
        mGpsLocationTextView.setText(location.toString());
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

    private void showEnableGpsAlertDialog() {
        mAlertDialogFragment = AlertDialogFragment.newInstance(R.string.alert_dialog_title_gps, R.string.alert_dialog_message_enable_gps, android.R.string.ok, android.R.string.cancel);
        mAlertDialogFragment.setTargetFragment(this, REQUEST_ENABLE_GPS);
        mAlertDialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
    }
}
