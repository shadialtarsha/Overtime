package com.shadialtarsha.overtime.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseOperations {

    private static final String USERS_NODE = "Users";
    private static final String POSTS_NODE = "Posts";
    private static final String LIKES_NODE = "Likes";
    private static final String COMMENTS_NODE = "Comments";
    private static final String FOLLOWERS_NODE = "Followers";
    private static final String FOLLOWING_NODE = "Following";
    private static final String WALL_POSTS_NODE = "Wall Posts";
    private static final String INBOX_NODE = "Inbox";
    private static final String MESSAGES_NODE = "Messages";
    private static final String GROUPS_NODE = "Groups";
    private static final String GROUP_MEMBERS_NODE = "Group Members";

    private FirebaseDatabase mFirebaseDatabase;

    public FirebaseDatabaseOperations() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    public DatabaseReference getUsersNodeReference() {
        return mFirebaseDatabase.getReference().child(USERS_NODE);
    }

    public DatabaseReference getSpecificUserNodeReference(String userId) {
        return getUsersNodeReference().child(userId);
    }

    public DatabaseReference getPostsNodeReference() {
        return mFirebaseDatabase.getReference().child(POSTS_NODE);
    }

    public DatabaseReference getSpecificPostsNodeReference(String userId) {
        return getPostsNodeReference().child(userId);
    }

    public DatabaseReference getFollowersNodeReference() {
        return mFirebaseDatabase.getReference().child(FOLLOWERS_NODE);
    }

    public DatabaseReference getSpecificFollowersNodeReference(String userId) {
        return getFollowersNodeReference().child(userId);
    }

    public DatabaseReference getFollowingNodeReference() {
        return mFirebaseDatabase.getReference().child(FOLLOWING_NODE);
    }

    public DatabaseReference getSpecificFollowingNodeReference(String userId) {
        return getFollowingNodeReference().child(userId);
    }

    public DatabaseReference getLikesNodeReference() {
        return mFirebaseDatabase.getReference().child(LIKES_NODE);
    }

    public DatabaseReference getSpecificLikesNodeReference(String postId) {
        return getLikesNodeReference().child(postId);
    }

    public DatabaseReference getCommentsNodeReference() {
        return mFirebaseDatabase.getReference().child(COMMENTS_NODE);
    }

    public DatabaseReference getSpecificCommentsNodeReference(String postId) {
        return getCommentsNodeReference().child(postId);
    }

    public DatabaseReference getWallPostsNodeReference() {
        return mFirebaseDatabase.getReference().child(WALL_POSTS_NODE);
    }

    public DatabaseReference getSpecificWallPostsNodeReference(String userId) {
        return getWallPostsNodeReference().child(userId);
    }

    public DatabaseReference getInboxNodeReference() {
        return mFirebaseDatabase.getReference().child(INBOX_NODE);
    }

    public DatabaseReference getSpecificInboxNodeReference(String userId){
        return getInboxNodeReference().child(userId);
    }

    public DatabaseReference getMessagesNodeReference() {
        return mFirebaseDatabase.getReference().child(MESSAGES_NODE);
    }

    public DatabaseReference getSpecificMessagesNodeReference(String conversationId){
        return getMessagesNodeReference().child(conversationId);
    }

    public DatabaseReference getGroupsNodeReference(){
        return mFirebaseDatabase.getReference().child(GROUPS_NODE);
    }

    public DatabaseReference getSpecificGroupsNodeReference(String groupId){
        return getGroupsNodeReference().child(groupId);
    }

    public DatabaseReference getGroupMembersNodeReference(){
        return mFirebaseDatabase.getReference().child(GROUP_MEMBERS_NODE);
    }

    public DatabaseReference getSpecificGroupMembersNodeReference(String groupId){
        return getGroupMembersNodeReference().child(groupId);
    }
}
