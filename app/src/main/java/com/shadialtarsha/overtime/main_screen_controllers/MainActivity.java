package com.shadialtarsha.overtime.main_screen_controllers;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.group_controllers.CreateGroupActivity;
import com.shadialtarsha.overtime.group_controllers.GroupActivity;
import com.shadialtarsha.overtime.group_controllers.GroupAddPostActivity;
import com.shadialtarsha.overtime.group_controllers.GroupSearchActivity;
import com.shadialtarsha.overtime.inbox_controllers.InboxActivity;
import com.shadialtarsha.overtime.login_controllers.LoginActivity;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.User;
import com.shadialtarsha.overtime.profile_controllers.AddPostActivity;
import com.shadialtarsha.overtime.profile_controllers.ProfileActivity;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.lapism.searchview.SearchAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mMainToolbar;
    private ProgressDialog mProgressDialog;
    private FloatingActionButton mAddPostFloatingActionButton;
    private FloatingActionButton mAddGroupPostFloatingActionButton;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private View mNavigationHeader;
    private ImageView mNavigationHeaderUserProfilePhoto;
    private TextView mNavigationHeaderUserName;
    private TextView mNavigationHeaderUserEmail;
    private TabLayout mMainTabLayout;
    private ViewPager mMainViewPager;
    private SearchView mSearchView;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private FirebaseAuthOperations mFirebaseAuthOperations;
    private SearchAdapter mSearchSuggestionsAdapter;
    private HashSet<String> mSearchSuggestionsHashSet;
    private User mCurrentUser;
    private Group mCurrentGroup;
    private Context mContext;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, MainActivity.class);
    }

    private void setUpViews() {
        mMainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mAddPostFloatingActionButton = (FloatingActionButton) findViewById(R.id.add_post_fab);
        mAddPostFloatingActionButton.hide();
        mAddGroupPostFloatingActionButton = (FloatingActionButton) findViewById(R.id.add_group_post_fab);
        mAddGroupPostFloatingActionButton.hide();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.main_navigation_view);
        mNavigationHeader = mNavigationView.getHeaderView(0);
        mNavigationView.setItemIconTintList(null);
        mNavigationHeaderUserProfilePhoto = (ImageView) mNavigationHeader.findViewById(R.id.navigation_header_user_profile_image);
        mNavigationHeaderUserName = (TextView) mNavigationHeader.findViewById(R.id.navigation_header_username);
        mNavigationHeaderUserEmail = (TextView) mNavigationHeader.findViewById(R.id.navigation_header_user_email);
        mMainTabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        mMainViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.progress_dialog_message));
        mSearchView = (SearchView) findViewById(R.id.main_search_view);
        mContext = this;
    }

    private void setUpCurrentUser() {
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFirebaseAuthOperations = new FirebaseAuthOperations();
        mProgressDialog.show();
        mFirebaseDatabaseOperations.getSpecificUserNodeReference(mFirebaseAuthOperations.getCurrentUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            mCurrentUser = dataSnapshot.getValue(User.class);
                            mProgressDialog.dismiss();
                            Glide.with(getApplicationContext())
                                    .load(mCurrentUser.getUserProfilePhotoUri())
                                    .placeholder(R.drawable.user_profile_photo_placeholder)
                                    .dontAnimate()
                                    .into(mNavigationHeaderUserProfilePhoto);
                            mNavigationHeaderUserName.setText(mCurrentUser.getUserName());
                            mNavigationHeaderUserEmail.setText(mCurrentUser.getUserEmail());
                            if (!mCurrentUser.getGroupId().equalsIgnoreCase("None")) {
                                mFirebaseDatabaseOperations.getSpecificGroupsNodeReference(mCurrentUser.getGroupId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                mCurrentGroup = dataSnapshot.getValue(Group.class);
                                                setupViewPager(mMainViewPager);
                                                setUpNavigationMenu();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(Constants.MAIN_ACTIVITY, databaseError.getMessage());
                                            }
                                        });
                                ;
                            } else {
                                mCurrentGroup = null;
                                setupViewPager(mMainViewPager);
                                mAddGroupPostFloatingActionButton.hide();
                                setUpNavigationMenu();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.MAIN_ACTIVITY, databaseError.getMessage());
                    }
                });
        ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();
        setUpCurrentUser();
        setUpSearchView();
        setSupportActionBar(mMainToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mMainToolbar, R.string.nav_drawer_open, R.string.nav_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mAddPostFloatingActionButton.hide();
                mAddGroupPostFloatingActionButton.hide();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mCurrentGroup != null && mMainViewPager.getCurrentItem() != 0) {
                    mAddGroupPostFloatingActionButton.show();
                }else if(mMainViewPager.getCurrentItem() == 0){
                    mAddPostFloatingActionButton.show();
                }
            }
        };
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);
        mMainTabLayout.setupWithViewPager(mMainViewPager);
        mMainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mAddPostFloatingActionButton.show();
                    mAddGroupPostFloatingActionButton.hide();
                } else if(position == 1){
                    if(mCurrentGroup != null){
                        mAddGroupPostFloatingActionButton.show();
                    }
                    mAddPostFloatingActionButton.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAddPostFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(AddPostActivity.newIntent(MainActivity.this, mCurrentUser), 0);
            }
        });

        mAddGroupPostFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(GroupAddPostActivity.newIntent(MainActivity.this, mCurrentGroup, mCurrentUser), 0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setUpNavigationMenu() {
        Menu menu = mNavigationView.getMenu();
        final MenuItem userGroupItem = menu.findItem(R.id.nav_menu_user_group_item);
        MenuItem createGroupItem = menu.findItem(R.id.nav_menu_create_group_item);
        MenuItem joinGroupItem = menu.findItem(R.id.nav_menu_join_group_item);
        if (mCurrentUser != null || mCurrentGroup != null) {
            if (!mCurrentUser.getGroupId().equalsIgnoreCase("None")) {
                userGroupItem.setVisible(true);
                createGroupItem.setVisible(false);
                joinGroupItem.setVisible(false);
                userGroupItem.setTitle(mCurrentGroup.getGroupName());
                Glide.with(mContext.getApplicationContext())
                        .load(mCurrentGroup.getGroupPhotoUri())
                        .asBitmap()
                        .dontAnimate()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                userGroupItem.setIcon(new BitmapDrawable(mContext.getResources(), resource));
                            }
                        })
                ;
            }
        } else {
            userGroupItem.setVisible(false);
            createGroupItem.setVisible(true);
            joinGroupItem.setVisible(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_menu_search_item) {
            mSearchView.open(true, item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_menu_profile_item) {
            startActivity(ProfileActivity.newIntent(MainActivity.this, mCurrentUser));
        } else if (id == R.id.nav_menu_matches_item) {
            startActivity(MatchesActivity.newIntent(MainActivity.this));
        } else if (id == R.id.nav_menu_inbox_item) {
            startActivity(InboxActivity.newIntent(MainActivity.this, mCurrentUser));
        } else if (id == R.id.nav_menu_create_group_item) {
            startActivity(CreateGroupActivity.newIntent(MainActivity.this, mCurrentUser));
        } else if (id == R.id.nav_menu_join_group_item) {
            startActivity(GroupSearchActivity.newIntent(MainActivity.this, mCurrentUser));
        } else if (id == R.id.nav_menu_user_group_item) {
            if (mCurrentGroup != null) {
                startActivity(GroupActivity.newIntent(MainActivity.this, mCurrentGroup, mCurrentUser));
            }
        } else if (id == R.id.nav_menu_about_us_item) {
            startActivity(AboutUsActivity.newIntent(MainActivity.this));
        } else if (id == R.id.nav_menu_logout_item) {
            if (mFirebaseAuthOperations.getUserProviderInfo(mFirebaseAuthOperations.getCurrentUser())
                    .getProviderId().equals("facebook.com")) {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
            } else {
                FirebaseAuth.getInstance().signOut();
            }
            startActivity(LoginActivity.newIntent(MainActivity.this));
            finish();
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());

        MainPostsFragment mainPostsFragment = MainPostsFragment.newInstance(mCurrentUser);
        adapter.addFragment(mainPostsFragment, getString(R.string.main_posts_tab));

        MainGroupPostsFragment mainGroupPostsFragment = MainGroupPostsFragment.newInstance(mCurrentGroup, mCurrentUser);
        adapter.addFragment(mainGroupPostsFragment, getString(R.string.main_group_posts_tab));

        viewPager.setAdapter(adapter);
    }


    private class FragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            try {
                super.finishUpdate(container);
            } catch (NullPointerException nullPointerException) {
                Log.d(Constants.MAIN_ACTIVITY, nullPointerException.getMessage());
            }
        }

    }

    private void setUpSearchView() {
        mSearchView.setVersion(SearchView.VERSION_MENU_ITEM);
        mSearchView.setVoice(false);
        mSearchView.setHint(getString(R.string.search_view_hint));
        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onClose() {
                if (mCurrentGroup != null && mMainViewPager.getCurrentItem() != 0) {
                    mAddGroupPostFloatingActionButton.show();
                }else if(mMainViewPager.getCurrentItem() == 0){
                    mAddPostFloatingActionButton.show();
                }
                return true;
            }

            @Override
            public boolean onOpen() {
                mAddPostFloatingActionButton.hide();
                mAddGroupPostFloatingActionButton.hide();
                return true;
            }
        });
        searchSuggestionsAdapter();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchedItemClick(query);
                mSearchView.close(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void searchSuggestionsAdapter() {
        mSearchSuggestionsHashSet = new HashSet<>();
        mFirebaseDatabaseOperations.getUsersNodeReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                    User user = dataSnap.getValue(User.class);
                    mSearchSuggestionsHashSet.add(user.getUserName());
                }
                ArrayList<SearchItem> searchSuggestionsArrayList = new ArrayList<>();
                for (String name : mSearchSuggestionsHashSet) {
                    searchSuggestionsArrayList.add(new SearchItem(name));
                }
                mSearchSuggestionsAdapter = new SearchAdapter(MainActivity.this, searchSuggestionsArrayList);
                mSearchSuggestionsAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                        onSearchedItemClick(textView.getText().toString());
                        mSearchView.close(false);
                    }
                });
                mSearchView.setAdapter(mSearchSuggestionsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(Constants.MAIN_ACTIVITY, databaseError.getMessage());
            }
        });
    }

    private void onSearchedItemClick(String query) {
        query = query.toLowerCase();
        startActivity(SearchResultsActivity.newIntent(MainActivity.this, query));
    }
}
