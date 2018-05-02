package com.shadialtarsha.overtime.group_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;

public class GroupPostOptionsMenuControl {

    private Context mContext;
    private AlertDialog.Builder mAlertDialogBuilder;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private User mCurrentUser;
    private Post mCurrentPost;

    public GroupPostOptionsMenuControl(Context context, User currentUser, Post currentPost) {
        mContext = context;
        mCurrentUser = currentUser;
        mCurrentPost = currentPost;
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
    }

    public void showPostOptionsMenu() {
        mAlertDialogBuilder = new AlertDialog.Builder(mContext);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.post_options_menu_item);
        adapter.add("Delete");
        mAlertDialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    new AlertDialog.Builder(mContext)
                            .setMessage(mContext.getString(R.string.alert_dialog_are_you_sure_message))
                            .setPositiveButton(mContext.getText(R.string.alert_dialog_yes_button),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mFirebaseDatabaseOperations
                                                    .getSpecificPostsNodeReference(mCurrentPost.getGroupId())
                                                    .child(mCurrentPost.getPostId())
                                                    .removeValue();
                                            mFirebaseDatabaseOperations
                                                    .getSpecificCommentsNodeReference(mCurrentPost.getPostId())
                                                    .removeValue();
                                            mFirebaseDatabaseOperations
                                                    .getSpecificLikesNodeReference(mCurrentPost.getPostId())
                                                    .removeValue();
                                        }
                                    })
                            .setNegativeButton(mContext.getText(R.string.alert_dialog_cancel_button),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show()
                    ;
                }
            }
        })
                .show()
        ;
    }
}
