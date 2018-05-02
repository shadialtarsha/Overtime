package com.shadialtarsha.overtime.group_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.firebase.FirebaseStorageOperations;
import com.shadialtarsha.overtime.login_controllers.ValidateInput;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;

public class EditGroupActivity extends AppCompatActivity {

    public static final String CURRENT_GROUP_EXTRA = "com.shadialtarsha.overtime.group_controllers.EditGroupActivity.current_group";
    public static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.group_controllers.EditGroupActivity.current_user";
    private static final int RC_PHOTO_PICKER = 2;

    private Toolbar mEditGroupToolbar;
    private ImageView mGroupPhotoImageView;
    private EditText mGroupNameEditText;
    private EditText mGroupBioEditText;
    private FloatingActionButton mEditGroupPhotoFloatingActionButton;
    private ProgressDialog mProgressDialog;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseStorageOperations mFirebaseStorageOperations;
    private Group mCurrentGroup;
    private User mCurrentUser;
    private Bitmap mGroupPhotoBitmap;
    private Uri mGroupPhotoUri;
    private boolean mGroupPhotoChangedAndSaved;
    private boolean mGroupPhotoChanged;
    private ValidateInput mValidateInput;


    public static Intent newIntent(Context packageContext, Group currentGroup, User currentUser) {
        Intent intent = new Intent(packageContext, EditGroupActivity.class);
        intent.putExtra(CURRENT_GROUP_EXTRA, currentGroup);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    private void setUpViews() {
        mEditGroupToolbar = (Toolbar) findViewById(R.id.edit_group_toolbar);
        mEditGroupToolbar.setTitle(getString(R.string.edit_group_activity));
        mGroupPhotoImageView = (ImageView) findViewById(R.id.edit_group_group__photo);
        mGroupNameEditText = (EditText) findViewById(R.id.edit_group_group_name_edit_text);
        mGroupBioEditText = (EditText) findViewById(R.id.edit_group_group_bio_edit_text);
        mEditGroupPhotoFloatingActionButton = (FloatingActionButton) findViewById(R.id.edit_group_group_photo_fab);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
    }

    private void setUpInformation() {
        mCurrentGroup = (Group) getIntent().getSerializableExtra(CURRENT_GROUP_EXTRA);
        Glide.with(this)
                .load(mCurrentGroup.getGroupPhotoUri())
                .placeholder(R.drawable.group_photo_placeholder)
                .dontAnimate()
                .into(mGroupPhotoImageView)
        ;
        mGroupNameEditText.setText(mCurrentGroup.getGroupName());
        mGroupBioEditText.setText(mCurrentGroup.getGroupBio());
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        setUpViews();
        setSupportActionBar(mEditGroupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFirebaseStorageOperations = new FirebaseStorageOperations();
        mValidateInput = new ValidateInput();

        setUpInformation();

        mEditGroupPhotoFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Choose photo using:"), RC_PHOTO_PICKER);
            }
        });
    }

    @Override
    public void onBackPressed() {
        returnToGroupActivity();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            try {
                if (mGroupPhotoBitmap != null) {
                    mGroupPhotoBitmap.recycle();
                }
                mGroupPhotoUri = data.getData();
                InputStream stream = getContentResolver().openInputStream(data.getData());
                mGroupPhotoBitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                mGroupPhotoChanged = true;
                mGroupPhotoImageView.setImageBitmap(mGroupPhotoBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGroupInfo() {
        mProgressDialog.show();
        if (mGroupPhotoUri != null) {
            StorageReference storageReference = mFirebaseStorageOperations
                    .getSpecificGroupStorageReference(mCurrentGroup.getGroupID())
                    .child(mGroupPhotoUri.getLastPathSegment());
            storageReference.putFile(mGroupPhotoUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mCurrentGroup.setGroupPhotoUri(taskSnapshot.getDownloadUrl().toString());
                    mGroupPhotoChangedAndSaved = true;
                    updateGroupNameAndBio();
                }
            });
        } else {
            updateGroupNameAndBio();
        }
    }

    private void updateGroupNameAndBio() {
        mCurrentGroup.setGroupName(mGroupNameEditText.getText().toString());
        mCurrentGroup.setGroupBio(mGroupBioEditText.getText().toString());
        mCurrentGroup.setGroupNameLowerCase(mGroupNameEditText.getText().toString().toLowerCase());
        mFirebaseDatabaseOperations
                .getSpecificGroupsNodeReference(mCurrentGroup.getGroupID())
                .setValue(mCurrentGroup)
        ;
        mProgressDialog.dismiss();
        returnToGroupActivity();
    }

    private void returnToGroupActivity() {
        if (somethingChangeAndNotSaved()) {
            new AlertDialog.Builder(EditGroupActivity.this)
                    .setMessage(R.string.alert_dialog_are_you_sure_message)
                    .setPositiveButton(R.string.alert_dialog_yes_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent data = GroupActivity.newIntent(EditGroupActivity.this, mCurrentGroup, mCurrentUser);
                            startActivity(data);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel_button, null)
                    .show()
            ;
        } else {
            Intent data = GroupActivity.newIntent(EditGroupActivity.this, mCurrentGroup, mCurrentUser);
            startActivity(data);
            finish();
        }
    }

    private boolean somethingChangeAndNotSaved() {
        if (mGroupPhotoChanged) {
            if (!mGroupPhotoChangedAndSaved) {
                return true;
            }
        }
        if (!mCurrentGroup.getGroupName().equals(mGroupNameEditText.getText().toString())
                || !mCurrentGroup.getGroupBio().equals(mGroupBioEditText.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            returnToGroupActivity();
            return true;
        } else if (id == R.id.edit_group_menu_save_item) {
            if (mValidateInput.isValidName(mGroupNameEditText.getText().toString()) &&
                    mValidateInput.isValidBio(mGroupBioEditText.getText().toString())) {
                updateGroupInfo();
            } else {
                Toast.makeText(EditGroupActivity.this, R.string.edit_group_invalid_input, Toast.LENGTH_SHORT)
                        .show()
                ;
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
