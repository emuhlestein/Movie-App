package com.intelliviz.movieapp3;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.intelliviz.movieapp3.db.MovieContract;
import com.intelliviz.movieapp3.ui.MovieListFragment;
import com.squareup.picasso.Picasso;

/**
 * Created by edm on 4/6/2016.
 */
public class MovieCursorAdapter extends RecyclerView.Adapter<MovieCursorAdapter.MovieHolder> {
    private Cursor mCursor;
    private Context mContext;
    private MovieListFragment.OnSelectMovieListener mListener;

    public MovieCursorAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_item_layout, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieHolder holder, int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return;
        }

        int posterIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
        int movieIdIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);

        if(movieIdIndex == -1 || posterIndex == -1) {
            return;
        }
        String movieId = mCursor.getString(movieIdIndex);
        String poster = mCursor.getString(posterIndex);

        holder.bindMovie(movieId);

        String url = String.format(ApiKeyMgr.PosterUrl, poster);

        if (url != null) {
            Picasso
                    .with(mContext)
                    .load(url)
                    .placeholder(R.mipmap.placeholder)
                    .into((ImageView) holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        if(mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setOnSelectMovieListener (MovieListFragment.OnSelectMovieListener listener) {
        mListener = listener;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MovieHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private String mMovieId;
        private ImageView mImageView;

        public MovieHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView;
            mImageView.setOnClickListener(this);
        }

        public String getMovieId() {
            return mMovieId;
        }

        public void bindMovie(String movieId) {
            mMovieId = movieId;
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onSelectFavoriteMovie(mMovieId);
            }
        }
    }
}
