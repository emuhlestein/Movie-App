package com.intelliviz.movieapp3.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.intelliviz.movieapp3.MovieCursorAdapter;
import com.intelliviz.movieapp3.MovieFilter;
import com.intelliviz.movieapp3.MovieState;
import com.intelliviz.movieapp3.MovieUtils;
import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.db.MovieContract;
import com.intelliviz.movieapp3.syncadapter.MovieSyncAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MovieListFragment.class.getSimpleName();
    private static final String PREF_NEED_TO_SYNC = "need_to_sync";
    public static final int MOVIE_LOADER = 0;
    public static final int STATUS_LOADER = 1;
    private static final String COLUMN_SPAN_KEY = "column span key";
    private MovieCursorAdapter mMovieCursorAdapter;
    private OnSelectMovieListener mListener;
    private String mFilterBy;
    private int mSpanCount;
    private boolean mRestartLoader;

    @Bind(R.id.gridView) RecyclerView mRecyclerView;
    @Bind(R.id.emptyView) TextView mEmptyView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.movie_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    public interface OnSelectMovieListener {

        /**
         * Callback for when a movie is selected..
         * @param movieId The id of the selected movie.
         */
        void onSelectMovie(String movieId);
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

        mMovieCursorAdapter = new MovieCursorAdapter(getActivity());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        mRecyclerView.setAdapter(mMovieCursorAdapter);
        mMovieCursorAdapter.setOnSelectMovieListener(mListener);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);

        mFilterBy = sp.getString(MovieFilter.PREF_FILTER_BY, MovieFilter.DEFAULT_FILTER);
        boolean needToSync = sp.getBoolean(PREF_NEED_TO_SYNC, true);
        if(needToSync) {
            checkNeedToSync(mFilterBy);
            SharedPreferences.Editor editor= sp.edit();
            editor.putBoolean(PREF_NEED_TO_SYNC, false);
            editor.apply();
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkNeedToSync(mFilterBy);
            }
        });


        mProgressBar.setVisibility(View.INVISIBLE);

        getLoaderManager().initLoader(STATUS_LOADER, null, this);

        setSubtitle(mFilterBy);

        if(mRestartLoader) {
            mRestartLoader = false;
            restartLoader(mFilterBy);
        } else {
            initLoader(mFilterBy);
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mFilterBy = sp.getString(MovieFilter.PREF_FILTER_BY, MovieFilter.DEFAULT_FILTER);
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
    public void onResume() {
        super.onResume();
        if (mRestartLoader) {
            restartLoader(mFilterBy);
            mRestartLoader = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * This method is called when the settings fragment is in the foreground. That means
     * this fragment is in the background and dettached from the activity.
     * @param sharedPreferences The shared preference.
     * @param key The shared preference key.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(MovieFilter.PREF_FILTER_BY)) {
            mRestartLoader = true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Loader<Cursor> loader;
        Uri uri;
        String selection;

        if(loaderId == STATUS_LOADER) {
            uri = MovieContract.StateEntry.CONTENT_URI;
            loader = new CursorLoader(getActivity(), uri, null, null, null, null);
        } else {
            int filterBy = args.getInt(MovieFilter.PREF_FILTER_BY);

            switch (filterBy) {
                case MovieContract.TYPE_TOP_RATED:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri(MovieFilter.FILTER_TOP_RATED);
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_TOP_RATED + " = ?";
                    break;
                case MovieContract.TYPE_POPULAR:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri(MovieFilter.FILTER_MOST_POPULAR);
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_POPULAR + " = ?";
                    break;
                case MovieContract.TYPE_FAVORITE:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri(MovieFilter.FILTER_FAVORITE);
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_FAVORITE + " = ?";
                    break;
                case MovieContract.TYPE_UPCOMING:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri(MovieFilter.FILTER_UPCOMING);
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_UPCOMING + " = ?";
                    break;
                case MovieContract.TYPE_NOW_PLAYING:
                    uri = MovieContract.MovieEntry.buildMovieByListTypeUri(MovieFilter.FILTER_NOW_PLAYING);
                    selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                            MovieContract.MovieEntry.COLUMN_NOW_PLAYING + " = ?";
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
                                    MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_UPCOMING,
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
        if(loader.getId() == STATUS_LOADER) {
            if(cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(MovieContract.StateEntry.COLUMN_STATUS);
                if(statusIndex != -1) {
                    int status = cursor.getInt(statusIndex);
                    if(status == MovieContract.StateEntry.STATUS_UPDATED) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
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
        mRestartLoader = true;
        restartLoader(mFilterBy);
    }

    public void refreshList(String filter) {
        mFilterBy = filter;
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit();

        editor.putString(MovieFilter.PREF_FILTER_BY, filter);
        editor.apply();
        mRestartLoader = true;
        restartLoader(mFilterBy);
        setSubtitle(filter);
    }

    private String getFilterString(String value) {
        if(isAdded()) {
            String[] sortByOptions = getActivity().getResources().getStringArray(R.array.filter_by_options);
            String[] sortByValues = getActivity().getResources().getStringArray(R.array.filter_by_values);
            for (int i = 0; i < sortByValues.length; i++) {
                if (sortByValues[i].equals(value)) {
                    return sortByOptions[i];
                }
            }
        }
        return value;
    }

    private void restartLoader(String sortBy) {
        Bundle bundle = new Bundle();
        switch (sortBy) {
            case MovieFilter.FILTER_FAVORITE:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_FAVORITE);
                break;
            case MovieFilter.FILTER_MOST_POPULAR:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_POPULAR);
                break;
            case MovieFilter.FILTER_UPCOMING:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_UPCOMING);
                break;
            case MovieFilter.FILTER_NOW_PLAYING:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_NOW_PLAYING);
                break;
            case MovieFilter.FILTER_TOP_RATED:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_TOP_RATED);
                break;
            default:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_POPULAR);
                break;
        }
        getLoaderManager().destroyLoader(MOVIE_LOADER);
        getLoaderManager().initLoader(MOVIE_LOADER, bundle, this);
    }

    private void initLoader(String sortBy) {
        Bundle bundle = new Bundle();
        switch (sortBy) {
            case  MovieFilter.FILTER_FAVORITE:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_FAVORITE);
                getLoaderManager().initLoader(MOVIE_LOADER, bundle, this);
                break;
            case MovieFilter.FILTER_MOST_POPULAR:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_POPULAR);
                getLoaderManager().initLoader(MOVIE_LOADER, bundle, this);
                break;
            case MovieFilter.FILTER_TOP_RATED:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_TOP_RATED);
                getLoaderManager().initLoader(MOVIE_LOADER, bundle, this);
                break;
            case MovieFilter.FILTER_UPCOMING:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_UPCOMING);
                getLoaderManager().initLoader(MOVIE_LOADER, bundle, this);
                break;
            case MovieFilter.FILTER_NOW_PLAYING:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_NOW_PLAYING);
                getLoaderManager().initLoader(MOVIE_LOADER, bundle, this);
                break;
            default:
                bundle.putInt(MovieFilter.PREF_FILTER_BY, MovieContract.TYPE_TOP_RATED);

                break;
        }
        getLoaderManager().initLoader(MOVIE_LOADER, bundle, this);
    }

    private void checkNeedToSync(String filter) {

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
                    bundle.putString(MovieSyncAdapter.EXTRA_FILTER, filter);
                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                    ContentResolver.requestSync(account, MovieContract.CONTENT_AUTHORITY, bundle);
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

    private void setSubtitle(String subtitle) {
        if(isAdded() ) {
            if(getActivity() instanceof AppCompatActivity) {
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setSubtitle(getFilterString(subtitle));
                }
            }
        }
    }
}
