package com.udacity.popularmovies.fragments;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.udacity.popularmovies.ConnectionDetector;
import com.udacity.popularmovies.MainActivity;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.adapter.AdapterReviews;
import com.udacity.popularmovies.adapter.AdapterTrailer;
import com.udacity.popularmovies.api.ApiService;
import com.udacity.popularmovies.data.MoviesContract;
import com.udacity.popularmovies.model.ItemTypeAdapterFactory;
import com.udacity.popularmovies.model.MovieModel;
import com.udacity.popularmovies.model.ReviewModel;
import com.udacity.popularmovies.model.TrailerModel;

import org.parceler.Parcels;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class DetailFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.votes)
    TextView vote_count;
    @Bind(R.id.favourite)
    TextView popularity;
    @Bind(R.id.release_date)
    TextView date;
    @Bind(R.id.description)
    TextView description;
    @Bind(R.id.movie_image)
    ImageView image;
    @Bind(R.id.trailer_recycler)
    RecyclerView trailerRecycler;
    @Bind(R.id.reviews_recycler)
    RecyclerView reviewRecycler;
    @Bind(R.id.review_heading)
    LinearLayout reviewHeading;
    @Bind(R.id.trailer_heading)
    LinearLayout trailerHeading;

    ArrayList<TrailerModel> dataSetTrailers;
    ArrayList<ReviewModel> dataSetReviews;

    String firstTrailer = "Sorry! Not able to fetch trailer URL";
    static long movie_id_recieved;

    MovieModel receivedUtil;
    @Bind(R.id.fab_favourite)
    FloatingActionButton fab_favourite;
    @Bind(R.id.detail_parent)
    CoordinatorLayout parentLayout;
    byte[] byteArray;
    static boolean alreadyInDB = false;
    ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View detailView = inflater.inflate(R.layout.detail_view_fragment, container, false);
        ButterKnife.bind(this, detailView);

        AppCompatActivity activityCompat = (AppCompatActivity) getActivity();
        actionBar = activityCompat.getSupportActionBar();
        // toolbar.setNavigationIcon(R.drawable.ic_backspace);
        /*activityCompat.setSupportActionBar(toolbar);*/
        //collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.collapsingBar);

        if (savedInstanceState == null) {
            Bundle recievedBundle = getArguments();
            receivedUtil = Parcels.unwrap(recievedBundle.getParcelable("parcelable_data"));
            callTrailerReviewFunction();
        } else {
            receivedUtil = Parcels.unwrap(savedInstanceState.getParcelable("movieDetails"));
            dataSetTrailers = Parcels.unwrap(savedInstanceState.getParcelable("movieTrailerSet"));
            dataSetReviews = Parcels.unwrap(savedInstanceState.getParcelable("movieReviewSet"));

            if (dataSetReviews != null && dataSetTrailers != null) {
                reviewRecycler.setAdapter(new AdapterReviews(dataSetReviews));
                trailerRecycler.setAdapter(new AdapterTrailer(getActivity(), dataSetTrailers));
            } else if (dataSetTrailers != null && dataSetReviews == null) {
                trailerRecycler.setAdapter(new AdapterTrailer(getActivity(), dataSetTrailers));
            } else if (dataSetReviews != null) {
                reviewRecycler.setAdapter(new AdapterReviews(dataSetReviews));
            }
        }

        date.setText(receivedUtil.release_date);
        description.setText(receivedUtil.overview);
        vote_count.setText(Integer.toString(receivedUtil.vote_count));
        popularity.setText(Double.toString(receivedUtil.vote_average));
        movie_id_recieved = receivedUtil.id;
        if (activityCompat.getSupportActionBar() != null)
            activityCompat.getSupportActionBar().setTitle(receivedUtil.title);

        if (receivedUtil.poster_path == null) {

            alreadyInDB = true;
            fab_favourite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.toolbar_fav)));
            image.setImageBitmap(receivedUtil.imageBitmap);
            trailerHeading.setVisibility(View.GONE);
            reviewHeading.setVisibility(View.GONE);

        } else {
            fab_favourite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.toolbar_color)));
            trailerHeading.setVisibility(View.VISIBLE);
            reviewHeading.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load("http://image.tmdb.org/t/p/w342/" + receivedUtil.poster_path)
                    .centerCrop()
                    .crossFade()
                    .placeholder(R.drawable.placeholder_movies)
                    .into(image);

            LinearLayoutManager layoutManagerreview = new org.solovyev.android.views.llm.LinearLayoutManager
                    (getActivity(), LinearLayoutManager.VERTICAL, false);

            reviewRecycler.setLayoutManager(layoutManagerreview);

            LinearLayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager
                    (getActivity(), LinearLayoutManager.HORIZONTAL, false);
            trailerRecycler.setLayoutManager(layoutManager);

            trailerRecycler.setHasFixedSize(true);
            reviewRecycler.setHasFixedSize(true);

        }

        if (!((MainActivity) getActivity()).getTwoPaneUI()) {
            activityCompat.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activityCompat.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        fab_favourite.setOnClickListener(this);
        setHasOptionsMenu(true);
        return detailView;
    }

    public void callTrailerReviewFunction() {

        dataSetReviews = new ArrayList<>();
        dataSetTrailers = new ArrayList<>();
        if (ConnectionDetector.isAvailiable(getActivity())) {
            RequestReviews(Long.toString(receivedUtil.id), getString(R.string.URLRear));
            RequestTrailer(Long.toString(receivedUtil.id), getString(R.string.URLRear));
        } else {
            final Snackbar snackBar = Snackbar.make(parentLayout, "Please check your internet Connection",
                    Snackbar.LENGTH_SHORT);
            snackBar.show();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_favourite) {
            queryForMovie();
        }
    }

    public void queryForMovie() {

        Uri uri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI,
                movie_id_recieved);

        Cursor c = getActivity().getContentResolver().query(uri,
                null,
                null,
                null,
                null);

        if (c != null && c.getCount() != 0) {
            alreadyInDB = true;

            c.moveToFirst();
            if (c.getString(6) != null)
                firstTrailer = c.getString(6);

            final Snackbar snackBar = Snackbar.make(parentLayout, "Mark it as unfavourite",
                    Snackbar.LENGTH_INDEFINITE);

            snackBar.setAction("DELETE", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI,
                            movie_id_recieved);

                    int mRowsDeleted = 0;
                    mRowsDeleted = getActivity().getContentResolver().delete(
                            uri,
                            null,
                            null);

                    if (mRowsDeleted >= 0) {
                        alreadyInDB = false;
                        Toast.makeText(getActivity(), "This movie has been deleted", Toast.LENGTH_SHORT).show();

                        if (getActivity().getSupportFragmentManager() != null) {

                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            Fragment fragment = new GridFragment();
                            Bundle uiBundle = new Bundle();
                            uiBundle.putBoolean("twoPaneExists", ((MainActivity) getActivity()).getTwoPaneUI());
                            fragment.setArguments(uiBundle);
                            fragmentTransaction.replace(R.id.grid_fragment_container, fragment).commit();

                        }

                    }
                    Log.e("deleted number", Integer.toString(mRowsDeleted));
                    snackBar.dismiss();
                }
            });
            snackBar.show();

            c.close();
        } else {
            alreadyInDB = false;
            if (receivedUtil.poster_path != null && ConnectionDetector.isAvailiable(getActivity()))
                new FetchEventImage().execute("http://image.tmdb.org/t/p/w185/" + receivedUtil.poster_path);
            else {
                Snackbar.make(parentLayout, "Unable to fetch details for this movie", Snackbar.LENGTH_LONG).show();
            }
        }

    }

    private class FetchEventImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            return (getEventImageBitmap(params[0]));
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            byteArray = getBytes(bitmap);

            ContentValues movieDetails = new ContentValues();

            movieDetails.put(MoviesContract.MovieEntry.COLUMN_TITLE, receivedUtil.title);
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, receivedUtil.release_date);
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_DESCRIPTION, receivedUtil.overview);
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_IMAGE, byteArray);
            movieDetails.put(MoviesContract.MovieEntry.MOVIE_ID, receivedUtil.id);
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, receivedUtil.vote_average);
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, receivedUtil.vote_count);

            if (firstTrailer.contentEquals("Sorry! Not able to fetch trailer URL")) {
                Toast.makeText(getActivity(), "Please try after few seconds.We are trying to fetch trailer url from server.", Toast.LENGTH_SHORT).show();
            } else {
                movieDetails.put(MoviesContract.MovieEntry.COLUMN_TRAILER_URL, firstTrailer);

                // singleInsert our ContentValues array
                Uri muri = getActivity().getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI,
                        movieDetails);

                long Idrecieved = ContentUris.parseId(muri);

                if (Idrecieved > 0) {
                    alreadyInDB = true;
                    fab_favourite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.toolbar_fav)));
                    Snackbar.make(parentLayout, R.string.snackbar_movie_added,
                            Snackbar.LENGTH_SHORT).show();
                }

            }

        }
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    private Bitmap getEventImageBitmap(String eventImageUrl) {

        Log.e("url recieved", eventImageUrl);
        Bitmap userbmp = null;
        try {
            URL actualUrl = new URL(eventImageUrl);
            HttpURLConnection conn = (HttpURLConnection) actualUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            userbmp = BitmapFactory.decodeStream(is);
            conn.disconnect();

        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
        }
        return userbmp;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.share_trailer) {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, firstTrailer);
                startActivity(Intent.createChooser(i, "Share URL"));

            } catch (ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "Sharing Failed", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("Movie Hall");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void RequestTrailer(String movieId, String Key) {

        //ApiService(Rest api call function) interface object
        ApiService apiService;

        //Gson object for "results" JSONArray
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ItemTypeAdapterFactory()).create();

        //Retrofit object as per 2.0 version library
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.URLInitial))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);

        //Asynchronuos call to REST moviedb.org API
        Call<ArrayList<TrailerModel>> call = apiService.getMovieTrailers(movieId, Key);

        call.enqueue(new Callback<ArrayList<TrailerModel>>() {
            @Override
            public void onResponse(Call<ArrayList<TrailerModel>> call, Response<ArrayList<TrailerModel>> response) {

                if (response.isSuccess()) {

                    //assign recieved arraylist to dataset
                    ArrayList<TrailerModel> recievedSet = response.body();

                    int size = recievedSet.size();

                    for (int i = 0; i < size; i++) {
                        if (recievedSet.get(i).type.contentEquals("Trailer")) {
                            dataSetTrailers.add(recievedSet.get(i));
                            if (i == 0) {
                                firstTrailer = "http://www.youtube.com/watch?v=" + recievedSet.get(i).key;
                            }
                        }
                    }

                    if (getActivity() != null && trailerRecycler != null) {
                        //handler to run code on ui thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //set recyclerview adapter
                                trailerRecycler.setAdapter(new AdapterTrailer(getActivity(), dataSetTrailers));

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<TrailerModel>> call, Throwable t) {
                Toast.makeText(getActivity(), "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void RequestReviews(String movieId, String Key) {

        //ApiService(Rest api call function) interface object
        ApiService apiService;

        //Gson object for "results" JSONArray
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ItemTypeAdapterFactory()).create();

        //Retrofit object as per 2.0 version library
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.URLInitial))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);

        //Asynchronuos call to REST moviedb.org API
        Call<ArrayList<ReviewModel>> call = apiService.getMovieReviews(movieId, Key);
        call.enqueue(new Callback<ArrayList<ReviewModel>>() {
            @Override
            public void onResponse(Call<ArrayList<ReviewModel>> call, Response<ArrayList<ReviewModel>> response) {

                if (response.isSuccess()) {

                    //assign recieved arraylist to dataset
                    dataSetReviews = response.body();

                    if (dataSetReviews.isEmpty()) {
                        dataSetReviews.add(new ReviewModel("Sorry", "No reviews Found"));
                    }

                    if (getActivity() != null && reviewRecycler != null) {
                        //handler to run code on ui thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //set recyclerview adapter
                                reviewRecycler.setAdapter(new AdapterReviews(dataSetReviews));
                            }
                        });
                    }

                }
            }

            @Override
            public void onFailure(Call<ArrayList<ReviewModel>> call, Throwable t) {
                Toast.makeText(getActivity(), "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelable("movieReviewSet", Parcels.wrap(dataSetReviews));
        outState.putParcelable("movieTrailerSet", Parcels.wrap(dataSetTrailers));
        outState.putParcelable("movieDetails", Parcels.wrap(receivedUtil));
        super.onSaveInstanceState(outState);
    }
}
