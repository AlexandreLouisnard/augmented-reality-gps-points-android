package com.louisnard.augmentedreality;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.louisnard.augmentedreality.model.objects.Point;

import java.util.List;

/**
 * Created by louisnard on 14/04/2017.
 */

public class PointsAdapter extends BaseAdapter {

    // Points
    private List<Point> mPoints;
    private Point mOriginPoint;
    private float mAzimuthMin;
    private float mAzimuthMax;

    // Layout inflater
    private LayoutInflater mLayoutInflater;

    public PointsAdapter(Context context, List<Point> points, Point originPoint, float azimuthMin, float azimuthMax) {
        mLayoutInflater = LayoutInflater.from(context);
        mPoints = points;
        mOriginPoint = originPoint;
        mAzimuthMin = azimuthMin;
        mAzimuthMax = azimuthMax;
    }

    @Override
    public int getCount() {
        return mPoints.size();
    }

    @Override
    public Object getItem(int position) {
        return mPoints;
    }

    @Override
    public long getItemId(int position) {
        return mPoints.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //if (mOriginPoint.azimuthTo(mPoints.get(position)) > mAzimuthMin && mOriginPoint.azimuthTo(mPoints.get(position)) < mAzimuthMax) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_point, parent, false);
                holder = new ViewHolder();
                holder.nameTextView = (TextView) convertView.findViewById(android.R.id.text1);
                holder.nameTextView.setText(mPoints.get(position).getName());
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //convertView.setTranslationX(mOriginPoint.azimuthTo(mPoints.get(position)));
            return convertView;
    }

    static class ViewHolder {
        TextView nameTextView;
    }
}
