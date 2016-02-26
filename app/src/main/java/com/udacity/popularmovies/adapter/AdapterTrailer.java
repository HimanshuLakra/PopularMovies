package com.udacity.popularmovies.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.TrailerModel;

import java.util.ArrayList;

public class AdapterTrailer extends RecyclerView.Adapter<AdapterTrailer.TrailerViewHolder> {

    ArrayList<TrailerModel> receivedData;
    Context context;

    public AdapterTrailer(Context context, ArrayList<TrailerModel> dataSet) {
        receivedData = dataSet;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return receivedData.size();
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {

        final int pos = position;

        Glide.with(context)
                .load("http://img.youtube.com/vi/" + receivedData.get(pos).key + "/0.jpg")
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.placeholder_movies)
                .into(holder.frame_thumbnail);

        holder.frame_container.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + receivedData.get(pos).key));
                    context.startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + receivedData.get(pos).key));
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.trailer_single_layout, parent, false);

        return new TrailerViewHolder(itemView);
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {

        protected FrameLayout frame_container;
        protected ImageView frame_thumbnail;

        public TrailerViewHolder(View v) {
            super(v);

            frame_container = (FrameLayout) v.findViewById(R.id.frame_trailer);
            frame_thumbnail=(ImageView) v.findViewById(R.id.thumbnail_trailer);

        }
    }

}
