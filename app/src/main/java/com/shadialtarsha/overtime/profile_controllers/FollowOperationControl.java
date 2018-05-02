package com.shadialtarsha.overtime.profile_controllers;

import android.util.Log;

import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Follower;
import com.shadialtarsha.overtime.model.Following;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class FollowOperationControl {

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseAuthOperations mFirebaseAuthOperations;
    private FirebaseUser mCurrentFirebaseUser;
    private User mUser;

    public FollowOperationControl() {
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFirebaseAuthOperations = new FirebaseAuthOperations();
        mCurrentFirebaseUser = mFirebaseAuthOperations.getCurrentUser();
    }

    public void followOperation(User user) {
        mUser = user;
        Following following = new Following();
        following.setUserId(mUser.getUserId());
        mFirebaseDatabaseOperations.
                getSpecificFollowingNodeReference(mCurrentFirebaseUser.getUid())
                .child(mUser.getUserId()).setValue(following)
        ;

        Follower follower = new Follower();
        follower.setUserId(mCurrentFirebaseUser.getUid());
        mFirebaseDatabaseOperations.getSpecificFollowersNodeReference(mUser.getUserId())
                .child(mCurrentFirebaseUser.getUid()).setValue(follower)
        ;

        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mUser.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                            Post post = dataSnap.getValue(Post.class);
                            mFirebaseDatabaseOperations
                                    .getSpecificWallPostsNodeReference(mCurrentFirebaseUser.getUid())
                                    .child(post.getPostId())
                                    .setValue(post)
                            ;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.FOLLOW_OPERATION_CONTROL, databaseError.getMessage());
                    }
                });
        ;
    }

    public void unFollowOperation(User user) {
        mUser = user;
        mFirebaseDatabaseOperations
                .getSpecificFollowingNodeReference(mCurrentFirebaseUser.getUid())
                .child(user.getUserId())
                .removeValue()
        ;
        mFirebaseDatabaseOperations
                .getSpecificFollowersNodeReference(mUser.getUserId())
                .child(mCurrentFirebaseUser.getUid())
                .removeValue()
        ;
        mFirebaseDatabaseOperations.getSpecificWallPostsNodeReference(mCurrentFirebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot posts : dataSnapshot.getChildren()) {
                            Post post = posts.getValue(Post.class);
                            if (post.getUserId().equals(mUser.getUserId())) {
                                mFirebaseDatabaseOperations
                                        .getSpecificWallPostsNodeReference(mCurrentFirebaseUser.getUid())
                                        .child(post.getPostId())
                                        .removeValue()
                                ;

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.FOLLOW_OPERATION_CONTROL, databaseError.getMessage());
                    }
                })
        ;
    }
}
