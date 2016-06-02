package com.intelliviz.movieapp3;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.intelliviz.movieapp3.ui.MovieListFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for creating views that display images.
 *
 * Created by edm on 3/18/2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieHolder> {
    public static final String TAG = MovieAdapter.class.getSimpleName();
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Movie> mMovies;
    private MovieListFragment.OnSelectMovieListener mListener;

    public MovieAdapter(Context context, List<Movie> movies) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mMovies = movies;
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_item_layout, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieHolder holder, int position) {
        Movie movie = mMovies.get(position);
        holder.bindMovie(movie);

        String url = String.format(ApiKeyMgr.PosterUrl, movie.getPoster());

        if (url != null) {
            Picasso
                    .with(mContext)
                    .load(url)
                    .into((ImageView) holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public void setMovies(ArrayList<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    public void clear() {
        mMovies.clear();
    }

    public void setOnSelectMovieListener (MovieListFragment.OnSelectMovieListener listener) {
        mListener = listener;
    }

    public class MovieHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener {
        private Movie mMovie;
        private ImageView mImageView;

        public MovieHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView;
            mImageView.setOnClickListener(this);
        }

        public Movie getMovie() {
            return mMovie;
        }

        public void bindMovie(Movie movie) {
            mMovie = movie;
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onSelectMovie("");
            }
        }
    }
}
