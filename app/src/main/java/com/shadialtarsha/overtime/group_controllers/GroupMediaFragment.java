package com.shadialtarsha.overtime.group_controllers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Group;
import com.shadialtarsha.overtime.model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class GroupMediaFragment extends Fragment {

    public static final String CURRENT_GROUP_BUNDLE = "group_media_fragment_current_group_bundle";

    private RecyclerView mGroupMediaRecyclerView;
    private NestedScrollView mGroupMediaEmptyLayout;
    private RecyclerView.AdapterDataObserver mObserver;
    private FirebaseRecyclerAdapter mGroupMediaAdapter;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private Group mCurrentGroup;

    public static GroupMediaFragment newInstance(Group currentGroup) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENT_GROUP_BUNDLE, currentGroup);
        GroupMediaFragment groupMediaFragment = new GroupMediaFragment();
        groupMediaFragment.setArguments(bundle);
        return groupMediaFragment;
    }

    private void setUpInformation() {
        Bundle bundle = getArguments();
        mCurrentGroup = (Group) bundle.getSerializable(CURRENT_GROUP_BUNDLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpInformation();
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_media, container, false);
        mGroupMediaRecyclerView = (RecyclerView) view.findViewById(R.id.group_media_recycler_view);
        mGroupMediaEmptyLayout = (NestedScrollView) view.findViewById(R.id.group_media_empty_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mGroupMediaRecyclerView.setLayoutManager(layoutManager);
        mGroupMediaRecyclerView.setHasFixedSize(false);
        mGroupMediaRecyclerView.setItemViewCacheSize(20);
        setUpAdapter();
        return view;
    }

    private void setUpAdapter() {
        mGroupMediaAdapter = new FirebaseRecyclerAdapter<Post, MediaHolder>(
                Post.class,
                R.layout.media_card_view,
                MediaHolder.class,
                mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
        ) {
            @Override
            protected void populateViewHolder(MediaHolder viewHolder, Post model, int position) {
                viewHolder.bindMedia(model);
            }
        };

        mFirebaseDatabaseOperations
                .getSpecificPostsNodeReference(mCurrentGroup.getGroupID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mGroupMediaEmptyLayout.setVisibility(View.VISIBLE);
                            mGroupMediaRecyclerView.setVisibility(View.INVISIBLE);
                        } else {
                            for (DataSnapshot posts : dataSnapshot.getChildren()) {
                                Post post = posts.getValue(Post.class);
                                if (post.getPostImageUri() != null) {
                                    mGroupMediaEmptyLayout.setVisibility(View.INVISIBLE);
                                    mGroupMediaRecyclerView.setVisibility(View.VISIBLE);
                                    break;
                                }
                            }
                        }
                        addObserverForRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.MEDIA_FRAGMENT, databaseError.getMessage());
                    }
                })
        ;
        mGroupMediaRecyclerView.setAdapter(mGroupMediaAdapter);
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mGroupMediaAdapter.getItemCount() > 0) {
                    mGroupMediaRecyclerView.setVisibility(View.VISIBLE);
                    mGroupMediaEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mGroupMediaAdapter.getItemCount() == 0) {
                    mGroupMediaRecyclerView.setVisibility(View.INVISIBLE);
                    mGroupMediaEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mGroupMediaAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class MediaHolder extends RecyclerView.ViewHolder {

        private ImageView mMediaImageView;

        public MediaHolder(View itemView) {
            super(itemView);
            mMediaImageView = (ImageView) itemView.findViewById(R.id.media_image_view);
        }

        public void bindMedia(Post post) {
            if (post.getPostImageUri() != null)
                Picasso.with(mMediaImageView.getContext())
                        .load(post.getPostImageUri())
                        .placeholder(R.drawable.image_placeholder)
                        .into(mMediaImageView);
        }
    }
}
