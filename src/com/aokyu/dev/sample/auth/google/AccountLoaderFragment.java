/*
 * Copyright 2013 Yu AOKI
 */

package com.aokyu.dev.sample.auth.google;

import java.lang.ref.WeakReference;
import java.util.List;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

public abstract class AccountLoaderFragment extends Fragment {

    protected Context mContext;
    private LoaderManager mLoaderManager;

    private AccountLoaderCallbacks mLoaderCallbacks;

    private static final int ID_ACCOUNT_LOADER = 0x00000001;

    protected abstract void onLoadFinished(Loader<List<Account>> loader, List<Account> accounts);
    protected abstract void onLoaderReset(Loader<List<Account>> loader);

    public AccountLoaderFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity != null) {
            mContext = activity.getApplicationContext();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoaderManager = getLoaderManager();
        mLoaderCallbacks = new AccountLoaderCallbacks(mContext, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadAccounts();
    }

    protected void loadAccounts() {
        Loader<List<Account>> loader = mLoaderManager.getLoader(ID_ACCOUNT_LOADER);

        Bundle args = new Bundle();
        if (loader != null) {
            mLoaderManager.restartLoader(ID_ACCOUNT_LOADER, args, mLoaderCallbacks);
        } else {
            mLoaderManager.initLoader(ID_ACCOUNT_LOADER, args, mLoaderCallbacks);
        }
    }

    private static final class AccountLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<List<Account>> {

        private WeakReference<Context> mContext;
        private WeakReference<AccountLoaderFragment> mFragment;

        public AccountLoaderCallbacks(Context context, AccountLoaderFragment fragment) {
            mContext = new WeakReference<Context>(context);
            mFragment = new WeakReference<AccountLoaderFragment>(fragment);
        }

        @Override
        public Loader<List<Account>> onCreateLoader(int id, Bundle args) {
            return new AccountLoader(mContext.get());
        }

        @Override
        public void onLoadFinished(Loader<List<Account>> loader, List<Account> accounts) {
            AccountLoaderFragment fragment = mFragment.get();
            if (fragment != null) {
                fragment.onLoadFinished(loader, accounts);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Account>> loader) {
            AccountLoaderFragment fragment = mFragment.get();
            if (fragment != null) {
                fragment.onLoaderReset(loader);
            }
        }
    }
}
