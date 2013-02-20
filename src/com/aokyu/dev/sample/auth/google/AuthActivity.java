/*
 * Copyright 2013 Yu AOKI
 */

package com.aokyu.dev.sample.auth.google;

import android.accounts.Account;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;

import com.aokyu.dev.sample.auth.google.AccountListFragment.OnAccountItemClickListener;
import com.aokyu.dev.sample.auth.google.AuthFragment.Argument;
import com.aokyu.dev.sample.auth.google.AuthFragment.AuthListener;

public class AuthActivity extends Activity
    implements OnAccountItemClickListener, AuthListener {

    private boolean mTransactionAllowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_screen);

        mTransactionAllowed = true;
        showAccountListFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTransactionAllowed = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTransactionAllowed = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mTransactionAllowed = false;
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auth_screen, menu);
        return true;
    }

    private void showAccountListFragment() {
        FragmentManager manager = getFragmentManager();

        AccountListFragment fragment =
                (AccountListFragment) manager.findFragmentByTag(AccountListFragment.TAG);
        if (fragment == null) {
            fragment = AccountListFragment.newInstance();
        }

        showFragment(fragment);
    }

    private void showAuthFragment(Account account) {
        FragmentManager manager = getFragmentManager();

        AuthFragment fragment =
                (AuthFragment) manager.findFragmentByTag(AuthFragment.TAG);
        if (fragment == null) {
            fragment = AuthFragment.newInstance();
        }

        Bundle args = new Bundle();
        args.putParcelable(Argument.ACCOUNT, account);
        fragment.setArguments(args);
        showFragment(fragment, AuthFragment.TAG);
    }

    private void showFragment(Fragment fragment) {
        if (!isFragmentTransactionAllowed()) {
            return;
        }

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container_view, fragment);
        transaction.commit();
    }

    private void showFragment(Fragment fragment, String tag) {
        if (!isFragmentTransactionAllowed()) {
            return;
        }

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.addToBackStack(tag);
        transaction.replace(R.id.container_view, fragment, tag);
        transaction.commit();
    }

    public boolean isFragmentTransactionAllowed() {
        return mTransactionAllowed;
    }

    @Override
    public void onAccountItemClick(Account account) {
        showAuthFragment(account);
    }

    @Override
    public void onAuthStart() {
        showProgressDialog();
    }

    @Override
    public void onAuthFinish(String token) {
        hideProgressDialog();
    }

    private void showProgressDialog() {
        FragmentManager manager = getFragmentManager();

        ProgressDialogFragment fragment =
                (ProgressDialogFragment) manager.findFragmentByTag(ProgressDialogFragment.TAG);
        if (fragment == null) {
            fragment = ProgressDialogFragment.newInstance();
        }

        fragment.show(manager, ProgressDialogFragment.TAG);
    }

    private void hideProgressDialog() {
        FragmentManager manager = getFragmentManager();

        ProgressDialogFragment fragment =
                (ProgressDialogFragment) manager.findFragmentByTag(ProgressDialogFragment.TAG);
        if (fragment != null) {
            fragment.dismissAllowingStateLoss();
        }
    }

}
