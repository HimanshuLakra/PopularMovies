package com.udacity.popularmovies.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.udacity.popularmovies.MainActivity;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.fragments.DetailFragment;
import com.udacity.popularmovies.model.MovieModel;

import org.parceler.Parcels;

import java.util.ArrayList;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.GridViewHolder> {

    private Context mContext;
    ArrayList<MovieModel> dataSet;
    FragmentManager fragmentManager;

    public GridAdapter(Context c, ArrayList<MovieModel> dataSet, FragmentManager fragmentManager) {
        mContext = c;
        this.dataSet = dataSet;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.grid_view_single_layout
                , parent, false);

        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GridViewHolder holder, int position) {

        holder.textView.setText(dataSet.get(position).title);
        if (dataSet.get(position).poster_path != null) {

            Glide.with(mContext)
                    .load("http://image.tmdb.org/t/p/w342/" + dataSet.get(position).poster_path)
                    .asBitmap().placeholder(R.drawable.grid_placeholder)
                    .into(new BitmapImageViewTarget(holder.imageView) {

                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            super.onResourceReady(bitmap, anim);
                            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {

                                @Override
                                public void onGenerated(Palette palette) {
                                    // Here's your generated palette
                                    Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                                    if (swatch != null) {
                                        holder.pal_background.setBackgroundColor
                                                (swatch.getRgb());
                                        holder.textView.setTextColor
                                                (swatch.getTitleTextColor());
                                    }
                                }
                            });
                        }

                    });
        } else {

            if (dataSet.get(position).imageBitmap != null) {
                holder.imageView.setImageBitmap(dataSet.get(position).imageBitmap);

                Palette.generateAsync(dataSet.get(position).imageBitmap, new Palette.PaletteAsyncListener() {

                    @Override
                    public void onGenerated(Palette palette) {
                        // Here's your generated palette
                        Palette.Swatch swatch = palette.getLightVibrantSwatch();
                        if (swatch != null) {
                            holder.pal_background.setBackgroundColor
                                    (palette.getDarkVibrantColor(swatch.getBodyTextColor()));
                            holder.textView.setTextColor
                                    (palette.getLightMutedColor(swatch.getTitleTextColor()));
                        }
                    }
                });
            }

        }

        holder.parent_single_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DetailFragment detailFragment;
                int pos = holder.getAdapterPosition();
                Bundle bundleData = new Bundle();

                if (dataSet.get(pos).poster_path != null) {
                    bundleData.putParcelable("parcelable_data", Parcels.wrap(new MovieModel(dataSet.get(pos).poster_path,
                            dataSet.get(pos).overview, dataSet.get(pos).title,
                            dataSet.get(pos).release_date, dataSet.get(pos).vote_count,
                            dataSet.get(pos).vote_average, dataSet.get(pos).id, null)));
                } else {
                    bundleData.putParcelable("parcelable_data", Parcels.wrap(new MovieModel(null,
                            dataSet.get(pos).overview, dataSet.get(pos).title,
                            dataSet.get(pos).release_date, dataSet.get(pos).vote_count,
                            dataSet.get(pos).vote_average, dataSet.get(pos).id, dataSet.get(pos).imageBitmap)));
                }

                detailFragment = new DetailFragment();
                detailFragment.setArguments(bundleData);

                if (((MainActivity) mContext).getTwoPaneUI()) {
                    fragmentManager.beginTransaction().replace(R.id.fragment_detail_container,
                            detailFragment).commit();
                } else {
                    fragmentManager.beginTransaction().replace(R.id.grid_fragment_container,
                            detailFragment).addToBackStack(null).commit();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;
        LinearLayout pal_background, parent_single_layout;

        public GridViewHolder(View v) {
            super(v);

            imageView = (ImageView) v.findViewById(R.id.gridImage);
            textView = (TextView) v.findViewById(R.id.title_movie);
            pal_background = (LinearLayout) v.findViewById(R.id.palette_background);
            parent_single_layout = (LinearLayout) v.findViewById(R.id.parent_single_layout);
        }

    }
}

