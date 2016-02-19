package com.udacity.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //arrayList for recieved movies details
    static ArrayList<DataUtils> dataSet;

    Toolbar toolbar;
    GridView gridview;
    ProgressBar progressBar;
    RelativeLayout noInternet;
    Button tryAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        noInternet = (RelativeLayout) findViewById(R.id.no_internet_connection);
        gridview = (GridView) findViewById(R.id.gridview);
        tryAgain = (Button) findViewById(R.id.try_again);
        toolbar = (Toolbar) findViewById(R.id.toolbar_movies);
        toolbar.setTitle("PopularMovies");
        setSupportActionBar(toolbar);

        if (savedInstanceState == null || !savedInstanceState.containsKey("movieDataSet")) {
            implementTask();
        } else {

            dataSet = savedInstanceState.getParcelableArrayList("movieDataSet");
            if (dataSet != null) {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.GONE);
                gridview.setVisibility(View.VISIBLE);
                gridview.setAdapter(new GridAdapter(MainActivity.this, dataSet));
            } else {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                gridview.setVisibility(View.GONE);
            }
        }

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent detailActivity = new Intent(MainActivity.this, DetailActivity.class);
                detailActivity.putExtra("TITLE", dataSet.get(position).title);
                detailActivity.putExtra("IMAGE_URL", dataSet.get(position).poster_path);
                detailActivity.putExtra("DESCRIPTION", dataSet.get(position).overview);
                detailActivity.putExtra("POPULARITY", dataSet.get(position).vote_average);
                detailActivity.putExtra("VOTES", dataSet.get(position).vote_count);
                detailActivity.putExtra("RELEASE_DATE", dataSet.get(position).release_date);

                startActivity(detailActivity);
            }
        });

        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                implementTask();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movieDataSet", dataSet);
        super.onSaveInstanceState(outState);
    }

    /*Check internet connection and
    * call for AsyncTask */
    public void implementTask() {

        if (ConnectionDetector.isAvailiable(MainActivity.this)) {
            noInternet.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            String Url = getString(R.string.URLInitial) + "popularity.desc&" + getString(R.string.URLRear);
            progressBar.setIndeterminate(true);
            dataSet = new ArrayList<>();
            RequestPoster(Url);

        } else {
            progressBar.setVisibility(View.GONE);
            gridview.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String Url;
        if (item.getItemId() == R.id.most_popular) {
            Url = getString(R.string.URLInitial) + "popularity.desc&" + getString(R.string.URLRear);
        } else {
            Url = getString(R.string.URLInitial) + "vote_count.desc&" + getString(R.string.URLRear);
        }

        if (ConnectionDetector.isAvailiable(MainActivity.this)) {

            noInternet.setVisibility(View.GONE);
            gridview.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            dataSet = new ArrayList<>();
            // new RequestPoster().execute(Url);
            RequestPoster(Url);

        } else {

            progressBar.setVisibility(View.GONE);
            gridview.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "Check your internet Connection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }


    public void RequestPoster(String Url) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Toast.makeText(MainActivity.this, "error occured",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {

                    String responseReceieved = response.body().string();

                    try {
                        JSONObject recievedObject = new JSONObject(responseReceieved);
                        JSONArray results = recievedObject.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject movieresult = results.getJSONObject(i);

                            String posterPath = "http://image.tmdb.org/t/p/w185/" + movieresult.getString("poster_path");

                            DataUtils dataUtil = new DataUtils(posterPath, movieresult.getString("overview"),
                                    movieresult.getString("title"), movieresult.getString("release_date"),
                                    movieresult.getInt("vote_count"), movieresult.getDouble("vote_average"));

                            dataSet.add(dataUtil);

                        }

                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "error occured",
                                Toast.LENGTH_SHORT).show();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressBar.setVisibility(View.GONE);
                            noInternet.setVisibility(View.GONE);
                            gridview.setVisibility(View.VISIBLE);

                            gridview.setAdapter(new GridAdapter(MainActivity.this, dataSet));
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "error occured",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

}
