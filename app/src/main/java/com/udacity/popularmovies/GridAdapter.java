package com.udacity.popularmovies;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GridAdapter extends BaseAdapter{

    private Context mContext;
    ArrayList<DataUtils> dataSet;

    public GridAdapter(Context c, ArrayList<DataUtils> dataSet) {
        mContext = c;
        this.dataSet=dataSet;
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
                .load(dataSet.get(position).ImageUrl)
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.grid_placeholder)
                .into(imageView);

        return imageView;
    }


}

