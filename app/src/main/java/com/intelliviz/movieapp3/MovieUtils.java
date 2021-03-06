package com.intelliviz.movieapp3;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.intelliviz.movieapp3.db.MovieContract;

/**
 * Created by edm on 4/8/2016.
 */
public class MovieUtils {
    public static final String TAG = MovieUtils.class.getSimpleName();

    public static MovieState getMovieState(Context context) {
        String[] projection = {MovieContract.StateEntry._ID,
                MovieContract.StateEntry.COLUMN_PAGE,
                MovieContract.StateEntry.COLUMN_STATUS};
        Uri uri = MovieContract.StateEntry.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToFirst()) {
            int pageIndex = cursor.getColumnIndex(MovieContract.StateEntry.COLUMN_PAGE);
            if(pageIndex != -1) {
                int page = cursor.getInt(pageIndex);
                MovieState state = new MovieState(page);
                return state;
            }
        }

        return null;
    }

    public static void updateSyncStatus(Context context, int status) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.StateEntry.COLUMN_STATUS, status);

        Uri uri = MovieContract.StateEntry.CONTENT_URI;
        context.getContentResolver().update(uri, values, null, null);
    }

    public static void updateMoviePage(Context context, int page) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.StateEntry.COLUMN_PAGE, page);

        Uri uri = MovieContract.StateEntry.CONTENT_URI;
        context.getContentResolver().update(uri, values, null, null);
    }

    public static void addMovieToDatabase(Context context, String movieId, String poster, String averageVote, String releaseDate, String synopsis, String title, String filter) {
        int numRows = 0;
        Cursor cursor = getMovie(context, movieId);
        if(cursor != null && cursor.moveToFirst()) {
            try {
                int typeIndex = -1;
                if (filter.equals(MovieFilter.FILTER_MOST_POPULAR)) {
                    typeIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULAR);
                    if (typeIndex != -1) {
                        if (cursor.getInt(typeIndex) == 1) {
                            return;
                        } else {
                            ContentValues values = new ContentValues();
                            values.put(MovieContract.MovieEntry.COLUMN_POPULAR, 1);
                            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                            uri = Uri.withAppendedPath(uri, "" + movieId);
                            numRows = context.getContentResolver().update(uri, values, null, null);
                            return;
                        }
                    }
            } else if(filter.equals(MovieFilter.FILTER_TOP_RATED)) {
                    typeIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TOP_RATED);
                    if (typeIndex != -1) {
                        if (cursor.getInt(typeIndex) == 1) {
                            return;
                        } else {
                            ContentValues values = new ContentValues();
                            values.put(MovieContract.MovieEntry.COLUMN_TOP_RATED, 1);
                            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                            uri = Uri.withAppendedPath(uri, "" + movieId);
                            numRows = context.getContentResolver().update(uri, values, null, null);
                            return;
                        }
                    }
                } else if(filter.equals(MovieFilter.FILTER_UPCOMING)) {
                    typeIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_UPCOMING);
                    if (typeIndex != -1) {
                        if (cursor.getInt(typeIndex) == 1) {
                            return;
                        } else {
                            ContentValues values = new ContentValues();
                            values.put(MovieContract.MovieEntry.COLUMN_UPCOMING, 1);
                            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                            uri = Uri.withAppendedPath(uri, "" + movieId);
                            numRows = context.getContentResolver().update(uri, values, null, null);
                            return;
                        }
                    }
                } else if(filter.equals(MovieFilter.FILTER_NOW_PLAYING)) {
                    typeIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NOW_PLAYING);
                    if (typeIndex != -1) {
                        if (cursor.getInt(typeIndex) == 1) {
                            return;
                        } else {
                            ContentValues values = new ContentValues();
                            values.put(MovieContract.MovieEntry.COLUMN_NOW_PLAYING, 1);
                            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
                            uri = Uri.withAppendedPath(uri, "" + movieId);
                            numRows = context.getContentResolver().update(uri, values, null, null);
                            return;
                        }
                    }
                }
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
            }
        } else {
            // need to add movie
            ContentValues values = new ContentValues();
            values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
            values.put(MovieContract.MovieEntry.COLUMN_POSTER, poster);
            values.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, averageVote);
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATA, releaseDate);
            values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, "0");
            values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, synopsis);
            values.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
            values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0);
            if(filter.equals(MovieFilter.FILTER_MOST_POPULAR)) {
                values.put(MovieContract.MovieEntry.COLUMN_POPULAR, 1);
                values.put(MovieContract.MovieEntry.COLUMN_TOP_RATED, 0);
                values.put(MovieContract.MovieEntry.COLUMN_UPCOMING, 0);
                values.put(MovieContract.MovieEntry.COLUMN_NOW_PLAYING, 0);
            } else if(filter.equals(MovieFilter.FILTER_TOP_RATED)) {
                values.put(MovieContract.MovieEntry.COLUMN_POPULAR, 0);
                values.put(MovieContract.MovieEntry.COLUMN_TOP_RATED, 1);
                values.put(MovieContract.MovieEntry.COLUMN_UPCOMING, 0);
                values.put(MovieContract.MovieEntry.COLUMN_NOW_PLAYING, 0);
            } else if(filter.equals(MovieFilter.FILTER_UPCOMING)) {
                values.put(MovieContract.MovieEntry.COLUMN_POPULAR, 0);
                values.put(MovieContract.MovieEntry.COLUMN_TOP_RATED, 0);
                values.put(MovieContract.MovieEntry.COLUMN_UPCOMING, 1);
                values.put(MovieContract.MovieEntry.COLUMN_NOW_PLAYING, 0);
            }else if(filter.equals(MovieFilter.FILTER_NOW_PLAYING)) {
                values.put(MovieContract.MovieEntry.COLUMN_POPULAR, 0);
                values.put(MovieContract.MovieEntry.COLUMN_TOP_RATED, 0);
                values.put(MovieContract.MovieEntry.COLUMN_UPCOMING, 0);
                values.put(MovieContract.MovieEntry.COLUMN_NOW_PLAYING, 1);
            }

            Uri uri = context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
            String id = uri.getLastPathSegment();
        }
    }

    public static int updateRuntime(Context context, String movieId, String runtime) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, runtime);
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + movieId);
        int numRows = context.getContentResolver().update(uri, values, null, null);
        return numRows;
    }

    public static int updateFavoriteMovie(Context context, String movieId, int favorite) {
        Cursor cursor = getMovie(context, movieId);
        if(cursor == null) {
            return 0;
        }

        if(!cursor.moveToFirst()) {
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, favorite);
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + movieId);
        return context.getContentResolver().update(uri, values, null, null);
    }

    public static void dumpMovies(Activity activity) {
        Cursor cursor = getAllMovies(activity);
        while(cursor.moveToNext()) {
            int movieTitleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
            int movieIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            int favoriteIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_FAVORITE);
            int popularIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULAR);
            int topRatedIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TOP_RATED);
            int upcomingIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_UPCOMING);
            int nowPlayingIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NOW_PLAYING);
            int runtimeIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RUNTIME);
            String title = "??????";
            String movieId = "??????";
            int favorite = -1;
            int popular = -1;
            int top_rated = -1;
            int upcoming = -1;
            int now_playing = -1;
            String runtime = "?????";
            if(movieTitleIndex != -1) {
                title = cursor.getString(movieTitleIndex);
            }
            if(movieIdIndex != -1) {
                movieId = cursor.getString(movieIdIndex);
            }
            if(favoriteIndex != -1) {
                favorite = cursor.getInt(favoriteIndex);
            }
            if(popularIndex != -1) {
                popular = cursor.getInt(popularIndex);
            }
            if(topRatedIndex != -1) {
                top_rated = cursor.getInt(topRatedIndex);
            }
            if(upcomingIndex != -1) {
                upcoming = cursor.getInt(upcomingIndex);
            }
            if(nowPlayingIndex != -1) {
                now_playing = cursor.getInt(nowPlayingIndex);
            }
            if(runtimeIndex != -1) {
                runtime = cursor.getString(runtimeIndex);
            }
            Log.d(TAG, title + " " + movieId + "  favorite: " + favorite + " popular: " + popular + " top rated: " + top_rated + " runtime: " + runtime + " upcoming: " + upcoming);
        }
    }

    public static Cursor getAllMovies(Context context) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String[] projection = {MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_TITLE,
                MovieContract.MovieEntry.COLUMN_POPULAR,
                MovieContract.MovieEntry.COLUMN_TOP_RATED,
                MovieContract.MovieEntry.COLUMN_FAVORITE,
                MovieContract.MovieEntry.COLUMN_UPCOMING,
                MovieContract.MovieEntry.COLUMN_NOW_PLAYING,
                MovieContract.MovieEntry.COLUMN_RUNTIME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        return cursor;
    }

    /**
     * NOTE cursor can be null. Need to check for this.
     * @param context
     * @param movieId
     * @return
     */
    public static Cursor getMovie(Context context, String movieId) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String selectionClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movieId};
        String[] projection = null; // get all columns
        Cursor cursor = context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);

        return cursor;
    }

    public static void addReview(Context context, String reviewId, String movieId, String author, String content) {
        Cursor cursor = getReview(context, reviewId);
        try {
            if (cursor == null || cursor.moveToFirst()) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, reviewId);
            values.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
            values.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, author);
            values.put(MovieContract.ReviewEntry.COLUMN_CONTENT, content);
            Uri uri = context.getContentResolver().insert(MovieContract.ReviewEntry.CONTENT_URI, values);
        }finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    public static Cursor getReview(Context context, String reviewId) {
        Uri uri = MovieContract.ReviewEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + reviewId);
        String selectionClause = null;
        String[] selectionArgs = null;
        String[] projection = null; // get all columns
        Cursor cursor = context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);

        return cursor;
    }

    public static void addTrailer(Context context, String trailerId, String movieId, String url) {
        Cursor cursor = getTrailer(context, trailerId);
        try {
            if (cursor == null || cursor.moveToFirst()) {
                return;
            }
            ContentValues values = new ContentValues();
            values.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, trailerId);
            values.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieId);
            values.put(MovieContract.TrailerEntry.COLUMN_URL, url);
            Uri uri = context.getContentResolver().insert(MovieContract.TrailerEntry.CONTENT_URI, values);
        }finally{
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    public static Cursor getTrailers(Context context, String movieId) {
        Uri uri = MovieContract.TrailerEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, MovieContract.MovieEntry.TABLE_NAME);
        uri = Uri.withAppendedPath(uri, movieId);
        String selectionClause = null;
        String[] selectionArgs = null;
        String[] projection = null; // get all columns
        Cursor cursor = context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);

        return cursor;
    }

    public static Cursor getTrailer(Context context, String trailerId) {
        Uri uri = MovieContract.TrailerEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + trailerId);
        String selectionClause = null;
        String[] selectionArgs = null;
        String[] projection = null; // get all columns
        Cursor cursor = context.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);

        return cursor;
    }
}

