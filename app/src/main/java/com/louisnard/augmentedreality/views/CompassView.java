package com.louisnard.augmentedreality.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

/**
 * Custom {@link View} drawing a compass.<br>
 *
 * @author Alexandre Louisnard
 */

public class CompassView extends View {

    // Compass
    private float mAzimuthDegrees = 0;

    // Drawing
    private Paint mPaint;
    private Bitmap mCachedBitmap;
    private int mX;
    private int mY;
    private float mRadius;

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(25);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw compass structure on a cached bitmap
        if (mCachedBitmap == null) {
            // Prepare cached bitmap & canvas
            mCachedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888); //Change to lower bitmap config if possible.
            Canvas cachedCanvas = new Canvas(mCachedBitmap);

            // Sizing
            mX = getMeasuredWidth() / 2;
            mY = getMeasuredHeight() / 2;
            mRadius = (float) (Math.max(mX, mY) * 0.6);

            // Draw compass structure on the cached canvas
            cachedCanvas.drawCircle(mX, mY, mRadius, mPaint);
            cachedCanvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        }

        // Set cached bitmap on the canvas
        canvas.drawBitmap(mCachedBitmap, 0, 0, mPaint);

        // Draw compass line
        canvas.drawLine(mX, mY,
                (float) (mX + mRadius * Math.sin((double) (-mAzimuthDegrees) / 180 * Math.PI)),
                (float) (mY - mRadius * Math.cos((double) (-mAzimuthDegrees) / 180 * Math.PI)),
                mPaint);

        // Set text
        canvas.drawText(String.format(Locale.getDefault(), "%.0f °", mAzimuthDegrees), mX, mY, mPaint);
    }

    /**
     * Updates the azimuth of the {@link CompassView}.
     * @param azimuthDegrees the azimuth in degrees.
     */
    public void updateAzimuth(float azimuthDegrees) {
        mAzimuthDegrees = azimuthDegrees;
        invalidate();
    }
}
