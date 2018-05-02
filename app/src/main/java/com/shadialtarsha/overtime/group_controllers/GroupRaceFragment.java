package com.shadialtarsha.overtime.group_controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.User;

public class GroupRaceFragment extends Fragment {

    public static final String CURRENT_USER_BUNDLE = "group_race_fragment_current_user_bundle";
    public static final String CURRENT_GROUP_BUNDLE = "group_race_fragment_current_group_bundle";

    private static User mCurrentUser;
    private static Group mCurrentGroup;

    public static GroupRaceFragment newInstance(Group currentGroup, User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENT_USER_BUNDLE, currentUser);
        bundle.putSerializable(CURRENT_GROUP_BUNDLE, currentGroup);
        GroupRaceFragment groupRaceFragment = new GroupRaceFragment();
        groupRaceFragment.setArguments(bundle);
        return groupRaceFragment;
    }

    public void setUpInformation() {
        Bundle bundle = getArguments();
        mCurrentUser = (User) bundle.getSerializable(CURRENT_USER_BUNDLE);
        mCurrentGroup = (Group) bundle.getSerializable(CURRENT_GROUP_BUNDLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpInformation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_race, container, false);
        return view;
    }
}
