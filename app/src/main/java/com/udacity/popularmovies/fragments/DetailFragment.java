package com.udacity.popularmovies.fragments;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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


public class DetailFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

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
    @Bind(R.id.review_heading)
    LinearLayout reviewHeading;
    @Bind(R.id.trailer_heading)
    LinearLayout trailerHeading;

    ArrayList<TrailerModel> dataSetTrailers;
    ArrayList<ReviewModel> dataSetReviews;

    String firstTrailer;

    static DialogUnMark dialog;

    static long movie_id_recieved;

    MovieModel receivedUtil;
    @Bind(R.id.fab_favourite)
    FloatingActionButton fab_favourite;

    byte[] byteArray;

    private Cursor mDetailCursor;
    private long mPosition;
    private Uri mUri;
    private static final int CURSOR_LOADER_ID = 0;
    static boolean alreadyInDB = false;

    static int pos;

    int COLUMN_INDEX_MOVIE_ID = 1;
    int COLUMN_INDEX_TITLE = 2;
    int COLUMN_INDEX_DESCRIPTION = 3;
    int COLUMN_INDEX_RELEASE_DATE = 4;
    int COLUMN_INDEX_VOTE_COUNT = 5;
    int COLUMN_INDEX_TRAILER_URL = 6;
    int COLUMN_INDEX_VOTE_AVERAGE = 7;
    int COLUMN_INDEX_IMAGE = 8;

    public static DetailFragment newInstance(long position, Uri uri) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        fragment.mPosition = position;
        fragment.mUri = uri;
        args.putLong("id", position);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View detailView = inflater.inflate(R.layout.detail_view_fragment, container, false);
        ButterKnife.bind(this, detailView);

        AppCompatActivity activityCompat = (AppCompatActivity) getActivity();

        // toolbar.setNavigationIcon(R.drawable.ic_backspace);
        activityCompat.setSupportActionBar(toolbar);

        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.collapsingBar);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.toolbar_color));

        Bundle recievedBundle = getArguments();
        if (recievedBundle != null && recievedBundle.containsKey("id")) {
            alreadyInDB = true;
            pos = recievedBundle.getInt("id");
            //fab_favourite.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            getLoaderManager().initLoader(CURSOR_LOADER_ID, recievedBundle, DetailFragment.this);

        } else {

            trailerHeading.setVisibility(View.VISIBLE);
            reviewHeading.setVisibility(View.VISIBLE);
            receivedUtil = Parcels.unwrap(recievedBundle.getParcelable("parcelable_data"));
            date.setText(receivedUtil.release_date);
            description.setText(receivedUtil.overview);
            vote_count.setText(Integer.toString(receivedUtil.vote_count));
            popularity.setText(Double.toString(receivedUtil.vote_average));

            movie_id_recieved = receivedUtil.id;

            collapsingToolbarLayout.setTitle(receivedUtil.title);

            queryForMovie();
            Glide.with(this)
                    .load("http://image.tmdb.org/t/p/w185/" + receivedUtil.poster_path)
                    .centerCrop()
                    .crossFade()
                    .placeholder(R.drawable.placeholder_movies)
                    .into(image);

            if (ConnectionDetector.isAvailiable(getActivity())) {

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
            } else {

                Toast.makeText(getActivity(), "Please check your internet Connection", Toast.LENGTH_SHORT).show();
            }

        }


        fab_favourite.setOnClickListener(this);
        setHasOptionsMenu(true);
        return detailView;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_favourite) {

            if (!alreadyInDB) {
                new FetchEventImage().execute("http://image.tmdb.org/t/p/w185/" + receivedUtil.poster_path);
                alreadyInDB = true;
            } else {
                dialog = new DialogUnMark();
                dialog.show(getChildFragmentManager(),
                        "dialog_unmark");
            }
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
            c.close();
        }

    }

    public static class DialogUnMark extends DialogFragment {

        public DialogUnMark() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View addProfileLayout = inflater.inflate(R.layout.dialog_unfavourite, null);

            builder.setView(addProfileLayout);

            TextView accepted = (TextView) addProfileLayout.findViewById(R.id.accept);

            accepted.setOnClickListener(new View.OnClickListener() {
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
                        dialog.dismiss();
                        Toast.makeText(getActivity(), "This movie has been deleted", Toast.LENGTH_SHORT).show();
                    }
                    Log.e("deleted number", Integer.toString(mRowsDeleted));
                }
            });

            TextView cancel = (TextView) addProfileLayout.findViewById(R.id.discard);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            return builder.create();
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
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_TRAILER_URL, firstTrailer);
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, receivedUtil.vote_average);
            movieDetails.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, receivedUtil.vote_count);

            // singleInsert our ContentValues array
            Uri muri = getActivity().getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI,
                    movieDetails);

            long Idrecieved = ContentUris.parseId(muri);

            if (Idrecieved > 0)
                Toast.makeText(getActivity(), "This movie has been added to your favourite collection", Toast.LENGTH_SHORT).show();


        }
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
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
            //BufferedReader reader =new BufferedReader(new InputStreamReader(is, "UTF-8"));

            userbmp = BitmapFactory.decodeStream(is);
            conn.disconnect();

        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
        }


        return userbmp;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;

        if (args != null) {
            selection = MoviesContract.MovieEntry.MOVIE_ID;
            selectionArgs = new String[]{String.valueOf(mPosition)};
        }
        return new CursorLoader(getActivity(),
                mUri,
                null,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            mDetailCursor = data;
            mDetailCursor.moveToFirst();
            DatabaseUtils.dumpCursor(data);
            //set all details
            description.setText(data.getString(COLUMN_INDEX_DESCRIPTION));
            collapsingToolbarLayout.setTitle(data.getString(COLUMN_INDEX_TITLE));
            vote_count.setText(Integer.toString(data.getInt(COLUMN_INDEX_VOTE_COUNT)));
            popularity.setText(Double.toString(data.getDouble(COLUMN_INDEX_VOTE_AVERAGE)));
            date.setText(data.getString(COLUMN_INDEX_RELEASE_DATE));
            image.setImageBitmap(getImage(data.getBlob(COLUMN_INDEX_IMAGE)));
            if (data.getString(COLUMN_INDEX_TRAILER_URL) != null)
                firstTrailer = data.getString(COLUMN_INDEX_TRAILER_URL);
            movie_id_recieved = data.getLong(COLUMN_INDEX_MOVIE_ID);

            trailerHeading.setVisibility(View.GONE);
            reviewHeading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDetailCursor = null;
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
