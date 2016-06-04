package com.intelliviz.movieapp3;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by edm on 3/31/2016.
 */
public class ApiKeyMgr {
    public static final String PosterUrl = "http://image.tmdb.org/t/p/w185%s";
    private static final String TAG = ApiKeyMgr.class.getSimpleName();
    private static final String MOVIEDB_END_POINT = "https://api.themoviedb.org/3/movie/";
    private static final String API_KEY_NOT_SET = "api key not set";
    private static final String DEFAULT_PAGE = "1";
    private static String mApiKey = API_KEY_NOT_SET;


    /**
     * Check to see if an api key exists. Return null if there is no key.
     *
     * @param context The android context.
     * @param apiKey The user-supplied api key.
     * @return
     */
    public static boolean checkApiKey(Context context, String apiKey) {
        mApiKey = apiKey;
        if(mApiKey == null) {
            mApiKey = getApiKeyFromFile(context);
        }

        return (mApiKey != null);
    }

    public static String getMovieUrl(String id) {
        String url = MOVIEDB_END_POINT
                + id
                + "?api_key=" + mApiKey;
        return url;
    }

    public static String getMoviesUrl(String sortBy) {
        String url = MOVIEDB_END_POINT
                + sortBy
                + "?page="+ DEFAULT_PAGE
                + "&api_key=" + mApiKey;
        return url;
    }

    public static String getMoviesUrl(String sortBy, String page) {
        String url = MOVIEDB_END_POINT
                + sortBy
                + "?page="+ page
                + "&api_key=" + mApiKey;
        return url;
    }

    public static String getReviewsUrl(String id) {
        String url = MOVIEDB_END_POINT
                + id
                + "/reviews"
                + "?api_key=" + mApiKey;
        return url;
    }

    public static String getTrailersUrl(String id) {
        String url = MOVIEDB_END_POINT
                + id
                + "/videos"
                + "?api_key=" + mApiKey;
        return url;
    }

    /**
     * Get the api key from an external file: api_key.json located in the assets directory.
     * File is not under source code control. It is listed in .gitignore.
     * @return The api key, if found. Otherwise, null.
     */
    private static String getApiKeyFromFile(Context context) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open("api_key.json");
            InputStreamReader inputStream = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(inputStream);
            StringBuffer buffer = new StringBuffer();
            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }

            JSONObject obj = new JSONObject(buffer.toString());
            String api_key = obj.getString("api_key");
            return api_key;
        } catch (IOException e) {
            Log.e(TAG, "Error: Could not read api key from asset file");
        } catch (JSONException e) {
            Log.e(TAG, "Error, JSON parse error");
        }

        return null;
    }
}
