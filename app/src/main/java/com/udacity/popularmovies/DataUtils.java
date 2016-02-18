package com.udacity.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class DataUtils implements Parcelable {

    String ImageUrl;
    String description;
    String movieTitle;
    String releaseDate;
    int voteCount;
    double popularity;

    public DataUtils(String ImageUrl, String description, String movieTitle, String releaseDate, int voteCount, double popularity) {
        this.ImageUrl = ImageUrl;
        this.description = description;
        this.movieTitle = movieTitle;
        this.releaseDate = releaseDate;
        this.voteCount = voteCount;
        this.popularity = popularity;
    }

    private DataUtils(Parcel in) {
        ImageUrl = in.readString();
        description = in.readString();
        releaseDate = in.readString();
        movieTitle = in.readString();
        voteCount = in.readInt();
        popularity = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(ImageUrl);
        dest.writeString(movieTitle);
        dest.writeString(description);
        dest.writeString(releaseDate);
        dest.writeInt(voteCount);
        dest.writeDouble(popularity);
    }


    public final Parcelable.Creator<DataUtils> CREATOR = new Parcelable.Creator<DataUtils>() {
        @Override
        public DataUtils createFromParcel(Parcel parcel) {
            return new DataUtils(parcel);
        }

        @Override
        public DataUtils[] newArray(int i) {
            return new DataUtils[i];
        }

    };
}
