package com.shadialtarsha.overtime.login_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.main_screen_controllers.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseEmailSignIn {

    private Context mContext;
    private AppCompatActivity mActivity;
    private ProgressDialog mProgressDialog;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private ValidateInput mValidateInput;
    private FirebaseAuth mFirebaseAuth;

    public FirebaseEmailSignIn(Context context, ProgressDialog progressDialog, EditText emailEditText, EditText passwordEditText) {
        mValidateInput = new ValidateInput();
        mContext = context;
        mActivity = (AppCompatActivity) mContext;
        mProgressDialog = progressDialog;
        mEmailEditText = emailEditText;
        mPasswordEditText = passwordEditText;
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void login() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        if (mValidateInput.isValidEmail(email) && mValidateInput.isValidPassword(password)) {
            mProgressDialog.show();
            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                CustomAlertDialog.showAlertDialog(
                                        mContext,
                                        mContext.getString(R.string.alert_dialog_authentication_title),
                                        mContext.getString(R.string.alert_dialog_invalid_email_password_message)
                                );
                            } else {
                                mActivity.startActivity(MainActivity.newIntent(mContext));
                                mActivity.finish();
                            }
                            mProgressDialog.dismiss();
                        }
                    });
        } else {
            if (!mValidateInput.isValidEmail(email)) {
                mEmailEditText.setError(mContext.getString(R.string.edit_text_invalid_email));
                mEmailEditText.requestFocus();
            }
            if (!mValidateInput.isValidPassword(password)) {
                mPasswordEditText.setError(mContext.getString(R.string.edit_text_invalid_password));
                mPasswordEditText.requestFocus();
            }
        }
    }
}
