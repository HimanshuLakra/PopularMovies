package com.udacity.popularmovies.api;

import com.udacity.popularmovies.model.MovieModel;
import com.udacity.popularmovies.model.ReviewModel;
import com.udacity.popularmovies.model.TrailerModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("discover/movie")
    Call<ArrayList<MovieModel>> getMovieDetails(@Query("sort_by") String SortOrder ,
                                                @Query("api_key") String APIKey);

    @GET("movie/{movie_id}/videos")
    Call<ArrayList<TrailerModel>> getMovieTrailers(@Path("movie_id") String MovieId,
                                                   @Query("api_key") String APIKey);

    @GET("movie/{movie_id}/reviews")
    Call<ArrayList<ReviewModel>> getMovieReviews(@Path("movie_id") String MovieId,
                                                  @Query("api_key") String APIKey);

}
