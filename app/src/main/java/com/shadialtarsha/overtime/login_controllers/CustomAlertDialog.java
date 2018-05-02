package com.shadialtarsha.overtime.login_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.shadialtarsha.overtime.R;

public class CustomAlertDialog {

    public static void showAlertDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getText(R.string.alert_dialog_ok_button), null)
                .setIcon(context.getDrawable(R.drawable.ic_warning_alert_dialog))
                .show()
        ;
    }

    public static void showResetPasswordDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getText(R.string.alert_dialog_reset_password_title));
        builder.setMessage(context.getText(R.string.alert_dialog_enter_email_message));
        LayoutInflater inflater = LayoutInflater.from(context);
        View alertDialogView = inflater.inflate(R.layout.alert_dialog_reset_password, null);
        builder.setView(alertDialogView);
        final EditText emailEditText = (EditText) alertDialogView.findViewById(R.id.reset_password_edit_text);
        builder.setPositiveButton(context.getText(R.string.alert_dialog_ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseForgetPassword firebaseForgetPassword = new FirebaseForgetPassword(context, emailEditText);
                firebaseForgetPassword.forgetPassword();
            }
        });
        builder.show();
    }
}
