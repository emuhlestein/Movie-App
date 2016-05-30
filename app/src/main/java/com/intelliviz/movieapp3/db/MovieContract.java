package com.intelliviz.movieapp3.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by edm on 4/4/2016.
 */
public class MovieContract {
    public static final String CONTENT_AUTHORITY =
            "com.intelliviz.movieapp3.db.MovieProvider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "themoviedb.org";
    // The account name
    public static final String ACCOUNT = "movieaccount";

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_MOVIE_TYPE = "type";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_STATE = "state";

    public static final int TYPE_FAVORITE = 1;
    public static final int TYPE_POPULAR = 2;
    public static final int TYPE_TOP_RATED = 3;
    public static final int LOAD_STATUS = 4;

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = PATH_MOVIE;
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_RELEASE_DATA = "release_date";
        public static final String COLUMN_AVERAGE_VOTE = "ave_vote";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_FAVORITE = "favorite";
        public static final String COLUMN_TOP_RATED = "top_rated";
        public static final String COLUMN_POPULAR = "popular";

        public static Uri buildMovieByListTypeUri(String type) {
            return CONTENT_URI.buildUpon().appendPath(type).build();
        }
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static final String TABLE_NAME = PATH_REVIEW;
        public static final String COLUMN_MOVIE_ID = "movie_id"; // FK
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "title";
        public static final String COLUMN_CONTENT = "content";
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static final String TABLE_NAME = PATH_TRAILER;
        public static final String COLUMN_MOVIE_ID = "movie_id"; // FK
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_URL = "url";
    }

    public static final class StateEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATE).build();
        public static final int STATUS_UPDATED = 0;
        public static final int STATUS_UPDATING = 1;
        public static final int STATUS_ERROR = 2;

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATE;

        public static final String TABLE_NAME = PATH_STATE;
        public static final String COLUMN_PAGE = "page";
        public static final String COLUMN_STATUS = "status";
    }
}
