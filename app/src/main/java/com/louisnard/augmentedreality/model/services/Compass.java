package com.louisnard.augmentedreality.model.services;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.support.compat.BuildConfig;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import static android.content.Context.SENSOR_SERVICE;


/**
 * Compass implementation for Android providing azimuth, vertical inclination (pitch) and horizontal inclination (roll) of the device, in degrees.<br/>
 *
 * Azimuth: angle of rotation about the -z axis. This value represents the angle between the device's y axis and the magnetic north pole.<br/>
 * Vertical inclination = pitch: angle of rotation about the x axis. This value represents the angle between a plane parallel to the device's screen and a plane parallel to the ground.<br/>
 * Horizontal inclination = roll: angle of rotation about the y axis. This value represents the angle between a plane perpendicular to the device's screen and a plane perpendicular to the ground.<br/>
 *
 * This implementation takes into account the orientation (portrait / landscape) of the device and corrects the values accordingly.<br/>
 *
 * Uses magnetic and accelerometer device sensors.<br/>
 *
 * @author Alexandre Louisnard
 */

public class Compass implements SensorEventListener {

    // Tag
    private static final String TAG = Compass.class.getSimpleName();

    // Constants
    private static final float GEOMAGNETIC_SMOOTHING_FACTOR = 0.4f;
    private static final float GRAVITY_SMOOTHING_FACTOR = 0.1f;

    // Context
    private final Context mContext;

    // Sensors
    private final SensorManager mSensorManager;
    private final Sensor mMagnetometer;
    private final Sensor mAccelerometer;

    // Orientation
    private float mAzimuthDegrees;
    private float mVerticalInclinationDegrees;
    private float mHorizontalInclinationDegrees;
    private float[] mGeomagnetic = new float[3];
    private float[] mGravity = new float[3];

    // Listener
    private final CompassListener mCompassListener;
    // The minimum difference in degrees with the last orientation value for the CompassListener to be notified
    private float mAzimuthSensibility;
    private float mVerticalInclinationSensibility;
    private float mHorizontalInclinationSensibility;
    // The last orientation value sent to the CompassListener
    private float mLastAzimuthDegrees;
    private float mLastVerticalInclinationDegrees;
    private float mLastHorizontalInclinationDegrees;

    /**
     * Interface definition for {@link Compass} callbacks.
     */
    public interface CompassListener {
        /**
         * Called whenever the device orientation has changed, providing azimuth, vertical inclination and horizontal inclination values taking into account the screen orientation of the device.
         * @param azimuth the azimuth of the device, in degrees.
         * @param verticalInclination the vertical inclination of the device, in degrees.<br/>
         *                            Equals 0 if the device top and bottom edges are on the same level.<br/>
         *                            Equals -90 if the device top edge is up and the device bottom edge is down.<br/>
         *                            Equals 90 if the device top edge is down and the device bottom edge is up.
         * @param horizontalInclination the horizontal inclination of the device, in degrees.<br/>
         *                            Equals 0 if the device left and right edges are on the same level.<br/>
         *                            Equals -90 if the device right edge is up and the device left edge is down.<br/>
         *                            Equals 90 if the device right edge is down and the device left edge is up.
         */
        void onOrientationChanged(float azimuth, float verticalInclination, float horizontalInclination);
    }

    // Private constructor
    private Compass(Context context, CompassListener compassListener) {
        mContext = context;
        // Sensors
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Listener
        mCompassListener = compassListener;
    }

