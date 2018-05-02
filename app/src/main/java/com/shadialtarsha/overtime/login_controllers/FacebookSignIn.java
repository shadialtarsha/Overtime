package com.shadialtarsha.overtime.login_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseUserController;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class FacebookSignIn {

    private static final String FACEBOOK_EMAIL_PERMISSION = "email";
    private static final String FACEBOOK_PUBLIC_PROFILE_PERMISSION = "public_profile";

    private Context mContext;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;
    private CallbackManager mCallbackManager;
    private FirebaseUserController mFirebaseUserController;

    public FacebookSignIn(Context context, ProgressDialog progressDialog) {
        mContext = context;
        mProgressDialog = progressDialog;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUserController = new FirebaseUserController();
    }

    public void signIn(LoginButton facebookLoginButton) {
        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setReadPermissions(FACEBOOK_EMAIL_PERMISSION, FACEBOOK_PUBLIC_PROFILE_PERMISSION);
        facebookLoginButton.performClick();
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(mContext, mContext.getString(R.string.facebook_connection_canceled), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(mContext, mContext.getString(R.string.facebook_connection_failed), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mProgressDialog.show();
        mFirebaseAuth
                .signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mFirebaseUserController.addNewUser(task.getResult().getUser());
                        }
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException ex) {
                                CustomAlertDialog.showAlertDialog(
                                        mContext,
                                        mContext.getString(R.string.alert_dialog_authentication_title),
                                        mContext.getString(R.string.alert_dialog_email_exists_message)
                                );
                            } catch (Exception ex) {
                                CustomAlertDialog.showAlertDialog(
                                        mContext,
                                        mContext.getString(R.string.alert_dialog_authentication_title),
                                        mContext.getString(R.string.alert_dialog_authentication_error_message)
                                );
                            } finally {
                                LoginManager.getInstance().logOut();
                            }
                        }
                        mProgressDialog.dismiss();
                    }
                })
        ;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
