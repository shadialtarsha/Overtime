package com.shadialtarsha.overtime.login_controllers;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateInput {

    public boolean isValidName(String name) {
        if (name != null && name.length() > 2) {
            return true;
        }
        return false;
    }

    public boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isValidPassword(String password) {
        if (password != null && password.length() > 4) {
            return true;
        }
        return false;
    }

    public boolean isValidBio(String bio) {
        if (bio != null && bio.length() > 2) {
            return true;
        }
        return false;
    }
}
