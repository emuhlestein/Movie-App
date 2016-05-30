package com.intelliviz.movieapp3.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.intelliviz.movieapp3.ApiKeyMgr;
import com.intelliviz.movieapp3.Movie;
import com.intelliviz.movieapp3.MovieAdapter;
import com.intelliviz.movieapp3.MovieBox;
import com.intelliviz.movieapp3.MovieCursorAdapter;
import com.intelliviz.movieapp3.MovieState;
import com.intelliviz.movieapp3.MovieUtils;
import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.db.MovieContract;
import com.intelliviz.movieapp3.syncadapter.MovieSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MovieListFragment.class.getSimpleName();
    private static final String DEFAULT_SORT_BY_OPTION = "popular";
    private static final String MOVIE_LIST_KEY = "movie_list_key";
    public static final int FAVORITE_MOVIE_LOADER = MovieContract.TYPE_FAVORITE;
    public static final int POPULAR_MOVIE_LOADER = MovieContract.TYPE_POPULAR;
    public static final int TOPRATED_MOVIE_LOADER = MovieContract.TYPE_TOP_RATED;
    public static final int STATUS_LOADER = MovieContract.LOAD_STATUS;
    private static final String COLUMN_SPAN_KEY = "column span key";
    private String mMovieUrls;
    private MovieAdapter mPopularAdapter;
    private MovieCursorAdapter mMovieCursorAdapter;
    private OnSelectMovieListener mListener;
    private String mSortBy;
    private int mSpanCount;

    @Bind(R.id.layoutView) LinearLayout mLinearView;
    @Bind(R.id.gridView) RecyclerView mRecyclerView;
    @Bind(R.id.emptyView) TextView mEmptyView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    public interface OnSelectMovieListener {

        /**
         * Callback for when a movie is selected from the general list.
         * @param movie The selected movie.
         */
        void onSelectMovie(Movie movie);

        /**
         * Callback for when a movie is selected from the favorite list.
         * @param movieId The id of the selected movie.
         */
        void onSelectFavoriteMovie(String movieId);

        /**
         * Callback for when sorting method changes.
         *
         * @param sortBy The sorting method.
         */
        void onChangeSort(String sortBy);
    }

    public MovieListFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(int spanCount) {
        Bundle args = new Bundle();

        Fragment fragment = new MovieListFragment();

        args.putInt(COLUMN_SPAN_KEY, spanCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "MovieListFragment onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        ButterKnife.bind(this, view);

        int spanCount = mSpanCount;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);

        if(savedInstanceState == null) {
            //loadMovies();
        } else {
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
            if(movies != null) {
                MovieBox.get().addMovies(movies);
            }
        }



        //mPopularAdapter = new MovieAdapter(getActivity(), MovieBox.get().getMovies());
        //mPopularAdapter.setOnSelectMovieListener(mListener);
        //mRecyclerView.setLayoutManager(gridLayoutManager);
        //mRecyclerView.setAdapter(mPopularAdapter);
        /*
        mPopularRecyclerView.addOnScrollListener(new EndlessOnScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                Log.d(TAG, "Loading more...");
                mPopularAdapter.clear();
                mPopularAdapter.notifyDataSetChanged();
                mMovieUrls = ApiKeyMgr.getMoviesUrl(mSortBy, currentPage);
                getMovies();
            }
        });
        */

        mMovieCursorAdapter = new MovieCursorAdapter(getActivity());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        mRecyclerView.setAdapter(mMovieCursorAdapter);
        mMovieCursorAdapter.setOnSelectMovieListener(mListener);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);

        String sort_key = getResources().getString(R.string.pref_sort_by_key);
        mSortBy = sp.getString(sort_key, DEFAULT_SORT_BY_OPTION);
        if(mSortBy.equals("favorite")) {
            getLoaderManager().initLoader(FAVORITE_MOVIE_LOADER, null, this);
        } else if(mSortBy.equals("popular")) {
            getLoaderManager().initLoader(POPULAR_MOVIE_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(TOPRATED_MOVIE_LOADER, null, this);
        }

/*
        if(mSortBy.equals(ApiKeyMgr.DEFAULT_SORT)) {
            mFavoriteView.setVisibility(View.VISIBLE);
            mPopularView.setVisibility(View.GONE);
        } else {
            mFavoriteView.setVisibility(View.GONE);
            mPopularView.setVisibility(View.VISIBLE);
            if(mPopularAdapter.getItemCount() == 0) {
                mPopularEmptyView.setVisibility(View.VISIBLE);
                mPopularEmptyView.setText(R.string.empty_list);
                mPopularRecyclerView.setVisibility(View.GONE);
            }
        }
*/
        getLoaderManager().initLoader(STATUS_LOADER, null, this);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(getSortedBy(mSortBy));

        initLoader(mSortBy);

        if(!mSortBy.equals("favorite")) {
            checkNeedToSync(mSortBy);
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_key = getResources().getString(R.string.pref_sort_by_key);
        mSortBy = sp.getString(sort_key, DEFAULT_SORT_BY_OPTION);
        mMovieUrls = ApiKeyMgr.getMoviesUrl(mSortBy);

        mSpanCount = getArguments().getInt(COLUMN_SPAN_KEY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "MovieListFragment onAttached");

        if(context instanceof OnSelectMovieListener) {
            mListener = (OnSelectMovieListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "MovieListFragment onDetached");
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(getActivity() == null) {
            Log.d(TAG, "MovieListFragment activity is null");
        }

        if(isAdded()) {
            Log.d(TAG, "MovieListFragment isAdded is true");
        } else {
            Log.d(TAG, "MovieListFragment isAdded is false");
        }
        Log.d(TAG, "MovieListFragment onDestroyView");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST_KEY, MovieBox.get().getMovies());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * This method is called when the settings fragment is in the foreground. That means
     * this fragment is in the background and dettached from the activity.
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean noActivity = false;
        if(!isAdded()) {
            Log.d(TAG, "Activity is not attached");
            noActivity = true;
        }

        Object obj = getHost();

        if(obj instanceof Context) {

        }

        if(noActivity) {
            return;
        }

        Log.d(TAG, "Activity is attached");
        mSortBy = sharedPreferences.getString(key, DEFAULT_SORT_BY_OPTION);

        if(mSortBy.equals("favorite")) {
            Loader loader = getLoaderManager().getLoader(FAVORITE_MOVIE_LOADER);
            if(loader == null) {
                initLoader(mSortBy);
            } else {
                restartLoader(mSortBy);
            }
        } else if(mSortBy.equals("popular")) {
            Loader loader = getLoaderManager().getLoader(POPULAR_MOVIE_LOADER);
            if(loader == null) {
                initLoader(mSortBy);
            } else {
                restartLoader(mSortBy);
            }
        } else {
            Loader loader = getLoaderManager().getLoader(TOPRATED_MOVIE_LOADER);
            if(loader == null) {
                initLoader(mSortBy);
            } else {
                restartLoader(mSortBy);
            }
        }

        if(mListener != null) {
            mListener.onChangeSort(mSortBy);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Loader<Cursor> loader = null;
        Uri uri = null;
        String selection;

        if(loaderId == STATUS_LOADER) {
            uri = MovieContract.StateEntry.CONTENT_URI;
            loader = new CursorLoader(getActivity(), uri, null, null, null, null);
        } else {

            switch (loaderId) {
                case TOPRATED_MOVIE_LOADER:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri("top_rated");
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_TOP_RATED + " = ?";
                    break;
                case POPULAR_MOVIE_LOADER:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri("popular");
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_POPULAR + " = ?";
                    break;
                case FAVORITE_MOVIE_LOADER:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri("favorite");
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_FAVORITE + " = ?";
                    break;
                default:
                    return null;
            }

            String[] selectionArgs = {"1"};
            loader = new CursorLoader(getActivity(),
                    uri,
                    new String[]
                            {
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_TITLE,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_SYNOPSIS,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RELEASE_DATA,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_POSTER,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RUNTIME,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_FAVORITE,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_POPULAR,
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_TOP_RATED,
                            },
                    selection,
                    selectionArgs,
                    null);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId() == MovieContract.LOAD_STATUS) {
            if(cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(MovieContract.StateEntry.COLUMN_STATUS);
                if(statusIndex != -1) {
                    int status = cursor.getInt(statusIndex);
                    if(status == MovieContract.StateEntry.STATUS_UPDATING) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    } else {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                    Log.d(TAG, "Sync status: " + status);
                }
            }
        } else {
            mMovieCursorAdapter.swapCursor(cursor);
            if (mMovieCursorAdapter.getItemCount() == 0) {
                mEmptyView.setText(R.string.empty_list);
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieCursorAdapter.swapCursor(null);
    }

    public void refreshList() {
        restartLoader(mSortBy);
    }

    /*
    private void loadMovies() {

        if(isAdded()) {
            if (isNetworkAvailable((AppCompatActivity) getActivity())) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(mMovieUrls)
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        String jsonData = response.body().string();
                        if (isAdded()) {
                            if (response.isSuccessful()) {
                                ArrayList<Movie> movies = extractMoviesFromJson(jsonData);
                                MovieUtils.markFavoriteMovies(getActivity(), movies);
                                MovieBox.get().addMovies(movies);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplay();
                                    }
                                });
                            }
                        }
                    }
                });
            } else {
                if (isAdded()) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "Network is not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    */

    private void updateDisplay() {
        if(mPopularAdapter.getItemCount() > 0) {
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        mPopularAdapter.notifyDataSetChanged();
    }

    public static boolean isNetworkAvailable(AppCompatActivity activity) {
        boolean isAvailable = false;
        if(activity != null) {
            ConnectivityManager manager =
                    (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();


            if (networkInfo != null && networkInfo.isConnected()) {
                isAvailable = true;
            }
        }
        return isAvailable;
    }

    private ArrayList<Movie> extractMoviesFromJson(String s) {
        JSONObject moviesObject = null;
        int page = 0;
        try {
            JSONObject oneMovie;
            moviesObject = new JSONObject(s);
            page = moviesObject.getInt("page");
            JSONArray movieArray = moviesObject.getJSONArray("results");
            Movie movie;
            ArrayList<Movie> movies = new ArrayList<>();
            for(int i = 0; i < movieArray.length(); i++) {
                oneMovie = movieArray.getJSONObject(i);
                movie = extractMovieFromJson(oneMovie);
                if(movie != null) {
                    movies.add(movie);
                }
            }

            return movies;
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

    private String getSortedBy(String value) {
        if(isAdded()) {
            String[] sortByOptions = getActivity().getResources().getStringArray(R.array.sort_by_options);
            String[] sortByValues = getActivity().getResources().getStringArray(R.array.sort_by_values);
            for (int i = 0; i < sortByValues.length; i++) {
                if (sortByValues[i].equals(value)) {
                    return sortByOptions[i];
                }
            }
        }
        return value;
    }

    private void restartLoader(String sortBy) {
        if (sortBy.equals("popular")) {
            getLoaderManager().restartLoader(POPULAR_MOVIE_LOADER, null, this);
        } else if (sortBy.equals("favorite")) {
            getLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER, null, this);
        } else {
            getLoaderManager().restartLoader(TOPRATED_MOVIE_LOADER, null, this);
        }
    }

    private void initLoader(String sortBy) {
        if (sortBy.equals("popular")) {
            getLoaderManager().initLoader(POPULAR_MOVIE_LOADER, null, this);
        } else if (sortBy.equals("favorite")) {
            getLoaderManager().initLoader(FAVORITE_MOVIE_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(TOPRATED_MOVIE_LOADER, null, this);
        }
    }

    private void checkNeedToSync(String sortBy) {

        MovieState state = MovieUtils.getMovieState(getContext());
        if(state != null) {
            if(state.getPage() == -1) {
                Account account = createSyncAccount(getContext());
                if(account != null) {
                    MovieUtils.updateSyncStatus(getContext(), MovieContract.StateEntry.STATUS_UPDATING);
                    ContentResolver.setIsSyncable(account, MovieContract.CONTENT_AUTHORITY, 1);
                    ContentResolver.setSyncAutomatically(account, MovieContract.CONTENT_AUTHORITY, true);

                    Bundle bundle = new Bundle();
                    bundle.putInt(MovieSyncAdapter.EXTRA_PAGE, state.getPage());
                    bundle.putString(MovieSyncAdapter.EXTRA_SORTBY, sortBy);
                    getContext().getContentResolver().requestSync(account, MovieContract.CONTENT_AUTHORITY, bundle);
                } else {
                    Toast.makeText(getContext(), "Error downloading movies", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static Account createSyncAccount(Context context) {
        Account newAccount = new Account(
                MovieContract.ACCOUNT, MovieContract.ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            ContentResolver.setIsSyncable(newAccount, MovieContract.CONTENT_AUTHORITY, 1);
            return newAccount;
        } else {
            Log.d(TAG, "Account already exists");
            return newAccount;
        }
    }
}
