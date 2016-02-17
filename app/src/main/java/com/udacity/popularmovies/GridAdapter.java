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
    ArrayList<String> moviesURl;

    public GridAdapter(Context c, ArrayList<String> moviesURl) {
        mContext = c;
        this.moviesURl=moviesURl;
    }

    public int getCount() {
        return moviesURl.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    /* create a new ImageView for each item referenced by the Adapter
        if it's not recycled, initialize some attributes
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.grid_view_single_layout, parent, false);
            /*imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(150,150));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);*/

        }

        imageView = (ImageView) convertView.findViewById(R.id.gridImage);

        Glide.with(mContext)
                .load(moviesURl.get(position))
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.grid_placeholder)
                .into(imageView);

        return imageView;
    }


}

