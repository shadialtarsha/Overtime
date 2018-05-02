package com.shadialtarsha.overtime.model;

import java.util.Calendar;
import java.util.Date;

public class Post {

    private String mPostId;
    private String mPostText;
    private String mPostImageUri;
    private Date mPostDate;
    private long mPostLikesCounter;
    private long mPostCommentsCounter;
    private String mUserId;
    private String mGroupId;

    public Post() {

    }

    public Post(String postId, String postText, String postImageUri,
                Date postDate, long postLikesCounter, long postCommentsCounter, String userId, String groupId) {
        mPostId = postId;
        mPostText = postText;
        mPostImageUri = postImageUri;
        mPostDate = postDate;
        mPostLikesCounter = postLikesCounter;
        mPostCommentsCounter = postCommentsCounter;
        mGroupId = groupId;
        mUserId = userId;
    }

    public String getPostId() {
        return mPostId;
    }

    public void setPostId(String postId) {
        mPostId = postId;
    }

    public String getPostText() {
        return mPostText;
    }

    public void setPostText(String postText) {
        mPostText = postText;
    }

    public String getPostImageUri() {
        return mPostImageUri;
    }

    public void setPostImageUri(String postImageUri) {
        mPostImageUri = postImageUri;
    }

    public Date getPostDate() {
        return mPostDate;
    }

    public void setPostDate(Date postDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(postDate);
        Calendar currentCalendar = Calendar.getInstance();
        if (currentCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        mPostDate = calendar.getTime();
    }

    public long getPostLikesCounter() {
        return mPostLikesCounter;
    }

    public void setPostLikesCounter(long postLikesCounter) {
        mPostLikesCounter = postLikesCounter;
    }

    public long getPostCommentsCounter() {
        return mPostCommentsCounter;
    }

    public void setPostCommentsCounter(long postCommentsCounter) {
        mPostCommentsCounter = postCommentsCounter;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }
}
