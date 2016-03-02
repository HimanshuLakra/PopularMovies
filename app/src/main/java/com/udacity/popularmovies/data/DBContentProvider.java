package com.udacity.popularmovies.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class DBContentProvider extends ContentProvider {

    private DBhelper dbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Codes for the UriMatcher
    private static final int MOVIE = 100;
    private static final int MOVIE_WITH_ID = 200;
    private static final int MOVIE_WITH_MOVIE_ID = 300;

    private static UriMatcher buildUriMatcher() {

        // Build a UriMatcher by adding a specific code to return based on a match
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = "com.udacity.popularmovies.app";

        // add a code for each type of URI you want
        matcher.addURI(authority, MoviesContract.MovieEntry.MOVIE_TABLE, MOVIE);
       // matcher.addURI(authority, MoviesContract.MovieEntry.MOVIE_TABLE + "/#", MOVIE_WITH_ID);
        matcher.addURI(authority, MoviesContract.MovieEntry.MOVIE_TABLE + "/#", MOVIE_WITH_MOVIE_ID);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        dbHelper = new DBhelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // All Flavors selected
            case MOVIE: {
                retCursor = dbHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.MOVIE_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }
            case MOVIE_WITH_MOVIE_ID: {
                retCursor = dbHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.MOVIE_TABLE,
                        projection,
                        MoviesContract.MovieEntry.MOVIE_ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            }

            default: {
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE: {
                return MoviesContract.MovieEntry.CONTENT_DIR_TYPE;
            }
            case MOVIE_WITH_MOVIE_ID: {
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numUpdated = 0;

        if (values == null) {
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                numUpdated = db.update(MoviesContract.MovieEntry.MOVIE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case MOVIE_WITH_MOVIE_ID: {
                numUpdated = db.update(MoviesContract.MovieEntry.MOVIE_TABLE,
                        values,
                        MoviesContract.MovieEntry.MOVIE_ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (numUpdated > 0) {
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MovieEntry.MOVIE_TABLE, null, values);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = MoviesContract.MovieEntry.buildFlavorsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);

            }
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch (match) {
            case MOVIE:
                numDeleted = db.delete(
                        MoviesContract.MovieEntry.MOVIE_TABLE, selection, selectionArgs);
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MoviesContract.MovieEntry.MOVIE_TABLE + "'");
                break;
            case MOVIE_WITH_MOVIE_ID:
                numDeleted = db.delete(MoviesContract.MovieEntry.MOVIE_TABLE,
                        MoviesContract.MovieEntry.MOVIE_ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        MoviesContract.MovieEntry.MOVIE_TABLE + "'");

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return numDeleted;
    }
}
