package com.udacity.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    TextView vote_count, popularity, date, description;
    ImageView image;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.movie_detail);

        vote_count = (TextView) findViewById(R.id.votes);
        popularity = (TextView) findViewById(R.id.favourite);
        description = (TextView) findViewById(R.id.description);
        date = (TextView) findViewById(R.id.release_date);
        image = (ImageView) findViewById(R.id.movie_image);
        toolbar = (Toolbar) findViewById(R.id.toolbar_movies_detail);

        Intent receivedIntent = getIntent();

        toolbar.setNavigationIcon(R.drawable.ic_backspace);
        setSupportActionBar(toolbar);


        date.setText(receivedIntent.getStringExtra("RELEASE_DATE"));
        description.setText(receivedIntent.getStringExtra("DESCRIPTION"));
        vote_count.setText(Integer.toString(receivedIntent.getIntExtra("VOTES", 0)));
        popularity.setText(Double.toString(receivedIntent.getDoubleExtra("POPULARITY", 0)));

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_bar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.collapsingBar);
        collapsingToolbarLayout.setTitle(receivedIntent.getStringExtra("TITLE"));
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.toolbar_color));


        Glide.with(this)
                .load(receivedIntent.getStringExtra("IMAGE_URL"))
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.placeholder_movies)
                .into(image);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
