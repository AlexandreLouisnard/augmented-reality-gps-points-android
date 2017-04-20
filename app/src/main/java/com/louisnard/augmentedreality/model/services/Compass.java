package com.louisnard.augmentedreality.model.services;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.louisnard.augmentedreality.BuildConfig;

import static android.content.Context.SENSOR_SERVICE;


/**
 * Compass implementation for Android using magnetic and accelerometer device sensors.
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
    private Context mContext;

    // Sensors
    private SensorManager mSensorManager;
    private Sensor mMagnetometer;
    private Sensor mAccelerometer;

    // Compass
    private float mAzimuthDegrees;
    private float[] mGeomagnetic = new float[3];
    private float[] mGravity = new float[3];

    // Listener
    private CompassListener mCompassListener;
    // The minimum difference in degrees with the last azimuth measure for the CompassListener to be notified
    private float mSensibility;
    // The last azimuth value sent to the CompassListener
    private float mLastAzimuthDegrees;


    /**
     * Interface definition for {@link Compass} callbacks.
     */
    public interface CompassListener {
        void onAzimuthChanged(float azimuth);
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
     * @param sensibility the minimum difference in degrees with the last azimuth measure for the {@link CompassListener} to be notified.
     */
    public void start(float sensibility) {
        mSensibility = sensibility;
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Starts the {@link Compass}.
     * Must be called in {@link Activity#onResume()}.
     */
    public void start() {
        start(0);
    }

    /**
     * Stops the {@link Compass}.
     * Must be called in {@link Activity#onPause()}.
     */
    public void stop() {
        mSensibility = 0;
        mSensorManager.unregisterListener(this);
    }

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
                if (screenRotation == Surface.ROTATION_90) {
                    mAzimuthDegrees += 90f;
                } else if (screenRotation == Surface.ROTATION_180) {
                    mAzimuthDegrees += 180f;
                } else if (screenRotation == Surface.ROTATION_270) {
                    mAzimuthDegrees += 270f;
                }
                // Force azimuth value between 0° and 360°.
                mAzimuthDegrees = (mAzimuthDegrees + 360) % 360;
                // Notify the compass listener
                if (Math.abs(mAzimuthDegrees - mLastAzimuthDegrees) >= mSensibility || mLastAzimuthDegrees == 0) {
                    mLastAzimuthDegrees = mAzimuthDegrees;
                    mCompassListener.onAzimuthChanged(mAzimuthDegrees);
                }
            }
        }
    }

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
