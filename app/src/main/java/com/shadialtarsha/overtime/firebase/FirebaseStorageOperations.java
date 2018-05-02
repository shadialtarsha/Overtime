package com.shadialtarsha.overtime.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageOperations {

    private static final String DEFAULT_USER_PHOTO_URL = "images/user_profile_default_photo.png";
    private static final String USERS_MEDIA_FIREBASE_STORAGE = "users_media";
    private static final String DEFAULT_GROUP_PHOTO_URL = "images/group_default_photo.png";
    private static final String GROUPS_MEDIA_FIREBASE_STORAGE = "groups_media";

    private FirebaseStorage mFirebaseStorage;

    public FirebaseStorageOperations(){
        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    public StorageReference getDefaultProfilePhotoUrlReference(){
        return mFirebaseStorage.getReference().child(DEFAULT_USER_PHOTO_URL);
    }

    private StorageReference getUsersStorageReference(){
        return mFirebaseStorage.getReference().child(USERS_MEDIA_FIREBASE_STORAGE);
    }

    public StorageReference getSpecificUserStorageReference(String userId){
        return getUsersStorageReference().child(userId);
    }

    public StorageReference getDefaultGroupPhotoUrlReference(){
        return mFirebaseStorage.getReference().child(DEFAULT_GROUP_PHOTO_URL);
    }

    private StorageReference getGroupsMediaReference(){
        return mFirebaseStorage.getReference().child(GROUPS_MEDIA_FIREBASE_STORAGE);
    }

    public StorageReference getSpecificGroupStorageReference(String groupId){
        return getGroupsMediaReference().child(groupId);
    }
}
