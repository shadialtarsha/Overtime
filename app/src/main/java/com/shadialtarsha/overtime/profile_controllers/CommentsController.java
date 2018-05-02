package com.shadialtarsha.overtime.profile_controllers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Comment;
import com.shadialtarsha.overtime.model.Follower;
import com.shadialtarsha.overtime.model.Post;
import com.shadialtarsha.overtime.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class CommentsController {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private View mInflatedView;
    private PopupWindow mPopupWindow;
    private RecyclerView mCommentsRecyclerView;
    private LinearLayoutManager mRecyclerViewLayoutManager;
    private NestedScrollView mCommentsEmptyLayout;
    private RecyclerView.AdapterDataObserver mObserver;
    private FirebaseRecyclerAdapter mCommentsAdapter;

    private ImageView mCurrentUserImageView;
    private EditText mNewCommentEditText;
    private ImageButton mAddNewCommentImageButton;

    private static FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private User mCurrentUser;
    private Post mCurrentPost;


    public CommentsController(Context context, User currentUser, Post currentPost) {
        mContext = context;
        mCurrentUser = currentUser;
        mCurrentPost = currentPost;
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
    }

    public void onShowCommentsPopupWindow(View v) {
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflatedView = mLayoutInflater.inflate(R.layout.comments_popup_window, null, false);

        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        mPopupWindow = new PopupWindow(mInflatedView, width, height, true);
        mPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.comment_list_background));
        mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
        mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 100);

        setUpCommentsRecyclerView();
        setUpNewCommentComponents();
    }

    private void setUpCommentsRecyclerView() {
        mCommentsRecyclerView = (RecyclerView) mInflatedView.findViewById(R.id.comments_recycler_view);
        mCommentsEmptyLayout = (NestedScrollView) mInflatedView.findViewById(R.id.comments_empty_layout);
        mRecyclerViewLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerViewLayoutManager.setStackFromEnd(true);
        mCommentsRecyclerView.setLayoutManager(mRecyclerViewLayoutManager);
        mCommentsRecyclerView.setHasFixedSize(false);
        mCommentsRecyclerView.setItemViewCacheSize(20);
        mCommentsRecyclerView.setItemAnimator(null);
        ItemAnimator animator = mCommentsRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, mRecyclerViewLayoutManager.getOrientation());
        mCommentsRecyclerView.addItemDecoration(dividerItemDecoration);
        setCommentsAdapter();
        mCommentsRecyclerView.setAdapter(mCommentsAdapter);
    }

    private void setUpNewCommentComponents() {
        mCurrentUserImageView = (ImageView) mInflatedView.findViewById(R.id.user_profile_image_view);
        mNewCommentEditText = (EditText) mInflatedView.findViewById(R.id.comment_list_item_content_text);
        mAddNewCommentImageButton = (ImageButton) mInflatedView.findViewById(R.id.add_comment_image_button);

        mNewCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence string, int start, int before, int count) {
                if (string.toString().trim().length() > 0) {
                    mAddNewCommentImageButton.setVisibility(View.VISIBLE);
                } else {
                    mAddNewCommentImageButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Glide.with(mContext)
                .load(mCurrentUser.getUserProfilePhotoUri())
                .placeholder(R.drawable.user_profile_photo_placeholder)
                .dontAnimate()
                .into(mCurrentUserImageView)
        ;
        mAddNewCommentImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Comment comment = new Comment();
                comment.setCommentId(mFirebaseDatabaseOperations
                        .getSpecificCommentsNodeReference(mCurrentPost.getPostId())
                        .push().getKey())
                ;
                comment.setUserId(mCurrentUser.getUserId());
                comment.setCommentDate(new Date());
                comment.setCommentText(mNewCommentEditText.getText().toString());
                mFirebaseDatabaseOperations.getSpecificCommentsNodeReference(mCurrentPost.getPostId())
                        .child(comment.getCommentId())
                        .setValue(comment)
                ;
                mCurrentPost.setPostCommentsCounter(mCurrentPost.getPostCommentsCounter() + 1);
                mFirebaseDatabaseOperations.getSpecificPostsNodeReference(mCurrentPost.getUserId())
                        .child(mCurrentPost.getPostId())
                        .setValue(mCurrentPost);
                mFirebaseDatabaseOperations.getSpecificWallPostsNodeReference(mCurrentPost.getUserId())
                        .child(mCurrentPost.getPostId()).setValue(mCurrentPost)
                ;
                mFirebaseDatabaseOperations.getSpecificFollowersNodeReference(mCurrentPost.getUserId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot followers : dataSnapshot.getChildren()) {
                                    Follower follower = followers.getValue(Follower.class);
                                    mFirebaseDatabaseOperations
                                            .getSpecificWallPostsNodeReference(follower.getUserId())
                                            .child(mCurrentPost.getPostId())
                                            .setValue(mCurrentPost)
                                    ;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(Constants.COMMENTS_CONTROLLER, databaseError.getMessage());
                            }
                        })
                ;
                mNewCommentEditText.setText("");
                mRecyclerViewLayoutManager.scrollToPositionWithOffset(mCommentsAdapter.getItemCount() - 1, 0);
            }
        });

    }

    private void setCommentsAdapter() {
        mCommentsAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(
                Comment.class,
                R.layout.comment_list_item,
                CommentHolder.class,
                mFirebaseDatabaseOperations.getSpecificCommentsNodeReference(mCurrentPost.getPostId())
        ) {
            @Override
            protected void populateViewHolder(CommentHolder viewHolder, Comment model, int position) {
                viewHolder.bindComment(model);

            }
        };

        mFirebaseDatabaseOperations.getSpecificCommentsNodeReference(mCurrentPost.getPostId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mCommentsEmptyLayout.setVisibility(View.VISIBLE);
                            mCommentsRecyclerView.setVisibility(View.INVISIBLE);
                        } else {
                            mCommentsEmptyLayout.setVisibility(View.INVISIBLE);
                            mCommentsRecyclerView.setVisibility(View.VISIBLE);
                        }
                        addObserverForRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.COMMENTS_CONTROLLER, databaseError.getMessage());
                    }
                })
        ;
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mCommentsAdapter.getItemCount() > 0) {
                    mCommentsRecyclerView.setVisibility(View.VISIBLE);
                    mCommentsEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mCommentsAdapter.getItemCount() == 0) {
                    mCommentsRecyclerView.setVisibility(View.INVISIBLE);
                    mCommentsEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mCommentsAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {

        private Comment mComment;
        private Context mContext;

        private ImageView mCommentUserImageView;
        private TextView mCommentUserName;
        private TextView mCommentText;
        private TextView mCommentDate;

        private void setUpViews(View itemView) {
            mContext = itemView.getContext();
            mCommentUserImageView = (ImageView) itemView.findViewById(R.id.comment_list_item_user_profile_photo);
            mCommentUserName = (TextView) itemView.findViewById(R.id.comment_list_item_username);
            mCommentText = (TextView) itemView.findViewById(R.id.comment_list_item_content_text);
            mCommentDate = (TextView) itemView.findViewById(R.id.comment_list_item_date);
        }

        public CommentHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
        }

        public void bindComment(Comment comment) {
            mComment = comment;
            setFormattedCommentDate();
            mCommentUserName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickOnPhotoOrName();
                }
            });
            mCommentUserImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickOnPhotoOrName();
                }
            });
            mFirebaseDatabaseOperations.getSpecificUserNodeReference(mComment.getUserId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            mCommentUserName.setText(user.getUserName());
                            Glide.with(mContext)
                                    .load(user.getUserProfilePhotoUri())
                                    .placeholder(R.drawable.user_profile_photo_placeholder)
                                    .dontAnimate()
                                    .into(mCommentUserImageView)
                            ;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.COMMENTS_CONTROLLER, databaseError.getMessage());
                        }
                    })
            ;
            mCommentText.setText(mComment.getCommentText());
        }

        private void setFormattedCommentDate() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mComment.getCommentDate());
            int hour = calendar.get(Calendar.HOUR);
            int minutes = calendar.get(Calendar.MINUTE);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(calendar.YEAR);
            String minutes_str = Integer.toString(minutes);
            if (minutes < 10) {
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
            if ((calendar.get(Calendar.AM_PM)) == 0) {
                amOrPm = "am";
            } else {
                amOrPm = "pm";
            }
            mCommentDate.setText(hour_str + ":" + minutes_str + " " + amOrPm + " at " + day + "/" + month + "/" + year);
        }

        private void onClickOnPhotoOrName() {
            mFirebaseDatabaseOperations.getSpecificUserNodeReference(mComment.getUserId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            mContext.startActivity(ProfileActivity.newIntent(mContext, user));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.COMMENTS_CONTROLLER, databaseError.getMessage());
                        }
                    })
            ;
        }
    }
}
