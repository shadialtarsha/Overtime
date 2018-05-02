package com.shadialtarsha.overtime.model;

import java.io.Serializable;

public class Group implements Serializable{

    private String mGroupID;
    private String mGroupName;
    private String mGroupBio;
    private String mGroupPhotoUri;
    private String mGroupAdminId;
    private String mGroupNameLowerCase;

    public Group(){

    }

    public String getGroupID() {
        return mGroupID;
    }

    public void setGroupID(String groupID) {
        mGroupID = groupID;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        mGroupName = groupName;
    }

    public String getGroupBio() {
        return mGroupBio;
    }

    public void setGroupBio(String groupBio) {
        mGroupBio = groupBio;
    }

    public String getGroupPhotoUri() {
        return mGroupPhotoUri;
    }

    public void setGroupPhotoUri(String groupPhotoUri) {
        mGroupPhotoUri = groupPhotoUri;
    }

    public String getGroupAdminId() {
        return mGroupAdminId;
    }

    public void setGroupAdminId(String groupAdminId) {
        mGroupAdminId = groupAdminId;
    }

    public String getGroupNameLowerCase() {
        return mGroupNameLowerCase;
    }

    public void setGroupNameLowerCase(String groupNameLowerCase) {
        mGroupNameLowerCase = groupNameLowerCase;
    }
}
