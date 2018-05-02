package com.shadialtarsha.overtime.profile_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.login_controllers.LoginActivity;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.profile_controllers.ProfileActivity.current_user";

    private FirebaseAuthOperations mFirebaseAuthOperations;
    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FollowOperationControl mFollowOperationControl;

    private Toolbar mProfileToolbar;
    private AppBarLayout mProfileAppBarLayout;
    private ImageView mProfileUserProfilePhotoImageView;
    private TextView mProfileUsernameTextView;
    private TextView mProfileBioTextView;
    private TextView mPostsCounterTextView;
    private TextView mFollowersCounterTextView;
    private TextView mLikesCounterTextView;
    private TabLayout mProfileTabLayout;
    private ViewPager mProfileViewPager;
    private FloatingActionButton mAddPostFloatingActionButton;
    private Context mContext;

    private User mCurrentUser;

    public static Intent newIntent(Context packageContext, User currentUser) {
        Intent intent = new Intent(packageContext, ProfileActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    private void setUpViews() {
        mProfileAppBarLayout = (AppBarLayout) findViewById(R.id.profile_appbar);
        mProfileToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mProfileUserProfilePhotoImageView = (ImageView) findViewById(R.id.profile_user_profile_photo);
        mProfileUsernameTextView = (TextView) findViewById(R.id.profile_username_text_view);
        mProfileBioTextView = (TextView) findViewById(R.id.profile_bio_text_view);
        mPostsCounterTextView = (TextView) findViewById(R.id.profile_posts_counter_text_view);
        mFollowersCounterTextView = (TextView) findViewById(R.id.profile_followers_counter_text_view);
        mLikesCounterTextView = (TextView) findViewById(R.id.profile_likes_counter_text_view);
        mAddPostFloatingActionButton = (FloatingActionButton) findViewById(R.id.add_post_fab);
        mProfileViewPager = (ViewPager) findViewById(R.id.profile_view_pager);
        mProfileTabLayout = (TabLayout) findViewById(R.id.profile_tab_layout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setUpViews();
        setSupportActionBar(mProfileToolbar);
        mProfileToolbar.setTitleTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.whiteColor));
        mProfileToolbar.setSubtitleTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.whiteColor));
        mProfileToolbar.setTitle(getString(R.string.profile_activity_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseAuthOperations = new FirebaseAuthOperations();
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFollowOperationControl = new FollowOperationControl();
        mContext = this;
        setUpCurrentUserInfo();
        mProfileAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                verticalOffset = Math.abs(verticalOffset);
                if (verticalOffset > (totalScrollRange / 2)) {
                    mProfileToolbar.setTitle(mCurrentUser.getUserName());
                    setUpPostsCounterForToolbar();
                } else {
                    mProfileToolbar.setTitle(getString(R.string.profile_activity_name));
                    mProfileToolbar.setSubtitle("");
                }
            }
        });

        setupViewPager(mProfileViewPager);
        mProfileTabLayout.setupWithViewPager(mProfileViewPager);

        if (!mCurrentUser.getUserId().equals(mFirebaseAuthOperations.getCurrentUserId())) {
            mAddPostFloatingActionButton.setVisibility(View.GONE);
        }

        mAddPostFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(AddPostActivity.newIntent(ProfileActivity.this, mCurrentUser), 0);
            }
        });
    }

    private void setUpCurrentUserInfo() {
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        mFirebaseDatabaseOperations.getSpecificUserNodeReference(mCurrentUser.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Glide.with(ProfileActivity.this)
                                .load(mCurrentUser.getUserProfilePhotoUri())
                                .placeholder(ContextCompat.getDrawable(mProfileUserProfilePhotoImageView.getContext(), R.drawable.user_profile_photo_placeholder))
                                .dontAnimate()
                                .into(mProfileUserProfilePhotoImageView);
                        mProfileUsernameTextView.setText(mCurrentUser.getUserName());
                        mProfileBioTextView.setText(mCurrentUser.getBioText());
                        setUpSocialInformation();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.PROFILE_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
    }

    private void setUpSocialInformation() {
        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mPostsCounterTextView.setText(Integer.toString(0));
                        } else {
                            mPostsCounterTextView.setText(Long.toString(dataSnapshot.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.PROFILE_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mFirebaseDatabaseOperations.getSpecificFollowersNodeReference(mCurrentUser.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mFollowersCounterTextView.setText(Integer.toString(0));
                        } else {
                            mFollowersCounterTextView.setText(Long.toString(dataSnapshot.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.PROFILE_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mLikesCounterTextView.setText(Integer.toString(0));
                        } else {
                            long counter = 0;
                            for (DataSnapshot posts : dataSnapshot.getChildren()) {
                                Post post = posts.getValue(Post.class);
                                counter += post.getPostLikesCounter();
                            }
                            mLikesCounterTextView.setText(Long.toString(counter));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.PROFILE_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
    }

    private void setUpPostsCounterForToolbar() {
        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mProfileToolbar.setSubtitle(Integer.toString(0) + " post");
                        } else {
                            mProfileToolbar.setSubtitle(Long.toString(dataSnapshot.getChildrenCount()) + " posts");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.PROFILE_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        if (!mCurrentUser.getUserId().equals(mFirebaseAuthOperations.getCurrentUserId())) {
            MenuItem logout_item = menu.findItem(R.id.profile_menu_logout_item);
            logout_item.setVisible(false);
            MenuItem edit_profile_item = menu.findItem(R.id.profile_menu_edit_profile_item);
            edit_profile_item.setVisible(false);
            final MenuItem follow_item = menu.findItem(R.id.profile_menu_follow_item);
            mFirebaseDatabaseOperations
                    .getSpecificFollowingNodeReference(mFirebaseAuthOperations.getCurrentUserId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(mCurrentUser.getUserId())) {
                                follow_item.setIcon(R.drawable.ic_profile_menu_followed_item);
                            } else {
                                follow_item.setIcon(R.drawable.ic_profile_menu_follow_item);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.PROFILE_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
            return true;
        } else {
            MenuItem follow_item = menu.findItem(R.id.profile_menu_follow_item);
            follow_item.setVisible(false);
            return true;
        }

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile_menu_logout_item) {
            if (mFirebaseAuthOperations
                    .getUserProviderInfo(mFirebaseAuthOperations.getCurrentUser())
                    .getProviderId().equals("facebook.com")) {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
            } else {
                FirebaseAuth.getInstance().signOut();
            }
            startActivity(LoginActivity.newIntent(ProfileActivity.this));
            finish();
            return true;
        } else if (id == R.id.profile_menu_edit_profile_item) {
            startActivity(EditProfileActivity.newIntent(ProfileActivity.this, mCurrentUser));
            finish();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.profile_menu_follow_item) {
            mFirebaseDatabaseOperations
                    .getSpecificFollowingNodeReference(mFirebaseAuthOperations.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(mCurrentUser.getUserId())) {
                                new AlertDialog.Builder(mContext)
                                        .setMessage("unFollow " + mCurrentUser.getUserName() + "?")
                                        .setPositiveButton(getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mFollowOperationControl.unFollowOperation(mCurrentUser);
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.alert_dialog_cancel_button), null)
                                        .show()
                                ;
                            } else {
                                new AlertDialog.Builder(mContext)
                                        .setMessage("Follow " + mCurrentUser.getUserName() + "?")
                                        .setPositiveButton(getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mFollowOperationControl.followOperation(mCurrentUser);
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.alert_dialog_cancel_button), null)
                                        .show()
                                ;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.PROFILE_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        PostsFragment postsFragment = PostsFragment.newInstance(mCurrentUser);
        adapter.addFragment(postsFragment, getString(R.string.profile_posts_tab_item));

        FollowersFragment followersFragment = FollowersFragment.newInstance(mCurrentUser);
        adapter.addFragment(followersFragment, getString(R.string.profile_followers_tab_item));

        FollowingFragment followingFragment = FollowingFragment.newInstance(mCurrentUser);
        adapter.addFragment(followingFragment, getString(R.string.profile_following_tab_item));

        MediaFragment mediaFragment = MediaFragment.newInstance(mCurrentUser);
        adapter.addFragment(mediaFragment, getString(R.string.profile_media_tab_item));

        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }
    }
}

