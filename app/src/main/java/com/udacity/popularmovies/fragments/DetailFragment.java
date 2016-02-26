package com.udacity.popularmovies.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.adapter.AdapterReviews;
import com.udacity.popularmovies.adapter.AdapterTrailer;
import com.udacity.popularmovies.api.ApiService;
import com.udacity.popularmovies.model.ItemTypeAdapterFactory;
import com.udacity.popularmovies.model.MovieModel;
import com.udacity.popularmovies.model.ReviewModel;
import com.udacity.popularmovies.model.TrailerModel;

import org.parceler.Parcels;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class DetailFragment extends Fragment {

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
    @Bind(R.id.toolbar_movies_detail)
    Toolbar toolbar;
    @Bind(R.id.collapsing_bar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.trailer_recycler)
    RecyclerView trailerRecycler;
    @Bind(R.id.reviews_recycler)
    RecyclerView reviewRecycler;

    ArrayList<TrailerModel> dataSetTrailers;
    ArrayList<ReviewModel> dataSetReviews;

    String firstTrailer;

    long id_recieved;
    MovieModel receivedUtil;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View detailView = inflater.inflate(R.layout.detail_view_fragment, container, false);
        ButterKnife.bind(this, detailView);

        Bundle recievedBundle = getArguments();

        receivedUtil = Parcels.unwrap(recievedBundle.getParcelable("parcelable_data"));

        AppCompatActivity activityCompat = (AppCompatActivity) getActivity();

        toolbar.setNavigationIcon(R.drawable.ic_backspace);
        activityCompat.setSupportActionBar(toolbar);

        date.setText(receivedUtil.release_date);
        description.setText(receivedUtil.overview);
        vote_count.setText(Integer.toString(receivedUtil.vote_count));
        popularity.setText(Double.toString(receivedUtil.vote_average));

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.collapsingBar);
        collapsingToolbarLayout.setTitle(receivedUtil.title);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.toolbar_color));

        Glide.with(this)
                .load("http://image.tmdb.org/t/p/w185/" + receivedUtil.poster_path)
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

        dataSetReviews = new ArrayList<>();
        dataSetTrailers = new ArrayList<>();
        RequestReviews(Long.toString(receivedUtil.id), getString(R.string.URLRear));
        RequestTrailer(Long.toString(receivedUtil.id), getString(R.string.URLRear));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        setHasOptionsMenu(true);
        return detailView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);

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

            @Override
            public void onFailure(Call<ArrayList<ReviewModel>> call, Throwable t) {
                Toast.makeText(getActivity(), "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
