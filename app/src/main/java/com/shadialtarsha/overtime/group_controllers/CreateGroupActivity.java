package com.shadialtarsha.overtime.group_controllers;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseStorageOperations;
import com.shadialtarsha.overtime.login_controllers.ValidateInput;
import com.shadialtarsha.overtime.main_screen_controllers.MainActivity;
import com.shadialtarsha.overtime.model.User;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.InputStream;

public class CreateGroupActivity extends AppCompatActivity {

    public static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.group_controllers.create_group_activity.current_user";
    private static final int RC_PHOTO_PICKER = 2;

    private Toolbar mCreateGroupToolbar;
    private ImageView mGroupPhotoImageView;
    private EditText mGroupNameEditText;
    private EditText mGroupBioEditText;
    private FloatingActionButton mEditGroupPhotoFloatingActionButton;

    private FirebaseStorageOperations mFirebaseStorageOperations;
    private CreateGroup mCreateGroup;
    private User mCurrentUser;
    private Bitmap mGroupPhotoBitmap;
    private Uri mGroupPhotoUri;
    private ValidateInput mValidateInput;

    public static Intent newIntent(Context packageContext, User currentUser) {
        Intent intent = new Intent(packageContext, CreateGroupActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    private void setUpViews() {
        mCreateGroupToolbar = (Toolbar) findViewById(R.id.create_group_toolbar);
        mCreateGroupToolbar.setTitle(getString(R.string.create_group_activity));
        mGroupPhotoImageView = (ImageView) findViewById(R.id.create_group_group_photo);
        mGroupNameEditText = (EditText) findViewById(R.id.create_group_group_name_edit_text);
        mGroupBioEditText = (EditText) findViewById(R.id.create_group_group_bio_edit_text);
        mEditGroupPhotoFloatingActionButton = (FloatingActionButton) findViewById(R.id.create_group_photo_picker_fab);
    }

    private void setUpInfo() {
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        mFirebaseStorageOperations
                .getDefaultGroupPhotoUrlReference()
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(mGroupPhotoImageView.getContext())
                                .load(uri.toString())
                                .into(mGroupPhotoImageView)
                        ;
                    }
                })
        ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setUpViews();
        setSupportActionBar(mCreateGroupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseStorageOperations = new FirebaseStorageOperations();
        mValidateInput = new ValidateInput();
        setUpInfo();
        mCreateGroup = new CreateGroup(CreateGroupActivity.this, mCurrentUser);

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
        handleBackEvent();
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
                mGroupPhotoImageView.setImageBitmap(mGroupPhotoBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createGroup() {
        String groupName = mGroupNameEditText.getText().toString();
        String groupBio = mGroupBioEditText.getText().toString();
        if (mGroupPhotoUri != null) {
            mCreateGroup.createGroup(
                    groupName,
                    groupBio,
                    mGroupPhotoUri
            );
        } else {
            mCreateGroup.createGroupWithDefaultPhoto(
                    groupName,
                    groupBio
            );
        }
    }

    private boolean checkIfUserWantsDiscard() {
        return mGroupPhotoUri != null || !mGroupNameEditText.getText().toString().trim().isEmpty()
                || !mGroupBioEditText.getText().toString().trim().isEmpty();
    }

    private void handleBackEvent() {
        if (checkIfUserWantsDiscard()) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.alert_dialog_are_you_sure_message))
                    .setPositiveButton(getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(MainActivity.newIntent(CreateGroupActivity.this));
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.alert_dialog_cancel_button), null)
                    .show()
            ;
        } else {
            startActivity(MainActivity.newIntent(CreateGroupActivity.this));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            handleBackEvent();
            return true;
        } else if (id == R.id.create_group_menu_create_item) {
            if (mValidateInput.isValidName(mGroupNameEditText.getText().toString()) &&
                    mValidateInput.isValidBio(mGroupBioEditText.getText().toString())) {
                createGroup();
            } else {
                Toast.makeText(CreateGroupActivity.this, R.string.edit_profile_invalid_input, Toast.LENGTH_SHORT)
                        .show()
                ;
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}

