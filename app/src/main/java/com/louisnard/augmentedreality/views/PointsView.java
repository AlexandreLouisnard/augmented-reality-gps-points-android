package com.louisnard.augmentedreality.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.louisnard.augmentedreality.BuildConfig;
import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.model.objects.Point;

import java.util.SortedMap;

/**
 * Custom {@link View} that displays points from a {@link SortedMap<Float, Point>} depending on their azimuth.<br/>
 *
 * @author Alexandre Louisnard
 */

public class PointsView extends View {

    // Tag
    private static final String TAG = PointsView.class.getSimpleName();

    // Constants
    // The size of the arrow placemark
    private static int ARROW_SIZE = 100;

    // Points
    private SortedMap<Float, Point> mPoints;
    private Point mUserPoint;

    // Device and view orientations
    private float mAzimuthViewLeft;
    private float mAzimuthViewRight;
    private float mVerticalAngleViewCenter;
    private float mVerticalAngleViewTop;
    private float mVerticalAngleViewBottom;
    private float mHorizontalInclination;

    // Screen to camera angles ratios: the number of pixels on the screen associated to a one degree variation on the camera
    // Default values are those of a Nexus 4 camera
    private float mHorizontalCameraAngle = 54.8f;
    private float mVerticalCameraAngle = 42.5f;
    private float mHorizontalPixelsPerDegree;
    private float mVerticalPixelsPerDegree;

    // Drawing
    private Paint mTextPaint;

