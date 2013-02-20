/*
 * Copyright 2013 Yu AOKI
 */

package com.aokyu.dev.sample.auth.google;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class AuthFragment extends Fragment {

    /* package */ static final String TAG = AuthFragment.class.getSimpleName();

    private static final String WORKER_THREAD_NAME = TAG + ":" + "WorkerThread";

    public final class Argument {
        public static final String ACCOUNT = "arg_account";

        private Argument() {}
    }

    public final class TokenType {
        public static final String CALENDAR = "cl";
        public static final String MAIL = "mail";
        public static final String READER = "reader";
        // And all that.

        private TokenType() {}
    }

    private static final String NO_TYPE = "__no_token_type__";

    private Context mContext;

    /* package */ static final int REQUEST_AUTH = 0x00000100;

    private AccountManager mAccountManager;
    private Callback mCallback;
    private Account mAccount;
    private String mTokenType = NO_TYPE;

    private AuthListener mListener;

    private TextView mNameView;
    private Button mAuthButton;
    private Button mInvalidateButton;
    private TextView mResultView;
    private Spinner mTypeSpinner;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;
    private CallbackHandler mCallbackHandler;

    public AuthFragment() {}

    public static  AuthFragment newInstance() {
        AuthFragment fragment = new AuthFragment();
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity != null) {
            mContext = activity.getApplicationContext();
        }

        if (activity instanceof AuthListener) {
            mListener = (AuthListener) activity;
        }
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWorkerThread = new HandlerThread(WORKER_THREAD_NAME);
        mWorkerThread.start();
        Looper looper = mWorkerThread.getLooper();
        mWorkerHandler = new Handler(looper);

        mCallbackHandler = new CallbackHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.auth_panel, container, false);
        setupViews(contentView);
        return contentView;
    }

    private void setupViews(View rootView) {
        mNameView = (TextView) rootView.findViewById(R.id.name_view);
        mResultView = (TextView) rootView.findViewById(R.id.result_view);
        mTypeSpinner = (Spinner) rootView.findViewById(R.id.type_spinner);
        mTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> pareint, View v,
                    int position, long id) {
                clearTokenView();
                switch (position) {
                case 0:
                    mTokenType = TokenType.CALENDAR;
                    break;
                case 1:
                    mTokenType = TokenType.MAIL;
                    break;
                case 2:
                    mTokenType = TokenType.READER;
                    break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mAuthButton = (Button) rootView.findViewById(R.id.auth_button);
        mAuthButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveAuthToken(mAccount, mTokenType);
            }
        });
        mInvalidateButton = (Button) rootView.findViewById(R.id.invalidate_button);
        mInvalidateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = mResultView.getText().toString();
                invalidateAuthToken(mAccount, token);
            }
        });
    }

    private void retrieveAuthToken(Account account, String tokenType) {
        if (account != null) {
            dispatchAuthStart();
            mAccountManager.getAuthToken(account, tokenType, null, false, mCallback, mWorkerHandler);
        }
    }

    private void dispatchAuthStart() {
        if (mListener != null) {
            mListener.onAuthStart();
        }
    }

    private void dispatchAuthFinish(String token) {
        if (mListener != null) {
            mListener.onAuthFinish(token);
        }
    }

    private void invalidateAuthToken(Account account, String token) {
        if (account != null) {
            mAccountManager.invalidateAuthToken(account.type, token);
            mInvalidateButton.setEnabled(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAccountManager = AccountManager.get(mContext);
        mCallback = new Callback(mCallbackHandler);

        Bundle args = getArguments();
        mAccount = args.getParcelable(Argument.ACCOUNT);

        if (mAccount != null) {
            String name = mAccount.name;
            mNameView.setText(name);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWorkerThread.quit();
    }

    private void onAuthCallback(Bundle result) {
        Intent intent = result.getParcelable(AccountManager.KEY_INTENT);

        if (intent != null) {
            int flags = intent.getFlags();
            // Clear Intent#FLAG_ACTIVITY_NEW_TASK flag so that onActivityResult() can be called.
            if ((flags & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
                flags ^= Intent.FLAG_ACTIVITY_NEW_TASK;
                intent.setFlags(flags);
            }
            startActivityForResult(intent, REQUEST_AUTH);
        } else {
            String token = result.getString(AccountManager.KEY_AUTHTOKEN);
            dispatchAuthFinish(token);
            showToken(token);
        }
    }

    private void showToken(String token) {
        if (mResultView != null) {
            if (mInvalidateButton != null) {
                mInvalidateButton.setEnabled(true);
            }
            mResultView.setText(token);
        }
    }

    private void clearTokenView() {
        if (mResultView != null) {
            if (mInvalidateButton != null) {
                mInvalidateButton.setEnabled(false);
            }
            mResultView.setText("");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case AuthFragment.REQUEST_AUTH:
            if (resultCode == Activity.RESULT_OK) {
                retrieveAuthToken(mAccount, mTokenType);
            } else {
                dispatchAuthFinish(null);
            }
            break;
        }
    }


    private static final class Callback implements AccountManagerCallback<Bundle> {

        private WeakReference<CallbackHandler> mHandler;

        public Callback(CallbackHandler handler) {
            mHandler = new WeakReference<CallbackHandler>(handler);
        }

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            Bundle result = null;
            try {
                result = future.getResult();
            } catch (OperationCanceledException e) {
            } catch (AuthenticatorException e) {
            } catch (IOException e) {
            }

            if (result != null) {
                dispatchAuthCallback(result);
            }
        }

        private void dispatchAuthCallback(Bundle result) {
            sendMessage(MessageCode.GET_AUTH_TOKEN, result);
        }

        private void sendMessage(int what, Object object) {
            CallbackHandler handler = mHandler.get();
            if (handler != null) {
                Message msg = new Message();
                msg.what = what;
                msg.obj = object;
                handler.sendMessage(msg);
            }
        }
    }

    public static final class MessageCode {
        public static final int GET_AUTH_TOKEN = 0x00001000;

        private MessageCode() {}
    }

    private static class CallbackHandler extends Handler {

        private WeakReference<AuthFragment> mFragment;

        public CallbackHandler(AuthFragment fragment) {
            super(fragment.getContext().getMainLooper());
            mFragment = new WeakReference<AuthFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            AuthFragment fragment = mFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                case MessageCode.GET_AUTH_TOKEN:
                    Bundle result = (Bundle) msg.obj;
                    fragment.onAuthCallback(result);
                    break;
                }
            }
        }
    }

    public interface AuthListener {
        public void onAuthStart();
        public void onAuthFinish(String token);
    }
}
