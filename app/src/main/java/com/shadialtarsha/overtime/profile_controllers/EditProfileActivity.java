package com.shadialtarsha.overtime.profile_controllers;

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
import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.firebase.FirebaseStorageOperations;
import com.shadialtarsha.overtime.login_controllers.ValidateInput;
import com.shadialtarsha.overtime.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    public static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.profile_controllers.edit_profile_activity.current_user";
    private static final int RC_PHOTO_PICKER = 2;

    private Toolbar mEditProfileToolbar;
    private ImageView mUserProfilePhotoImageView;
    private EditText mUserNameEditText;
    private EditText mUserBioEditText;
    private FloatingActionButton mEditUserProfilePhotoFloatingActionButton;
    private ProgressDialog mProgressDialog;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseStorageOperations mFirebaseStorageOperations;
    private FirebaseAuthOperations mFirebaseAuthOperations;
    private User mCurrentUser;
    private Bitmap mUserProfilePhotoBitmap;
    private Uri mUserProfilePhotoUri;
    private boolean mUserProfilePhotoChangedAndSaved;
    private boolean mUserProfilePhotoChanged;
    private ValidateInput mValidateInput;


    public static Intent newIntent(Context packageContext, User currentUser) {
        Intent intent = new Intent(packageContext, EditProfileActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    private void setUpViews() {
        mEditProfileToolbar = (Toolbar) findViewById(R.id.edit_profile_toolbar);
        mEditProfileToolbar.setTitle(getString(R.string.edit_profile_activity));
        mUserProfilePhotoImageView = (ImageView) findViewById(R.id.edit_profile_user_profile_photo);
        mUserNameEditText = (EditText) findViewById(R.id.edit_profile_username_edit_text);
        mUserBioEditText = (EditText) findViewById(R.id.edit_profile_user_bio_edit_text);
        mEditUserProfilePhotoFloatingActionButton = (FloatingActionButton) findViewById(R.id.edit_user_profile_photo_fab);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
    }

    private void setUpUserInfo() {
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        Glide.with(EditProfileActivity.this)
                .load(mCurrentUser.getUserProfilePhotoUri())
                .placeholder(R.drawable.user_profile_photo_placeholder)
                .dontAnimate()
                .into(mUserProfilePhotoImageView)
        ;
        mUserNameEditText.setText(mCurrentUser.getUserName());
        mUserBioEditText.setText(mCurrentUser.getBioText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setUpViews();
        setSupportActionBar(mEditProfileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFirebaseStorageOperations = new FirebaseStorageOperations();
        mFirebaseAuthOperations = new FirebaseAuthOperations();
        mValidateInput = new ValidateInput();

        setUpUserInfo();

        mEditUserProfilePhotoFloatingActionButton.setOnClickListener(new View.OnClickListener() {
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
        returnToProfileActivity();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            try {
                if (mUserProfilePhotoBitmap != null) {
                    mUserProfilePhotoBitmap.recycle();
                }
                mUserProfilePhotoUri = data.getData();
                InputStream stream = getContentResolver().openInputStream(data.getData());
                mUserProfilePhotoBitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                mUserProfilePhotoChanged = true;
                mUserProfilePhotoImageView.setImageBitmap(mUserProfilePhotoBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUserInfo() {
        mProgressDialog.show();
        if (mUserProfilePhotoUri != null) {
            StorageReference storageReference = mFirebaseStorageOperations
                    .getSpecificUserStorageReference(mCurrentUser.getUserId())
                    .child(mUserProfilePhotoUri.getLastPathSegment());
            storageReference.putFile(mUserProfilePhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mCurrentUser.setUserProfilePhotoUri(taskSnapshot.getDownloadUrl().toString());
                    mUserProfilePhotoChangedAndSaved = true;
                    updateUserNameAndBio();
                    UserProfileChangeRequest update = new UserProfileChangeRequest
                            .Builder()
                            .setDisplayName(mCurrentUser.getUserName())
                            .setPhotoUri(Uri.parse(mCurrentUser.getUserProfilePhotoUri()))
                            .build();
                    mFirebaseAuthOperations.getCurrentUser().updateProfile(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            mProgressDialog.dismiss();
                            returnToProfileActivity();
                        }
                    });
                }
            });
        } else {
            updateUserNameAndBio();
            UserProfileChangeRequest update = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(mCurrentUser.getUserName())
                    .build();
            mFirebaseAuthOperations
                    .getCurrentUser()
                    .updateProfile(update)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    mProgressDialog.dismiss();
                    returnToProfileActivity();
                }
            });
        }
    }

    private void updateUserNameAndBio() {
        mCurrentUser.setUserName(mUserNameEditText.getText().toString());
        mCurrentUser.setBioText(mUserBioEditText.getText().toString());
        mCurrentUser.setUserNameLowerCase(mUserNameEditText.getText().toString().toLowerCase());
        mFirebaseDatabaseOperations
                .getSpecificUserNodeReference(mCurrentUser.getUserId())
                .setValue(mCurrentUser)
        ;
    }

    private void returnToProfileActivity() {
        if (somethingChangeAndNotSaved()) {
            new AlertDialog.Builder(EditProfileActivity.this)
                    .setMessage(R.string.alert_dialog_are_you_sure_message)
                    .setPositiveButton(R.string.alert_dialog_yes_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent data = ProfileActivity.newIntent(EditProfileActivity.this, mCurrentUser);
                            startActivity(data);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel_button, null)
                    .show()
            ;
        } else {
            Intent data = ProfileActivity.newIntent(EditProfileActivity.this, mCurrentUser);
            startActivity(data);
            finish();
        }
    }

    private boolean somethingChangeAndNotSaved() {
        if (mUserProfilePhotoChanged) {
            if (!mUserProfilePhotoChangedAndSaved) {
                return true;
            }
        }
        if (!mCurrentUser.getUserName().equals(mUserNameEditText.getText().toString())
                || !mCurrentUser.getBioText().equals(mUserBioEditText.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            returnToProfileActivity();
            return true;
        } else if (id == R.id.edit_profile_menu_save_item) {
            if (mValidateInput.isValidName(mUserNameEditText.getText().toString()) &&
                    mValidateInput.isValidBio(mUserBioEditText.getText().toString())) {
                updateUserInfo();
            } else {
                Toast.makeText(EditProfileActivity.this, R.string.edit_profile_invalid_input, Toast.LENGTH_SHORT)
                        .show()
                ;
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
