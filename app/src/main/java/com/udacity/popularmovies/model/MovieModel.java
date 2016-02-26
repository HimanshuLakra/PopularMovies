package com.udacity.popularmovies.model;


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

    @ParcelConstructor
    public MovieModel(String poster_path, String overview, String title, String release_date,
                      int vote_count, double vote_average,long id) {
        this.poster_path = poster_path;
        this.overview = overview;
        this.title = title;
        this.release_date = release_date;
        this.vote_count = vote_count;
        this.vote_average = vote_average;
        this.id=id;
    }

    public String getTitle(){
        return title;
    }
}
