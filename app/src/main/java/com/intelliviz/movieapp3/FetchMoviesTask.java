package com.intelliviz.movieapp3;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetch move data from internet using background thread.
 * Created by edm on 3/18/2016.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {
    public static final String TAG = FetchMoviesTask.class.getSimpleName();
    private ArrayAdapter<Movie> mMovieAdapter;
    private List<Movie> mMovies;

    public FetchMoviesTask(ArrayAdapter<Movie> adapter, List<Movie> movies) {
        mMovieAdapter = adapter;
        mMovies = movies;
    }

    @Override
    protected List<Movie> doInBackground(String... urls) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        List<Movie> movies = new ArrayList<>();

        try {
            URL url = new URL(urls[0]);
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
            movies = extractMoviesFromJson(buffer.toString());
        } catch (IOException e) {
            Log.e(TAG, "Error accessing internet");
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

        return movies;
    }

    private List<Movie> extractMoviesFromJson(String s) {
        JSONObject moviesObject = null;
        int page = 0;
        try {
            JSONObject oneMovie;
            moviesObject = new JSONObject(s);
            page = moviesObject.getInt("page");
            JSONArray movieArray = moviesObject.getJSONArray("results");
            List<Movie> movieList = new ArrayList<>();
            Movie movie;
            for(int i = 0; i < movieArray.length(); i++) {
                oneMovie = movieArray.getJSONObject(i);
                movie = extractMovieFromJson(oneMovie);
                if(movie != null) {
                    movieList.add(movie);
                }
            }

            return movieList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Movie extractMovieFromJson(JSONObject object) {
        try {
            String posterPath = object.getString("poster_path");
            String overview = object.getString("overview");
            String releaseDate = object.getString("release_date");
            String id = object.getString("id");
            String title = object.getString("title");
            String averageVote = object.getString("vote_average");
            Movie movie = new Movie(title, posterPath, overview, id, releaseDate, averageVote);
            return movie;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading movie");
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if(movies.size() > 0) {
            mMovies.clear();
            mMovies.addAll(movies);
            mMovieAdapter.notifyDataSetChanged();
        }
    }
}
