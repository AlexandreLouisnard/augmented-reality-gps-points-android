package com.louisnard.augmentedreality.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.louisnard.augmentedreality.BuildConfig;

public class PortView extends View {

    // Drawing
    private Paint mPaint;

    // Sizing
    // TODO: do not hardcode these values
    private float mHorizontalPixelsPerDegree = 14f;
    private float mVerticalPixelsPerDegree = 27f;

    public PortView(Context context, @Nullable AttributeSet attrs) {
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
        canvas.drawRect(10, 10, getMeasuredWidth()-10, getMeasuredHeight()-10, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mHorizontalPixelsPerDegree * 360, (int) mVerticalPixelsPerDegree * 180);
    }
}