package com.shadialtarsha.overtime.firebase;

import android.net.Uri;
import android.util.Log;

import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class FirebaseUserController {

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseUser mFirebaseUser;

    public FirebaseUserController(){
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
    }

    public void addNewUser(FirebaseUser user) {
        mFirebaseUser = user;
        mFirebaseDatabaseOperations
                .getUsersNodeReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mFirebaseUser.getUid())) {
                    User user = new User(
                            mFirebaseUser.getDisplayName(),
                            mFirebaseUser.getEmail(),
                            "your bio",
                            modifyPhotoQuality(mFirebaseUser),
                            "None"
                    );
                    user.setUserId(mFirebaseUser.getUid());
                    mFirebaseDatabaseOperations
                            .getSpecificUserNodeReference(mFirebaseUser.getUid())
                            .setValue(user)
                    ;
                } else {
                    User user = dataSnapshot.child(mFirebaseUser.getUid()).getValue(User.class);
                    user.setUserEmail(mFirebaseUser.getEmail());
                    mFirebaseDatabaseOperations
                            .getSpecificUserNodeReference(mFirebaseUser.getUid())
                            .setValue(user)
                    ;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(Constants.FIREBASE_OPERATIONS, databaseError.getMessage());
            }
        });
    }

    public void updateUserNameAndPhoto(StorageReference storageReference , final FirebaseUser firebaseUser, final String name) {
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                UserProfileChangeRequest update = new UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(name)
                        .setPhotoUri(uri)
                        .build();
                firebaseUser.updateProfile(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        addNewUser(firebaseUser);
                    }
                });
            }
        });
    }

    private String modifyPhotoQuality(FirebaseUser user) {
        String profilePhotoUrl = new String();
        for (UserInfo profile : user.getProviderData()) {
            if (profile.getProviderId().equals("facebook.com")) {
                profilePhotoUrl = "https://graph.facebook.com/" + profile.getUid() + "/picture?height=400&width=400";
            } else if (profile.getProviderId().equals("google.com")) {
                profilePhotoUrl = user.getPhotoUrl().toString();
                profilePhotoUrl = profilePhotoUrl.replace("/s96-c/", "/s400-c/");
            } else {
                profilePhotoUrl = user.getPhotoUrl().toString();
            }
        }
        return profilePhotoUrl;
    }
}
