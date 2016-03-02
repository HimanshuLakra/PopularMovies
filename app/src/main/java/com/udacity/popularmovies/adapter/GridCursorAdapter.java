package com.udacity.popularmovies.adapter;


import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.data.MoviesContract;

public class GridCursorAdapter extends CursorAdapter {

    private Context mContext;

    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.gridImage);
        }
    }

    @TargetApi(11)
    public GridCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.mContext=context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = R.layout.grid_view_single_layout;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int imageIndex = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_IMAGE);
        byte[] image = cursor.getBlob(imageIndex);


        viewHolder.imageView.setImageBitmap(getImage(image));
       /* Glide.with(mContext)
                .load(image)
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.grid_placeholder)
                .into(viewHolder.imageView);*/

    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
