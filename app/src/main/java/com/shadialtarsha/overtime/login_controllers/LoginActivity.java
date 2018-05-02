package com.shadialtarsha.overtime.login_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.main_screen_controllers.MainActivity;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FacebookSignIn mFacebookSignIn;
    private TwitterSignIn mTwitterSignIn;
    private GoogleSignIn mGoogleSignIn;
    private FirebaseEmailSignIn mFirebaseEmailSignIn;
    private ValidateInput mValidateInput;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private LoginButton mFacebookLoginButton;
    private ImageView mFacebookLogoLoginImageView;
    private SignInButton mGoogleLoginButton;
    private ImageView mGoogleLogoLoginImageView;
    private ImageView mTwitterLoginButton;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private TextView mForgetPasswordTextView;
    private AppCompatButton mLoginButton;
    private TextView mSignUpLinkTextView;

    private FirebaseAuthOperations mFirebaseAuthOperations;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, LoginActivity.class);
    }

    private void setupViews() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title));
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        mEmailEditText = (EditText) findViewById(R.id.login_email_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        mLoginButton = (AppCompatButton) findViewById(R.id.login_button);
        mForgetPasswordTextView = (TextView) findViewById(R.id.login_forget_password_text_view);
        mFacebookLoginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        mFacebookLogoLoginImageView = (ImageView) findViewById(R.id.facebook_logo_login_button);
        mGoogleLoginButton = (SignInButton) findViewById(R.id.google_login_button);
        mGoogleLogoLoginImageView = (ImageView) findViewById(R.id.google_logo_login_button);
        mTwitterLoginButton = (ImageView) findViewById(R.id.twitter_login_button);
        mSignUpLinkTextView = (TextView) findViewById(R.id.link_to_sign_up);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        mFirebaseAuthOperations = new FirebaseAuthOperations();
        mContext = LoginActivity.this;
        mValidateInput = new ValidateInput();

        setupViews();
        firebaseEmailLoginInitialize();
        facebookSignInInitialize();
        twitterSignInInitialize();
        googleSignInInitialize();
        forgetPasswordInitialize();
        signUpLinkInitialize();
        navigateUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleSignIn.RC_SIGN_IN) {
            mGoogleSignIn.onActivityResult(data);
        } else {
            mFacebookSignIn.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAuthStateListener != null) {
            mFirebaseAuthOperations.attachAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuthOperations.detachAuthStateListener(mAuthStateListener);
        }
    }

    private void firebaseEmailLoginInitialize() {
        mEmailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mEmailEditText.getText().toString().length() != 0)
                    if (!mValidateInput.isValidEmail(mEmailEditText.getText().toString()))
                        mEmailEditText.setError(getString(R.string.edit_text_invalid_email));
            }
        });
        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mPasswordEditText.getText().toString().length() != 0)
                    if (!mValidateInput.isValidPassword(mPasswordEditText.getText().toString()))
                        mPasswordEditText.setError(getString(R.string.edit_text_invalid_password));
            }
        });
        mFirebaseEmailSignIn = new FirebaseEmailSignIn(mContext, mProgressDialog, mEmailEditText, mPasswordEditText);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseEmailSignIn.login();
            }
        });
    }

    private void facebookSignInInitialize() {
        mFacebookSignIn = new FacebookSignIn(mContext, mProgressDialog);
        mFacebookLogoLoginImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFacebookSignIn.signIn(mFacebookLoginButton);
            }
        });
    }

    private void twitterSignInInitialize() {
        mTwitterSignIn = new TwitterSignIn(mContext, mProgressDialog);
        mTwitterLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTwitterSignIn.signIn();
            }
        });
    }

    private void googleSignInInitialize() {
        mGoogleSignIn = new GoogleSignIn(mContext, mProgressDialog);
        mGoogleLogoLoginImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignIn.signIn(mGoogleLoginButton);
            }
        });
    }

    private void forgetPasswordInitialize() {
        mForgetPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomAlertDialog.showResetPasswordDialog(mContext);
                mEmailEditText.setText("");
                mPasswordEditText.setText("");
            }
        });
    }

    private void signUpLinkInitialize() {
        mSignUpLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SignUpActivity.newIntent(mContext));
            }
        });
    }

    private void navigateUser() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String providerName = mFirebaseAuthOperations.getUserProviderInfo(firebaseUser).getProviderId();
                    if (providerName.equals("facebook.com") || providerName.equals("google.com")) {
                        startActivity(MainActivity.newIntent(mContext));
                        finish();
                    }
                }
            }
        };
    }
}
