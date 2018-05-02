package com.shadialtarsha.overtime.login_controllers;


import android.app.ProgressDialog;
import android.content.Context;

import com.shadialtarsha.overtime.R;

public class TwitterSignIn {

    private Context mContext;
    private ProgressDialog mProgressDialog;

    public TwitterSignIn(Context context, ProgressDialog progressDialog) {
        mContext = context;
        mProgressDialog = progressDialog;
    }

    public void signIn() {
        CustomAlertDialog.showAlertDialog(
                mContext,
                mContext.getString(R.string.alert_dialog_authentication_title),
                mContext.getString(R.string.twitter_alert_message)
        );
    }
}
