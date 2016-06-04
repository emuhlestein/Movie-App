package com.intelliviz.movieapp3.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.intelliviz.movieapp3.ApiKeyMgr;
import com.intelliviz.movieapp3.MovieFilter;
import com.intelliviz.movieapp3.MovieUtils;
import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.Review;
import com.intelliviz.movieapp3.Trailer;

/**
 * Main activity for movie app
 */
public class MainActivity extends AppCompatActivity implements
        MovieListFragment.OnSelectMovieListener,
        MovieDetailsFragment.OnSelectReviewListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int DETAILS_ACTIVITY = 0;
    private static final String DETAIL_FRAG_TAG = "detail frag tag";
    private static final String LIST_FRAG_TAG = "list frag tag";
    private boolean mIsTablet;
    private String API_KEY = null; // Put api key here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        if (!ApiKeyMgr.checkApiKey(this, API_KEY)) {
            fatalError();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment;
        View detailsView = findViewById(R.id.details_fragment);
        if (detailsView == null) {
            fragment = fm.findFragmentByTag(LIST_FRAG_TAG);
            if(fragment == null) {
                fragment = MovieListFragment.newInstance(2);
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.fragment_holder, fragment, LIST_FRAG_TAG);
                ft.commit();
            }
            mIsTablet = false;
        } else {
            boolean fragmentAdded = false;

            FragmentTransaction ft = fm.beginTransaction();
            fragment = fm.findFragmentByTag(LIST_FRAG_TAG);
            if (fragment == null) {
                fragment = MovieListFragment.newInstance(4);
                ft.add(R.id.fragment_holder, fragment, LIST_FRAG_TAG);
                fragmentAdded = true;
            }

            fragment = fm.findFragmentByTag(DETAIL_FRAG_TAG);
            if(fragment == null) {
                fragment = MovieDetailsFragment.newInstance(null);
                ft.add(R.id.details_fragment, fragment, DETAIL_FRAG_TAG);
                fragmentAdded = true;
            }

            if(fragmentAdded) {
                ft.commit();
            }
            mIsTablet = true;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_favorite) {
            refreshMovieList(MovieFilter.FILTER_FAVORITE);
        } else if(id == R.id.action_top_rated) {
            refreshMovieList(MovieFilter.FILTER_TOP_RATED);
        } else if(id == R.id.action_popular) {
            refreshMovieList(MovieFilter.FILTER_MOST_POPULAR);
        } else if(id == R.id.action_upcoming) {
            refreshMovieList(MovieFilter.FILTER_UPCOMING);
        } else if(id == R.id.action_now_playing) {
            refreshMovieList(MovieFilter.FILTER_NOW_PLAYING);
        } else if(id == R.id.dump_db) {
            MovieUtils.dumpMovies(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /*
    @Override
    public void onSelectMovie(Movie movie) {

        if (mIsTablet) {
            MovieDetailsFragment detailsFragment = ((MovieDetailsFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAG_TAG));
            if (detailsFragment != null) {
                detailsFragment.updateMovie(movie);
            }
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(MovieDetailsActivity.MOVIE_EXTRA, movie);
            intent.putExtra(MovieDetailsActivity.FAVORITE_EXTRA, false);
            startActivityForResult(intent, DETAILS_ACTIVITY);
        }
    }
    */

    @Override
    public void onSelectMovie(String movieId) {
        if (mIsTablet) {
            FragmentManager fm = getSupportFragmentManager();
            MovieDetailsFragment fragment = (MovieDetailsFragment) fm.findFragmentByTag(DETAIL_FRAG_TAG);
            if(fragment != null) {
                fragment.updateMovie(movieId);
            }
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(MovieDetailsActivity.MOVIE_EXTRA, movieId);
            startActivityForResult(intent, DETAILS_ACTIVITY);
        }
    }

    @Override
    public void onSelectReview(Review review) {
        Fragment fragment = MovieReviewFragment.newInstance(review);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_holder, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onSelectTrailer(Trailer trailer) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(trailer.getUrl()));
        startActivity(intent);
    }

    @Override
    public void onUpdateMovieList() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment;
        fragment = fm.findFragmentByTag(LIST_FRAG_TAG);
        if (fragment != null && fragment instanceof MovieListFragment ) {
            ((MovieListFragment)fragment).refreshList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DETAILS_ACTIVITY) {
            if (data != null) {
                boolean refresh = data.getBooleanExtra(MovieDetailsActivity.EXTRA_REFRESH_LIST, false);
                if(refresh) {
                    MovieListFragment movieListFragment = ((MovieListFragment) getSupportFragmentManager()
                            .findFragmentByTag(LIST_FRAG_TAG));
                    if (movieListFragment != null) {
                        movieListFragment.refreshList();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mIsTablet) {
            MovieDetailsFragment detailsFragment = ((MovieDetailsFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAG_TAG));
            if (detailsFragment != null) {
                detailsFragment.updateMovie(null);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void refreshMovieList(String sortBy) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment;
        fragment = fm.findFragmentByTag(LIST_FRAG_TAG);
        if (fragment != null && fragment instanceof MovieListFragment ) {
            ((MovieListFragment)fragment).refreshList(sortBy);
        }
    }

    /**
     * Show fatal error and exit app.
     */
    private void fatalError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Application Error")
                .setMessage("No api key-app must exit");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        builder.show();
    }
}
