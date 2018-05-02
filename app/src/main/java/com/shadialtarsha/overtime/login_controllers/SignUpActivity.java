package com.shadialtarsha.overtime.login_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.main_screen_controllers.MainActivity;
import com.shadialtarsha.overtime.R;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;


public class SignUpActivity extends AppCompatActivity {

    private FacebookSignIn mFacebookSignIn;
    private TwitterSignIn mTwitterSignIn;
    private GoogleSignIn mGoogleSignIn;
    private FirebaseEmailSignUp mFirebaseEmailSignUp;
    private ValidateInput mValidateInput;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private LoginButton mFacebookSignUpButton;
    private ImageView mFacebookLogoSignUpImageView;
    private SignInButton mGoogleSignUpButton;
    private ImageView mGoogleLogoSignUpImageView;
    private ImageView mTwitterSignUpImageView;
    private EditText mNameEditText;
    private EditText mEmailEditText;
    private AppCompatButton mSignUpButton;
    private TextView mLoginLinkTextView;

    private FirebaseAuthOperations mFirebaseAuthOperations;
    private AuthStateListener mAuthStateListener;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, SignUpActivity.class);
    }

    private void setupViews() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title));
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        mNameEditText = (EditText) findViewById(R.id.sign_up_name_edit_text);
        mEmailEditText = (EditText) findViewById(R.id.sign_up_email_edit_text);
        mSignUpButton = (AppCompatButton) findViewById(R.id.sign_up_create_account_button);
        mFacebookSignUpButton = (LoginButton) findViewById(R.id.facebook_sign_up_button);
        mFacebookLogoSignUpImageView = (ImageView) findViewById(R.id.facebook_logo_sign_up_button);
        mGoogleSignUpButton = (SignInButton) findViewById(R.id.google_sign_up_button);
        mGoogleLogoSignUpImageView = (ImageView) findViewById(R.id.google_logo_sign_up_button);
        mTwitterSignUpImageView = (ImageView) findViewById(R.id.twitter_sign_up_button);
        mLoginLinkTextView = (TextView) findViewById(R.id.link_to_login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        mFirebaseAuthOperations = new FirebaseAuthOperations();
        mContext = SignUpActivity.this;
        mValidateInput = new ValidateInput();

        setupViews();
        firebaseEmailSignUpInitialize();
        facebookSignInInitialize();
        twitterSignInInitialize();
        googleSignInInitialize();
        loginLinkInitialize();
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

    private void firebaseEmailSignUpInitialize() {
        mFirebaseEmailSignUp = new FirebaseEmailSignUp(mContext, mProgressDialog, mNameEditText, mEmailEditText);
        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    if (!mValidateInput.isValidName(mNameEditText.getText().toString()))
                        mNameEditText.setError(getString(R.string.edit_text_invalid_name));
            }
        });
        mEmailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mEmailEditText.getText().toString().length() != 0)
                    if (!mValidateInput.isValidEmail(mEmailEditText.getText().toString()))
                        mEmailEditText.setError(getString(R.string.edit_text_invalid_email));
            }
        });
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseEmailSignUp.signUp();
            }
        });
    }

    private void facebookSignInInitialize() {
        mFacebookSignIn = new FacebookSignIn(mContext, mProgressDialog);
        mFacebookLogoSignUpImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFacebookSignIn.signIn(mFacebookSignUpButton);
            }
        });
    }

    private void twitterSignInInitialize() {
        mTwitterSignIn = new TwitterSignIn(mContext, mProgressDialog);
        mTwitterSignUpImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTwitterSignIn.signIn();
            }
        });
    }

    private void googleSignInInitialize() {
        mGoogleSignIn = new GoogleSignIn(mContext, mProgressDialog);
        mGoogleLogoSignUpImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignIn.signIn(mGoogleSignUpButton);
            }
        });
    }

    private void loginLinkInitialize() {
        mLoginLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LoginActivity.newIntent(mContext));
            }
        });
    }

    private void navigateUser() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(final FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    boolean isEmailProvider = mFirebaseAuthOperations
                            .getUserProviderInfo(firebaseUser)
                            .getProviderId()
                            .equals(EmailAuthProvider.PROVIDER_ID);
                    if (isEmailProvider) {
                        new AlertDialog.Builder(mContext)
                                .setTitle(getString(R.string.alert_dialog_authentication_title))
                                .setMessage(getString(R.string.alert_dialog_check_your_email_message))
                                .setPositiveButton(getText(R.string.alert_dialog_ok_button), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(LoginActivity.newIntent(mContext));
                                        finish();
                                        firebaseAuth.signOut();
                                    }
                                })
                                .setIcon(getDrawable(R.drawable.ic_warning_alert_dialog))
                                .show();
                    } else {
                        startActivity(MainActivity.newIntent(mContext));
                        finish();
                    }
                }
            }
        };
    }
}
