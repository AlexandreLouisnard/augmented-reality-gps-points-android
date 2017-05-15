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
 * {@link View} class that places and displays points from a {@link SortedMap<Float, Point>} depending on their azimuth.
 *
 * @author Alexandre Louisnard
 */

public class PointsView extends View {

    // Tag
    private static final String TAG = PointsView.class.getSimpleName();

    // Constants
    private static int ARROW_SIZE = 100;

    // Points
    private SortedMap<Float, Point> mPoints;
    //private SortedMap<Float, Point> mVisiblePoints;
    private float mAzimuthFrom;
    private float mAzimuthTo;

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
     * Sets the device camera angles of view.
     * This angle of view is used to calculate the placement of the points.
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
            Log.d(TAG, "Invalid camera angles, must be: 0) < cameraAngle <= 360°");
        }
    }

    /**
     * Sets the points that will be displayed in the {@link PointsView}.
     * @param points the {@link SortedMap<Float, Point>} mapping the relative azimuth of the point as the key with the associated {@link Point} as the value. Must be sorted by ascending azimuths.
     */
    public void setPoints(SortedMap<Float, Point> points) {
        if (BuildConfig.DEBUG) Log.d(TAG, "Updating points list with " + (points != null ? points.size() : 0) + " points.");
        mPoints = points;
        //mVisiblePoints = mPoints.subMap(mAzimuthFrom, mAzimuthTo);
        invalidate();
    }

    /**
     * Sets the azimuth at the center of the {@link PointsView}.
     * @param azimuth the azimuth in degrees.
     */
    public void setAzimuth(float azimuth) {
        // mAzimuthFrom can be <0° in some cases
        mAzimuthFrom = (azimuth - mHorizontalCameraAngle / 2);
        /*if (mAzimuthFrom < 0) {
            mAzimuthFrom += 360;
        }*/
        // mAzimuthTo can be >360° in some cases
        mAzimuthTo = (azimuth + mHorizontalCameraAngle / 2);// % 360;
        if (mPoints != null) {
            //mVisiblePoints = mPoints.subMap(mAzimuthFrom, mAzimuthTo);
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
        if (mPoints != null && !mPoints.isEmpty()) {
            for (SortedMap.Entry<Float, Point> entry : mPoints.entrySet()) {
                final int x = azimuthToXPixelCoordinate(entry.getKey());
                final int y = getHeight() / 2; // TODO: handle vertical placement of points
                if (x != 0) {
                    final Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_drop_down_24dp, null);
                    drawable.setBounds(x - ARROW_SIZE/2, y - ARROW_SIZE, x + ARROW_SIZE/2, y);
                    drawable.draw(canvas);
                    canvas.drawText(entry.getValue().getName(), x, y - ARROW_SIZE, mTextPaint);
                }
            }
        }
    }

    /**
     * Returns the the x coordinate value in pixels where the point should be horizontally placed on the {@link PointsView} depending on its azimuth.
     * Returns 0 if the point is located outside of the {@link PointsView}.
     * @param azimuth the azimuth of the point in degrees. It should be 0 < azimuth < 360.
     * @return the x coordinate in pixels or 0 if it is located outside of the view.
     */
    private int azimuthToXPixelCoordinate(float azimuth) {
        // Invalid azimuth
        if (azimuth < 0 || azimuth >= 360) {
            return 0;
        // Normal case : 0 < azimuthFrom < azimuth < azimuthTo < 360
        } else if (azimuth > mAzimuthFrom && azimuth < mAzimuthTo) {
            return (int) (mHorizontalPixelsPerDegree * (azimuth - mAzimuthFrom - (mAzimuthTo - mAzimuthFrom) / 2) + getWidth() / 2);
        // Special case 1 : azimuthFrom < 0 < azimuth < azimuthTo < 360
        } else if (mAzimuthFrom < 0 && azimuth > 360 + mAzimuthFrom) {
            return (int) (mHorizontalPixelsPerDegree * (azimuth - 360 - mAzimuthFrom - (mAzimuthTo - mAzimuthFrom) / 2) + getWidth() / 2);
        // Special case 2 : 0 < azimuthFrom < azimuth < 360 < azimuthTo
        } else if (mAzimuthTo > 360 && azimuth < mAzimuthTo - 360) {
            return (int) (mHorizontalPixelsPerDegree * (azimuth + 360 - mAzimuthFrom - (mAzimuthTo - mAzimuthFrom) / 2) + getWidth() / 2);
        } else {
            return 0;
        }
    }
}
