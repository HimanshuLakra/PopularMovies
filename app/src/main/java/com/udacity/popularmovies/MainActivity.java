package com.udacity.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //arrayList for recieved movies poster URL's
    static ArrayList<String> moviesURLS;

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

        implementTask();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent detailActivity = new Intent(MainActivity.this, DetailActivity.class);
                detailActivity.putExtra("TITLE", dataSet.get(position).movieTitle);
                detailActivity.putExtra("IMAGE_URL", dataSet.get(position).ImageUrl);
                detailActivity.putExtra("DESCRIPTION", dataSet.get(position).description);
                detailActivity.putExtra("POPULARITY", dataSet.get(position).popularity);
                detailActivity.putExtra("VOTES", dataSet.get(position).voteCount);
                detailActivity.putExtra("RELEASE_DATE", dataSet.get(position).releaseDate);

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

    public void implementTask() {

        if (ConnectionDetector.isAvailiable(MainActivity.this)) {

            noInternet.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            String Url = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=xxxxxxxxxx";
            progressBar.setIndeterminate(true);
            moviesURLS = new ArrayList<String>();
            dataSet = new ArrayList<>();

            new RequestPoster().execute(Url);

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
            Url = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=xxxxxxxxx";
        } else {
            Url = "http://api.themoviedb.org/3/discover/movie?sort_by=vote_count.desc&api_key=xxxxxxxxx";
        }

        if (ConnectionDetector.isAvailiable(MainActivity.this)) {

            moviesURLS.clear();
            dataSet.clear();
            new RequestPoster().execute(Url);

        } else {

            Toast.makeText(MainActivity.this, "Check your internet Connection", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public class RequestPoster extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            InputStream in = null;
            int resCode = -1;
            StringBuilder result = null;
            try {
                URL url = new URL(params[0]);
                URLConnection urlConn = url.openConnection();

                if (!(urlConn instanceof HttpURLConnection)) {
                    throw new IOException("URL is not an Http URL");
                }
                HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                httpConn.setAllowUserInteraction(false);
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestMethod("GET");
                httpConn.connect();
                resCode = httpConn.getResponseCode();
                result = new StringBuilder();

                if (resCode == HttpURLConnection.HTTP_OK) {
                    in = httpConn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }

                httpConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (result == null) {
                String dummy = "Recieved null";
                return dummy;
            } else
                return result.toString();
        }

        @Override
        protected void onPostExecute(String dataReceieved) {

            if (dataReceieved.contentEquals("Recieved null")) {

                Toast.makeText(MainActivity.this, "Failed to recieve data from server", Toast.LENGTH_SHORT).show();

            } else {
                try {
                    JSONObject recievedObject = new JSONObject(dataReceieved);
                    JSONArray results = recievedObject.getJSONArray("results");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject movieresult = results.getJSONObject(i);

                        DataUtils dataUtil = new DataUtils();

                        String posterPath = "http://image.tmdb.org/t/p/w185/" + movieresult.getString("poster_path");
                        dataUtil.ImageUrl = posterPath;
                        dataUtil.releaseDate = movieresult.getString("release_date");
                        dataUtil.movieTitle = movieresult.getString("title");
                        dataUtil.popularity = movieresult.getDouble("popularity");
                        dataUtil.voteCount = movieresult.getInt("vote_count");
                        dataUtil.description = movieresult.getString("overview");

                        dataSet.add(dataUtil);
                        moviesURLS.add(posterPath);
                    }

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "error occured",
                            Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.GONE);
                gridview.setVisibility(View.VISIBLE);

                gridview.setAdapter(new GridAdapter(MainActivity.this, moviesURLS));
            }

        }
    }

}
