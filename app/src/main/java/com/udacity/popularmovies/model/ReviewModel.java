package com.udacity.popularmovies.model;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class ReviewModel {

    public String author;
    public String content;

    @ParcelConstructor
    public ReviewModel(String author, String content) {
        this.author = author;
        this.content = content;
    }
}
