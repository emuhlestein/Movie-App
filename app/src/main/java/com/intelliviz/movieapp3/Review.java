package com.intelliviz.movieapp3;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by edm on 3/31/2016.
 */
public class Review implements Parcelable {
    private String mAuthor;
    private String mContent;
    private String mMovieId;

    public Review() {
    }

    public Review(String movieId, String author, String content) {
        mMovieId = movieId;
        mAuthor = author;
        mContent = content;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public Review(Parcel in) {
        mMovieId = in.readString();
        mAuthor = in.readString();
        mContent = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMovieId);
        dest.writeString(mAuthor);
        dest.writeString(mContent);
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {

        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
