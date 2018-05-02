package com.shadialtarsha.overtime.model;

import java.io.Serializable;

public class User implements Serializable {

    private String mUserId;
    private String mUserName;
    private String mUserNameLowerCase;
    private String mUserEmail;
    private String mBioText;
    private String mUserProfilePhotoUri;
    private String mGroupId;

    public User(){

    }

    public User(String userName, String userEmail, String bioText,
                String userProfilePhotoUri, String groupId) {
        mUserName = userName;
        mUserNameLowerCase = mUserName.toLowerCase();
        mUserEmail = userEmail;
        mBioText = bioText;
        mUserProfilePhotoUri = userProfilePhotoUri;
        mGroupId = groupId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public void setUserEmail(String userEmail) {
        mUserEmail = userEmail;
    }

    public String getBioText() {
        return mBioText;
    }

    public void setBioText(String bioText) {
        mBioText = bioText;
    }

    public String getUserProfilePhotoUri() {
        return mUserProfilePhotoUri;
    }

    public void setUserProfilePhotoUri(String userProfilePhotoUri) {
        mUserProfilePhotoUri = userProfilePhotoUri;
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public String getUserNameLowerCase() {
        return mUserNameLowerCase;
    }

    public void setUserNameLowerCase(String userNameLowerCase) {
        mUserNameLowerCase = userNameLowerCase;
    }
}
