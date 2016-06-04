package com.intelliviz.movieapp3.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Details activity. Show the details of a selected movie.
 */
public class MovieDetailsFragment extends Fragment implements OnLoadMovieListener, OnMarkFavoriteMovieListener {
    private static final String MOVIE_KEY = "movie_key";
    private String mMovieId;
    private String mNewMovieId;
    private List<Review> mReviews;
    private List<Trailer> mTrailers;
    private OnSelectReviewListener mListener;

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


    public MovieDetailsFragment() {
        mNewMovieId = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        ButterKnife.bind(this, view);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(mNewMovieId != null ) {
            mMovieId = mNewMovieId;
            mNewMovieId = null;
        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            String movieId = sp.getString(MOVIE_KEY, null);
            if (movieId != null) {
                mMovieId = movieId;
            }
        }
        mAddToFavoriteButton.setVisibility(View.GONE);
        if(mMovieId != null) {
            updateMovie(mMovieId);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        mNewMovieId = getArguments().getString(MOVIE_KEY);
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
        outState.putString(MOVIE_KEY, mMovieId);
    }

    public void updateMovie(String movieId) {
        if(movieId == null){
            return;
        }

        mMovieId = movieId;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(MOVIE_KEY, movieId);
        editor.apply();

        clearSelectedMovie();
        MovieQueryHandler movieQueryHandler = new MovieQueryHandler(getContext().getContentResolver(), this);
        String selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {mMovieId};

        Uri uri = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(mMovieId).build();
        movieQueryHandler.startQuery(1, null, uri, null, selection, selectionArgs, null);

        ReviewQueryHandler reviewQueryHandler = new ReviewQueryHandler(getContext().getContentResolver(), this);

        uri = MovieContract.ReviewEntry.CONTENT_URI.buildUpon().appendPath(MovieContract.MovieEntry.TABLE_NAME).appendPath(mMovieId).build();
        reviewQueryHandler.startQuery(1, null, uri, null, null, null, null);

        TrailerQueryHandler trailerQueryHandler = new TrailerQueryHandler(getContext().getContentResolver(), this);

        uri = MovieContract.TrailerEntry.CONTENT_URI.buildUpon().appendPath(MovieContract.MovieEntry.TABLE_NAME).appendPath(mMovieId).build();
        trailerQueryHandler.startQuery(1, null, uri, null, null, null, null);
    }

    @Override
    public void onMarkFavoriteMovie(int favorite) {
        updateUI(favorite);
        if(mListener != null) {
            mListener.onUpdateMovieList();
        }
    }

    public void onMarkMovieAsFavoriteClick() {
        new MarkFavoriteMovieTask(getContext(), mMovieId, 1, this).execute();
        if(mListener != null) {
            mListener.onUpdateMovieList();
        }
    }

    public void onUnmarkMovieAsFavoriteClick() {
        new MarkFavoriteMovieTask(getContext(), mMovieId, 0, this).execute();
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
                    onUnmarkMovieAsFavoriteClick();
                }
            });
        } else {
            String markFavorite = getActivity().getResources().getString(R.string.mark_as_favorite);
            mAddToFavoriteButton.setText(markFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMarkMovieAsFavoriteClick();
                }
            });
        }
    }

    private View createReviewRow(int num) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // With false as 3rd parameter, view is not added to parent, but the layourparams
        // are those of the parent. View will have the textview. It has to be added
        // manually, so return it.
        Button view = (Button) inflater.inflate(R.layout.review_item_layout, mReviewLayout, false);

        StringBuilder sb = new StringBuilder();
        sb.append(getContext().getString(R.string.review_string));
        sb.append(" ");
        sb.append(Integer.toString(num + 1));
        view.setText(sb.toString());
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

    private View createTrailerRow(int num) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // With false as 3rd parameter, view is not added to parent, but the layourparams
        // are those of the parent. View will have the textview. It has to be added
        // manually, so return it.
        Button view = (Button) inflater.inflate(R.layout.review_item_layout, mReviewLayout, false);

        StringBuilder sb = new StringBuilder();
        sb.append(getContext().getString(R.string.trailer_string));
        sb.append(" ");
        sb.append(Integer.toString(num + 1));
        view.setText(sb.toString());
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
        if(cursor == null || !cursor.moveToFirst()) {
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
        
        StringBuilder sb = new StringBuilder();
        sb.append(runtime);
        sb.append(getContext().getString(R.string.min_label));
        mRuntimeView.setText(sb.toString());

        if (favorite) {
            String unmarkFavorite = getActivity().getResources().getString(R.string.unmark_favorite);
            mAddToFavoriteButton.setText(unmarkFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUnmarkMovieAsFavoriteClick();
                }
            });
        } else {
            String markFavorite = getActivity().getResources().getString(R.string.mark_as_favorite);
            mAddToFavoriteButton.setText(markFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMarkMovieAsFavoriteClick();
                }
            });
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd", Locale.US);
        String str = releaseDate;
        try {
            Date date = formatter.parse(releaseDate);

            formatter = new SimpleDateFormat("yyyy", Locale.US);
            str = formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mReleaseDateView.setText(str);

        String aveVoteString = String.format(Locale.US, "%1$.1f", Float.parseFloat(aveVote)) + "/10";
        mAverageVoteView.setText(aveVoteString);

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

        public MovieQueryHandler(ContentResolver cr, OnLoadMovieListener listener) {
            super(cr);
            mListener = new WeakReference<>(listener);
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

        public ReviewQueryHandler(ContentResolver cr, OnLoadMovieListener listener) {
            super(cr);
            mListener = new WeakReference<>(listener);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final OnLoadMovieListener listener = mListener.get();
            if(listener != null) {
                listener.onLoadReview(cursor);
            }
        }
    }

    private static class TrailerQueryHandler extends AsyncQueryHandler {

        private WeakReference<OnLoadMovieListener> mListener;

        public TrailerQueryHandler(ContentResolver cr, OnLoadMovieListener listener) {
            super(cr);
            mListener = new WeakReference<>(listener);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final OnLoadMovieListener listener = mListener.get();
            if(listener != null) {
                listener.onLoadTrailer(cursor);
            }
        }
    }

    private static class MarkFavoriteMovieTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private String mMovieId;
        private int mFavorite;
        private OnMarkFavoriteMovieListener mListener;

        public MarkFavoriteMovieTask(Context context, String movieId, int favorite, OnMarkFavoriteMovieListener listener) {
            mContext = context;
            mMovieId = movieId;
            mFavorite = favorite;
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            MovieUtils.updateFavoriteMovie(mContext, mMovieId, mFavorite);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(mListener != null) {
                mListener.onMarkFavoriteMovie(mFavorite);
            }
        }
    }
}
