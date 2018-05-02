package com.shadialtarsha.overtime.inbox_controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Conversation;
import com.shadialtarsha.overtime.model.Following;
import com.shadialtarsha.overtime.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class NewMessageActivity extends AppCompatActivity {

    private static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.inbox_controllers.NewMessageActivity.current_user";

    private Toolbar mNewMessageToolbar;
    private RecyclerView mNewMessageRecyclerView;
    private NestedScrollView mNewMessageEmptyLayout;
    private RecyclerView.AdapterDataObserver mObserver;
    private LinearLayoutManager mNewMessageLinearLayoutManager;
    private FirebaseRecyclerAdapter mUsersFirebaseRecyclerAdapter;
    private static FirebaseDatabaseOperations mFirebaseDatabaseOperations;

    private static User mCurrentUser;

    public static Intent newIntent(Context packageContext, User currentUser) {
        Intent intent = new Intent(packageContext, NewMessageActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        mNewMessageToolbar = (Toolbar) findViewById(R.id.new_message_toolbar);
        mNewMessageToolbar.setTitle(getString(R.string.new_message_activity));
        setSupportActionBar(mNewMessageToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mNewMessageRecyclerView = (RecyclerView) findViewById(R.id.new_message_recycler_view);
        mNewMessageEmptyLayout = (NestedScrollView) findViewById(R.id.new_message_empty_layout);
        setUpNewMessageRecyclerView();
    }

    private void setUpNewMessageRecyclerView() {
        mNewMessageLinearLayoutManager = new LinearLayoutManager(this);
        mNewMessageRecyclerView.setLayoutManager(mNewMessageLinearLayoutManager);
        mNewMessageRecyclerView.setHasFixedSize(false);
        mNewMessageRecyclerView.setItemViewCacheSize(20);
        mNewMessageRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mNewMessageRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, mNewMessageLinearLayoutManager.getOrientation());
        mNewMessageRecyclerView.addItemDecoration(dividerItemDecoration);
        setUpUsersAdapter();
    }

    public void setUpUsersAdapter() {
        mUsersFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Following, UsersHolder>(
                Following.class,
                R.layout.new_message_list_item,
                UsersHolder.class,
                mFirebaseDatabaseOperations.getSpecificFollowingNodeReference(mCurrentUser.getUserId())
        ) {
            @Override
            protected void populateViewHolder(UsersHolder viewHolder, Following model, int position) {
                viewHolder.bindUser(model);
            }
        };

        mFirebaseDatabaseOperations.getSpecificFollowingNodeReference(mCurrentUser.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mNewMessageEmptyLayout.setVisibility(View.VISIBLE);
                            mNewMessageRecyclerView.setVisibility(View.INVISIBLE);
                        } else {
                            mNewMessageEmptyLayout.setVisibility(View.INVISIBLE);
                            mNewMessageRecyclerView.setVisibility(View.VISIBLE);
                        }
                        addObserverForRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.NEW_MESSAGE_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mNewMessageRecyclerView.setAdapter(mUsersFirebaseRecyclerAdapter);
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mUsersFirebaseRecyclerAdapter.getItemCount() > 0) {
                    mNewMessageRecyclerView.setVisibility(View.VISIBLE);
                    mNewMessageEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mUsersFirebaseRecyclerAdapter.getItemCount() == 0) {
                    mNewMessageRecyclerView.setVisibility(View.INVISIBLE);
                    mNewMessageEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mUsersFirebaseRecyclerAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class UsersHolder extends RecyclerView.ViewHolder {

        private ImageView mUserProfilePhoto;
        private TextView mUsernameTextView;
        private Following mUser;
        private Context mContext;

        public UsersHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
            mContext = itemView.getContext();
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFirebaseDatabaseOperations.getSpecificUserNodeReference(mUser.getUserId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    final User secondPartner = dataSnapshot.getValue(User.class);
                                    mFirebaseDatabaseOperations.getSpecificInboxNodeReference(mCurrentUser.getUserId())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    boolean isFirstHasConversation = false;
                                                    for (DataSnapshot conversations : dataSnapshot.getChildren()) {
                                                        Conversation conversation = conversations.getValue(Conversation.class);
                                                        if (conversation.getFirstPartnerId().equals(mCurrentUser.getUserId())
                                                                && conversation.getSecondPartnerId().equals(secondPartner.getUserId())) {
                                                            mContext.startActivity(ConversationActivity.newIntent(mContext, conversation.getConversationId(), mCurrentUser, secondPartner));
                                                            AppCompatActivity newMessageActivity = (AppCompatActivity) mContext;
                                                            newMessageActivity.finish();
                                                            isFirstHasConversation = true;
                                                            break;
                                                        }
                                                    }
                                                    if (!isFirstHasConversation) {
                                                        mFirebaseDatabaseOperations.getSpecificInboxNodeReference(secondPartner.getUserId())
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        boolean isSecondHasConversation = false;
                                                                        for (DataSnapshot secondPartnerConversations : dataSnapshot.getChildren()) {
                                                                            Conversation secondPartnerConversation = secondPartnerConversations.getValue(Conversation.class);
                                                                            if (secondPartnerConversation.getFirstPartnerId().equals(secondPartner.getUserId())
                                                                                    && secondPartnerConversation.getSecondPartnerId().equals(mCurrentUser.getUserId())) {
                                                                                mContext.startActivity(ConversationActivity.newIntent(mContext, secondPartnerConversation.getConversationId(), mCurrentUser, secondPartner));
                                                                                AppCompatActivity newMessageActivity = (AppCompatActivity) mContext;
                                                                                newMessageActivity.finish();
                                                                                secondPartnerConversation.setFirstPartnerId(mCurrentUser.getUserId());
                                                                                secondPartnerConversation.setSecondPartnerId(secondPartner.getUserId());
                                                                                mFirebaseDatabaseOperations
                                                                                        .getSpecificInboxNodeReference(mCurrentUser.getUserId())
                                                                                        .child(secondPartnerConversation.getConversationId())
                                                                                        .setValue(secondPartnerConversation)
                                                                                ;
                                                                                isSecondHasConversation = true;
                                                                                break;
                                                                            }
                                                                        }
                                                                        if (!isSecondHasConversation) {
                                                                            mContext.startActivity(ConversationActivity.newIntent(mContext, null, mCurrentUser, secondPartner));
                                                                            AppCompatActivity newMessageActivity = (AppCompatActivity) mContext;
                                                                            newMessageActivity.finish();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {
                                                                        Log.d(Constants.NEW_MESSAGE_ACTIVITY, databaseError.getMessage());
                                                                    }
                                                                })
                                                        ;
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Log.d(Constants.NEW_MESSAGE_ACTIVITY, databaseError.getMessage());
                                                }
                                            })
                                    ;
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(Constants.NEW_MESSAGE_ACTIVITY, databaseError.getMessage());
                                }
                            })
                    ;
                }
            });
        }

        private void setUpViews(View itemView) {
            mUserProfilePhoto = (ImageView) itemView.findViewById(R.id.new_message_user_profile_photo);
            mUsernameTextView = (TextView) itemView.findViewById(R.id.new_message_username_text_view);
        }

        private void bindUser(Following user) {
            mUser = user;
            mFirebaseDatabaseOperations
                    .getSpecificUserNodeReference(mUser.getUserId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User userInfo = dataSnapshot.getValue(User.class);
                            Glide.with(mContext.getApplicationContext())
                                    .load(userInfo.getUserProfilePhotoUri())
                                    .placeholder(R.drawable.user_profile_photo_placeholder)
                                    .dontAnimate()
                                    .into(mUserProfilePhoto)
                            ;
                            mUsernameTextView.setText(userInfo.getUserName());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.NEW_MESSAGE_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
