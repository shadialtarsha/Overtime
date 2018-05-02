package com.shadialtarsha.overtime.launch_intro_controllers;

import android.content.Context;
import android.preference.PreferenceManager;

public class FirstLaunchPreference {

    private static final String PREF_FIRST_LAUNCH = "firstLaunch";

    public static boolean isFirstLaunch(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(PREF_FIRST_LAUNCH, true);
    }

    public static void setPrefFirstLaunch(Context context, boolean isFirstLaunch) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_FIRST_LAUNCH, isFirstLaunch)
                .apply()
        ;
    }
}
