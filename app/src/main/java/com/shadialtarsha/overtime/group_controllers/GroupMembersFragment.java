package com.shadialtarsha.overtime.group_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.GroupMember;
import com.shadialtarsha.overtime.model.User;
import com.shadialtarsha.overtime.profile_controllers.ProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class GroupMembersFragment extends Fragment {

    public static final String CURRENT_USER_BUNDLE = "group_members_fragment_current_user_bundle";
    public static final String CURRENT_GROUP_BUNDLE = "group_members_fragment_current_group_bundle";

    private RecyclerView mGroupMembersRecyclerView;
    private FirebaseRecyclerAdapter mGroupMembersAdapter;

    private static FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private static User mCurrentUser;
    private static Group mCurrentGroup;

    public static GroupMembersFragment newInstance(Group currentGroup, User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENT_USER_BUNDLE, currentUser);
        bundle.putSerializable(CURRENT_GROUP_BUNDLE, currentGroup);
        GroupMembersFragment groupMembersFragment = new GroupMembersFragment();
        groupMembersFragment.setArguments(bundle);
        return groupMembersFragment;
    }

    public void setUpInformation() {
        Bundle bundle = getArguments();
        mCurrentUser = (User) bundle.getSerializable(CURRENT_USER_BUNDLE);
        mCurrentGroup = (Group) bundle.getSerializable(CURRENT_GROUP_BUNDLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        setUpInformation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_members, container, false);
        mGroupMembersRecyclerView = (RecyclerView) view.findViewById(R.id.group_members_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mGroupMembersRecyclerView.setLayoutManager(layoutManager);
        mGroupMembersRecyclerView.setHasFixedSize(false);
        mGroupMembersRecyclerView.setItemViewCacheSize(20);
        mGroupMembersRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mGroupMembersRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        setUpAdapter();
        return view;
    }


    private void setUpAdapter() {
        mGroupMembersAdapter = new FirebaseRecyclerAdapter<GroupMember, MemberHolder>(
                GroupMember.class,
                R.layout.group_members_list_item,
                MemberHolder.class,
                mFirebaseDatabaseOperations.getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
        ) {
            @Override
            protected void populateViewHolder(MemberHolder viewHolder, GroupMember model, int position) {
                viewHolder.bindMember(model);
            }
        };
        mGroupMembersRecyclerView.setAdapter(mGroupMembersAdapter);
    }

    public static class MemberHolder extends RecyclerView.ViewHolder {

        private ImageView mUserProfilePhoto;
        private TextView mUsernameTextView;
        private TextView mUserRankTextView;
        private AlertDialog.Builder mAlertDialogBuilder;
        private GroupMember mGroupMember;
        private User mUser;
        private Context mContext;

        public MemberHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
            mContext = itemView.getContext();
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(ProfileActivity.newIntent(mContext, mUser));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showManagementOptions();
                    return true;
                }
            });
        }

        private void setUpViews(View itemView) {
            mUserProfilePhoto = (ImageView) itemView.findViewById(R.id.group_members_user_profile_photo);
            mUsernameTextView = (TextView) itemView.findViewById(R.id.group_members_username_text_view);
            mUserRankTextView = (TextView) itemView.findViewById(R.id.group_members_users_rank_text_view);
        }

        private void bindMember(GroupMember groupMember) {
            mGroupMember = groupMember;
            mFirebaseDatabaseOperations
                    .getSpecificUserNodeReference(mGroupMember.getUserId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            mUser = user;
                            Glide.with(mContext)
                                    .load(mUser.getUserProfilePhotoUri())
                                    .placeholder(R.drawable.user_profile_photo_placeholder)
                                    .dontAnimate()
                                    .into(mUserProfilePhoto)
                            ;
                            mUsernameTextView.setText(mUser.getUserName());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_MEMBERS_FRAGMENT, databaseError.getMessage());
                        }
                    })
            ;
            mUserRankTextView.setText(mGroupMember.getUserRank());
        }

        private void showManagementOptions() {
            mFirebaseDatabaseOperations
                    .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                    .child(mCurrentUser.getUserId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GroupMember member = dataSnapshot.getValue(GroupMember.class);
                            if (member.getUserRank().equals(mContext.getString(R.string.group_admin_user_rank))) {
                                if (mGroupMember.getUserRank().equals(mContext.getString(R.string.group_member_user_rank))) {
                                    mAlertDialogBuilder = new AlertDialog.Builder(mContext);
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.post_options_menu_item);
                                    adapter.add("Promote");
                                    adapter.add("Delete");
                                    mAlertDialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                new AlertDialog.Builder(mContext)
                                                        .setMessage("Are you want to promote " + mUser.getUserName() + " to co-Admin?")
                                                        .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                mGroupMember.setUserRank(mContext.getString(R.string.group_co_admin_user_rank));
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                                                                        .child(mGroupMember.getUserId())
                                                                        .setValue(mGroupMember)
                                                                ;
                                                                mUserRankTextView.setText(mGroupMember.getUserRank());
                                                            }
                                                        })
                                                        .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                                                        .show()
                                                ;
                                            } else if (which == 1) {
                                                new AlertDialog.Builder(mContext)
                                                        .setMessage("Are you want to delete " + mUser.getUserName() + " ?")
                                                        .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                                                                        .child(mGroupMember.getUserId())
                                                                        .removeValue()
                                                                ;
                                                                mUser.setGroupId("None");
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificUserNodeReference(mUser.getUserId())
                                                                        .setValue(mUser)
                                                                ;
                                                            }
                                                        })
                                                        .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                                                        .show()
                                                ;
                                            }
                                        }
                                    })
                                            .show()
                                    ;
                                } else if (mGroupMember.getUserRank().equals(mContext.getString(R.string.group_co_admin_user_rank))) {
                                    mAlertDialogBuilder = new AlertDialog.Builder(mContext);
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.post_options_menu_item);
                                    adapter.add("Demote");
                                    adapter.add("Delete");
                                    mAlertDialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                new AlertDialog.Builder(mContext)
                                                        .setMessage("Are you want to demote " + mUser.getUserName() + " to member?")
                                                        .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                mGroupMember.setUserRank(mContext.getString(R.string.group_member_user_rank));
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                                                                        .child(mGroupMember.getUserId())
                                                                        .setValue(mGroupMember)
                                                                ;
                                                                mUserRankTextView.setText(mGroupMember.getUserRank());
                                                            }
                                                        })
                                                        .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                                                        .show()
                                                ;
                                            } else if (which == 1) {
                                                new AlertDialog.Builder(mContext)
                                                        .setMessage("Are you want to delete " + mUser.getUserName() + " ?")
                                                        .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                                                                        .child(mGroupMember.getUserId())
                                                                        .removeValue()
                                                                ;
                                                                mUser.setGroupId("None");
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificUserNodeReference(mUser.getUserId())
                                                                        .setValue(mUser)
                                                                ;
                                                            }
                                                        })
                                                        .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                                                        .show()
                                                ;
                                            }
                                        }
                                    })
                                            .show()
                                    ;
                                }
                            } else if (member.getUserRank().equals(mContext.getString(R.string.group_co_admin_user_rank))) {
                                if (mGroupMember.getUserRank().equals(mContext.getString(R.string.group_member_user_rank))) {
                                    mAlertDialogBuilder = new AlertDialog.Builder(mContext);
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.post_options_menu_item);
                                    adapter.add("Delete");
                                    mAlertDialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                new AlertDialog.Builder(mContext)
                                                        .setMessage("Are you want to delete " + mUser.getUserName() + " ?")
                                                        .setPositiveButton(mContext.getString(R.string.alert_dialog_yes_button), new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificGroupMembersNodeReference(mCurrentGroup.getGroupID())
                                                                        .child(mGroupMember.getUserId())
                                                                        .removeValue()
                                                                ;
                                                                mUser.setGroupId("None");
                                                                mFirebaseDatabaseOperations
                                                                        .getSpecificUserNodeReference(mUser.getUserId())
                                                                        .setValue(mUser)
                                                                ;
                                                            }
                                                        })
                                                        .setNegativeButton(mContext.getString(R.string.alert_dialog_cancel_button), null)
                                                        .show()
                                                ;
                                            }
                                        }
                                    })
                                            .show()
                                    ;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.GROUP_MEMBERS_FRAGMENT, databaseError.getMessage());
                        }
                    })
            ;
        }
    }
}
