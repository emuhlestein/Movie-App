package com.intelliviz.movieapp3.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelliviz.movieapp3.ApiKeyMgr;
import com.intelliviz.movieapp3.MovieUtils;
import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.Review;
import com.intelliviz.movieapp3.Trailer;
import com.intelliviz.movieapp3.db.MovieContract;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Details activity. Show the details of a selected movie.
 */
public class MovieDetailsFragment extends Fragment implements OnLoadMovieListener {
    private static final String TAG = MovieDetailsFragment.class.getSimpleName();
    private static final String MOVIE_KEY = "movie_key";
    private static final String FAVORITE_KEY = "favorite_key";
    private static final String REVIEWS_KEY = "reviews_key";
    private static final String SELECTED_MOVIE_KEY = "selected_movie";
    public static final String MOVIE_TO_DELETE_EXTRA = "movie to delete";
    private String mMovieId;
    //private Movie mMovie;
    private List<Review> mReviews;
    private List<Trailer> mTrailers;
    //private String mMovieUrl;
    private OnSelectReviewListener mListener;
    //private boolean mLoadFromDatabase = false;
    //private boolean mIsNetworkAvailable = false;
    private ShareActionProvider mShareActionProvider;

    @Bind(R.id.posterView) ImageView mPosterView;
    @Bind(R.id.titleView) TextView mTitleView;
    @Bind(R.id.summaryView) TextView mSummaryView;
    @Bind(R.id.releaseDateView) TextView mReleaseDateView;
    @Bind(R.id.runtimeView) TextView mRuntimeView;
    @Bind(R.id.averageVoteView) TextView mAverageVoteView;
    @Bind(R.id.review_layout) LinearLayout mReviewLayout;
    @Bind(R.id.addToFavoritesButton) Button mAddToFavoriteButton;


    public interface OnSelectReviewListener {

        /**
         * A movie review has been selected.
         * @param review The review.
         */
        void onSelectReview(Review review);

        /**
         * A movie trailer has been selected.
         * @param trailer The trailer.
         */
        void onSelectTrailer(Trailer trailer);

        /**
         *
         */
        void onUpdateMovieList();
    }

