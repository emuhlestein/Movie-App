package com.intelliviz.movieapp3.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by edm on 4/4/2016.
 */
public class MovieProvider extends ContentProvider {
    private SqliteHelper mSqliteHelper;
    private static final String DBASE_NAME = "movies";
    private static final int DBASE_VERSION = 13;
    private static final int MOVIE_LIST = 101;
    private static final int MOVIE_ID = 102;
    private static final int MOVIE_TYPE_ID = 103;
    private static final int REVIEW_LIST = 201;
    private static final int REVIEW_MOVIE_LIST = 202;
    private static final int REVIEW_ID = 203;
    private static final int TRAILER_LIST = 301;
    private static final int TRAILER_MOVIE_LIST = 302;
    private static final int TRAILER_ID = 303;
    private static final int STATE_ID = 401;

    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher((UriMatcher.NO_MATCH));

        // all movies
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIE_LIST);

        // a particular movie
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_ID);

        // a particular type of movie
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/*", MOVIE_TYPE_ID);

        // list of reviews
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW, REVIEW_LIST);

        // the reviews for a particular movie
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW + "/" + MovieContract.PATH_MOVIE + "/*", REVIEW_MOVIE_LIST);

        // a particular review
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW + "/*", REVIEW_ID);

        // list of trailers
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER, TRAILER_LIST);

        // the trailer for a particular movie
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER + "/" + MovieContract.PATH_MOVIE + "/*", TRAILER_MOVIE_LIST);

        // a particular trailer
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER + "/*", TRAILER_ID);

        // the state
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_STATE, STATE_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mSqliteHelper = new SqliteHelper(context);
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEW_MOVIE_LIST:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_LIST:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_ID:
                return MovieContract.ReviewEntry.CONTENT_ITEM_TYPE;
            case TRAILER_MOVIE_LIST:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER_LIST:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER_ID:
                return MovieContract.TrailerEntry.CONTENT_ITEM_TYPE;
            case STATE_ID:
                return MovieContract.StateEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                // get all movies: "movie/"
                sqLiteQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
                break;
            case MOVIE_ID:
                // get a particular movie: "movie/#"
                sqLiteQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                        "=" + uri.getLastPathSegment());
                break;
            case MOVIE_TYPE_ID:
                // get a list of movies by type
                sqLiteQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
                break;
            case REVIEW_LIST:
                // get all reviews: "review/movie/<movieid>"
                sqLiteQueryBuilder.setTables(MovieContract.ReviewEntry.TABLE_NAME);
                break;
            case REVIEW_MOVIE_LIST:
                // get reviews for a particular movie: "review/movie/#"
                sqLiteQueryBuilder.setTables(MovieContract.ReviewEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MovieContract.ReviewEntry.COLUMN_MOVIE_ID +
                        "=" + uri.getLastPathSegment());
                break;
            case REVIEW_ID:
                // get a particular review: "review/*"
                sqLiteQueryBuilder.setTables(MovieContract.ReviewEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MovieContract.ReviewEntry.COLUMN_REVIEW_ID +
                        "='" + uri.getLastPathSegment() + "'");
                break;
            case TRAILER_LIST:
                // get all reviews: "trailer/movie/<movieid>"
                sqLiteQueryBuilder.setTables(MovieContract.TrailerEntry.TABLE_NAME);
                break;
            case TRAILER_MOVIE_LIST:
                // get reviews for a particular movie: "trailer/movie/#"
                sqLiteQueryBuilder.setTables(MovieContract.TrailerEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MovieContract.TrailerEntry.COLUMN_MOVIE_ID +
                        "=" + uri.getLastPathSegment());
                break;
            case TRAILER_ID:
                // get a particular review: "trailer/#"
                sqLiteQueryBuilder.setTables(MovieContract.TrailerEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MovieContract.TrailerEntry.COLUMN_TRAILER_ID +
                        "='" + uri.getLastPathSegment() + "'");
                break;
            case STATE_ID:
                // get the state: "state" (there should be only one)
                sqLiteQueryBuilder.setTables(MovieContract.StateEntry.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }

        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId;
        SQLiteDatabase db;
        Uri returnUri;

        db = mSqliteHelper.getWritableDatabase();

        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                // The second parameter will allow an empty row to be inserted. If it was null, then no row
                // can be inserted if values is empty.
                rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (rowId > -1) {
                    returnUri = ContentUris.withAppendedId(uri, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case REVIEW_LIST:
                // The second parameter will allow an empty row to be inserted. If it was null, then no row
                // can be inserted if values is empty.
                rowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (rowId > -1) {
                    returnUri = ContentUris.withAppendedId(uri, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case TRAILER_LIST:
                // The second parameter will allow an empty row to be inserted. If it was null, then no row
                // can be inserted if values is empty.
                rowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if (rowId > -1) {
                    returnUri = ContentUris.withAppendedId(uri, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri.toString());
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        int rowsDeleted = 0;
        String id;

        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry._ID + "=" + id, null);
                break;
            case REVIEW_LIST:
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME,
                        MovieContract.ReviewEntry.COLUMN_REVIEW_ID + "=" + id, null);
                break;
            case TRAILER_LIST:
                rowsDeleted = db.delete(MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = db.delete(MovieContract.TrailerEntry.TABLE_NAME,
                        MovieContract.TrailerEntry.COLUMN_TRAILER_ID + "=" + id, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        String id;

        switch(sUriMatcher.match(uri)) {
            case MOVIE_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                            values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                            new String[]{id});
                } else {
                    rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                            values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            case REVIEW_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME,
                            values,
                            MovieContract.ReviewEntry._ID + "=?",
                            new String[]{id});
                } else {
                    rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME,
                            values,
                            MovieContract.ReviewEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            case TRAILER_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MovieContract.TrailerEntry.TABLE_NAME,
                            values,
                            MovieContract.TrailerEntry._ID + "=?",
                            new String[]{id});
                } else {
                    rowsUpdated = db.update(MovieContract.TrailerEntry.TABLE_NAME,
                            values,
                            MovieContract.TrailerEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            case STATE_ID:
                rowsUpdated = db.update(MovieContract.StateEntry.TABLE_NAME,
                        values, null, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private static class SqliteHelper extends SQLiteOpenHelper {

        public SqliteHelper(Context context) {
            super(context, DBASE_NAME, null, DBASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create the movie table
            String sql = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME +
                    " ( " + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_RELEASE_DATA + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_POPULAR + " INTEGER NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_TOP_RATED + " INTEGER NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_UPCOMING + " INTEGER NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL);";

            db.execSQL(sql);

            // create the review table
            sql = "CREATE TABLE " + MovieContract.ReviewEntry.TABLE_NAME +
                    " ( " + MovieContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL," +
                    MovieContract.ReviewEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL," +
                    MovieContract.ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                    MovieContract.ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL);";

            db.execSQL(sql);

            // create the trailer table
            sql = "CREATE TABLE " + MovieContract.TrailerEntry.TABLE_NAME +
                    " ( " + MovieContract.TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL," +
                    MovieContract.TrailerEntry.COLUMN_TRAILER_ID + " TEXT NOT NULL," +
                    MovieContract.TrailerEntry.COLUMN_URL + " TEXT NOT NULL);";

            db.execSQL(sql);

            // create the state table
            sql = "CREATE TABLE " + MovieContract.StateEntry.TABLE_NAME +
                    " ( " + MovieContract.StateEntry._ID + " INTEGER NOT NULL, " +
                    MovieContract.StateEntry.COLUMN_PAGE + " INTEGER NOT NULL, " +
                    MovieContract.StateEntry.COLUMN_STATUS + " INTEGER NOT NULL);";

            db.execSQL(sql);

            String ROW = "INSERT INTO " + MovieContract.StateEntry.TABLE_NAME + " Values ('0', '-1', '0');";
            db.execSQL(ROW);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MovieContract.StateEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}
