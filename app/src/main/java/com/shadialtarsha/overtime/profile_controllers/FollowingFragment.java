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

public class FollowingFragment extends Fragment {

    public static final String CURRENT_USER_BUNDLE = "following_fragment_current_user_bundle";

    private RecyclerView mFollowingRecyclerView;
    private NestedScrollView mFollowingEmptyLayout;
    private LinearLayoutManager mFollowingLinearLayoutManager;
    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private User mCurrentUser;
    private UsersAdapter mUsersAdapter;

    public static FollowingFragment newInstance(User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENT_USER_BUNDLE, currentUser);
        FollowingFragment followingFragment = new FollowingFragment();
        followingFragment.setArguments(bundle);
        return followingFragment;
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
        mUsersAdapter = new UsersAdapter(mFirebaseDatabaseOperations.getSpecificFollowingNodeReference(mCurrentUser.getUserId()));
    }

    private void setUpFollowingRecyclerView() {
        mFollowingLinearLayoutManager = new LinearLayoutManager(getActivity());
        mFollowingRecyclerView.setLayoutManager(mFollowingLinearLayoutManager);
        mFollowingRecyclerView.setHasFixedSize(false);
        mFollowingRecyclerView.setItemViewCacheSize(20);
        mFollowingRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mFollowingRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), mFollowingLinearLayoutManager.getOrientation());
        mFollowingRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        mFollowingRecyclerView = (RecyclerView) view.findViewById(R.id.following_recycler_view);
        mFollowingEmptyLayout = (NestedScrollView) view.findViewById(R.id.follow_empty_layout);
        setUpFollowingRecyclerView();
        mUsersAdapter.setUpUsersAdapter(mFollowingRecyclerView, mFollowingEmptyLayout);
        return view;
    }
}
