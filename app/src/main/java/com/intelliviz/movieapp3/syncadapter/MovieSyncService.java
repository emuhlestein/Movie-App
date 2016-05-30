package com.intelliviz.movieapp3.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by edm on 5/12/2016.
 */
public class MovieSyncService extends Service {
    private static MovieSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if(sSyncAdapter == null) {
                sSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /**
         * Get the object that allows external processes to call onPerformSync().
         * The object is created in the base class code when the SyncAdatper
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
