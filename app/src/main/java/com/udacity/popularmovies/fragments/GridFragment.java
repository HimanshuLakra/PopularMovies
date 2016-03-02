package com.udacity.popularmovies.fragments;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.udacity.popularmovies.ConnectionDetector;
import com.udacity.popularmovies.R;
import com.udacity.popularmovies.adapter.GridAdapter;
import com.udacity.popularmovies.adapter.GridCursorAdapter;
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

    boolean twoPaneUI;
    MovieModel firstRecievedItem;

    @Bind(R.id.toolbar_movies)
    Toolbar toolbar;
    @Bind(R.id.gridview)
    GridView gridview;
    @Bind(R.id.progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.no_internet_connection)
    RelativeLayout noInternet;
    @Bind(R.id.try_again)
    Button tryAgain;
    int flagSavedInstance = 0;

    GridCursorAdapter mCursorAdapter;
    private static final int CURSOR_LOADER_ID = 0;
    static boolean favouriteSelected = false;

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

        if (savedInstanceState == null || !savedInstanceState.containsKey("movieDataSet")) {
            flagSavedInstance = 0;
        } else {
            dataSet = Parcels.unwrap(savedInstanceState.getParcelable("movieDataSet"));
            flagSavedInstance = 1;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View gridFragmentView = inflater.inflate(R.layout.grid_view_fragment, container, false);

        ButterKnife.bind(this, gridFragmentView);
        AppCompatActivity activityCompat = (AppCompatActivity) getActivity();
        toolbar.setTitle("PopularMovies");
        activityCompat.setSupportActionBar(toolbar);

        setHasOptionsMenu(true);

        mCursorAdapter = new GridCursorAdapter(getActivity(), null, 0);

        Bundle recievedBundle = getArguments();
        twoPaneUI = recievedBundle.getBoolean("twoPaneExists");

        if (flagSavedInstance == 0 || savedInstanceState == null) {
            implementTask();
        } else {
            dataSet = Parcels.unwrap(savedInstanceState.getParcelable("movieDataSet"));
            if (dataSet != null) {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.GONE);
                gridview.setVisibility(View.VISIBLE);
                gridview.setAdapter(new GridAdapter(getActivity(), dataSet));
            } else {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                gridview.setVisibility(View.GONE);
            }
        }

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                DetailFragment detailFragment;

                if (!favouriteSelected) {

                    Bundle bundleData = new Bundle();
                    bundleData.putParcelable("parcelable_data", Parcels.wrap(new MovieModel(dataSet.get(position).poster_path,
                            dataSet.get(position).overview, dataSet.get(position).title,
                            dataSet.get(position).release_date, dataSet.get(position).vote_count,
                            dataSet.get(position).vote_average, dataSet.get(position).id)));

                    detailFragment = new DetailFragment();
                    detailFragment.setArguments(bundleData);


                } else {

                    long movieID = 0;

                    GridCursorAdapter cursorAdapter = (GridCursorAdapter) parent.getAdapter();
                    Cursor cursor = cursorAdapter.getCursor();

                    if (cursor != null && cursor.moveToPosition(position)) {
                        movieID = cursor.getLong(1);
                    }

                    Uri uri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI,
                            movieID);
                    detailFragment = DetailFragment.newInstance(movieID, uri);

                    favouriteSelected = false;

                }

                //For checking if device is tablet or not and change container accordingly
                if (twoPaneUI) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                            detailFragment).addToBackStack(null).commit();
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.grid_fragment_container,
                            detailFragment).addToBackStack(null).commit();
                }

            }
        });

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
        super.onSaveInstanceState(outState);
    }

    /*Check internet connection and
        * call for AsyncTask */
    public void implementTask() {

        if (ConnectionDetector.isAvailiable(getActivity())) {

            noInternet.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            dataSet = new ArrayList<>();
            RequestPoster(getString(R.string.sort_popular), getString(R.string.URLRear));

        } else {
            progressBar.setVisibility(View.GONE);
            gridview.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String Url;
        favouriteSelected = false;

        if (item.getItemId() == R.id.most_popular) {
            Url = getString(R.string.sort_popular);

            if (ConnectionDetector.isAvailiable(getActivity())) {

                noInternet.setVisibility(View.GONE);
                gridview.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                dataSet = new ArrayList<>();
                RequestPoster(Url, getString(R.string.URLRear));

            } else {

                progressBar.setVisibility(View.GONE);
                gridview.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Check your internet Connection", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.highest_rated) {
            Url = getString(R.string.sort_rated);

            if (ConnectionDetector.isAvailiable(getActivity())) {

                noInternet.setVisibility(View.GONE);
                gridview.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                dataSet = new ArrayList<>();
                RequestPoster(Url, getString(R.string.URLRear));

            } else {

                progressBar.setVisibility(View.GONE);
                gridview.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Check your internet Connection", Toast.LENGTH_SHORT).show();
            }
        } else {

            favouriteSelected = true;
            progressBar.setVisibility(View.GONE);
            noInternet.setVisibility(View.GONE);
            gridview.setVisibility(View.VISIBLE);
            gridview.setAdapter(mCursorAdapter);
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
        Call<ArrayList<MovieModel>> call = apiService.getMovieDetails(sortOrder, Key);

        call.enqueue(new Callback<ArrayList<MovieModel>>() {
            @Override
            public void onResponse(Call<ArrayList<MovieModel>> call, Response<ArrayList<MovieModel>> response) {

                if (response.isSuccess()) {

                    //assign recieved arraylist to dataset
                    dataSet = response.body();

                    firstRecievedItem = new MovieModel(dataSet.get(0).poster_path,
                            dataSet.get(0).overview, dataSet.get(0).title,
                            dataSet.get(0).release_date, dataSet.get(0).vote_count,
                            dataSet.get(0).vote_average, dataSet.get(0).id);

                    //handler to run code on ui thread
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressBar.setVisibility(View.GONE);
                            noInternet.setVisibility(View.GONE);
                            gridview.setVisibility(View.VISIBLE);

                            //set gridview adapter
                            gridview.setAdapter(new GridAdapter(getActivity(), dataSet));

                            if (twoPaneUI) {
                                Bundle bundleData = new Bundle();
                                bundleData.putParcelable("parcelable_data", Parcels.wrap(firstRecievedItem));

                                Fragment detail_fragment = new DetailFragment();
                                detail_fragment.setArguments(bundleData);

                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_detail_container,
                                        detail_fragment).addToBackStack(null).commit();
                            }

                        }
                    });
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

        mCursorAdapter.swapCursor(data);

    }

    @TargetApi(11)
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    @Override
    public void onResume() {
        super.onResume();
        //Starts a new or restarts an existing Loader in this manager
        getLoaderManager().restartLoader(0, null, this);
    }
}
