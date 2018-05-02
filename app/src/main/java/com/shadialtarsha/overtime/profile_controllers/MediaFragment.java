package com.shadialtarsha.overtime.profile_controllers;

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
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MediaFragment extends Fragment {

    public static final String CURRENT_USER_BUNDLE = "media_fragment_current_user_bundle";

    private RecyclerView mMediaRecyclerView;
    private NestedScrollView mMediaEmptyLayout;
    private RecyclerView.AdapterDataObserver mObserver;
    private FirebaseRecyclerAdapter mMediaAdapter;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;


    private User mCurrentUser;

    public static MediaFragment newInstance(User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENT_USER_BUNDLE, currentUser);
        MediaFragment mediaFragment = new MediaFragment();
        mediaFragment.setArguments(bundle);
        return mediaFragment;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container, false);
        mMediaRecyclerView = (RecyclerView) view.findViewById(R.id.media_recycler_view);
        mMediaEmptyLayout = (NestedScrollView) view.findViewById(R.id.media_empty_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mMediaRecyclerView.setLayoutManager(layoutManager);
        mMediaRecyclerView.setHasFixedSize(false);
        mMediaRecyclerView.setItemViewCacheSize(20);
        setUpAdapter();
        mMediaRecyclerView.setAdapter(mMediaAdapter);
        return view;
    }

    private void setUpAdapter() {
        mMediaAdapter = new FirebaseRecyclerAdapter<Post, MediaFragment.MediaHolder>(
                Post.class,
                R.layout.media_card_view,
                MediaFragment.MediaHolder.class,
                mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
        ) {
            @Override
            protected void populateViewHolder(MediaHolder viewHolder, Post model, int position) {
                viewHolder.bindMedia(model);
            }
        };

        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mMediaEmptyLayout.setVisibility(View.VISIBLE);
                            mMediaRecyclerView.setVisibility(View.INVISIBLE);
                        } else {
                            for (DataSnapshot posts : dataSnapshot.getChildren()) {
                                Post post = posts.getValue(Post.class);
                                if (post.getPostImageUri() != null) {
                                    mMediaEmptyLayout.setVisibility(View.INVISIBLE);
                                    mMediaRecyclerView.setVisibility(View.VISIBLE);
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
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mMediaAdapter.getItemCount() > 0) {
                    mMediaRecyclerView.setVisibility(View.VISIBLE);
                    mMediaEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mMediaAdapter.getItemCount() == 0) {
                    mMediaRecyclerView.setVisibility(View.INVISIBLE);
                    mMediaEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mMediaAdapter.registerAdapterDataObserver(mObserver);
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
                        .into(mMediaImageView)
                        ;
        }
    }
}
