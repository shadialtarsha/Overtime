package com.shadialtarsha.overtime.model;

public class GroupMember {

    private String mUserId;
    private String mUserRank;

    public GroupMember() {
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getUserRank() {
        return mUserRank;
    }

    public void setUserRank(String userRank) {
        mUserRank = userRank;
    }
}
