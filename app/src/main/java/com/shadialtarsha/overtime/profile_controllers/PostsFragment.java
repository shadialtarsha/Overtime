package com.shadialtarsha.overtime.profile_controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseAuthOperations;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.Calendar;

public class PostsFragment extends Fragment {

    public static final String CURRENT_USER_BUNDLE = "posts_fragment_current_user_bundle";

    private RecyclerView mPostsRecyclerView;
    private RecyclerView.AdapterDataObserver mObserver;
    private NestedScrollView mPostsEmptyLayout;
    private FirebaseRecyclerAdapter mPostsAdapter;

    private static FirebaseDatabaseOperations mFirebaseDatabaseOperations;

    private static User mCurrentUser;

    public static PostsFragment newInstance(User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CURRENT_USER_BUNDLE, currentUser);
        PostsFragment postsFragment = new PostsFragment();
        postsFragment.setArguments(bundle);
        return postsFragment;
    }

    public void setUpUserInformation() {
        Bundle bundle = getArguments();
        mCurrentUser = (User) bundle.getSerializable(CURRENT_USER_BUNDLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        setUpUserInformation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        mPostsRecyclerView = (RecyclerView) view.findViewById(R.id.posts_recycler_view);
        mPostsEmptyLayout = (NestedScrollView) view.findViewById(R.id.posts_empty_layout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mPostsRecyclerView.setLayoutManager(layoutManager);
        mPostsRecyclerView.setHasFixedSize(false);
        mPostsRecyclerView.setItemViewCacheSize(20);
        mPostsRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mPostsRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        setUpAdapter();
        mPostsRecyclerView.setAdapter(mPostsAdapter);
        return view;
    }


    private void setUpAdapter() {
        mPostsAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(
                Post.class,
                R.layout.post_card_view,
                PostHolder.class,
                mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
        ) {
            @Override
            protected void populateViewHolder(PostHolder viewHolder, Post model, int position) {
                viewHolder.bindPost(model);
            }
        };

        mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mPostsEmptyLayout.setVisibility(View.VISIBLE);
                            mPostsRecyclerView.setVisibility(View.INVISIBLE);
                        } else {
                            mPostsEmptyLayout.setVisibility(View.INVISIBLE);
                            mPostsRecyclerView.setVisibility(View.VISIBLE);
                        }
                        addObserverForRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.POSTS_FRAGMENT, databaseError.getMessage());
                    }
                })
        ;
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mPostsAdapter.getItemCount() > 0) {
                    mPostsRecyclerView.setVisibility(View.VISIBLE);
                    mPostsEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mPostsAdapter.getItemCount() == 0) {
                    mPostsRecyclerView.setVisibility(View.INVISIBLE);
                    mPostsEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mPostsAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class PostHolder extends RecyclerView.ViewHolder {

        private ImageView mPostUserPhotoImageView;
        private TextView mPostUsernameTextView;
        private TextView mPostDateTextView;
        private ImageView mPostPropertyImageView;
        private TextView mPostTextTextView;
        private ImageView mPostPhotoImageView;
        private ImageButton mPostLikeImageButton;
        private TextView mPostLikesCounterTextView;
        private ImageButton mPostCommentImageButton;
        private TextView mPostCommentCounterTextView;
        private ImageButton mPostMenuOptionsImageButton;

        private Post mPost;
        private Context mContext;
        private CommentsController mCommentsController;
        private PostOptionsMenuControl mPostOptionsMenuControl;
        private LikesController mLikesController;
        private FirebaseAuthOperations mFirebaseAuthOperations;

        public PostHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
            mContext = itemView.getContext();
            mFirebaseAuthOperations = new FirebaseAuthOperations();
        }

        private void setUpViews(View itemView) {
            mPostUserPhotoImageView = (ImageView) itemView.findViewById(R.id.post_user_profile_photo);
            mPostUsernameTextView = (TextView) itemView.findViewById(R.id.post_username_text_view);
            mPostDateTextView = (TextView) itemView.findViewById(R.id.post_date_text_view);
            mPostPropertyImageView = (ImageView) itemView.findViewById(R.id.post_property_image_view);
            mPostTextTextView = (TextView) itemView.findViewById(R.id.post_text_text_view);
            mPostPhotoImageView = (ImageView) itemView.findViewById(R.id.post_photo_image_view);
            mPostLikeImageButton = (ImageButton) itemView.findViewById(R.id.post_like_image_button);
            mPostLikesCounterTextView = (TextView) itemView.findViewById(R.id.post_like_counter_text_view);
            mPostCommentImageButton = (ImageButton) itemView.findViewById(R.id.post_comment_image_button);
            mPostCommentCounterTextView = (TextView) itemView.findViewById(R.id.post_comment_counter_text_view);
            mPostMenuOptionsImageButton = (ImageButton) itemView.findViewById(R.id.post_options_menu_image_button);

            mPostUsernameTextView.setText(mCurrentUser.getUserName());
            Glide.with(mPostUserPhotoImageView.getContext())
                    .load(mCurrentUser.getUserProfilePhotoUri())
                    .placeholder(R.drawable.user_profile_photo_placeholder)
                    .dontAnimate()
                    .into(mPostUserPhotoImageView)
            ;
        }

        public void bindPost(Post post) {
            mPost = post;
            mPostOptionsMenuControl = new PostOptionsMenuControl(mContext, mCurrentUser, mPost);
            setFormattedPostDate();
            if (mPost.getGroupId().equalsIgnoreCase("none")) {
                mPostPropertyImageView.setImageResource(R.drawable.ic_post_person);
            } else {
                mPostPropertyImageView.setImageResource(R.drawable.ic_post_group);
            }
            if (mPost.getPostText() != null) {
                mPostTextTextView.setVisibility(View.VISIBLE);
                mPostTextTextView.setText(mPost.getPostText());
            } else {
                mPostTextTextView.setVisibility(View.GONE);
            }
            if (mPost.getPostImageUri() != null) {
                Picasso.with(mContext)
                        .load(mPost.getPostImageUri())
                        .placeholder(R.drawable.image_placeholder)
                        .into(mPostPhotoImageView)
                ;
                mPostPhotoImageView.setVisibility(View.VISIBLE);
            } else {
                mPostPhotoImageView.setVisibility(View.GONE);
            }

            mFirebaseDatabaseOperations
                    .getSpecificUserNodeReference(mFirebaseAuthOperations.getCurrentUserId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final User currentUser = dataSnapshot.getValue(User.class);
                            mLikesController = new LikesController(currentUser, mPost);
                            mPostLikeImageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mLikesController.onPressLikeButton();
                                }
                            });

                            mCommentsController = new CommentsController(mContext, currentUser, mPost);
                            mPostCommentImageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mCommentsController.onShowCommentsPopupWindow(v);
                                }
                            });
                            mFirebaseDatabaseOperations.getSpecificLikesNodeReference(mPost.getPostId())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(currentUser.getUserId())) {
                                                mPostLikeImageButton.setImageResource(R.drawable.ic_post_liked);
                                            } else {
                                                mPostLikeImageButton.setImageResource(R.drawable.ic_post_like);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d(Constants.POSTS_FRAGMENT, databaseError.getMessage());
                                        }
                                    })
                            ;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.POSTS_FRAGMENT, databaseError.getMessage());
                        }
                    })
            ;

            mFirebaseDatabaseOperations
                    .getSpecificPostsNodeReference(mCurrentUser.getUserId())
                    .child(mPost.getPostId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mPostLikesCounterTextView.setText(Long.toString(mPost.getPostLikesCounter()));
                            mPostCommentCounterTextView.setText(Long.toString(mPost.getPostCommentsCounter()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.FIREBASE_OPERATIONS, databaseError.getMessage());
                        }
                    })
            ;

            if (!mCurrentUser.getUserId().equals(mFirebaseAuthOperations.getCurrentUserId())) {
                mPostMenuOptionsImageButton.setVisibility(View.GONE);
            } else {
                mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentUser.getUserId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(mPost.getPostId())) {
                                    mPostMenuOptionsImageButton.setVisibility(View.VISIBLE);
                                    mPostMenuOptionsImageButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mPostOptionsMenuControl.showPostOptionsMenu();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(Constants.POSTS_FRAGMENT, databaseError.getMessage());
                            }
                        })
                ;
            }
        }

        private void setFormattedPostDate() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mPost.getPostDate());
            int hour = calendar.get(Calendar.HOUR);
            int minutes = calendar.get(Calendar.MINUTE);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(calendar.YEAR);
            String minutes_str = Integer.toString(minutes);
            if (minutes > 0 && minutes < 10) {
                minutes_str = "0" + minutes;
            }
            String hour_str = Integer.toString(hour);
            if (hour > 0 && hour < 10) {
                hour_str = "0" + hour;
            }
            if (hour == 0) {
                hour_str = "12";
            }
            String amOrPm;
            if ((calendar.get(Calendar.AM_PM)) == 0)
                amOrPm = "am";
            else
                amOrPm = "pm";
            mPostDateTextView.setText(hour_str + ":" + minutes_str + " " + amOrPm + " at " + day + "/" + month + "/" + year);
        }
    }

}
