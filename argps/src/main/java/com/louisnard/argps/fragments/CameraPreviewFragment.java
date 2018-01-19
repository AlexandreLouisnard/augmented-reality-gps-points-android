package com.louisnard.argps.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.louisnard.argps.BuildConfig;
import com.louisnard.argps.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Abstract fragment showing a camera preview in a texture view using Camera2 API.<br>
 *
 * Requires Manifest.permission.CAMERA permission.
 *
 * @author Alexandre Louisnard
 */
public abstract class CameraPreviewFragment extends Fragment {

    // Tag
    private static final String TAG = CameraPreviewFragment.class.getSimpleName();

    // Permissions
    private boolean mHasPermissions;

    // Threads
    // An additional thread for running tasks that shouldn't block the UI
    private HandlerThread mBackgroundThread;
    // A handler for running tasks in the background.
    private Handler mBackgroundHandler;

    // Views
    // TextureView for the camera preview
    private TextureView mTextureView;

    // Camera
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private Size mPreviewSize;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private float[] mCameraHardwareAnglesOfView;

    // Max preview size that is guaranteed by Camera2 API
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * Returns the resource id of the {@link TextureView} on which the camera preview will be displayed.
     * Must be overriden by all subclasses of {@link CameraPreviewFragment}.
     * e.g. return R.id.texture_view;
     * @return the resource id of the {@link TextureView} on which the camera preview will be displayed.
     */
    protected abstract int getTextureViewResIdForCameraPreview();

    /**
     * Callback method invoked when the camera preview is ready and displayed in the {@link TextureView}.
     * @param cameraPreviewAnglesOfView the camera preview angles of view such as:<br/>
     *          result[0] the horizontal angle.<br/>
     *          result[1] the vertical angle.
     */
    protected abstract void onCameraPreviewReady(float[] cameraPreviewAnglesOfView);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHasPermissions = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (mHasPermissions) {
            mCameraId = getBackCameraId();
            mCameraHardwareAnglesOfView = getCameraAnglesOfView();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mTextureView = (TextureView) view.findViewById(getTextureViewResIdForCameraPreview());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mHasPermissions) {
            startBackgroundThread();

            // When the screen is turned off and turned back on, the SurfaceTexture is already available, and "onSurfaceTextureAvailable" will not be called
            // In that case, we can open a camera and start preview from here (otherwise, we wait until the surface is ready in the SurfaceTextureListener)
            if (mTextureView.isAvailable()) {
                openCamera(mTextureView.getWidth(), mTextureView.getHeight());
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        }
    }

    @Override
    public void onPause() {
        if (mHasPermissions) {
            closeCamera();
            stopBackgroundThread();
        }
        super.onPause();
    }

