package com.shadialtarsha.overtime.group_controllers;

import android.util.Log;

import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Like;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class GroupLikesController {

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private User mCurrentUser;
    private Post mCurrentPost;

    public GroupLikesController(User currentUser, Post currentPost) {
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
                                .getSpecificPostsNodeReference(mCurrentPost.getGroupId())
                                .child(mCurrentPost.getPostId())
                                .setValue(mCurrentPost)
                        ;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.GROUP_LIKES_CONTROLLER, databaseError.getMessage());
                    }
                })
        ;
    }
}
