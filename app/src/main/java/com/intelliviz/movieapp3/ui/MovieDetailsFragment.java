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
import android.util.Log;
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
import com.intelliviz.movieapp3.Movie;
import com.intelliviz.movieapp3.MovieUtils;
import com.intelliviz.movieapp3.R;
import com.intelliviz.movieapp3.Review;
import com.intelliviz.movieapp3.Trailer;
import com.intelliviz.movieapp3.db.MovieContract;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private Movie mMovie;
    private List<Review> mReviews;
    private List<Trailer> mTrailers;
    private String mMovieUrl;
    private OnSelectReviewListener mListener;
    private boolean mLoadFromDatabase = false;
    private boolean mIsNetworkAvailable = false;
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
         * Mark a movie as favorite.
         * @param movie The movie to mark.
         * @param mReviews The associated reviews.
         */
        void onMarkMovieAsFavorite(Movie movie, List<Review> mReviews);

        /**
         * Un mark a movie as a favorite.
         * @param movie The movie to unmark.
         */
        void onUnmarkMovieAsFavorite(Movie movie);
    }

    /**
     * Create the MovieDetailsFragment.
     * @param movieId The movie to show in the details fragment.
     * @param reviews The reviews for the movie.
     * @return The newly created fragment.
     */
    public static MovieDetailsFragment newInstance(String movieId, ArrayList<Review> reviews) {
        Bundle args = new Bundle();

        args.putString(MOVIE_KEY, movieId);
        //args.putParcelableArrayList(REVIEWS_KEY, reviews);
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
        String[] projection = {MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_TITLE,
                MovieContract.MovieEntry.COLUMN_RELEASE_DATA,
                MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE,
                MovieContract.MovieEntry.COLUMN_POSTER,
                MovieContract.MovieEntry.COLUMN_POPULAR,
                MovieContract.MovieEntry.COLUMN_SYNOPSIS,
                MovieContract.MovieEntry.COLUMN_TOP_RATED,
                MovieContract.MovieEntry.COLUMN_FAVORITE,
                MovieContract.MovieEntry.COLUMN_RUNTIME};
        String selection = MovieContract.MovieEntry.TABLE_NAME + "." +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectiongArgs = {mMovieId};

        Uri uri = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(mMovieId).build();
        movieQueryHandler.startQuery(1, null, uri, null, selection, selectiongArgs, null);

        ReviewQueryHandler reviewQueryHandler = new ReviewQueryHandler(getContext().getContentResolver(), this);
        projection = null;
        selection = null; //MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?";
        selectiongArgs = null; //new String[]{mMovieId};

        uri = MovieContract.ReviewEntry.CONTENT_URI.buildUpon().appendPath(MovieContract.MovieEntry.TABLE_NAME).appendPath(mMovieId).build();
        reviewQueryHandler.startQuery(1, null, uri, projection, selection, selectiongArgs, null);

        TrailerQueryHandler trailerQueryHandler = new TrailerQueryHandler(getContext().getContentResolver(), this);
        projection = null;
        selection = null; //MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?";
        selectiongArgs = null; //new String[]{mMovieId};

        uri = MovieContract.TrailerEntry.CONTENT_URI.buildUpon().appendPath(MovieContract.MovieEntry.TABLE_NAME).appendPath(mMovieId).build();
        trailerQueryHandler.startQuery(1, null, uri, projection, selection, selectiongArgs, null);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_fragment_menu, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = (MenuItem) menu.findItem(R.id.menu_item_share);

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
        mReviews = getArguments().getParcelableArrayList(REVIEWS_KEY);
        mIsNetworkAvailable = MovieListFragment.isNetworkAvailable((AppCompatActivity) getActivity());

        //if(mMovie.isFavorite() || !mIsNetworkAvailable) {
        //    mLoadFromDatabase = true;
       // }

        if(mMovieId != null) {
            //mMovieUrl = ApiKeyMgr.getMovieUrl(mMovie.getMovieId());
        }
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
        outState.putParcelable(SELECTED_MOVIE_KEY, mMovie);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            mMovie = savedInstanceState.getParcelable(SELECTED_MOVIE_KEY);
            if(mMovie != null) {
                mMovieUrl = ApiKeyMgr.getMovieUrl(mMovie.getMovieId());
            }
            updateUI();
        }
    }

    public void updateMovie(Movie movie) {
        mMovie = movie;
        if(mMovie != null) {
            mMovieUrl = ApiKeyMgr.getMovieUrl(mMovie.getMovieId());
        }
        updateUI();
    }

    public void updateSort(boolean isFavorite) {
        clearSelectedMovie();
        updateUI();
    }

    public void onMarkMovieAsFavoriteClick(View view) {
        MarkFavoriteMovieTask task = new MarkFavoriteMovieTask();
        task.execute();
        MovieUtils.updateFavorite(getContext(), mMovieId, 1);
        updateUI();
    }

    public void onUnmarkMovieAsFavoriteClick(View view) {
        RemoveMovieFromFavoritesTask  task = new RemoveMovieFromFavoritesTask();
        task.execute();
        MovieUtils.updateFavorite(getContext(), mMovieId, 0);
        updateUI();
    }

    private void updateUI() {
        /*
        if(mMovieId == null) {
            clearSelectedMovie();
        } else {
            mAddToFavoriteButton.setVisibility(View.VISIBLE);
            mTitleView.setText(mMovie.getTitle());
            mSummaryView.setText(mMovie.getSynopsis());
            mReleaseDateView.setText(mMovie.getReleaseDate());

            if (mIsFavorite) {
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
            String str = mMovie.getReleaseDate();
            try {
                Date date = formatter.parse(mMovie.getReleaseDate());

                formatter = new SimpleDateFormat("yyyy");
                str = formatter.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mReleaseDateView.setText(str);
            mAverageVoteView.setText(new DecimalFormat("#.#").format(Float.parseFloat(mMovie.getAverageVote())) + "/10");

            if(mMovie.getPoster() != null) {
                String url = String.format(ApiKeyMgr.PosterUrl, mMovie.getPoster());
                Picasso
                        .with(getActivity())
                        .load(url)
                        .placeholder(R.mipmap.placeholder)
                        .into(mPosterView);

            }

            mReviewLayout.removeAllViews();
            loadMovieRuntime();
            loadReviews();
            loadTrailers();
        }
        */
    }

    private void loadReviews() {
        if(mLoadFromDatabase) {
            createReviewViews();
        }
        else if(mIsNetworkAvailable && mMovie != null) {
            String url = ApiKeyMgr.getReviewsUrl(mMovie.getMovieId());
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();

                    extractReviewsFromJson(jsonData);

                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createReviewViews();
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadTrailers() {

        if(mIsNetworkAvailable && mMovie != null) {
            String url = ApiKeyMgr.getTrailersUrl(mMovie.getMovieId());
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();

                    extractTrailersFromJson(jsonData);

                    if (response.isSuccessful()) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createTrailerViews();
                            }
                        });
                    }
                }
            });
        } else {
            // network is unavailable
        }
    }

    private void loadMovieRuntime() {

        if(mIsNetworkAvailable) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(mMovieUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    JSONObject moviesObject;
                    try {
                        moviesObject = new JSONObject(jsonData);
                        if (mMovie != null) {
                            mMovie.setRuntime(moviesObject.getString("runtime"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mMovie != null) {
                                    mRuntimeView.setText(mMovie.getRuntime() + "min");
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private List<Review> extractReviewsFromJson(String s) {
        JSONObject reviewsObject = null;
        try {
            mReviews = new ArrayList<>();
            JSONObject oneReview;
            reviewsObject = new JSONObject(s);
            JSONArray reviewArray = reviewsObject.getJSONArray("results");
            Review review;
            for(int i = 0; i < reviewArray.length(); i++) {
                oneReview = reviewArray.getJSONObject(i);
                review = extractReviewFromJson(oneReview);
                if(review != null) {
                    mReviews.add(review);
                }
            }

            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<Review> extractTrailersFromJson(String jsonData) {
        JSONObject trailersObject = null;
        try {
            mTrailers = new ArrayList<>();
            JSONObject oneTrailer;
            trailersObject = new JSONObject(jsonData);
            JSONArray trailerArray = trailersObject.getJSONArray("results");
            Trailer trailer;
            for(int i = 0; i < trailerArray.length(); i++) {
                oneTrailer = trailerArray.getJSONObject(i);
                trailer = extractTrailerFromJson(oneTrailer);
                if(trailer != null) {
                    mTrailers.add(trailer);
                }
            }

            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Trailer extractTrailerFromJson(JSONObject object) {
        try {
            String key = object.getString("key");
            String url = "https://www.youtube.com/watch?v=" + key;
            Trailer trailer = new Trailer(url);
            return trailer;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading trailer");
        }

        return null;
    }

    private Review extractReviewFromJson(JSONObject object) {
        try {
            Review review = null;
            if(mMovie != null) {
                String author = object.getString("author");
                String content = object.getString("content");
                review = new Review(mMovie.getMovieId(), author, content);
            }
            return review;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading review");
        }

        return null;
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

        @Override
        protected Void doInBackground(Void... params) {
            MovieUtils.addMovieToFavorite(getActivity(), mMovie, mReviews);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateUI();
            if(mListener != null) {
                mListener.onMarkMovieAsFavorite(mMovie, mReviews);
            }
        }
    }

    private class RemoveMovieFromFavoritesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            MovieUtils.removeMovieFromFavorites(getActivity(), mMovie);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateUI();
            if(mListener != null) {
                mListener.onUnmarkMovieAsFavorite(mMovie);
            }
        }
    }

    private class MovieReview {
        String author;
        String content;
    }
}