    public PointsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Paint
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(25);
        mTextPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Sets the device camera angles of view.<br/>
     * This angle of view is used to calculate the placement of the points.<br/>
     * If not set, default values are those of a Nexus 4: horizontal angle = 54.8° and vertical angle = 42.5°.
     * @param horizontalCameraAngle the horizontal angle of view in degrees.
     * @param verticalCameraAngle the vertical angle of view in degrees.
     */
    public void setCameraAngles(float horizontalCameraAngle, float verticalCameraAngle) {
        // Camera angles
        if (horizontalCameraAngle > 0 && horizontalCameraAngle <= 360 && verticalCameraAngle > 0 && verticalCameraAngle <= 360) {
            mHorizontalCameraAngle = horizontalCameraAngle;
            mVerticalCameraAngle = verticalCameraAngle;
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "Invalid camera angles, must be: 0) < cameraAngle <= 360°");
        }
    }

    /**
     * Sets the points that will be displayed in the {@link PointsView}.
     * @param points the {@link SortedMap<Float, Point>} mapping the relative azimuth of the point as the key with the associated {@link Point} as the value. Must be sorted by ascending azimuths.
     * @param userPoint the current user location point, used as a reference.
     */
    public void setPoints(Point userPoint, SortedMap<Float, Point> points) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Updating points list with " + (points != null ? points.size() : 0) + " points.");
        mUserPoint = userPoint;
        mPoints = points;
        invalidate();
    }

    /**
     * Updates the orientation: azimuth, pitch and roll of the device.<br/>
     * On them depends which points will be displayed and where will they be on the {@link PointsView};
     * @param azimuth the azimuth in degrees.
     * @param pitch the vertical inclination in degrees.
     * @param roll the horizontal inclination in degrees.<br/>
     */
    public void updateOrientation(float azimuth, float pitch, float roll) {
        mAzimuthViewLeft = (azimuth - mHorizontalCameraAngle / 2);
        mAzimuthViewRight = (azimuth + mHorizontalCameraAngle / 2);
        // When the device screen is held perpendicular to the ground, its camera pointing horizontally towards the landscape:
        //      - The device pitch = -90°.
        //      - The vertical angle of the points displayed at the center of the view is 0°.
        mVerticalAngleViewCenter = -pitch - 90;
        mVerticalAngleViewTop = mVerticalAngleViewCenter + mVerticalCameraAngle / 2;
        mVerticalAngleViewBottom = mVerticalAngleViewCenter - mVerticalCameraAngle / 2;
        mHorizontalInclination = roll;
        // Update view
        if (mPoints != null) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Scaling: calculate the number of pixels on the screen associated to a 1° angle variation on the camera
        if(mHorizontalPixelsPerDegree == 0 || mVerticalPixelsPerDegree == 0) {
            mHorizontalPixelsPerDegree = getWidth() / mHorizontalCameraAngle;
            mVerticalPixelsPerDegree = getHeight() / mVerticalCameraAngle;
            if (BuildConfig.DEBUG)
                Log.d(TAG, "View size in pixels = " + getWidth() + "x" + getHeight());
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Screen pixels associated to 1° camera angle variation: horizontal=" + mHorizontalPixelsPerDegree + "px/° & vertical=" + mVerticalPixelsPerDegree + "px/°");
        }

        // Draw visible points on canvas
        if (mUserPoint != null && mPoints != null && !mPoints.isEmpty()) {
            for (SortedMap.Entry<Float, Point> entry : mPoints.entrySet()) {
                final int x = azimuthToXPixelCoordinate(entry.getKey());
                final int y = verticalAngleToYPixelCoordinate(mUserPoint.verticalAngleTo(entry.getValue()));
                // TODO: check that vertical placement of points is correct
                if (x != -1 && y != -1) {
                    final Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_drop_down_24dp, null);
                    drawable.setBounds(x - ARROW_SIZE/2, y - ARROW_SIZE, x + ARROW_SIZE/2, y);
                    drawable.draw(canvas);
                    canvas.drawText(entry.getValue().getName() + "; " + mUserPoint.distanceTo(entry.getValue()) + "m; " + mUserPoint.azimuthTo(entry.getValue()) + "° " + mUserPoint.verticalAngleTo(entry.getValue()) + "°", x, y - ARROW_SIZE, mTextPaint);
                }
            }
        }
    }

    /**
     * Returns the the x coordinate value in pixels where the point should be horizontally placed on the {@link PointsView} depending on its azimuth.<br/>
     * Returns -1 if the point is located outside of the {@link PointsView}.
     * @param azimuth the azimuth of the point in degrees. It should be 0 < azimuth < 360.
     * @return the x coordinate in pixels or -1 if it is located outside of the view.
     */
    private int azimuthToXPixelCoordinate(float azimuth) {
        // TODO: handle horizontal inclination impact on X coordinates
        // Invalid azimuth
        if (azimuth < 0 || azimuth >= 360) {
            return -1;
        // Normal case : 0 < azimuthFrom < azimuth < azimuthTo < 360
        } else if (azimuth > mAzimuthViewLeft && azimuth < mAzimuthViewRight) {
            return (int) (mHorizontalPixelsPerDegree * (azimuth - mAzimuthViewLeft - (mAzimuthViewRight - mAzimuthViewLeft) / 2) + getWidth() / 2);
        // Special case 1 : azimuthFrom < 0 < azimuth < azimuthTo < 360
        } else if (mAzimuthViewLeft < 0 && azimuth > 360 + mAzimuthViewLeft) {
            return (int) (mHorizontalPixelsPerDegree * (azimuth - 360 - mAzimuthViewLeft - (mAzimuthViewRight - mAzimuthViewLeft) / 2) + getWidth() / 2);
        // Special case 2 : 0 < azimuthFrom < azimuth < 360 < azimuthTo
        } else if (mAzimuthViewRight > 360 && azimuth < mAzimuthViewRight - 360) {
            return (int) (mHorizontalPixelsPerDegree * (azimuth + 360 - mAzimuthViewLeft - (mAzimuthViewRight - mAzimuthViewLeft) / 2) + getWidth() / 2);
        } else {
            return -1;
        }
    }

    private int verticalAngleToYPixelCoordinate(float verticalAngle) {
        // TODO: handle horizontal inclination impact on Y coordinates
        // Invalid vertical angle
        if ((verticalAngle > -90 || verticalAngle < 90) && verticalAngle < mVerticalAngleViewTop && verticalAngle > mVerticalAngleViewBottom) {
            return (int) ((mVerticalAngleViewTop - verticalAngle) * mVerticalPixelsPerDegree);
        } else {
            return -1;
        }
    }
}
