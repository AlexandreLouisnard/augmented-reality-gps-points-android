package com.louisnard.augmentedreality.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.louisnard.augmentedreality.BuildConfig;
import com.louisnard.augmentedreality.DevUtils;
import com.louisnard.augmentedreality.PointsAdapter;
import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.mock.MockPoint;
import com.louisnard.augmentedreality.model.Compass;
import com.louisnard.augmentedreality.model.database.DbContract;
import com.louisnard.augmentedreality.model.database.DbHelper;
import com.louisnard.augmentedreality.model.objects.Point;
import com.louisnard.augmentedreality.ui.util.AlertDialogFragment;
import com.louisnard.augmentedreality.ui.views.CompassView;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Main fragment showing {@link Compass} data in a {@link CompassView}.
 *
 * @author Alexandre Louisnard
 */

public class MainFragment extends Fragment implements LocationListener, Compass.CompassListener {

    // Tag
    private static final String TAG = MainFragment.class.getSimpleName();

    // Constants
    // The minimum distance the user must have moved from its previous location to recalculate azimuths and distances, in meters
    private static final int MIN_DISTANCE_DIFFERENCE_BETWEEN_RECALCULATIONS = 10;
    // The minimum distance the user must have moved from its previous location to reload the points from the database, in meters
    private static final int MIN_DISTANCE_DIFFERENCE_BETWEEN_DATABASE_RELOADS = 500;
    // The maximum distance to search and display points around the user's location, in meters
    private static final int MAX_RADIUS_DISTANCE_TO_SEARCH_POINTS_AROUND = 10000;
    // The minimum time interval between GPS location updates, in milliseconds
    private static final long MIN_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES = 5000;
    // The minimum difference with the last azimuth measured by the compass for the CompassListener to be notified, in degrees
    private static final float MIN_AZIMUTH_DIFFERENCE_BETWEEN_COMPASS_UPDATES = 1;

    // Location
    private LocationManager mLocationManager;

    // Compass
    private Compass mCompass;
    private float mAzimuth;

    // Camera
    private float mHorizontalCameraAngle;
    private float mVerticalCameraAngle;

    // Points
    private Point mLastDbReadUserLocationPoint;
    private Point mUserLocationPoint;
    private List<Point> mPoints;
    //private List<TextView> mPointViews;
    //private SortedMap<Float, Point> mPointsSortedByAzimuth;

    // Views
    private RelativeLayout mRelativeLayout;
    private CompassView mCompassView;
    private PointsAdapter mPointsAdapter;
    private PointsView mPointsView;
    //private TextView mTextView;
    //private ListView mListView;

