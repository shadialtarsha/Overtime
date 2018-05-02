package com.shadialtarsha.overtime.launch_intro_controllers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.login_controllers.LoginActivity;

public class IntroActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private LinearLayout mDotsLayout;
    private Button mSkipButton, mNextButton;
    private TextView[] mDots;
    private int[] mLayouts;

    public static Intent newIntent(Context packageContext){
        return new Intent(packageContext, IntroActivity.class);
    }

    private void setupViews(){
        mViewPager = (ViewPager) findViewById(R.id.intro_view_pager);
        mDotsLayout = (LinearLayout) findViewById(R.id.intro_dots_layout);
        mSkipButton = (Button) findViewById(R.id.intro_skip_button);
        mNextButton = (Button) findViewById(R.id.intro_next_button);
        mLayouts = new int[]{R.layout.intro_slide1, R.layout.intro_slide2, R.layout.intro_slide3};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);

        setupViews();
        addBottomDots(0);
        setupViewPager();
    }

    public void nextButtonOnClick(View v) {
        int current = mViewPager.getCurrentItem() + 1;
        if (current < mLayouts.length) {
            mViewPager.setCurrentItem(current);
        } else {
            launchHomeScreen();
        }
    }

    public void skipButtonOnClick(View v) {
        launchHomeScreen();
    }

    private void launchHomeScreen() {
        startActivity(LoginActivity.newIntent(IntroActivity.this));
        finish();
    }

    private void addBottomDots(int currentPage) {
        mDots = new TextView[mLayouts.length];
        mDotsLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                mDots[i].setText(Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY));
            } else {
                mDots[i].setText(Html.fromHtml("&#8226;"));
            }
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(ContextCompat.getColor(this, R.color.blackColor));
            mDotsLayout.addView(mDots[i]);
        }
        if (mDots.length > 0)
            mDots[currentPage].setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    private void setupViewPager(){
        mViewPagerAdapter = new ViewPagerAdapter();
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addBottomDots(position);
                if (position == mLayouts.length - 1) {
                    mNextButton.setText(getString(R.string.intro_start_button));
                    mSkipButton.setVisibility(View.GONE);
                } else {
                    mNextButton.setText(getString(R.string.intro_next_button));
                    mSkipButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class ViewPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(mLayouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mLayouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

}