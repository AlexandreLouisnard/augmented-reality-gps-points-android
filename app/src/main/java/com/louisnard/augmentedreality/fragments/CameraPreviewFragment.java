package com.louisnard.augmentedreality.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.louisnard.augmentedreality.BuildConfig;
import com.louisnard.augmentedreality.R;

import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * {@link Fragment} that shows a camera preview using the camera2 API.
 *
 * @author Alexandre Louisnard
 */

public class CameraPreviewFragment extends Fragment {

    // Tag
    private static final String TAG = CameraPreviewFragment.class.getSimpleName();

    // Camera
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private Size mPreviewSize;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    // Prevent the app from exiting before closing the camera.
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    // Views
    private TextureView mTextureView;

    // Request codes
    private final int REQUEST_PERMISSIONS = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        mTextureView = (TextureView) view.findViewById(R.id.texture_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mTextureViewSurfaceListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    // TextureView listener
    private TextureView.SurfaceTextureListener mTextureViewSurfaceListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // Nothing to do
        }
    };

    /**
     * Opens the camera.
     */
    private void openCamera() {
        // Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "Missing CAMERA permissions");
            return;
        }

        try {
            if (BuildConfig.DEBUG) Log.d(TAG, "Trying to open the camera...");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = mCameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            mCameraManager.openCamera(cameraId, mCameraDeviceStateCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the camera.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Interrupted while trying to lock camera closing.");
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    // Camera status listener
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            if (BuildConfig.DEBUG) Log.d(TAG, "CameraDevice.StateCallback onOpened()");
            mCameraDevice = camera;
            startPreview();
            mCameraOpenCloseLock.release();
            if (mTextureView != null) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }


        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (BuildConfig.DEBUG) Log.d(TAG, "CameraDevice.StateCallback onDisconnected()");
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "CameraDevice.StateCallback onError() with error code: " + error);
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }
    };

    /**
     * Starts the camera preview and sets it to mTextureView.
     */
    private void startPreview() {
        if (mCameraDevice == null || !mTextureView.isAvailable() || mPreviewSize == null) {
            return;
        }
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (texture == null) {
            return;
        }
        closePreviewSession();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(texture);
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), mCameraCaptureSessionStateCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the camera preview session.
     */
    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    // Camera capture listener
    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mPreviewSession = session;
            updatePreviewThread();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Failed to configure camera capture session.");
        }
    };

    /**
     * Updates the camera preview by starting a background {@link HandlerThread} and its {@link Handler}.
     * {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreviewThread() {
        if (mCameraDevice == null) {
            return;
        }
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Returns the camera horizontal and vertical angles of view.
     * @param cameraId the camera id.
     * @return the angles of view such as:
     *          result[0] the horizontal angle.
     *          result[1] the vertical angle.
     */
    private float[] getCameraAnglesIfView(String cameraId) {
        // Use the deprecated Camera class to get the camera angles of view
        final Camera camera = Camera.open(Integer.valueOf(cameraId));
        final Camera.Parameters cameraParameters = camera.getParameters();
        final float horizontalCameraAngle = cameraParameters.getHorizontalViewAngle();
        final float verticalCameraAngle = cameraParameters.getVerticalViewAngle();
        camera.release();
        if (BuildConfig.DEBUG) Log.d(TAG, "Back camera horizontal angle = " + horizontalCameraAngle + " and vertical angle = " + verticalCameraAngle);
        return new float[] {horizontalCameraAngle, verticalCameraAngle};
    }

    /**
     * Returns the device back camera id.
     * @return the device back camera id.
     */
    private String getBackCameraId() {
        try {
            final String[] cameraIdsList = mCameraManager.getCameraIdList();
            for (String id : cameraIdsList){
                final CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);
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
}
