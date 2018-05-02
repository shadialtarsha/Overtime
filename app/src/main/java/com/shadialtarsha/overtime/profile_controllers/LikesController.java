package com.shadialtarsha.overtime.profile_controllers;

import android.util.Log;

import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Follower;
import com.shadialtarsha.overtime.model.Like;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LikesController {

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private User mCurrentUser;
    private Post mCurrentPost;

    public LikesController(User currentUser, Post currentPost) {
        mCurrentUser = currentUser;
        mCurrentPost = currentPost;
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
    }

    public void onPressLikeButton() {
        mFirebaseDatabaseOperations.getSpecificLikesNodeReference(mCurrentPost.getPostId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(mCurrentUser.getUserId())) {
                            Like like = new Like();
                            like.setUserId(mCurrentUser.getUserId());
                            mFirebaseDatabaseOperations
                                    .getSpecificLikesNodeReference(mCurrentPost.getPostId())
                                    .child(mCurrentUser.getUserId())
                                    .setValue(like);
                            mCurrentPost.setPostLikesCounter(mCurrentPost.getPostLikesCounter() + 1);
                        } else {
                            mFirebaseDatabaseOperations
                                    .getSpecificLikesNodeReference(mCurrentPost.getPostId())
                                    .child(mCurrentUser.getUserId())
                                    .removeValue();
                            mCurrentPost.setPostLikesCounter(mCurrentPost.getPostLikesCounter() - 1);
                        }
                        mFirebaseDatabaseOperations
                                .getSpecificPostsNodeReference(mCurrentPost.getUserId())
                                .child(mCurrentPost.getPostId())
                                .setValue(mCurrentPost)
                        ;
                        mFirebaseDatabaseOperations
                                .getSpecificWallPostsNodeReference(mCurrentPost.getUserId())
                                .child(mCurrentPost.getPostId())
                                .setValue(mCurrentPost)
                        ;
                        mFirebaseDatabaseOperations.getSpecificFollowersNodeReference(mCurrentPost.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot followers : dataSnapshot.getChildren()) {
                                            Follower follower = followers.getValue(Follower.class);
                                            mFirebaseDatabaseOperations
                                                    .getSpecificWallPostsNodeReference(follower.getUserId())
                                                    .child(mCurrentPost.getPostId())
                                                    .setValue(mCurrentPost)
                                            ;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(Constants.LIKES_CONTROLLER, databaseError.getMessage());
                                    }
                                })
                        ;

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.LIKES_CONTROLLER, databaseError.getMessage());
                    }
                })
        ;
    }
}
