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
 * Adapter class that exposes {@link Point} data to a {@link View}.
 *
 * @author Alexandre Louisnard
 */

public class PointsAdapter extends BaseAdapter {

    // Points
    private List<Point> mPoints;

    // Layout inflater
    private LayoutInflater mLayoutInflater;

    public PointsAdapter(Context context, List<Point> points) {
        mLayoutInflater = LayoutInflater.from(context);
        mPoints = points;
    }

    @Override
    public int getCount() {
        return mPoints.size();
    }

    @Override
    public Object getItem(int position) {
        return mPoints.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mPoints.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        return convertView;
    }

    private static class ViewHolder {
        TextView nameTextView;
    }
}
