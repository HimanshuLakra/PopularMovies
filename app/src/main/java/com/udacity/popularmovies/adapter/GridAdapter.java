package com.udacity.popularmovies.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.MovieModel;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    ArrayList<MovieModel> dataSet;

    public GridAdapter(Context c, ArrayList<MovieModel> dataSet) {
        mContext = c;
        this.dataSet = dataSet;
    }

    public int getCount() {
        return dataSet.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.grid_view_single_layout, parent, false);

        }

        imageView = (ImageView) convertView.findViewById(R.id.gridImage);

        Glide.with(mContext)
                .load("http://image.tmdb.org/t/p/w185/"+dataSet.get(position).poster_path)
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.grid_placeholder)
                .into(imageView);

        return imageView;
    }


}

