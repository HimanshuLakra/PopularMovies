package com.udacity.popularmovies.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.udacity.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final class MovieEntry implements BaseColumns {

        // table name
        public static final String MOVIE_TABLE = "movie";
        // columns
        public static final String ID = "_id";
        public static final String MOVIE_ID = "movie_id";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "date";
        public static final String COLUMN_TRAILER_URL= "trailer_url";
        // create content uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(MOVIE_TABLE).build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MOVIE_TABLE;

        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + CONTENT_AUTHORITY + "/" + MOVIE_TABLE;

        // for building URIs on insertion
        public static Uri buildFlavorsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
