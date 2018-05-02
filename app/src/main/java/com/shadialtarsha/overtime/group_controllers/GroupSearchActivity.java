package com.shadialtarsha.overtime.group_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.GroupMember;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class GroupSearchActivity extends AppCompatActivity {

    private static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.group_controllers.GroupSearchActivity.current_user";

    private Toolbar mGroupSearchToolbar;
    private RecyclerView mGroupSearchRecyclerView;
    private LinearLayoutManager mGroupSearchLinearLayoutManager;
    private NestedScrollView mGroupSearchEmptyLayout;
    private FirebaseRecyclerAdapter mGroupSearchAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    private static FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private static User mCurrentUser;

    public static Intent newIntent(Context packageContext, User currentUser) {
        Intent intent = new Intent(packageContext, GroupSearchActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search);
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        mGroupSearchToolbar = (Toolbar) findViewById(R.id.group_search_toolbar);
        mGroupSearchToolbar.setTitle(getString(R.string.group_search_activity));
        setSupportActionBar(mGroupSearchToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        mGroupSearchRecyclerView = (RecyclerView) findViewById(R.id.group_search_recycler_view);
        mGroupSearchEmptyLayout = (NestedScrollView) findViewById(R.id.group_search_empty_layout);
        mGroupSearchLinearLayoutManager = new LinearLayoutManager(this);
        mGroupSearchRecyclerView.setLayoutManager(mGroupSearchLinearLayoutManager);
        mGroupSearchRecyclerView.setHasFixedSize(false);
        mGroupSearchRecyclerView.setItemViewCacheSize(20);
        mGroupSearchRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mGroupSearchRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        setUpGroupSearchAdapter(null);
    }

    private void setUpGroupSearchAdapter(String query) {
        if (query == null) {
            mGroupSearchAdapter = new FirebaseRecyclerAdapter<Group, GroupHolder>(
                    Group.class,
                    R.layout.group_search_list_item,
                    GroupHolder.class,
                    mFirebaseDatabaseOperations.getGroupsNodeReference()
            ) {
                @Override
                protected void populateViewHolder(GroupHolder viewHolder, Group model, int position) {
                    viewHolder.bindGroup(model);
                }
            };
            mFirebaseDatabaseOperations
                    .getGroupsNodeReference()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                mGroupSearchEmptyLayout.setVisibility(View.VISIBLE);
                                mGroupSearchRecyclerView.setVisibility(View.INVISIBLE);
                            } else {
                                mGroupSearchEmptyLayout.setVisibility(View.INVISIBLE);
                                mGroupSearchRecyclerView.setVisibility(View.VISIBLE);
                            }
                            addObserverForRecyclerView();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_SEARCH_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
        } else {
            mGroupSearchAdapter = new FirebaseRecyclerAdapter<Group, GroupHolder>(
                    Group.class,
                    R.layout.group_search_list_item,
                    GroupHolder.class,
                    mFirebaseDatabaseOperations
                            .getGroupsNodeReference()
                            .orderByChild("groupNameLowerCase")
                            .startAt(query)
                            .endAt(query + "\uf8ff")
            ) {
                @Override
                protected void populateViewHolder(GroupHolder viewHolder, Group model, int position) {
                    viewHolder.bindGroup(model);
                }
            };
            mFirebaseDatabaseOperations
                    .getGroupsNodeReference()
                    .orderByChild("groupNameLowerCase")
                    .startAt(query)
                    .endAt(query + "\uf8ff")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                mGroupSearchEmptyLayout.setVisibility(View.VISIBLE);
                                mGroupSearchRecyclerView.setVisibility(View.INVISIBLE);
                            } else {
                                mGroupSearchEmptyLayout.setVisibility(View.INVISIBLE);
                                mGroupSearchRecyclerView.setVisibility(View.VISIBLE);
                            }
                            addObserverForRecyclerView();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_SEARCH_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
        }
        mGroupSearchRecyclerView.setAdapter(mGroupSearchAdapter);
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mGroupSearchAdapter.getItemCount() > 0) {
                    mGroupSearchRecyclerView.setVisibility(View.VISIBLE);
                    mGroupSearchEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mGroupSearchAdapter.getItemCount() == 0) {
                    mGroupSearchRecyclerView.setVisibility(View.INVISIBLE);
                    mGroupSearchEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mGroupSearchAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class GroupHolder extends RecyclerView.ViewHolder {

        private ImageView mGroupPhotoImageView;
        private TextView mGroupNameTextView;
        private TextView mGroupBioTextView;
        private TextView mGroupPostsCounterTextView;
        private TextView mGroupMembersCounterTextView;
        private TextView mGroupLikesCounterTextView;
        private TextView mGroupJoinTextView;
        private Context mContext;
        private Group mGroup;

        private void setUpViews(View itemView) {
            mGroupPhotoImageView = (ImageView) itemView.findViewById(R.id.search_group_photo_image_view);
            mGroupNameTextView = (TextView) itemView.findViewById(R.id.search_group_name_text_view);
            mGroupBioTextView = (TextView) itemView.findViewById(R.id.search_group_bio_text_view);
            mGroupPostsCounterTextView = (TextView) itemView.findViewById(R.id.search_group_posts_counter_text_view);
            mGroupMembersCounterTextView = (TextView) itemView.findViewById(R.id.search_group_members_counter_text_view);
            mGroupLikesCounterTextView = (TextView) itemView.findViewById(R.id.search_group_likes_counter_text_view);
            mGroupJoinTextView = (TextView) itemView.findViewById(R.id.search_group_join_text_view);
            mContext = itemView.getContext();
        }

        public GroupHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
        }

        public void bindGroup(Group group) {
            mGroup = group;
            Glide.with(mContext)
                    .load(mGroup.getGroupPhotoUri())
                    .placeholder(R.drawable.group_photo_placeholder)
                    .dontAnimate()
                    .into(mGroupPhotoImageView)
            ;
            mGroupNameTextView.setText(mGroup.getGroupName());
            mGroupBioTextView.setText(mGroup.getGroupBio());

            mFirebaseDatabaseOperations
                    .getSpecificPostsNodeReference(mGroup.getGroupID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                mGroupPostsCounterTextView.setText("0");
                            } else {
                                mGroupPostsCounterTextView.setText(Long.toString(dataSnapshot.getChildrenCount()));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_SEARCH_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
            mFirebaseDatabaseOperations.getSpecificGroupMembersNodeReference(mGroup.getGroupID())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                mGroupMembersCounterTextView.setText(Integer.toString(0));
                            } else {
                                mGroupMembersCounterTextView.setText(Long.toString(dataSnapshot.getChildrenCount()));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_SEARCH_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
            mFirebaseDatabaseOperations
                    .getSpecificPostsNodeReference(mGroup.getGroupID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                mGroupLikesCounterTextView.setText(Integer.toString(0));
                            } else {
                                long counter = 0;
                                for (DataSnapshot posts : dataSnapshot.getChildren()) {
                                    Post post = posts.getValue(Post.class);
                                    counter += post.getPostLikesCounter();
                                }
                                mGroupLikesCounterTextView.setText(Long.toString(counter));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_SEARCH_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;

            mGroupJoinTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mContext)
                            .setMessage("Are you want to join " + mGroup.getGroupName())
                            .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mCurrentUser.setGroupId(mGroup.getGroupID());
                                    mFirebaseDatabaseOperations
                                            .getSpecificUserNodeReference(mCurrentUser.getUserId())
                                            .setValue(mCurrentUser)
                                    ;

                                    mFirebaseDatabaseOperations
                                            .getSpecificGroupsNodeReference(mGroup.getGroupID())
                                            .setValue(mGroup)
                                    ;

                                    GroupMember member = new GroupMember();
                                    member.setUserId(mCurrentUser.getUserId());
                                    member.setUserRank(mContext.getString(R.string.group_member_user_rank));
                                    mFirebaseDatabaseOperations
                                            .getSpecificGroupMembersNodeReference(mGroup.getGroupID())
                                            .child(member.getUserId())
                                            .setValue(member)
                                    ;

                                    AppCompatActivity appCompatActivity = (AppCompatActivity) mContext;
                                    mContext.startActivity(GroupActivity.newIntent(appCompatActivity, mGroup, mCurrentUser));
                                    appCompatActivity.finish();
                                }
                            })
                            .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                            .show()
                    ;
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_search, menu);
        MenuItem searchItem = menu.findItem(R.id.group_search_menu_search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    setUpGroupSearchAdapter(newText.toLowerCase());
                    return true;
                } else {
                    setUpGroupSearchAdapter(null);
                    return false;
                }
            }
        });
        return true;
    }
}
