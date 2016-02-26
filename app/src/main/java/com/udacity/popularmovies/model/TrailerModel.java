package com.udacity.popularmovies.model;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class TrailerModel {

    public String key;
    public String type;

    @ParcelConstructor
    public TrailerModel(String key, String type) {
        this.key = key;
        this.type = type;
    }
}
