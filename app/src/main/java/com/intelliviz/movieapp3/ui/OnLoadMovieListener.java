package com.intelliviz.movieapp3.ui;

import android.database.Cursor;

/**
 * Created by edm on 5/20/2016.
 */
public interface OnLoadMovieListener {
    void onLoadMovie(Cursor cursor);
    void onLoadReview(Cursor cursor);
    void onLoadTrailer(Cursor cursor);
}
