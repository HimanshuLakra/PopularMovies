package com.udacity.popularmovies.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.popularmovies.R;
import com.udacity.popularmovies.model.ReviewModel;

import java.util.ArrayList;

public class AdapterReviews extends RecyclerView.Adapter<AdapterReviews.ReviewViewHolder> {

    ArrayList<ReviewModel> receivedData;

    public AdapterReviews(ArrayList<ReviewModel> dataSet) {
        receivedData = dataSet;
    }

    @Override
    public int getItemCount() {
        return receivedData.size();
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {

        holder.author.setText(receivedData.get(position).author);
        String content = receivedData.get(position).content;
        String setContent = "\"" + content + "\"";
        holder.content.setText(setContent);
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.review_single_layout, parent, false);

        return new ReviewViewHolder(itemView);
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        protected TextView content;
        protected TextView author;

        public ReviewViewHolder(View v) {
            super(v);
            content=(TextView) v.findViewById(R.id.review_content);
            author=(TextView) v.findViewById(R.id.review_author);
        }

    }
}
