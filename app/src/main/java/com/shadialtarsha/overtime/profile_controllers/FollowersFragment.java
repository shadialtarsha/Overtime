package com.shadialtarsha.overtime.profile_controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.main_screen_controllers.UsersAdapter;
import com.shadialtarsha.overtime.model.User;


public class FollowersFragment extends Fragment {

    public static final String CURRENT_USER_BUNDLE = "followers_fragment_current_user_bundle";

    private RecyclerView mFollowersRecyclerView;
    private NestedScrollView mFollowersEmptyLayout;
    private LinearLayoutManager mFollowersLinearLayoutManager;
    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private User mCurrentUser;
    private UsersAdapter mUsersAdapter;

    public static FollowersFragment newInstance(User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENT_USER_BUNDLE, currentUser);
        FollowersFragment followersFragment = new FollowersFragment();
        followersFragment.setArguments(bundle);
        return followersFragment;
    }

    private void setUpUserInformation() {
        Bundle bundle = getArguments();
        mCurrentUser = (User) bundle.getSerializable(CURRENT_USER_BUNDLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpUserInformation();
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        mUsersAdapter = new UsersAdapter(mFirebaseDatabaseOperations.getSpecificFollowersNodeReference(mCurrentUser.getUserId()));
    }

    private void setUpFollowersRecyclerView() {
        mFollowersLinearLayoutManager = new LinearLayoutManager(getActivity());
        mFollowersRecyclerView.setLayoutManager(mFollowersLinearLayoutManager);
        mFollowersRecyclerView.setHasFixedSize(false);
        mFollowersRecyclerView.setItemViewCacheSize(20);
        mFollowersRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mFollowersRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), mFollowersLinearLayoutManager.getOrientation());
        mFollowersRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        mFollowersRecyclerView = (RecyclerView) view.findViewById(R.id.followers_recycler_view);
        mFollowersEmptyLayout = (NestedScrollView) view.findViewById(R.id.follow_empty_layout);
        setUpFollowersRecyclerView();
        mUsersAdapter.setUpUsersAdapter(mFollowersRecyclerView, mFollowersEmptyLayout);
        return view;
    }
}
