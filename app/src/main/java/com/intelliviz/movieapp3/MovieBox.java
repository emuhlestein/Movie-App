package com.intelliviz.movieapp3;

import java.util.ArrayList;

/**
 * Created by edm on 5/4/2016.
 */
public class MovieBox {
    private static MovieBox sMovieBox;
    private ArrayList<Movie> mMovies = new ArrayList<>();

    public static MovieBox get() {
        if(sMovieBox == null) {
            sMovieBox = new MovieBox();
        }
        return sMovieBox;
    }

    private MovieBox() {
    }

    public void addMovies(ArrayList<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }
}
