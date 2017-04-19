package com.louisnard.augmentedreality.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.louisnard.augmentedreality.model.objects.Point;

import java.util.SortedMap;

/**
 * Created by louisnard on 19/04/2017.
 */

public class PointsView extends View {

    // Points
    private SortedMap<Float, Point> mPoints;

    // Drawing
    private Paint mPaint;

    public PointsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(25);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
    }

    public void setPoints(SortedMap<Float, Point> points) {
        mPoints = points;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("View size" , getWidth() + "x" + getHeight());
        Log.d("Canvas size" , canvas.getWidth() + "x" + canvas.getHeight());

        canvas.drawText("TEST", getHeight() /2, getWidth() /2, mPaint);
    }
}
