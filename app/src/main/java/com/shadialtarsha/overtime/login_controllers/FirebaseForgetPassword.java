package com.shadialtarsha.overtime.login_controllers;

import android.content.Context;
import android.widget.EditText;

import com.shadialtarsha.overtime.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseForgetPassword {

    private Context mContext;
    private EditText mEmailEditText;

    public FirebaseForgetPassword(Context context, EditText emailEditText) {
        mContext = context;
        mEmailEditText = emailEditText;
    }

    public void forgetPassword() {
        FirebaseAuth
                .getInstance()
                .sendPasswordResetEmail(mEmailEditText.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (Exception ex) {
                                CustomAlertDialog.showAlertDialog(
                                        mContext,
                                        mContext.getString(R.string.alert_dialog_authentication_title),
                                        mContext.getString(R.string.alert_dialog_no_account_message)
                                );
                            }
                        } else {
                            CustomAlertDialog.showAlertDialog(
                                    mContext,
                                    mContext.getString(R.string.alert_dialog_authentication_title),
                                    mContext.getString(R.string.alert_dialog_check_your_email_message)
                            );
                        }
                    }
                })
        ;
    }
}
