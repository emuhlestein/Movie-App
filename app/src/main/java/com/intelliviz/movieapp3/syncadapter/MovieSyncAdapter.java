package com.intelliviz.movieapp3.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.intelliviz.movieapp3.ApiKeyMgr;
import com.intelliviz.movieapp3.MovieFilter;
import com.intelliviz.movieapp3.MovieUtils;
import com.intelliviz.movieapp3.db.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by edm on 5/11/2016.
 */
public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    private final String TAG = MovieSyncAdapter.class.getSimpleName();
    public static final String EXTRA_FILTER = "filter";
    public static final String EXTRA_PAGE= "page";
    ContentResolver mContentResolver;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public MovieSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        int page = extras.getInt(EXTRA_PAGE, -1);

        MovieUtils.updateSyncStatus(getContext(), MovieContract.StateEntry.STATUS_UPDATING);
        if(page == -1) {
            page = 1;
        }

        downLoadMovies(MovieFilter.FILTER_MOST_POPULAR, page);
        downLoadMovies(MovieFilter.FILTER_UPCOMING, page);
        downLoadMovies(MovieFilter.FILTER_TOP_RATED, page);
        downLoadMovies(MovieFilter.FILTER_NOW_PLAYING, page);

        loadRuntimes();
        downloadReviews();
        downloadTrailers();
        MovieUtils.updateSyncStatus(getContext(), MovieContract.StateEntry.STATUS_UPDATED);
    }

    private void downLoadMovies(String filter, int page) {
        String urlString = ApiKeyMgr.getMoviesUrl(filter, "" + page);
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if(url != null) {
            downLoadMovies(url, filter);
        }
    }

    private void downLoadMovies(URL url, String filter) {
        String jsonData = loadDataFromUrl(url);
        if(jsonData != null) {
            extractMoviesFromJson(jsonData, filter);
        }
    }

    private void extractMoviesFromJson(String s, String filter) {
        JSONObject moviesObject = null;
        try {
            JSONObject oneMovie;
            moviesObject = new JSONObject(s);
            JSONArray movieArray = moviesObject.getJSONArray("results");
            for(int i = 0; i < movieArray.length(); i++) {
                oneMovie = movieArray.getJSONObject(i);
                extractMovieFromJson(oneMovie, filter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractMovieFromJson(JSONObject object, String filter) {
        try {
            String posterPath = object.getString("poster_path");
            String overview = object.getString("overview");
            String releaseDate = object.getString("release_date");
            String id = object.getString("id");
            String title = object.getString("title");
            String averageVote = object.getString("vote_average");
            MovieUtils.addMovieToDatabase(getContext(), id, posterPath, averageVote, releaseDate, overview, title, filter);
        } catch (JSONException e) {
            Log.e(TAG, "Error reading movie");
        }
    }

    private void loadRuntimes() {
        Cursor cursor = MovieUtils.getAllMovies(getContext());
        if(cursor == null) {
            return;
        }

        try {
            while (cursor.moveToNext()) {
                int movieIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                int titleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
                String title = "???????";
                if(titleIndex != -1) {
                    title = cursor.getString(titleIndex);
                }

                if (movieIdIndex != -1) {
                    String movieId = cursor.getString(movieIdIndex);
                    String urlString = ApiKeyMgr.getMovieUrl(movieId);
                    Log.d(TAG, title + "   " + movieId);
                    URL url;
                    try {
                        url = new URL(urlString);
                        String runtime = downloadRuntime(url);
                        if (runtime != null) {
                            MovieUtils.updateRuntime(getContext(), movieId, runtime);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }finally{
            cursor.close();
        }
    }

    private String downloadRuntime(URL url) {
        String jsonData = loadDataFromUrl(url);
        JSONObject jsonObject;
        if(jsonData != null) {
            try {
                jsonObject = new JSONObject(jsonData);
                return jsonObject.getString("runtime");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }

    private void downloadReviews() {
        Cursor cursor = MovieUtils.getAllMovies(getContext());
        while(cursor.moveToNext()) {
            int movieIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            if(movieIdIndex != -1) {
                String movieId = cursor.getString(movieIdIndex);
                String urlString = ApiKeyMgr.getReviewsUrl(movieId);
                URL url;
                try {
                    url = new URL(urlString);
                    extractReviews(url, movieId);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void extractReviews(URL url, String movieId) {
        String jsonData = loadDataFromUrl(url);

        if(jsonData != null) {
            try {
                JSONObject review;
                JSONObject reviewsObject = new JSONObject(jsonData);
                JSONArray reviewArray = reviewsObject.getJSONArray("results");
                for (int i = 0; i < reviewArray.length(); i++) {
                    review = reviewArray.getJSONObject(i);
                    extractReviewFromJson(review, movieId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void extractReviewFromJson(JSONObject object, String movieId) {
        try {
            String reviewId = object.getString("id");
            String author = object.getString("author");
            String content = object.getString("content");
            MovieUtils.addReview(getContext(), reviewId, movieId, author, content);
        } catch (JSONException e) {
            Log.e(TAG, "Error reading review");
        }
    }

    private void downloadTrailers() {
        Cursor cursor = MovieUtils.getAllMovies(getContext());
        while(cursor.moveToNext()) {
            int movieIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            if(movieIdIndex != -1) {
                String movieId = cursor.getString(movieIdIndex);
                String urlString = ApiKeyMgr.getTrailersUrl(movieId);
                URL url;
                try {
                    url = new URL(urlString);
                    extractTrailers(url, movieId);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void extractTrailers(URL url, String movieId) {
        String jsonData = loadDataFromUrl(url);

        if(jsonData != null) {
            try {
                JSONObject trailer;
                JSONObject reviewsObject = new JSONObject(jsonData);
                JSONArray reviewArray = reviewsObject.getJSONArray("results");
                for (int i = 0; i < reviewArray.length(); i++) {
                    trailer = reviewArray.getJSONObject(i);
                    extractTrailerFromJson(trailer, movieId);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void extractTrailerFromJson(JSONObject object, String movieId) {
        try {
            String trailerId = object.getString("id");
            String key = object.getString("key");
            String url = "https://www.youtube.com/watch?v=" + key;
            MovieUtils.addTrailer(getContext(), trailerId, movieId, url);
        } catch (JSONException e) {
            Log.e(TAG, "Error reading trailer");
        }
    }

    private String loadDataFromUrl(URL url) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error accessing internet: " + e.toString());
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }

            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream");
                }
            }
        }
        return null;
    }
}