    /**
     * Create the MovieDetailsFragment.
     * @param movieId The movie to show in the details fragment.
     * @return The newly created fragment.
     */
    public static MovieDetailsFragment newInstance(String movieId) {
        Bundle args = new Bundle();

        args.putString(MOVIE_KEY, movieId);
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        ButterKnife.bind(this, view);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //updateUI();
        MovieQueryHandler movieQueryHandler = new MovieQueryHandler(getContext().getContentResolver(), this);
        String[] projection = null;
        String selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectiongArgs = {mMovieId};

        Uri uri = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(mMovieId).build();
        movieQueryHandler.startQuery(1, null, uri, null, selection, selectiongArgs, null);

        ReviewQueryHandler reviewQueryHandler = new ReviewQueryHandler(getContext().getContentResolver(), this);
        projection = null; // select all columns
        selection = null;
        selectiongArgs = null;

        uri = MovieContract.ReviewEntry.CONTENT_URI.buildUpon().appendPath(MovieContract.MovieEntry.TABLE_NAME).appendPath(mMovieId).build();
        reviewQueryHandler.startQuery(1, null, uri, projection, selection, selectiongArgs, null);

        TrailerQueryHandler trailerQueryHandler = new TrailerQueryHandler(getContext().getContentResolver(), this);
        projection = null;
        selection = null;
        selectiongArgs = null;

        uri = MovieContract.TrailerEntry.CONTENT_URI.buildUpon().appendPath(MovieContract.MovieEntry.TABLE_NAME).appendPath(mMovieId).build();
        trailerQueryHandler.startQuery(1, null, uri, projection, selection, selectiongArgs, null);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_fragment_menu, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        mMovieId = getArguments().getString(MOVIE_KEY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnSelectReviewListener) {
            mListener = (OnSelectReviewListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.menu_item_share:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_MOVIE_KEY, mMovieId);
    }

    /*
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            //mMovieId = savedInstanceState.getString(SELECTED_MOVIE_KEY);
            //if(mMovie != null) {
            //    mMovieUrl = ApiKeyMgr.getMovieUrl(mMovie.getMovieId());
            //}
            updateUI(0);
        }
    }
    */

    public void updateMovie(String movieId) {
        //mMovie = movie;
        //if(mMovie != null) {
        //    mMovieUrl = ApiKeyMgr.getMovieUrl(mMovie.getMovieId());
        //}
        updateUI(0);
    }

    public void onMarkMovieAsFavoriteClick(View view) {
        new MarkFavoriteMovieTask(mMovieId, 1).execute();
        if(mListener != null) {
            mListener.onUpdateMovieList();
        }
    }

    public void onUnmarkMovieAsFavoriteClick(View view) {
        new MarkFavoriteMovieTask(mMovieId, 0).execute();
        if(mListener != null) {
            mListener.onUpdateMovieList();
        }
    }

    private void updateUI(int favorite) {
        if (favorite == 1) {
            String unmarkFavorite = getActivity().getResources().getString(R.string.unmark_favorite);
            mAddToFavoriteButton.setText(unmarkFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUnmarkMovieAsFavoriteClick(v);
                }
            });
        } else {
            String markFavorite = getActivity().getResources().getString(R.string.mark_as_favorite);
            mAddToFavoriteButton.setText(markFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMarkMovieAsFavoriteClick(v);
                }
            });
        }
    }

    private void createReviewViews() {
        if(mReviews == null || mReviews.size() == 0) {
            return;
        }

        for(int i = 0; i < mReviews.size(); i++) {
            View view = createReviewRow(i);
            mReviewLayout.addView(view);
        }
    }

    private View createReviewRow(int num) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // With false as 3rd parameter, view is not added to parent, but the layourparams
        // are those of the parent. View will have the textview. It has to be added
        // manually, so return it.
        Button view = (Button) inflater.inflate(R.layout.review_item_layout, mReviewLayout, false);
        view.setText("Review " + (num+1));
        view.setTag(num);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int num = (int) v.getTag();
                    mListener.onSelectReview(mReviews.get(num));
                }
            }
        });

        return view;
    }

    private void createTrailerViews() {
        if(mTrailers == null || mTrailers.size() == 0) {
            return;
        }

        createShareIntent(mTrailers.get(0).getUrl());

        for(int i = 0; i < mTrailers.size(); i++) {
            View view = createTrailerRow(i);
            mReviewLayout.addView(view);
        }
    }

    private View createTrailerRow(int num) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // With false as 3rd parameter, view is not added to parent, but the layourparams
        // are those of the parent. View will have the textview. It has to be added
        // manually, so return it.
        Button view = (Button) inflater.inflate(R.layout.review_item_layout, mReviewLayout, false);
        view.setText("Trailer " + (num+1));
        view.setTag(num);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int num = (int) v.getTag();
                    mListener.onSelectTrailer(mTrailers.get(num));
                }
            }
        });

        return view;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void createShareIntent(String url) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("video/mp4");

        setShareIntent(intent);
    }

    private void clearSelectedMovie() {
        mAddToFavoriteButton.setVisibility(View.GONE);
        mTitleView.setText(R.string.no_movie_selected);
        mSummaryView.setText("");
        mReleaseDateView.setText("");
        mAverageVoteView.setText("");
        mPosterView.setImageResource(android.R.color.transparent);
        mReviewLayout.removeAllViews();
        mRuntimeView.setText("");

        if(mReviews != null) {
            mReviews.clear();
        }

        if(mTrailers != null) {
            mTrailers.clear();
        }
    }

    @Override
    public void onLoadMovie(Cursor cursor) {
        if(!cursor.moveToFirst()) {
            return;
        }

        String title = "";
        String synopsis = "";
        String releaseDate = "";
        String aveVote = "";
        String poster = "";
        String runtime = "0";
        boolean favorite = false;
        int titleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        int synopsisIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
        int favoriteIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_FAVORITE);
        int releaseDateIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATA);
        int aveVoteIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE);
        int posterIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
        int runtimeIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RUNTIME);
        if(titleIndex != -1) {
            title = cursor.getString(titleIndex);
        }
        if(synopsisIndex != -1) {
            synopsis = cursor.getString(synopsisIndex);
        }
        if(favoriteIndex != -1) {
            favorite = (cursor.getInt(favoriteIndex) == 1);
        }
        if(releaseDateIndex != -1) {
            releaseDate = cursor.getString(releaseDateIndex);
        }
        if(aveVoteIndex != -1) {
            aveVote = cursor.getString(aveVoteIndex);
        }
        if(posterIndex != -1) {
            poster = cursor.getString(posterIndex);
        }
        if(runtimeIndex != -1) {
            runtime = cursor.getString(runtimeIndex);
        }

        mAddToFavoriteButton.setVisibility(View.VISIBLE);

        mTitleView.setText(title);
        mSummaryView.setText(synopsis);
        mRuntimeView.setText(runtime + "min");

        if (favorite) {
            String unmarkFavorite = getActivity().getResources().getString(R.string.unmark_favorite);
            mAddToFavoriteButton.setText(unmarkFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUnmarkMovieAsFavoriteClick(v);
                }
            });
        } else {
            String markFavorite = getActivity().getResources().getString(R.string.mark_as_favorite);
            mAddToFavoriteButton.setText(markFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMarkMovieAsFavoriteClick(v);
                }
            });
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
        String str = releaseDate;
        try {
            Date date = formatter.parse(releaseDate);

            formatter = new SimpleDateFormat("yyyy");
            str = formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mReleaseDateView.setText(str);
        mAverageVoteView.setText(new DecimalFormat("#.#").format(Float.parseFloat(aveVote)) + "/10");

        if(!poster.equals("")) {
            String url = String.format(ApiKeyMgr.PosterUrl, poster);
            Picasso
                    .with(getActivity())
                    .load(url)
                    .placeholder(R.mipmap.placeholder)
                    .into(mPosterView);
        }
    }

    @Override
    public void onLoadReview(Cursor cursor) {
        int i = 0;
        mReviews = new ArrayList<>();
        while (cursor.moveToNext()) {
            String author = "";
            String content = "";
            int authorIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR);
            int contentIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT);
            if(authorIndex != -1) {
                author = cursor.getString(authorIndex);
            }
            if(contentIndex != -1 ) {
                content = cursor.getString(contentIndex);
            }
            Review review = new Review(mMovieId, author, content);
            mReviews.add(review);

            View view = createReviewRow(i);
            mReviewLayout.addView(view);
            i++;
        }
    }

    @Override
    public void onLoadTrailer(Cursor cursor) {
        int i = 0;
        mTrailers = new ArrayList<>();
        while (cursor.moveToNext()) {
            String url = "";
            int urlIndex = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_URL);
            if(urlIndex != -1) {
                url = cursor.getString(urlIndex);
            }
            Trailer trailer = new Trailer(url);
            mTrailers.add(trailer);

            View view = createTrailerRow(i);
            mReviewLayout.addView(view);
            i++;
        }
    }

    private class MovieQueryHandler extends AsyncQueryHandler {

        private WeakReference<OnLoadMovieListener> mListener;

        public MovieQueryHandler(ContentResolver cr) {
            super(cr);
        }

        public MovieQueryHandler(ContentResolver cr, OnLoadMovieListener listener) {
            super(cr);
            mListener = new WeakReference<OnLoadMovieListener>(listener);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final OnLoadMovieListener listener = mListener.get();
            if(listener != null) {
                listener.onLoadMovie(cursor);
            }
        }
    }

    private class ReviewQueryHandler extends AsyncQueryHandler {

        private WeakReference<OnLoadMovieListener> mListener;

        public ReviewQueryHandler(ContentResolver cr) {
            super(cr);
        }

        public ReviewQueryHandler(ContentResolver cr, OnLoadMovieListener listener) {
            super(cr);
            mListener = new WeakReference<OnLoadMovieListener>(listener);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final OnLoadMovieListener listener = mListener.get();
            if(listener != null) {
                listener.onLoadReview(cursor);
            }
        }
    }

    private class TrailerQueryHandler extends AsyncQueryHandler {

        private WeakReference<OnLoadMovieListener> mListener;

        public TrailerQueryHandler(ContentResolver cr) {
            super(cr);
        }

        public TrailerQueryHandler(ContentResolver cr, OnLoadMovieListener listener) {
            super(cr);
            mListener = new WeakReference<OnLoadMovieListener>(listener);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final OnLoadMovieListener listener = mListener.get();
            if(listener != null) {
                listener.onLoadTrailer(cursor);
            }
        }
    }

    private class MarkFavoriteMovieTask extends AsyncTask<Void, Void, Void> {
        private String mMovieId;
        private int mFavorite;

        public MarkFavoriteMovieTask(String movieId, int favorite) {
            mMovieId = movieId;
            mFavorite = favorite;
        }

        @Override
        protected Void doInBackground(Void... params) {
            MovieUtils.updateFavoriteMovie(getContext(), mMovieId, mFavorite);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateUI(mFavorite);
        }
    }
}