    // TextureView listener
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    // Camera state listener
    private final CameraDevice.StateCallback mCameraStateListener = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }
    };

    // Camera capture session state listener
    private final CameraCaptureSession.StateCallback mCameraCaptureSessionStateListener = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            // The camera is already closed
            if (null == mCameraDevice) {
                return;
            }

            // When the session is ready, we start displaying the preview
            mCameraCaptureSession = cameraCaptureSession;
            try {
                // Auto focus should be continuous for camera preview
                mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                // Finally, we start displaying the camera preview
                CaptureRequest previewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                mCameraCaptureSession.setRepeatingRequest(previewCaptureRequest, mCameraCaptureSessionCaptureCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "mCameraCaptureSessionStateListener onConfigured(): Camera Preview ready, calling onCameraPreviewReady()");
            final int screenRotation = (((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()).getRotation();
            final boolean isPortraitMode = (screenRotation == Surface.ROTATION_0 || screenRotation == Surface.ROTATION_180);
            final float[] cameraPreviewAnglesOfView = adaptCameraAnglesOfViewToASupport(mCameraHardwareAnglesOfView[0], mCameraHardwareAnglesOfView[1], mTextureView.getWidth(), mTextureView.getHeight(), isPortraitMode);
            onCameraPreviewReady(cameraPreviewAnglesOfView);
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            if(BuildConfig.DEBUG) Log.d(TAG, "CameraCaptureSession configuration failed");
        }
    };

    // Camera capture session capture listener
    private final CameraCaptureSession.CaptureCallback mCameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
        }
    };

    // Open the camera
    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening");
            }
            cameraManager.openCamera(mCameraId, mCameraStateListener, mBackgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening", e);
        }
    }

    // Close the camera
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraCaptureSession) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    // Start the background thread and its handler
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    // Stop the background thread and its handler
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

    // Creates a new CameraCaptureSession for camera preview.
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Collections.singletonList(surface), mCameraCaptureSessionStateListener, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up member variables related to camera.
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            int screenRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (screenRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    if (BuildConfig.DEBUG) Log.d(TAG, "Display rotation is invalid: " + screenRotation);
            }

            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);

            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                //noinspection SuspiciousNameCombination
                rotatedPreviewWidth = height;
                //noinspection SuspiciousNameCombination
                rotatedPreviewHeight = width;
                //noinspection SuspiciousNameCombination
                maxPreviewWidth = displaySize.y;
                //noinspection SuspiciousNameCombination
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation, resulting in gorgeous previews but the storage of garbage capture data.
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight);
        } catch (CameraAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the texture view. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size.
     *
     * @param choices           The list of sizes that the camera supports for the intended output class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        float textureViewRatio = (float) textureViewWidth / textureViewHeight;
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth
                    && option.getHeight() <= maxHeight
                    && Math.abs(option.getWidth() - option.getHeight() * textureViewRatio) <= option.getWidth() / 100) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the largest of those not big enough.
        Size optimalSize;
        if (bigEnough.size() > 0) {
            optimalSize = Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            optimalSize = Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Couldn't find any suitable camera preview size");
            optimalSize = choices[0];
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Using camera preview size: " + optimalSize.toString() + "(ratio:" + (float) optimalSize.getWidth() / optimalSize.getHeight() + ")" + "for texture view size: " + textureViewWidth + "x" + textureViewHeight + "(ratio:" + textureViewRatio + ")");
        return optimalSize;
    }

    // Compares two sizes based on their areas
    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
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
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Returns the camera horizontal and vertical angles of view as given by the {@link Camera} API.<br>
     * Does not take into account the target view aspect ratio (16/9, 4/3...) nor the device screen orientation.
     * @return the angles of view such as:<br>
     *          result[0] the horizontal angle (the widest).<br>
     *          result[1] the vertical angle (the narrowest).
     */
    @Deprecated
    private float[] getCameraAnglesOfView() {
        // Use the deprecated Camera class to get the camera angles of view
        final Camera camera = Camera.open(Integer.valueOf(mCameraId));
        final Camera.Parameters cameraParameters = camera.getParameters();
        // cameraParameters.getHorizontalViewAngle() is the widest angle
        float horizontalCameraAngle = cameraParameters.getHorizontalViewAngle();
        // cameraParameters.getVerticalViewAngle() the narrowest angle
        float verticalCameraAngle = cameraParameters.getVerticalViewAngle();
        camera.release();
        if (BuildConfig.DEBUG) Log.d(TAG, "Camera hardware horizontal angle = " + horizontalCameraAngle + " and vertical angle = " + verticalCameraAngle);
        return new float[] {horizontalCameraAngle, verticalCameraAngle};
    }

    /**
     * Returns the camera angle of view for this specific camera preview taking into account:<br>
     *     The target {@link TextureView} aspect ratio.<br>
     *     The device screen orientation.
     * @param horizontalCameraAngle the horizontal camera angle of view (the widest) as given by the {@link Camera} API.
     * @param verticalCameraAngle the vertical camera angle of view (the narrowest) as given by the {@link Camera} API.
     * @param targetWidth the target support {@link TextureView} width.
     * @param targetHeight the target support {@link TextureView} height.
     * @param isPortraitMode <b>true</b> if the device is currently in portrait mode. <b>false</b> otherwise
     * @return the angles of view such as:<br/>
     *          result[0] the horizontal angle.<br/>
     *          result[1] the vertical angle.
     */
    private float[] adaptCameraAnglesOfViewToASupport(float horizontalCameraAngle, float verticalCameraAngle, int targetWidth, int targetHeight, boolean isPortraitMode) {
        // Invert values in portrait mode
        if (isPortraitMode) {
            final float tmp = horizontalCameraAngle;
            horizontalCameraAngle = verticalCameraAngle;
            verticalCameraAngle = tmp;
        }

        // Adapt to the TextureView ratio
        if (targetWidth != 0 && targetHeight != 0) {
            final float ratio = (float) targetWidth / targetHeight;
            if (horizontalCameraAngle / verticalCameraAngle < ratio) {
                verticalCameraAngle = (float) Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(horizontalCameraAngle/2)) / ratio));
            } else if (horizontalCameraAngle / verticalCameraAngle > ratio) {
                horizontalCameraAngle = (float) Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(verticalCameraAngle/2)) * ratio));
            }
        }

        if (BuildConfig.DEBUG) Log.d(TAG, "Camera preview horizontal angle = " + horizontalCameraAngle + " and vertical angle = " + verticalCameraAngle);
        return new float[] {horizontalCameraAngle, verticalCameraAngle};
    }

    /**
     * Returns the device back camera id.
     * @return the device back camera id.
     */
    protected String getBackCameraId() {
        try {
            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            final String[] cameraIdsList = cameraManager.getCameraIdList();
            for (String id : cameraIdsList){
                final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                if(characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
        } catch (CameraAccessException | NullPointerException e) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Your device does not have a camera.");
            e.printStackTrace();
        }
        return null;
    }
}
