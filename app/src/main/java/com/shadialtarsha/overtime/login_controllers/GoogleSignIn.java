package com.shadialtarsha.overtime.login_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseUserController;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignIn {

    public static final int RC_SIGN_IN = 1;

    private Context mContext;
    private ProgressDialog mProgressDialog;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUserController mFirebaseUserController;

    public GoogleSignIn(Context context, ProgressDialog progressDialog) {
        mContext = context;
        mProgressDialog = progressDialog;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUserController = new FirebaseUserController();
    }

    public void signIn(SignInButton googleSignInButton) {
        AppCompatActivity activity = (AppCompatActivity) mContext;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(mContext.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .enableAutoManage(activity, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Toast.makeText(mContext, mContext.getString(R.string.google_connection_failed), Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleSignInButton.performClick();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleGoogleAccount(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
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
                            }
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    public void onActivityResult(Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            handleGoogleAccount(account);
        }
    }
}
