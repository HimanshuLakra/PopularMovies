package com.udacity.popularmovies.fragments;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.udacity.popularmovies.ConnectionDetector;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.adapter.GridAdapter;
import com.udacity.popularmovies.api.ApiService;
import com.udacity.popularmovies.data.MoviesContract;
import com.udacity.popularmovies.model.ItemTypeAdapterFactory;
import com.udacity.popularmovies.model.MovieModel;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //arrayList for recieved movies details
    ArrayList<MovieModel> dataSet;
    ArrayList<MovieModel> dataSetDB;

    //call object for async call through retrofit
    Call<ArrayList<MovieModel>> call;

    boolean twoPaneUI;
    MovieModel firstRecievedItem;

    @Bind(R.id.grid_view_recycler)
    RecyclerView gridview;
    @Bind(R.id.progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.no_internet_connection)
    RelativeLayout noInternet;
    @Bind(R.id.try_again)
    Button tryAgain;
    @Bind(R.id.collection_empty)
    TextView favouriteCollection;

    ActionBar actionBar;
    GridLayoutManager gridLayoutManager;
    private static final int CURSOR_LOADER_ID = 0;
    boolean favouriteSelected = false;
    GridAdapter gridAdapter;

    String[] projections = {
            MoviesContract.MovieEntry.ID,
            MoviesContract.MovieEntry.COLUMN_IMAGE,
            MoviesContract.MovieEntry.COLUMN_DESCRIPTION,
            MoviesContract.MovieEntry.COLUMN_VOTE_COUNT,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            dataSet = new ArrayList<>();
            dataSetDB = new ArrayList<>();
        } else {
            dataSet = Parcels.unwrap(savedInstanceState.getParcelable("movieDataSet"));
            dataSetDB = Parcels.unwrap(savedInstanceState.getParcelable("movieDataSetDB"));

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View gridFragmentView = inflater.inflate(R.layout.grid_view_fragment, container, false);
        ButterKnife.bind(this, gridFragmentView);

        AppCompatActivity activityCompat = (AppCompatActivity) getActivity();
        actionBar = activityCompat.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Movie Hall");
        }
        setHasOptionsMenu(true);

        gridLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        gridview.setLayoutManager(gridLayoutManager);
        gridAdapter = new GridAdapter(getActivity(), dataSetDB, getActivity().getSupportFragmentManager());

        Bundle recievedBundle = getArguments();
        twoPaneUI = recievedBundle.getBoolean("twoPaneExists");

        if (dataSet.isEmpty() && !favouriteSelected) {
            implementTask();
        } else {
            if (favouriteSelected) {
                makeGridViewVisible();
                gridview.setAdapter(new GridAdapter(getActivity(), dataSetDB, getActivity().getSupportFragmentManager()));
            } else if (dataSet != null) {
                makeGridViewVisible();
                gridview.setAdapter(new GridAdapter(getActivity(), dataSet, getActivity().getSupportFragmentManager()));
            } else {
                noInternetFunctionality();
            }
        }
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                implementTask();
            }
        });
        return gridFragmentView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelable("movieDataSet", Parcels.wrap(dataSet));
        outState.putParcelable("movieDataSetDB", Parcels.wrap(dataSetDB));
        super.onSaveInstanceState(outState);
    }

    /*Check internet connection and
        * call for AsyncTask */
    public void implementTask() {

        if (ConnectionDetector.isAvailiable(getActivity())) {
            makeProgessBarVisible();
            progressBar.setIndeterminate(true);
            RequestPoster(getString(R.string.sort_popular), getString(R.string.URLRear));
        } else {
            noInternetFunctionality();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void noInternetFunctionality() {
        progressBar.setVisibility(View.GONE);
        gridview.setVisibility(View.GONE);
        noInternet.setVisibility(View.VISIBLE);

        if (twoPaneUI) {
            Fragment detail_fragment = new ClearFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                    detail_fragment).commit();
        }
    }

    public void makeGridViewVisible() {
        progressBar.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        gridview.setVisibility(View.VISIBLE);

        gridview.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        if (Build.VERSION.SDK_INT < 16) {
                            gridview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            gridview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }

                        int viewWidth = gridview.getMeasuredWidth();
                        float cardViewWidth = getActivity().getResources().getDimension(R.dimen.movie_layout_width);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        if (newSpanCount != 0) {
                            gridLayoutManager.setSpanCount(newSpanCount);
                            gridLayoutManager.requestLayout();
                        }
                    }
                });
    }

    public void makeProgessBarVisible() {
        noInternet.setVisibility(View.GONE);
        gridview.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String Url;
        favouriteSelected = false;

        if (item.getItemId() == R.id.most_popular) {
            Url = getString(R.string.sort_popular);
            favouriteCollection.setVisibility(View.GONE);
            actionBar.setTitle("Most Popular");

            if (ConnectionDetector.isAvailiable(getActivity())) {
                makeProgessBarVisible();
                progressBar.setIndeterminate(true);
                RequestPoster(Url, getString(R.string.URLRear));
            } else {
                noInternetFunctionality();
            }

            return true;
        } else if (item.getItemId() == R.id.highest_rated) {
            Url = getString(R.string.sort_rated);
            favouriteCollection.setVisibility(View.GONE);
            actionBar.setTitle("Highest Rated");

            if (ConnectionDetector.isAvailiable(getActivity())) {

                makeProgessBarVisible();
                progressBar.setIndeterminate(true);
                RequestPoster(Url, getString(R.string.URLRear));

            } else {
                noInternetFunctionality();
            }
            return true;

        } else if (item.getItemId() == R.id.favourite_movies) {
            actionBar.setTitle("Favourite");
            if (ConnectionDetector.isAvailiable(getActivity()) && call != null && call.isExecuted()) {
                call.cancel();
            }
            favouriteSelected = true;
            makeGridViewVisible();

            if (dataSetDB.isEmpty()) {
                gridview.setVisibility(View.GONE);
                favouriteCollection.setVisibility(View.VISIBLE);
                if (twoPaneUI) {
                    Fragment detail_fragment = new ClearFragment();
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                            detail_fragment).commit();
                }
            } else {
                favouriteCollection.setVisibility(View.GONE);
                gridAdapter.notifyDataSetChanged();
                gridview.setAdapter(gridAdapter);
                if (twoPaneUI) {

                    firstRecievedItem = new MovieModel(null,
                            dataSetDB.get(0).overview, dataSetDB.get(0).title,
                            dataSetDB.get(0).release_date, dataSetDB.get(0).vote_count,
                            dataSetDB.get(0).vote_average, dataSetDB.get(0).id, dataSetDB.get(0).imageBitmap);
                    Bundle bundleData = new Bundle();
                    bundleData.putParcelable("parcelable_data", Parcels.wrap(firstRecievedItem));

                    Fragment detail_fragment = new DetailFragment();
                    detail_fragment.setArguments(bundleData);

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                            detail_fragment).commit();
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        Cursor c =
                getActivity().getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI,
                        projections,
                        null,
                        null,
                        null);

        // initialize loader
        if (c != null && c.getCount() != 0) {
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    //function that requests for movie detail as per sort_order of movies
    public void RequestPoster(String sortOrder, String Key) {

        dataSet.clear();
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
        call = apiService.getMovieDetails(sortOrder, Key);

        call.enqueue(new Callback<ArrayList<MovieModel>>() {
            @Override
            public void onResponse(Call<ArrayList<MovieModel>> call, Response<ArrayList<MovieModel>> response) {

                if (response.isSuccess()) {

                    //assign recieved arraylist to dataset
                    dataSet = response.body();

                    if (dataSet.size() != 0) {
                        firstRecievedItem = new MovieModel(dataSet.get(0).poster_path,
                                dataSet.get(0).overview, dataSet.get(0).title,
                                dataSet.get(0).release_date, dataSet.get(0).vote_count,
                                dataSet.get(0).vote_average, dataSet.get(0).id, null);

                        //handler to run code on ui thread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    makeGridViewVisible();
                                    //set gridview adapter
                                    gridview.setAdapter(new GridAdapter(getActivity(), dataSet, getActivity().getSupportFragmentManager()));

                                    if (twoPaneUI) {
                                        Bundle bundleData = new Bundle();
                                        bundleData.putParcelable("parcelable_data", Parcels.wrap(firstRecievedItem));

                                        Fragment detail_fragment = new DetailFragment();
                                        detail_fragment.setArguments(bundleData);

                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                                                detail_fragment).commit();
                                    }

                                }
                            });
                        }
                    } else {
                        if (twoPaneUI) {
                            Fragment detail_fragment = new ClearFragment();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                                    detail_fragment).commit();
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<ArrayList<MovieModel>> call, Throwable t) {
                Toast.makeText(getActivity(), "Failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @TargetApi(11)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null) {

            dataSetDB.clear();
            int COLUMN_INDEX_MOVIE_ID = 1;
            int COLUMN_INDEX_TITLE = 2;
            int COLUMN_INDEX_DESCRIPTION = 3;
            int COLUMN_INDEX_RELEASE_DATE = 4;
            int COLUMN_INDEX_VOTE_COUNT = 5;
            int COLUMN_INDEX_TRAILER_URL = 6;
            int COLUMN_INDEX_VOTE_AVERAGE = 7;
            int COLUMN_INDEX_IMAGE = 8;

            while (data.moveToNext()) {

                MovieModel movieDatabse = new MovieModel(null, data.getString(COLUMN_INDEX_DESCRIPTION),
                        data.getString(COLUMN_INDEX_TITLE), data.getString(COLUMN_INDEX_RELEASE_DATE),
                        data.getInt(COLUMN_INDEX_VOTE_COUNT),
                        data.getDouble(COLUMN_INDEX_VOTE_AVERAGE),
                        data.getLong(COLUMN_INDEX_MOVIE_ID), getImage(data.getBlob(COLUMN_INDEX_IMAGE)));

                dataSetDB.add(movieDatabse);
            }

            data.close();
        }
    }

    @TargetApi(11)
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        dataSetDB = new ArrayList<>();
    }

    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Starts a new or restarts an existing Loader in this manager
        getLoaderManager().restartLoader(0, null, this);
    }
}

