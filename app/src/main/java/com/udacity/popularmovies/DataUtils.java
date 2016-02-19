package com.udacity.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class DataUtils implements Parcelable {

    String poster_path;
    String overview;
    String title;
    String release_date;
    int vote_count;
    double vote_average;

    public DataUtils(String ImageUrl, String description, String movieTitle, String releaseDate, int voteCount, double popularity) {
        this.poster_path = ImageUrl;
        this.overview = description;
        this.title = movieTitle;
        this.release_date = releaseDate;
        this.vote_count = voteCount;
        this.vote_average = popularity;
    }

    private DataUtils(Parcel in) {
        this.poster_path = in.readString();
        this.overview = in.readString();
        this.title = in.readString();
        this.release_date = in.readString();
        this.vote_count = in.readInt();
        this.vote_average = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(poster_path);
        dest.writeString(overview);
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeInt(vote_count);
        dest.writeDouble(vote_average);
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
