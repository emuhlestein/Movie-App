package com.intelliviz.movieapp3.syncadapter;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.intelliviz.movieapp3.db.MovieContract;

/**
 * Created by edm on 5/12/2016.
 */
public class MovieAccountAuthenticator extends AbstractAccountAuthenticator {
    private Context mContext;
    public MovieAccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        AccountManager manager = AccountManager.get(mContext);
        final Account account = new Account(MovieContract.ACCOUNT, MovieContract.ACCOUNT_TYPE);
        manager.addAccountExplicitly(account, null, null);
        ContentResolver.setIsSyncable(account, MovieContract.CONTENT_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, MovieContract.CONTENT_AUTHORITY, true);
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
