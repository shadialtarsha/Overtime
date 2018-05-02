package com.shadialtarsha.overtime.login_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseStorageOperations;
import com.shadialtarsha.overtime.firebase.FirebaseUserController;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class FirebaseEmailSignUp {

    private Context mContext;
    private AppCompatActivity mActivity;
    private ProgressDialog mProgressDialog;
    private EditText mNameEditText;
    private EditText mEmailEditText;
    private ValidateInput mValidateInput;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorageOperations mFirebaseStorageOperations;
    private FirebaseUserController mFirebaseUserController;

    public FirebaseEmailSignUp(Context context, ProgressDialog progressDialog, EditText nameEditText, EditText emailEditText) {
        mValidateInput = new ValidateInput();
        mContext = context;
        mActivity = (AppCompatActivity) mContext;
        mProgressDialog = progressDialog;
        mNameEditText = nameEditText;
        mEmailEditText = emailEditText;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorageOperations = new FirebaseStorageOperations();
        mFirebaseUserController = new FirebaseUserController();
    }

    public void signUp() {
        String name = mNameEditText.getText().toString();
        String email = mEmailEditText.getText().toString();
        if (mValidateInput.isValidName(name) && mValidateInput.isValidEmail(email)) {
            mProgressDialog.show();
            mFirebaseAuth
                    .createUserWithEmailAndPassword(email, UUID.randomUUID().toString())
                    .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = task.getResult().getUser();
                                mFirebaseAuth.sendPasswordResetEmail(firebaseUser.getEmail());
                                StorageReference defaultUserPhotoStorageReference = mFirebaseStorageOperations.getDefaultProfilePhotoUrlReference();
                                mFirebaseUserController.updateUserNameAndPhoto(defaultUserPhotoStorageReference, firebaseUser, mNameEditText.getText().toString());
                            }
                            if (!task.isSuccessful()) {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthUserCollisionException ex) {
                                    CustomAlertDialog.showAlertDialog(
                                            mContext,
                                            mContext.getString(R.string.alert_dialog_authentication_title),
                                            mContext.getString(R.string.alert_dialog_email_exists_message)
                                    );
                                } catch (Exception ex) {
                                    CustomAlertDialog.showAlertDialog(
                                            mContext,
                                            mContext.getString(R.string.alert_dialog_authentication_title),
                                            mContext.getString(R.string.alert_dialog_authentication_error_message)
                                    );
                                } finally {
                                    LoginManager.getInstance().logOut();
                                }
                            }
                            mProgressDialog.dismiss();
                        }
                    });
        } else {
            if (!mValidateInput.isValidName(name)) {
                mNameEditText.setError(mContext.getString(R.string.edit_text_invalid_name));
                mNameEditText.requestFocus();
            }
            if (!mValidateInput.isValidEmail(email)) {
                mEmailEditText.setError(mContext.getString(R.string.edit_text_invalid_email));
                mEmailEditText.requestFocus();
            }
        }
    }
}
