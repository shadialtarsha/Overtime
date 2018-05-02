package com.shadialtarsha.overtime.group_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.BuildConfig;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.firebase.FirebaseStorageOperations;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class GroupAddPostActivity extends AppCompatActivity {

    private static final String TEXT_MAX_CHARACTERS = "text_post_length";
    private static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.group_controllers.GroupAddPostActivity.current_user";
    private static final String CURRENT_GROUP_EXTRA = "com.shadialtarsha.overtime.group_controllers.GroupAddPostActivity.current_group";
    private static final int RC_PHOTO_PICKER = 1;

    private ImageButton mCancelPostImageButton;
    private ImageView mAddPostProfileImageView;
    private EditText mPostEditText;
    private FrameLayout mPostImageFrameLayout;
    private ImageView mPostImageView;
    private ImageButton mCancelPostIconImageButton;
    private ImageButton mPhotoPickerImageButton;
    private TextView mPostTextMaxCharacters;
    private Button mPostButton;
    private ProgressDialog mProgressDialog;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseStorageOperations mFirebaseStorageOperations;

    private User mCurrentUser;
    private Group mCurrentGroup;
    private Post mCurrentPost;
    private Bitmap mPostImageBitmap;
    private Uri mPostImageUri;
    private int mTextLength;

    public static Intent newIntent(Context packageContext, Group currentGroup, User currentUser) {
        Intent intent = new Intent(packageContext, GroupAddPostActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        intent.putExtra(CURRENT_GROUP_EXTRA, currentGroup);
        return intent;
    }

    private void setUpUser() {
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        Glide.with(this)
                .load(mCurrentUser.getUserProfilePhotoUri())
                .placeholder(ContextCompat.getDrawable(this, R.drawable.user_profile_photo_placeholder))
                .dontAnimate()
                .into(mAddPostProfileImageView)
        ;
    }

    private void setUpViews() {
        mCancelPostImageButton = (ImageButton) findViewById(R.id.group_add_post_cancel_image_button);
        mAddPostProfileImageView = (ImageView) findViewById(R.id.group_add_post_user_profile_photo);
        mPostEditText = (EditText) findViewById(R.id.group_add_post_edit_text);
        mPostImageFrameLayout = (FrameLayout) findViewById(R.id.group_add_post_image_frame_layout);
        mPostImageView = (ImageView) findViewById(R.id.group_add_post_image_image_view);
        mCancelPostIconImageButton = (ImageButton) findViewById(R.id.group_add_post_cancel_post_icon_image_button);
        mPhotoPickerImageButton = (ImageButton) findViewById(R.id.group_add_post_photo_picker_image_button);
        mPostTextMaxCharacters = (TextView) findViewById(R.id.group_add_post_text_max_characters);
        mPostButton = (Button) findViewById(R.id.group_add_post_button);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add_post);

        setUpViews();
        setUpFirebaseConfig();
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFirebaseStorageOperations = new FirebaseStorageOperations();
        setUpUser();

        mCurrentGroup = (Group) getIntent().getSerializableExtra(CURRENT_GROUP_EXTRA);

        mCancelPostImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelPost();
            }
        });

        mPhotoPickerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddImageToPost();
            }
        });

        mCancelPostIconImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeletePostImage();
            }
        });

        mPostEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                onPostTextChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                addPost();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            try {
                if (mPostImageBitmap != null) {
                    mPostImageBitmap.recycle();
                }
                mPostImageUri = data.getData();
                InputStream stream = getContentResolver().openInputStream(data.getData());
                mPostImageBitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                mPostImageView.setImageBitmap(mPostImageBitmap);
                mPostImageFrameLayout.setVisibility(View.VISIBLE);
                if (mPostEditText.getText().toString().trim().length() < mTextLength) {
                    mPostButton.setEnabled(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        onCancelPost();
    }

    private void setUpFirebaseConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    mTextLength = Integer.parseInt(mFirebaseRemoteConfig.getString(TEXT_MAX_CHARACTERS));
                    mPostTextMaxCharacters.setText(Integer.toString(mTextLength));
                }
            }
        });
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
    }

    private void onCancelPost() {
        if (mPostEditText.getText().toString().trim().length() > 0
                || mPostImageView.getDrawable() != null) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.alert_dialog_are_you_sure_message))
                    .setPositiveButton(getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.alert_dialog_cancel_button), null)
                    .show()
            ;
        } else {
            finish();
        }
    }

    private void onAddImageToPost() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Choose photo using:"), RC_PHOTO_PICKER);
    }

    private void onDeletePostImage() {
        mPostImageFrameLayout.setVisibility(View.GONE);
        mPostImageView.setImageDrawable(null);
        if (mPostEditText.getText().toString().trim().length() == 0) {
            mPostButton.setEnabled(false);
        }
    }

    private void onPostTextChanged(CharSequence charSequence) {
        if (charSequence.toString().trim().length() > 0) {
            int textLength = mTextLength - charSequence.length();
            mPostButton.setEnabled(true);
            if (charSequence.length() > mTextLength) {
                mPostTextMaxCharacters.setTextColor(ContextCompat.getColor(this, R.color.darkRedColor));
                mPostButton.setEnabled(false);
            } else {
                mPostTextMaxCharacters.setTextColor(ContextCompat.getColor(this, R.color.blackColor));
            }
            mPostTextMaxCharacters.setText(Integer.toString(textLength));
        } else {
            if (mPostImageView.getDrawable() == null) {
                mPostButton.setEnabled(false);
            }
            mPostTextMaxCharacters.setText(Integer.toString(mTextLength));
        }
    }

    private void addPost() {
        mCurrentPost = new Post();
        if (mPostEditText.getText().toString().trim().length() > 0 && mPostImageView.getDrawable() == null) {
            mCurrentPost.setPostText(mPostEditText.getText().toString());
            mCurrentPost.setPostDate(new Date());
            mCurrentPost.setPostLikesCounter(0);
            mCurrentPost.setPostCommentsCounter(0);
            mCurrentPost.setUserId(mCurrentUser.getUserId());
            mCurrentPost.setGroupId(mCurrentGroup.getGroupID());
            mCurrentPost
                    .setPostId(
                            mFirebaseDatabaseOperations
                                    .getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                                    .push()
                                    .getKey()
                    )
            ;
            mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                    .child(mCurrentPost.getPostId())
                    .setValue(mCurrentPost)
            ;
            mProgressDialog.dismiss();
            finish();
        } else if (mPostImageView.getDrawable() != null) {
            postWithPhotoToUserStorage();
        }
    }

    private void postWithPhotoToUserStorage() {
        StorageReference mPhotoReference = mFirebaseStorageOperations
                .getSpecificGroupStorageReference(mCurrentGroup.getGroupID())
                .child(mPostImageUri.getLastPathSegment());
        mPhotoReference.putFile(mPostImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (mPostEditText.getText().toString().trim().length() > 0) {
                            mCurrentPost.setPostText(mPostEditText.getText().toString());
                        }
                        mCurrentPost.setPostImageUri(taskSnapshot.getDownloadUrl().toString());
                        mCurrentPost.setPostDate(new Date());
                        mCurrentPost.setPostLikesCounter(0);
                        mCurrentPost.setPostCommentsCounter(0);
                        mCurrentPost.setUserId(mCurrentUser.getUserId());
                        mCurrentPost.setGroupId(mCurrentGroup.getGroupID());
                        mCurrentPost
                                .setPostId(
                                        mFirebaseDatabaseOperations
                                                .getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                                                .push()
                                                .getKey()
                                )
                        ;
                        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                                .child(mCurrentPost.getPostId())
                                .setValue(mCurrentPost)
                        ;
                        mProgressDialog.dismiss();
                        finish();
                    }
                })
        ;
    }
}
