package com.intelliviz.movieapp3.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by edm on 5/12/2016.
 */
public class MovieAuthenticationService extends Service {
    private MovieAccountAuthenticator mMovieAccountAuthenticator;

    @Override
    public void onCreate() {
        mMovieAccountAuthenticator = new MovieAccountAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMovieAccountAuthenticator.getIBinder();
    }
}
