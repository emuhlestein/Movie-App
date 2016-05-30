package com.intelliviz.movieapp3;

/**
 * Created by edm on 5/14/2016.
 */
public class MovieState {
    private int _page;

    public MovieState() {
        this(-1);
    }

    public MovieState(int page) {
        _page = page;
    }

    public int getPage() {
        return _page;
    }

    public void setPage(int page) {
        _page = page;
    }
}
