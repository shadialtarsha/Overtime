package com.shadialtarsha.overtime.launch_intro_controllers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.main_screen_controllers.MainActivity;
import com.shadialtarsha.overtime.login_controllers.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    private AuthStateListener mAuthStateListener;
    private FirebaseAuthOperations mFirebaseAuthOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAuthOperations = new FirebaseAuthOperations();
        if (FirstLaunchPreference.isFirstLaunch(getApplicationContext())) {
            FirstLaunchPreference.setPrefFirstLaunch(getApplicationContext(), false);
            startActivity(IntroActivity.newIntent(SplashScreenActivity.this));
            finish();
        } else {
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                    FirebaseUser user = mFirebaseAuthOperations.getCurrentUser();
                    if (user != null) {
                        startActivity(MainActivity.newIntent(SplashScreenActivity.this));
                    } else {
                        startActivity(LoginActivity.newIntent(SplashScreenActivity.this));
                    }
                    finish();
                }
            };
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthStateListener != null) {
            mFirebaseAuthOperations.attachAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuthOperations.detachAuthStateListener(mAuthStateListener);
        }
    }
}
