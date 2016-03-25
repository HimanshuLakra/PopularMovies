package com.udacity.popularmovies.model;


import android.graphics.Bitmap;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class MovieModel {

    public String poster_path;
    public String overview;
    public String title;
    public String release_date;
    public int vote_count;
    public double vote_average;
    public long id;
    public Bitmap imageBitmap;

    @ParcelConstructor
    public MovieModel(String poster_path, String overview, String title, String release_date,
                      int vote_count, double vote_average,long id,Bitmap imageBitmap) {
        this.poster_path = poster_path;
        this.overview = overview;
        this.title = title;
        this.release_date = release_date;
        this.vote_count = vote_count;
        this.vote_average = vote_average;
        this.id=id;
        this.imageBitmap = imageBitmap;
    }

    public String getTitle(){
        return title;
    }
}