    // Request codes
    private final int REQUEST_PERMISSIONS = 1;
    private final int REQUEST_ENABLE_GPS = 2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "Missing permissions.");
            return;
        }

        // Location
        mLocationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES, 5, this);

        // Check that GPS is enabled
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "GPS is disabled.");
            showEnableGpsAlertDialog();
        }

        // Compass
        mCompass = Compass.newInstance(getContext(), this);

        // Camera
        final CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        final String cameraId = getBackCameraId(cameraManager);

        // Use the deprecated Camera class to get the camera angles of view
        final Camera camera = Camera.open(Integer.valueOf(cameraId));
        final Camera.Parameters cameraParameters = camera.getParameters();
        mHorizontalCameraAngle = cameraParameters.getHorizontalViewAngle();
        mVerticalCameraAngle = cameraParameters.getVerticalViewAngle();
        if (BuildConfig.DEBUG) Log.d(TAG, "Back camera horizontal angle = " + mHorizontalCameraAngle + " and vertical angle = " + mVerticalCameraAngle);

        // Screen size
        /*final Display display = getActivity().getWindowManager().getDefaultDisplay();
        final DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        final float density  = getResources().getDisplayMetrics().density;
        mScreenWidthPixels = outMetrics.widthPixels / density;
        mScreenHeightPixels = outMetrics.heightPixels / density;;
        if (BuildConfig.DEBUG) Log.d(TAG, "Screen size in pixels = " + mScreenWidthPixels + "x" + mScreenHeightPixels);

        // Calculate the number of pixels on the screen associated to a 1° angle variation on the camera
        mHorizontalPixelsPerDegree = mScreenWidthPixels / mHorizontalCameraAngle;
        mVerticalPixelsPerDegree = mScreenHeightPixels / mVerticalCameraAngle;
        if (BuildConfig.DEBUG) Log.d(TAG, "Screen pixels associated to 1° camera angle variation: horizontal=" + mHorizontalPixelsPerDegree + "px/° & vertical=" + mVerticalPixelsPerDegree + "px/°");*/
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
        mRelativeLayout = (RelativeLayout) view.findViewById(R.id.relative_layout);
        //mTextView = (TextView) view.findViewById(android.R.id.text1);
        mCompassView = (CompassView) view.findViewById(R.id.compass_view);
        //mListView = (ListView) view.findViewById(R.id.list_view);
        mPointsView = (PointsView) view.findViewById(R.id.points_view);
        mPointsView.setCameraAngles(mHorizontalCameraAngle, mVerticalCameraAngle);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Dump database for debug use only
        if (BuildConfig.DEBUG) {
            DevUtils.exportDatabaseToExternalStorage(getActivity(), DbHelper.getDbName());
        }

        // Start compass
        if (mCompass != null) mCompass.start(MIN_AZIMUTH_DIFFERENCE_BETWEEN_COMPASS_UPDATES);

        // TODO: FOR TEST USE ONLY: populate database
        final DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
        dbHelper.clearTable(DbContract.PointsColumns.TABLE_NAME);
        dbHelper.addPoints(MockPoint.getPoints());
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
        //mAzimuth = azimuth;
        mCompassView.updateAzimuth(azimuth);

        // TODO: draw points
        mPointsView.setAzimuth(azimuth);
    }

    // LocationListener interface
    @Override
    public void onLocationChanged(Location location) {
        if (BuildConfig.DEBUG) Log.d(TAG, "LocationListener.onLocationChanged(): " + location.toString());

        // Load points around the user from the database
        if (mPoints == null || mLastDbReadUserLocationPoint == null || mLastDbReadUserLocationPoint.distanceTo(location) > MIN_DISTANCE_DIFFERENCE_BETWEEN_DATABASE_RELOADS) {
            mLastDbReadUserLocationPoint = new Point("", location);
            final DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
            mPoints = dbHelper.getPointsAround(location, MAX_RADIUS_DISTANCE_TO_SEARCH_POINTS_AROUND);
            //mPointsAdapter = new PointsAdapter(getContext(), mPoints);
            if (BuildConfig.DEBUG) Log.d(TAG, "Found " + mPoints.size() + " points in the database around the new user location.");
        }

        // Update user location and recalculate relative azimuths of points from the new user location
        if (mUserLocationPoint == null || mUserLocationPoint.distanceTo(location) > MIN_DISTANCE_DIFFERENCE_BETWEEN_RECALCULATIONS) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Recalculating points azimuth from the new user location");
            mUserLocationPoint = new Point("", location);
            // TODO: calculate relative azimuths
            mPointsView.setPoints(getPointsSortedMapByAzimuth(mUserLocationPoint, mPoints));
        }
    }

    // LocationListener interface
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (BuildConfig.DEBUG) Log.d(TAG, "LocationListener.onStatusChanged()");
    }

    // LocationListener interface
    @Override
    public void onProviderEnabled(String provider) {
        if (BuildConfig.DEBUG) Log.d(TAG, "LocationListener.onProviderEnabled()");
    }

    // LocationListener interface
    @Override
    public void onProviderDisabled(String provider) {
        if (BuildConfig.DEBUG) Log.d(TAG, "LocationListener.onProviderDisabled()");
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

    // Calculate the relative azimuth of points from the user's location point of view
    /**
     * Returns a {@link SortedMap<Float, Point>} mapping:
     * - As key: each point azimuth, as seen from {@param originPoint}.
     * - As value: each {@link Point} from {@param points}.
     * The {@link SortedMap<Float, Point>} is sorted by key value (which means by point azimuth).
     * @param originPoint the {@link Point} from which to calculate the relative azimuths of the other points. For instance, the user location.
     * @param points the {@link List<Point>} to sort by relative azimuth.
     * @return the {@link SortedMap<Float, Point>} of points sorted by azimuth as seen from {@param originPoint}, ans using azimuth values as keys.
     */
    private SortedMap<Float, Point> getPointsSortedMapByAzimuth(Point originPoint, List<Point> points) {
        SortedMap<Float, Point> pointsSortedMap = new TreeMap<Float, Point>();
        for (Point p : points) {
            pointsSortedMap.put(originPoint.azimuthTo(p), p);
        }
        return pointsSortedMap;
    }

    // Calculate the relative azimuth of points from the user's location point of view and generate views
    /**
     * Returns a {@link SortedMap<Float, TextView>} mapping:
     * - As key: each point azimuth, as seen from {@param originPoint}.
     * - As value: each {@link Point} from {@param points}.
     * The {@link SortedMap<Float, TextView>} is sorted by key value (which means by point azimuth).
     * @param originPoint the {@link Point} from which to calculate the relative azimuths of the other points. For instance, the user location.
     * @param points the {@link List<Point>} to sort by relative azimuth.
     * @return the {@link SortedMap<Float, TextView>} of point views sorted by azimuth as seen from {@param originPoint}, ans using azimuth values as keys.
     */
    private SortedMap<Float, TextView> generatePointViewsSortedMap(Point originPoint, List<Point> points) {
        SortedMap<Float, TextView> pointViewsSortedMap = new TreeMap<Float, TextView>();
        for (Point point : points) {
            final TextView pointView = new TextView(getContext());
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            pointView.setLayoutParams(params);
            pointView.setBackgroundColor(Color.BLUE);
            pointView.setText(point.getName());
            pointViewsSortedMap.put(originPoint.azimuthTo(point), pointView);
        }
        return pointViewsSortedMap;
    }


    /**
     * Returns a {@List<TextView>} with a point view for each point from {@param points}.
     * @param points the {@link List<Point>} to generate views from.
     * @return the {@List<TextView>} of generated views.
     */
    private List<TextView> generatePointViews(List<Point> points) {
        List<TextView> pointViews = new ArrayList<TextView>();
        for (Point point : points) {
            final TextView pointView = new TextView(getContext());
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            pointView.setLayoutParams(params);
            pointView.setBackgroundColor(Color.BLUE);
            pointView.setText(point.getName());
            pointViews.add(pointView);
        }
        return pointViews;
    }

    // Display an alert dialog asking the user to enable the GPS
    private void showEnableGpsAlertDialog() {
        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(R.string.alert_dialog_title_gps, R.string.alert_dialog_message_enable_gps, android.R.string.ok, android.R.string.cancel);
        alertDialogFragment.setTargetFragment(this, REQUEST_ENABLE_GPS);
        alertDialogFragment.show(getFragmentManager(), AlertDialogFragment.TAG);
    }

    // Get the device back camera id
    private String getBackCameraId(CameraManager cameraManager) {
        try {
            final String[] cameraIdsList = cameraManager.getCameraIdList();
            for (String id : cameraIdsList){
                final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        } catch (CameraAccessException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Your device does not have a camera.");
            e.printStackTrace();
        }
        return null;
    }
}
