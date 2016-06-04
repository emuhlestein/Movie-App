package com.intelliviz.movieapp3.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.Review;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by edm on 4/2/2016.
 */
public class MovieReviewFragment extends Fragment {
    private static final String REVIEW_KEY = "review_key";
    private Review mReview;

    @Bind(R.id.reviewText) TextView mReviewText;

    public MovieReviewFragment() {
    }

    public static MovieReviewFragment newInstance(Review review) {
        Bundle args = new Bundle();

        MovieReviewFragment fragment = new MovieReviewFragment();
        args.putParcelable(REVIEW_KEY, review);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_review, container, false);
        ButterKnife.bind(this, view);

        mReviewText.setText(mReview.getContent());

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setSubtitle(mReview.getAuthor());
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReview = getArguments().getParcelable(REVIEW_KEY);
        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
