package com.shadialtarsha.overtime.main_screen_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Follower;
import com.shadialtarsha.overtime.model.User;
import com.shadialtarsha.overtime.profile_controllers.FollowOperationControl;
import com.shadialtarsha.overtime.profile_controllers.ProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UsersAdapter {

    private RecyclerView mUsersRecyclerView;
    private NestedScrollView mUsersEmptyLayout;
    private RecyclerView.AdapterDataObserver mObserver;
    private Query mQuery;

    private FirebaseRecyclerAdapter mUsersFirebaseRecyclerAdapter;
    private static FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private static FirebaseAuthOperations mFirebaseAuthOperations;
    private static FollowOperationControl mFollowOperationControl;

    public UsersAdapter(Query query) {
        mFirebaseAuthOperations = new FirebaseAuthOperations();
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mFollowOperationControl = new FollowOperationControl();
        mQuery = query;
    }

    public void setUpSearchAdapter(RecyclerView recyclerView, NestedScrollView emptyLayout) {
        mUsersRecyclerView = recyclerView;
        mUsersEmptyLayout = (NestedScrollView) emptyLayout.getRootView()
                .findViewById(R.id.search_results_empty_layout);
        mUsersFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersHolder>(
                User.class,
                R.layout.users_list_item,
                UsersHolder.class,
                mQuery
        ) {
            @Override
            protected void populateViewHolder(UsersHolder viewHolder, User model, int position) {
                viewHolder.bindUser(model);
            }
        };

        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    mUsersEmptyLayout.setVisibility(View.VISIBLE);
                    mUsersRecyclerView.setVisibility(View.INVISIBLE);
                } else {
                    mUsersEmptyLayout.setVisibility(View.INVISIBLE);
                    mUsersRecyclerView.setVisibility(View.VISIBLE);
                }
                addObserverForRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(Constants.USERS_ADAPTER, databaseError.getMessage());
            }
        });

        mUsersRecyclerView.setAdapter(mUsersFirebaseRecyclerAdapter);
    }

    public void setUpUsersAdapter(RecyclerView recyclerView, NestedScrollView emptyLayout) {
        mUsersRecyclerView = recyclerView;
        mUsersEmptyLayout = (NestedScrollView) emptyLayout.getRootView()
                .findViewById(R.id.follow_empty_layout);
        mUsersFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Follower, UsersHolder>(
                Follower.class,
                R.layout.users_list_item,
                UsersHolder.class,
                mQuery
        ) {
            @Override
            protected void populateViewHolder(final UsersHolder viewHolder, Follower model, int position) {
                mFirebaseDatabaseOperations.getSpecificUserNodeReference(model.getUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.bindUser(dataSnapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.USERS_ADAPTER, databaseError.getMessage());
                    }
                });
            }
        };

        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    mUsersEmptyLayout.setVisibility(View.VISIBLE);
                    mUsersRecyclerView.setVisibility(View.INVISIBLE);
                } else {
                    mUsersEmptyLayout.setVisibility(View.INVISIBLE);
                    mUsersRecyclerView.setVisibility(View.VISIBLE);
                }
                addObserverForRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(Constants.USERS_ADAPTER, databaseError.getMessage());
            }
        });

        mUsersRecyclerView.setAdapter(mUsersFirebaseRecyclerAdapter);
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mUsersFirebaseRecyclerAdapter.getItemCount() > 0) {
                    mUsersRecyclerView.setVisibility(View.VISIBLE);
                    mUsersEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mUsersFirebaseRecyclerAdapter.getItemCount() == 0) {
                    mUsersRecyclerView.setVisibility(View.INVISIBLE);
                    mUsersEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mUsersFirebaseRecyclerAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class UsersHolder extends RecyclerView.ViewHolder {

        private ImageView mUserProfilePhoto;
        private TextView mUsernameTextView;
        private ImageButton mFollowImageButton;
        private User mUser;
        private Context mContext;

        public UsersHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
            mContext = itemView.getContext();
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(ProfileActivity.newIntent(mContext, mUser));
                }
            });
        }

        private void setUpViews(View itemView) {
            mUserProfilePhoto = (ImageView) itemView.findViewById(R.id.users_user_profile_photo);
            mUsernameTextView = (TextView) itemView.findViewById(R.id.users_username_text_view);
            mFollowImageButton = (ImageButton) itemView.findViewById(R.id.users_follow_image_button);
        }

        private void bindUser(User user) {
            mUser = user;
            Glide.with(mUserProfilePhoto.getContext().getApplicationContext())
                    .load(mUser.getUserProfilePhotoUri())
                    .placeholder(R.drawable.user_profile_photo_placeholder)
                    .dontAnimate()
                    .into(mUserProfilePhoto)
            ;
            mUsernameTextView.setText(mUser.getUserName());
            if (mUser.getUserId().equals(mFirebaseAuthOperations.getCurrentUserId())) {
                mFollowImageButton.setVisibility(View.GONE);
            } else {
                mFirebaseDatabaseOperations
                        .getSpecificFollowingNodeReference(mFirebaseAuthOperations.getCurrentUserId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                mFollowImageButton.setVisibility(View.VISIBLE);
                                if (dataSnapshot.hasChild(mUser.getUserId())) {
                                    mFollowImageButton.setImageResource(R.drawable.ic_followed);
                                } else {
                                    mFollowImageButton.setImageResource(R.drawable.ic_follow);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(Constants.USERS_ADAPTER, databaseError.getMessage());
                            }
                        })
                ;

                mFollowImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFirebaseDatabaseOperations.getSpecificFollowingNodeReference(mFirebaseAuthOperations.getCurrentUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.hasChild(mUser.getUserId())) {
                                            new AlertDialog.Builder(mContext)
                                                    .setMessage("follow " + mUser.getUserName() + "?")
                                                    .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            mFollowOperationControl.followOperation(mUser);
                                                        }
                                                    })
                                                    .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                                                    .show()
                                            ;
                                        } else {
                                            new AlertDialog.Builder(mContext)
                                                    .setMessage("UnFollow " + mUser.getUserName() + "?")
                                                    .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            mFollowOperationControl.unFollowOperation(mUser);
                                                        }
                                                    })
                                                    .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                                                    .show()
                                            ;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(Constants.USERS_ADAPTER, databaseError.getMessage());
                                    }
                                })
                        ;
                    }
                });
            }
        }

    }
}
