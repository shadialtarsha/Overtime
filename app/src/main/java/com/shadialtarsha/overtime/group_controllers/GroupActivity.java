package com.shadialtarsha.overtime.group_controllers;

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
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.firebase.FirebaseStorageOperations;
import com.shadialtarsha.overtime.main_screen_controllers.MainActivity;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.GroupMember;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private static final String CURRENT_GROUP_EXTRA = "com.shadialtarsha.overtime.group_controllers.GroupActivity.current_group";
    private static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.group_controllers.GroupActivity.current_user";

    private Toolbar mGroupToolbar;
    private AppBarLayout mGroupAppBarLayout;
    private ImageView mGroupPhotoImageView;
    private TextView mGroupNameTextView;
    private TextView mGroupBioTextView;
    private TextView mUserRankTextView;
    private TextView mPostsCounterTextView;
    private TextView mMembersCounterTextView;
    private TextView mLikesCounterTextView;
    private TabLayout mGroupTabLayout;
    private ViewPager mGroupViewPager;
    private FloatingActionButton mAddPostFloatingActionButton;
    private Context mContext;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseStorageOperations mFirebaseStorageOperations;

    private Group mCurrentGroup;
    private User mCurrentUser;

    public static Intent newIntent(Context packageContext, Group currentGroup, User currentUser) {
        Intent intent = new Intent(packageContext, GroupActivity.class);
        intent.putExtra(CURRENT_GROUP_EXTRA, currentGroup);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    private void setUpViews() {
        mGroupAppBarLayout = (AppBarLayout) findViewById(R.id.group_appbar);
        mGroupToolbar = (Toolbar) findViewById(R.id.group_toolbar);
        mGroupPhotoImageView = (ImageView) findViewById(R.id.group_photo_image_view);
        mGroupNameTextView = (TextView) findViewById(R.id.group_name_text_view);
        mGroupBioTextView = (TextView) findViewById(R.id.group_bio_text_view);
        mPostsCounterTextView = (TextView) findViewById(R.id.group_posts_counter_text_view);
        mMembersCounterTextView = (TextView) findViewById(R.id.group_members_counter_text_view);
        mLikesCounterTextView = (TextView) findViewById(R.id.group_likes_counter_text_view);
        mAddPostFloatingActionButton = (FloatingActionButton) findViewById(R.id.group_add_post_fab);
        mGroupViewPager = (ViewPager) findViewById(R.id.group_view_pager);
        mGroupTabLayout = (TabLayout) findViewById(R.id.group_tab_layout);
        mUserRankTextView = (TextView) findViewById(R.id.group_user_rank_text_view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setUpViews();
        setSupportActionBar(mGroupToolbar);
        mGroupToolbar.setTitleTextColor(ContextCompat.getColor(GroupActivity.this, R.color.whiteColor));
        mGroupToolbar.setSubtitleTextColor(ContextCompat.getColor(GroupActivity.this, R.color.whiteColor));
        mGroupToolbar.setTitle(getString(R.string.group_activity_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFirebaseStorageOperations = new FirebaseStorageOperations();
        mContext = this;
        setUpCurrentUserAndGroupInfo();
        mGroupAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int totalScrollRange = appBarLayout.getTotalScrollRange();
                verticalOffset = Math.abs(verticalOffset);
                if (verticalOffset > (totalScrollRange / 2)) {
                    mGroupToolbar.setTitle(mCurrentGroup.getGroupName());
                    setUpPostsCounterForToolbar();
                } else {
                    mGroupToolbar.setTitle(getString(R.string.group_activity_name));
                    mGroupToolbar.setSubtitle("");
                }
            }
        });

        setupViewPager(mGroupViewPager);
        mGroupTabLayout.setupWithViewPager(mGroupViewPager);

        mAddPostFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(GroupAddPostActivity.newIntent(GroupActivity.this, mCurrentGroup, mCurrentUser), 0);
            }
        });

    }

    private void setUpCurrentUserAndGroupInfo() {
        mCurrentGroup = (Group) getIntent().getSerializableExtra(CURRENT_GROUP_EXTRA);
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        mFirebaseDatabaseOperations.getSpecificGroupsNodeReference(mCurrentGroup.getGroupID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Glide.with(GroupActivity.this)
                                .load(mCurrentGroup.getGroupPhotoUri())
                                .placeholder(ContextCompat.getDrawable(mGroupPhotoImageView.getContext(), R.drawable.group_photo_placeholder))
                                .dontAnimate()
                                .into(mGroupPhotoImageView);
                        mGroupNameTextView.setText(mCurrentGroup.getGroupName());
                        mGroupBioTextView.setText(mCurrentGroup.getGroupBio());
                        setUpSocialInformation();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mFirebaseDatabaseOperations
                .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(mCurrentUser.getUserId())) {
                            GroupMember member = dataSnapshot.child(mCurrentUser.getUserId()).getValue(GroupMember.class);
                            mUserRankTextView.setText(member.getUserRank());
                        } else {
                            mUserRankTextView.setText(getString(R.string.group_join_button));
                            mUserRankTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialog.Builder(mContext)
                                            .setMessage("Are you want to join " + mCurrentGroup.getGroupName() + "?")
                                            .setPositiveButton(getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    GroupMember member = new GroupMember();
                                                    member.setUserId(mCurrentUser.getUserId());
                                                    member.setUserRank(getString(R.string.group_member_user_rank));
                                                    mFirebaseDatabaseOperations
                                                            .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                                                            .child(mCurrentUser.getUserId())
                                                            .setValue(member)
                                                    ;
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.alert_dialog_cancel_button), null)
                                            .show()
                                    ;
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
    }

    private void setUpSocialInformation() {
        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
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
                        Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mFirebaseDatabaseOperations.getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mMembersCounterTextView.setText(Integer.toString(0));
                        } else {
                            mMembersCounterTextView.setText(Long.toString(dataSnapshot.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
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
                        Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
    }

    private void setUpPostsCounterForToolbar() {
        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mGroupToolbar.setSubtitle(Integer.toString(0) + " post");
                        } else {
                            mGroupToolbar.setSubtitle(Long.toString(dataSnapshot.getChildrenCount()) + " posts");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        GroupPostsFragment groupPostsFragment = GroupPostsFragment.newInstance(mCurrentGroup, mCurrentUser);
        adapter.addFragment(groupPostsFragment, getString(R.string.group_posts_tab_item));

        GroupMembersFragment groupMembersFragment = GroupMembersFragment.newInstance(mCurrentGroup, mCurrentUser);
        adapter.addFragment(groupMembersFragment, getString(R.string.group_members_tab_item));

        /*GroupRaceFragment groupRaceFragment = new GroupRaceFragment();
        adapter.addFragment(groupRaceFragment, getString(R.string.group_race_tab_item));*/

        GroupMediaFragment groupMediaFragment = GroupMediaFragment.newInstance(mCurrentGroup);
        adapter.addFragment(groupMediaFragment, getString(R.string.group_media_tab_item));

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_group, menu);
        //final MenuItem chatItem = menu.findItem(R.id.group_menu_chat_item);
        final MenuItem editItem = menu.findItem(R.id.group_menu_edit_item);
        final MenuItem deleteItem = menu.findItem(R.id.group_menu_delete_item);
        final MenuItem leaveItem = menu.findItem(R.id.group_menu_leave_item);
        mFirebaseDatabaseOperations
                .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(mCurrentUser.getUserId())) {
                            GroupMember groupMember = dataSnapshot.child(mCurrentUser.getUserId()).getValue(GroupMember.class);
                            switch (groupMember.getUserRank()) {
                                case "Admin":
                                    //chatItem.setVisible(true);
                                    editItem.setVisible(true);
                                    deleteItem.setVisible(true);
                                    leaveItem.setVisible(false);
                                    break;
                                case "Co-Admin":
                                    //chatItem.setVisible(true);
                                    editItem.setVisible(true);
                                    deleteItem.setVisible(false);
                                    leaveItem.setVisible(true);
                                    break;
                                case "Member":
                                    //chatItem.setVisible(true);
                                    editItem.setVisible(false);
                                    deleteItem.setVisible(false);
                                    leaveItem.setVisible(true);
                                    break;
                            }
                        } else {
                            //chatItem.setVisible(false);
                            editItem.setVisible(false);
                            deleteItem.setVisible(false);
                            leaveItem.setVisible(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*if (id == R.id.group_menu_chat_item) {
            return true;
        } else*/ if (id == R.id.group_menu_edit_item) {
            startActivity(EditGroupActivity.newIntent(GroupActivity.this, mCurrentGroup, mCurrentUser));
            finish();
            return true;
        } else if (id == R.id.group_menu_delete_item) {
            mFirebaseDatabaseOperations
                    .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot members : dataSnapshot.getChildren()) {
                                GroupMember member = members.getValue(GroupMember.class);
                                mFirebaseDatabaseOperations
                                        .getSpecificUserNodeReference(member.getUserId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                User user = dataSnapshot.getValue(User.class);
                                                user.setGroupId("None");
                                                mFirebaseDatabaseOperations
                                                        .getSpecificUserNodeReference(user.getUserId())
                                                        .setValue(user)
                                                ;
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                                            }
                                        })
                                ;
                            }
                            mFirebaseStorageOperations
                                    .getSpecificGroupStorageReference(mCurrentGroup.getGroupID())
                                    .delete()
                            ;
                            mFirebaseDatabaseOperations
                                    .getSpecificGroupsNodeReference(mCurrentGroup.getGroupID())
                                    .removeValue()
                            ;
                            mCurrentUser.setGroupId("None");
                            mFirebaseDatabaseOperations
                                    .getSpecificUserNodeReference(mCurrentUser.getUserId())
                                    .setValue(mCurrentUser)
                            ;
                            startActivity(MainActivity.newIntent(mContext));
                            finish();
                            mFirebaseDatabaseOperations
                                    .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                                    .removeValue()
                            ;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
            mFirebaseDatabaseOperations
                    .getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot posts : dataSnapshot.getChildren()) {
                                Post post = posts.getValue(Post.class);
                                mFirebaseDatabaseOperations
                                        .getSpecificCommentsNodeReference(post.getPostId())
                                        .removeValue()
                                ;
                                mFirebaseDatabaseOperations
                                        .getSpecificLikesNodeReference(post.getPostId())
                                        .removeValue()
                                ;
                            }
                            mFirebaseDatabaseOperations
                                    .getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                                    .removeValue()
                            ;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
            return true;
        } else if (id == R.id.group_menu_leave_item) {
            mFirebaseDatabaseOperations
                    .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                    .child(mCurrentUser.getUserId())
                    .removeValue()
            ;
            mCurrentUser.setGroupId("None");
            mFirebaseDatabaseOperations
                    .getSpecificUserNodeReference(mCurrentUser.getUserId())
                    .setValue(mCurrentUser)
            ;
            startActivity(MainActivity.newIntent(mContext));
            finish();
            return true;
        } else {
            return false;
        }
    }
}
