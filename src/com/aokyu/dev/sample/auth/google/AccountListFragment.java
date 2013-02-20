/*
 * Copyright 2013 Yu AOKI
 */

package com.aokyu.dev.sample.auth.google;

import java.util.List;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AccountListFragment extends AccountLoaderFragment {

    /* package */ static final String TAG = AccountListFragment.class.getSimpleName();

    private ListView mAccountView;
    private AccountAdapter mAccountAdapter;

    private OnAccountItemClickListener mListener;

    public AccountListFragment() {
        super();
    }

    public static AccountListFragment newInstance() {
        AccountListFragment fragment = new AccountListFragment();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnAccountItemClickListener) {
            mListener = (OnAccountItemClickListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.account_panel, null);
        setupViews(contentView);
        return contentView;
    }

    private void setupViews(View rootView) {
        mAccountView = (ListView) rootView.findViewById(R.id.account_view);
        mAccountAdapter = new AccountAdapter(mContext);
        mAccountView.setAdapter(mAccountAdapter);
        mAccountView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mListener != null) {
                    if (mAccountAdapter != null) {
                        Account account = (Account) mAccountAdapter.getItem(position);
                        mListener.onAccountItemClick(account);
                    }
                }
            }
        });
    }

    @Override
    protected void onLoadFinished(Loader<List<Account>> loader, List<Account> accounts) {
        if (mAccountAdapter != null) {
            mAccountAdapter.set(accounts);
        }
    }

    @Override
    protected void onLoaderReset(Loader<List<Account>> loader) {
        if (mAccountAdapter != null) {
            mAccountAdapter.set(null);
        }
    }

    private static class AccountAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        private List<Account> mAccounts;

        private static final class ViewCache {
            public final TextView nameView;

            public ViewCache(View rootView) {
                nameView = (TextView) rootView.findViewById(R.id.name_view);
            }
        }

        public AccountAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /* package */ void set(List<Account> accounts) {
            if (accounts == null) {
                mAccounts = null;
                notifyDataSetChanged();
                return;
            }

            if (mAccounts != null) {
                mAccounts.clear();
                mAccounts.addAll(accounts);
            } else {
                mAccounts = accounts;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (mAccounts != null) {
                int size = mAccounts.size();
                return size;
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            if (mAccounts != null) {
                Account account = mAccounts.get(position);
                return account;
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewCache cache = null;
            if (view == null) {
                view = newView();
            }
            cache = (ViewCache) view.getTag();

            Account account = mAccounts.get(position);
            String name = account.name;
            cache.nameView.setText(name);

            return view;
        }

        private View newView() {
            View view = mInflater.inflate(R.layout.account_list_item, null);
            ViewCache cache = new ViewCache(view);
            view.setTag(cache);
            return view;
        }
    }

    public interface OnAccountItemClickListener {
        public void onAccountItemClick(Account account);
    }
}
