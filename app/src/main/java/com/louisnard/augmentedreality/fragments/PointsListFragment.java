package com.louisnard.augmentedreality.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.louisnard.augmentedreality.R;
import com.louisnard.augmentedreality.model.database.DbHelper;
import com.louisnard.augmentedreality.model.objects.Point;

import java.util.List;

/**
 * Points list {@link Fragment} showing the list of {@link Point}s in the database.<br>
 *
 * @author Alexandre Louisnard
 */
public class PointsListFragment extends Fragment {

    // Tag
    private static final String TAG = PointsListFragment.class.getSimpleName();

    // Views
    private RecyclerView mRecyclerView;

    // Adapter
    private PointsAdapter mAdapter;

    // Points
    private List<Point> mPointsList;

    // TODO: test this points list fragment and improve it

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPointsList = DbHelper.getInstance(getContext()).getAllPoints();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_view_with_empty_state, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Views
        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        mRecyclerView.setHasFixedSize(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set adapter
        mAdapter = new PointsAdapter(mPointsList);
        mRecyclerView.setAdapter(mAdapter);
    }


    /**
     * {@link RecyclerView.Adapter} that exposes {@link Point}s data to a {@link RecyclerView} through a {@link PointViewHolder}.
     */
    private class PointsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Point> mPointsList;

        public PointsAdapter(List<Point> pointsList) {
            mPointsList = pointsList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.point_list_item, parent, false);
            return new PointViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((PointViewHolder) holder).mNameTextView.setText(mPointsList.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return mPointsList.size();
        }
    }

    /**
     * Point {@link RecyclerView.ViewHolder}.
     */
    private class PointViewHolder extends RecyclerView.ViewHolder {

        public TextView mNameTextView;

        public PointViewHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.point_name_text_view);
        }
    }

}
