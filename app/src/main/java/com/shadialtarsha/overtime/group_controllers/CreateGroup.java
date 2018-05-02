package com.shadialtarsha.overtime.group_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.firebase.FirebaseStorageOperations;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.GroupMember;
import com.shadialtarsha.overtime.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class CreateGroup {

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseStorageOperations mFirebaseStorageOperations;
    private Group mGroup;
    private String mGroupId;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private User mCurrentUser;

    public CreateGroup(Context context, User currentUser) {
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFirebaseStorageOperations = new FirebaseStorageOperations();
        mGroup = new Group();
        mGroupId = mFirebaseDatabaseOperations.getGroupsNodeReference().push().getKey();
        mGroup.setGroupID(mGroupId);
        mContext = context;
        mCurrentUser = currentUser;
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(context.getString(R.string.progress_dialog_message));
    }

    public void createGroup(
            final String groupName,
            final String groupBio,
            final Uri groupPhotoUri
    ) {
        mProgressDialog.show();
        StorageReference storageReference = mFirebaseStorageOperations
                .getSpecificGroupStorageReference(mGroupId)
                .child(groupPhotoUri.getLastPathSegment());
        storageReference
                .putFile(groupPhotoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mGroup.setGroupName(groupName);
                        mGroup.setGroupBio(groupBio);
                        mGroup.setGroupNameLowerCase(groupName.toLowerCase());
                        mGroup.setGroupPhotoUri(taskSnapshot.getDownloadUrl().toString());
                        mGroup.setGroupAdminId(mCurrentUser.getUserId());
                        mFirebaseDatabaseOperations
                                .getSpecificGroupsNodeReference(mGroupId)
                                .setValue(mGroup)
                        ;
                        GroupMember groupMember = new GroupMember();
                        groupMember.setUserId(mCurrentUser.getUserId());
                        groupMember.setUserRank(mContext.getString(R.string.group_admin_user_rank));
                        mFirebaseDatabaseOperations
                                .getSpecificGroupMembersNodeReference(mGroupId)
                                .child(mCurrentUser.getUserId())
                                .setValue(groupMember)
                        ;
                        mFirebaseDatabaseOperations.getSpecificUserNodeReference(mCurrentUser.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        user.setGroupId(mGroupId);
                                        mFirebaseDatabaseOperations
                                                .getSpecificUserNodeReference(user.getUserId())
                                                .setValue(user)
                                        ;
                                        mProgressDialog.dismiss();
                                        AppCompatActivity appCompatActivity = (AppCompatActivity) mContext;
                                        appCompatActivity.startActivity(GroupActivity.newIntent(appCompatActivity, mGroup, mCurrentUser));
                                        appCompatActivity.finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(Constants.CREATE_GROUP, databaseError.getMessage());
                                    }
                                })
                        ;
                    }
                })
        ;
    }

    public void createGroupWithDefaultPhoto(
            final String groupName,
            final String groupBio
    ) {
        mProgressDialog.show();
        mFirebaseStorageOperations
                .getDefaultGroupPhotoUrlReference()
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mGroup.setGroupName(groupName);
                        mGroup.setGroupBio(groupBio);
                        mGroup.setGroupNameLowerCase(groupName.toLowerCase());
                        mGroup.setGroupPhotoUri(uri.toString());
                        mGroup.setGroupAdminId(mCurrentUser.getUserId());
                        mFirebaseDatabaseOperations
                                .getSpecificGroupsNodeReference(mGroupId)
                                .setValue(mGroup)
                        ;
                        GroupMember groupMember = new GroupMember();
                        groupMember.setUserId(mCurrentUser.getUserId());
                        groupMember.setUserRank(mContext.getString(R.string.group_admin_user_rank));
                        mFirebaseDatabaseOperations
                                .getSpecificGroupMembersNodeReference(mGroupId)
                                .child(mCurrentUser.getUserId())
                                .setValue(groupMember)
                        ;
                        mFirebaseDatabaseOperations.getSpecificUserNodeReference(mCurrentUser.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        user.setGroupId(mGroupId);
                                        mFirebaseDatabaseOperations
                                                .getSpecificUserNodeReference(user.getUserId())
                                                .setValue(user)
                                        ;
                                        mProgressDialog.dismiss();
                                        AppCompatActivity appCompatActivity = (AppCompatActivity) mContext;
                                        appCompatActivity.startActivity(GroupActivity.newIntent(appCompatActivity, mGroup, mCurrentUser));
                                        appCompatActivity.finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(Constants.CREATE_GROUP, databaseError.getMessage());
                                    }
                                })
                        ;
                    }
                })
        ;
    }
}