    /**
     * Returns a new {@link Compass} instance.
     * @param context the current {@link Context}.
     * @param compassListener the listener for this {@link Compass} events.
     * @return a new {@link Compass} instance or <b>null</b> if the device does not have the required sensors.
     */
    @Nullable
    public static Compass newInstance(Context context, CompassListener compassListener) {
        Compass compass = new Compass(context, compassListener);
        if (compass.hasRequiredSensors()) {
            return compass;
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "The device does not have the required sensors for Compass.");
            return null;
        }
    }

    // Check that the device has the required sensors
    private boolean hasRequiredSensors() {
        if (mMagnetometer == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "No Magnetic sensor.");
            return false;
        } else if (mAccelerometer == null) {
            if (BuildConfig.DEBUG) Log.d(TAG, "No Accelerometer sensor.");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Starts the {@link Compass}.
     * Must be called in {@link Activity#onResume()}.
     * @param azimuthSensibility the minimum difference in degrees with the last azimuth measure for the {@link CompassListener} to be notified. Set to 0 (default value) to be notified of the slightest change, set to 360 to never be notified.
     * @param verticalInclinationSensibility the minimum difference in degrees with the last vertical inclination measure for the {@link CompassListener} to be notified. Set to 0 (default value) to be notified of the slightest change, set to 360 to never be notified.
     * @param horizontalInclinationSensibility the minimum difference in degrees with the last horizontal inclination measure for the {@link CompassListener} to be notified. Set to 0 (default value) to be notified of the slightest change, set to 360 to never be notified.
     */
    public void start(float azimuthSensibility, float verticalInclinationSensibility, float horizontalInclinationSensibility) {
        mAzimuthSensibility = azimuthSensibility;
        mVerticalInclinationSensibility = verticalInclinationSensibility;
        mHorizontalInclinationSensibility = horizontalInclinationSensibility;
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Starts the {@link Compass}.
     * Must be called in {@link Activity#onResume()}.
     */
    public void start() {
        start(0, 0, 0);
    }

    /**
     * Stops the {@link Compass}.
     * Must be called in {@link Activity#onPause()}.
     */
    public void stop() {
        mAzimuthSensibility = 0;
        mVerticalInclinationSensibility = 0;
        mHorizontalInclinationSensibility = 0;
        mSensorManager.unregisterListener(this);
    }

    // SensorEventListener
    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = exponentialSmoothing(event.values, mGeomagnetic, GEOMAGNETIC_SMOOTHING_FACTOR);
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = exponentialSmoothing(event.values, mGravity, GRAVITY_SMOOTHING_FACTOR);
            }
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                mAzimuthDegrees = (float) Math.toDegrees(orientation[0]);
                // Correct azimuth value depending on screen orientation
                final int screenRotation = (((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
                if (screenRotation == Surface.ROTATION_0) {
                    mVerticalInclinationDegrees = (float) Math.toDegrees(orientation[1]);
                    mHorizontalInclinationDegrees = (float) Math.toDegrees(orientation[2]);
                } else if (screenRotation == Surface.ROTATION_90) {
                    mAzimuthDegrees += 90f;
                    mVerticalInclinationDegrees = (float) Math.toDegrees(orientation[2]);
                    mHorizontalInclinationDegrees = (float) -Math.toDegrees(orientation[1]);
                } else if (screenRotation == Surface.ROTATION_180) {
                    mAzimuthDegrees += 180f;
                    mVerticalInclinationDegrees = (float) -Math.toDegrees(orientation[1]);
                    mHorizontalInclinationDegrees = (float) -Math.toDegrees(orientation[2]);
                } else if (screenRotation == Surface.ROTATION_270) {
                    mAzimuthDegrees += 270f;
                    mVerticalInclinationDegrees = (float) -Math.toDegrees(orientation[2]);
                    mHorizontalInclinationDegrees = (float) Math.toDegrees(orientation[1]);
                }
            }
            // Force azimuth value between 0° and 360°.
            mAzimuthDegrees = (mAzimuthDegrees + 360) % 360;
            // Notify the compass listener
            if (Math.abs(mAzimuthDegrees - mLastAzimuthDegrees) >= mAzimuthSensibility
                    || Math.abs(mVerticalInclinationDegrees - mLastVerticalInclinationDegrees) >= mVerticalInclinationSensibility
                    || Math.abs(mHorizontalInclinationDegrees - mLastHorizontalInclinationDegrees) >= mHorizontalInclinationSensibility
                    || mLastAzimuthDegrees == 0) {
                mLastAzimuthDegrees = mAzimuthDegrees;
                mLastVerticalInclinationDegrees = mVerticalInclinationDegrees;
                mLastHorizontalInclinationDegrees = mHorizontalInclinationDegrees;
                mCompassListener.onOrientationChanged(mAzimuthDegrees, mVerticalInclinationDegrees, mHorizontalInclinationDegrees);
            }
        }
    }

    // SensorEventListener
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nothing to do
    }

    /**
     * Exponential smoothing of data series, acting as a low-pass filter in order to remove high-frequency noise.
     * @param newValue the new data entry.
     * @param lastValue the last data entry.
     * @param alpha the smoothing factor. 0 < alpha < 1. If alpha = 0, the data will never change (lastValue = newValue). If alpha = 1, no smoothing at all will be applied (lastValue = newValue).
     * @return output the new data entry, smoothened.
     */
    private float[] exponentialSmoothing(float[] newValue, float[] lastValue, float alpha) {
        float[] output = new float[newValue.length];
        if (lastValue == null) {
            return newValue;
        }
        for (int i=0; i<newValue.length; i++) {
            output[i] = lastValue[i] + alpha * (newValue[i] - lastValue[i]);
        }
        return output;
    }
}
