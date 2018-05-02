package com.shadialtarsha.overtime.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class FirebaseAuthOperations {

    private FirebaseAuth mFirebaseAuth;

    public FirebaseAuthOperations(){
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void attachAuthStateListener(AuthStateListener authStateListener) {
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    public  void detachAuthStateListener(AuthStateListener authStateListener) {
        mFirebaseAuth.removeAuthStateListener(authStateListener);
    }

    public FirebaseUser getCurrentUser() {
        return mFirebaseAuth.getCurrentUser();
    }

    public String getCurrentUserId(){
        return getCurrentUser().getUid();
    }

    public UserInfo getUserProviderInfo(FirebaseUser user){
        return user.getProviderData().get(1);
    }
}
