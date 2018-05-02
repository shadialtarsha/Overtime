package com.shadialtarsha.overtime.main_screen_controllers;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.shadialtarsha.overtime.R;

public class AboutUsActivity extends AppCompatActivity {

    private Toolbar mAboutUsToolbar;

    public static Intent newIntent(Context packageContext){
        return new Intent(packageContext, AboutUsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        mAboutUsToolbar = (Toolbar) findViewById(R.id.about_us_toolbar);
        mAboutUsToolbar.setTitle(getString(R.string.about_us_activity));
        setSupportActionBar(mAboutUsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
