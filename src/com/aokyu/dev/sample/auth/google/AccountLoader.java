/*
 * Copyright 2013 Yu AOKI
 */

package com.aokyu.dev.sample.auth.google;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.AsyncTaskLoader;
import android.content.Context;

public class AccountLoader extends AsyncTaskLoader<List<Account>> {

    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    private AccountManager mAccountManager;
    private UpdateListener mInternalListener;

    public AccountLoader(Context context) {
        super(context);
        mAccountManager = AccountManager.get(context);
        mInternalListener = new UpdateListener(this);
        mAccountManager.addOnAccountsUpdatedListener(mInternalListener, null, true);
    }

    @Override
    public List<Account> loadInBackground() {
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
        List<Account> list = new ArrayList<Account>();
        if (accounts != null) {
            list.addAll(Arrays.asList(accounts));
        }
        return list;
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        mAccountManager.removeOnAccountsUpdatedListener(mInternalListener);
    }

    private static final class UpdateListener implements OnAccountsUpdateListener {

        private WeakReference<AccountLoader> mLoader;

        public UpdateListener(AccountLoader loader) {
            mLoader = new WeakReference<AccountLoader>(loader);
        }

        @Override
        public void onAccountsUpdated(Account[] accounts) {
            AccountLoader loader = mLoader.get();
            if (loader != null) {
                loader.forceLoad();
            }
        }
        
    }
}
