package com.intelliviz.movieapp3.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.Review;
import com.intelliviz.movieapp3.Trailer;

public class MovieDetailsActivity extends AppCompatActivity implements
        MovieDetailsFragment.OnSelectReviewListener {
    private static final String LIST_FRAG_TAG = "list frag tag";
    public static final String MOVIE_EXTRA = "movie";
    public static final String EXTRA_REFRESH_LIST = "refresh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        String movieId = intent.getStringExtra(MOVIE_EXTRA);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(LIST_FRAG_TAG);

        if(fragment == null) {
            fragment = MovieDetailsFragment.newInstance(movieId);
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
    public void onUpdateMovieList() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REFRESH_LIST, true);
        this.setResult(RESULT_OK, intent);
    }
}
