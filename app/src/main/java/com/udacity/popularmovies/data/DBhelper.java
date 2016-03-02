package com.udacity.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBhelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favourite.db";


    public DBhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        //create command
        String CREATE_FAVOURITE_MOVIE_TABLE = "CREATE TABLE " +
                MoviesContract.MovieEntry.MOVIE_TABLE + " ( " +
                MoviesContract.MovieEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesContract.MovieEntry.MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                MoviesContract.MovieEntry.COLUMN_VOTE_COUNT + " INTEGER," +
                MoviesContract.MovieEntry.COLUMN_TRAILER_URL + " INTEGER," +
                MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL," +
                MoviesContract.MovieEntry.COLUMN_IMAGE + " BLOB)";

        // create movie table
        db.execSQL(CREATE_FAVOURITE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older movie table if existed
        db.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MovieEntry.MOVIE_TABLE);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                MoviesContract.MovieEntry.MOVIE_TABLE + "'");
        // create fresh movie table
        this.onCreate(db);
    }
}
