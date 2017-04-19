package com.louisnard.augmentedreality.ui;

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
 * Created by louisnard on 19/04/2017.
 */

public class PointsView extends View {

    // Tag
    private static final String TAG = PointsView.class.getSimpleName();

    // Constants
    private static int ARROW_SIZE = 100;

    // Points
    private SortedMap<Float, Point> mPoints;
    private SortedMap<Float, Point> mVisiblePoints;
    private float mAzimuthFrom;
    private float mAzimuthTo;

    // Screen to camera angles ratios: the number of pixels on the screen associated to a one degree variation on the camera
    private float mHorizontalCameraAngle;
    private float mVerticalCameraAngle;
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

    public void setCameraAngles(float horizontalCameraAngle, float verticalCameraAngle) {
        // Camera angles
        mHorizontalCameraAngle = horizontalCameraAngle;
        mVerticalCameraAngle = verticalCameraAngle;
    }

    public void setPoints(SortedMap<Float, Point> points) {
        mPoints = points;
        mVisiblePoints = mPoints.subMap(mAzimuthFrom, mAzimuthTo);
        invalidate();
    }

    public void setAzimuth(float azimuth) {
        mAzimuthFrom = azimuth - mHorizontalCameraAngle / 2;
        mAzimuthTo = azimuth + mHorizontalCameraAngle / 2;
        if (mPoints != null) {
            mVisiblePoints = mPoints.subMap(mAzimuthFrom, mAzimuthTo);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Scaling: calculate the number of pixels on the screen associated to a 1° angle variation on the camera
        if((mHorizontalPixelsPerDegree == 0 || mVerticalCameraAngle == 0) && (mHorizontalCameraAngle != 0 && mVerticalCameraAngle != 0)) {
            mHorizontalPixelsPerDegree = getWidth() / mHorizontalCameraAngle;
            mVerticalPixelsPerDegree = getHeight() / mVerticalCameraAngle;
            if (BuildConfig.DEBUG)
                Log.d(TAG, "View size in pixels = " + getWidth() + "x" + getHeight());
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Screen pixels associated to 1° camera angle variation: horizontal=" + mHorizontalPixelsPerDegree + "px/° & vertical=" + mVerticalPixelsPerDegree + "px/°");
        }

        // Draw visible points on canvas
        if (mVisiblePoints != null && !mVisiblePoints.isEmpty()) {
            for (SortedMap.Entry<Float, Point> entry : mVisiblePoints.entrySet()) {
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

    // Return the x value in pixels where the point should be horizontally placed on the screen depending on its azimuth
    private int azimuthToXPixelCoordinate(float azimuth) {
        if (azimuth > mAzimuthFrom && azimuth < mAzimuthTo && mHorizontalPixelsPerDegree != 0) {
            return (int) (mHorizontalPixelsPerDegree * (azimuth - mAzimuthFrom - (mAzimuthTo - mAzimuthFrom) / 2) + getWidth() / 2);
        } else {
            return 0;
        }
    }
}
