package com.intelliviz.movieapp3.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.intelliviz.movieapp3.ApiKeyMgr;
import com.intelliviz.movieapp3.Movie;
import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.Review;
import com.intelliviz.movieapp3.Trailer;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity implements
        MovieDetailsFragment.OnSelectReviewListener {
    private static final String LIST_FRAG_TAG = "list frag tag";
    public static final String MOVIE_EXTRA = "movie";
    public static final String FAVORITE_EXTRA = ApiKeyMgr.DEFAULT_SORT;
    public static final String REVIEWS_EXTRA = "reviews";
    public static final String REFRESH_LIST_EXTRA = "refresh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        String movieId = intent.getStringExtra(MOVIE_EXTRA);
        ArrayList<Review> reviews = intent.getParcelableArrayListExtra(REVIEWS_EXTRA);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(LIST_FRAG_TAG);

        if(fragment == null) {
            fragment = MovieDetailsFragment.newInstance(movieId, reviews);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_holder, fragment, LIST_FRAG_TAG);
            ft.commit();
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
    public void onMarkMovieAsFavorite(Movie movie, List<Review> reviews) {
       // send message to main activity

    }

    @Override
    public void onUnmarkMovieAsFavorite(Movie movie) {
        // send message to main activity
        Intent data = new Intent();
        data.putExtra(REFRESH_LIST_EXTRA, 1);
        setResult(0, data);
    }
}
