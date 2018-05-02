package com.shadialtarsha.overtime.inbox_controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shadialtarsha.overtime.Constants;
import com.shadialtarsha.overtime.R;
import com.shadialtarsha.overtime.firebase.FirebaseDatabaseOperations;
import com.shadialtarsha.overtime.model.Conversation;
import com.shadialtarsha.overtime.model.Message;
import com.shadialtarsha.overtime.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class InboxActivity extends AppCompatActivity {

    private static final String CURRENT_USER_EXTRA = "com.shadialtarsha.overtime.inbox_controllers.InboxActivity.current_user";

    private Toolbar mInboxToolbar;
    private RecyclerView mInboxRecyclerView;
    private NestedScrollView mInboxEmptyLayout;
    private LinearLayoutManager mInboxRecLinearLayoutManager;
    private FirebaseRecyclerAdapter mInboxAdapter;
    private RecyclerView.AdapterDataObserver mObserver;
    private static FirebaseDatabaseOperations mFirebaseDatabaseOperations;

    private static User mCurrentUser;

    public static Intent newIntent(Context packageContext, User currentUser) {
        Intent intent = new Intent(packageContext, InboxActivity.class);
        intent.putExtra(CURRENT_USER_EXTRA, currentUser);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        mInboxToolbar = (Toolbar) findViewById(R.id.inbox_toolbar);
        mInboxToolbar.setTitle(getString(R.string.inbox_activity));
        setSupportActionBar(mInboxToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCurrentUser = (User) getIntent().getSerializableExtra(CURRENT_USER_EXTRA);
        mInboxRecyclerView = (RecyclerView) findViewById(R.id.inbox_recycler_view);
        mInboxEmptyLayout = (NestedScrollView) findViewById(R.id.inbox_empty_layout);
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        setUpInboxRecyclerView();

    }

    private void setUpInboxRecyclerView() {
        mInboxRecLinearLayoutManager = new LinearLayoutManager(this);
        mInboxRecyclerView.setLayoutManager(mInboxRecLinearLayoutManager);
        mInboxRecyclerView.setHasFixedSize(false);
        mInboxRecyclerView.setItemViewCacheSize(20);
        mInboxRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mInboxRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, mInboxRecLinearLayoutManager.getOrientation());
        mInboxRecyclerView.addItemDecoration(dividerItemDecoration);
        setUpAdapter();
    }

    private void setUpAdapter() {
        mInboxAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationHolder>(
                Conversation.class,
                R.layout.inbox_users_list_item,
                ConversationHolder.class,
                mFirebaseDatabaseOperations.getSpecificInboxNodeReference(mCurrentUser.getUserId())
                        .orderByChild("lastModifiedDate")
        ) {
            @Override
            protected void populateViewHolder(ConversationHolder viewHolder, Conversation model, int position) {
                viewHolder.bindConversation(model);
            }
        };

        mFirebaseDatabaseOperations.getSpecificInboxNodeReference(mCurrentUser.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mInboxEmptyLayout.setVisibility(View.VISIBLE);
                            mInboxRecyclerView.setVisibility(View.INVISIBLE);
                        } else {
                            mInboxEmptyLayout.setVisibility(View.INVISIBLE);
                            mInboxRecyclerView.setVisibility(View.VISIBLE);
                        }
                        addObserverForRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.INBOX_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mInboxRecyclerView.setAdapter(mInboxAdapter);
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mInboxAdapter.getItemCount() > 0) {
                    mInboxRecyclerView.setVisibility(View.VISIBLE);
                    mInboxEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mInboxAdapter.getItemCount() == 0) {
                    mInboxRecyclerView.setVisibility(View.INVISIBLE);
                    mInboxEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mInboxAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class ConversationHolder extends RecyclerView.ViewHolder {

        private ImageView mConversationUserProfilePhoto;
        private TextView mConversationUsernameTextView;
        private TextView mConversationLastMessageTextView;
        private Context mContext;
        private Conversation mConversation;
        private AlertDialog.Builder mAlertDialogBuilder;

        private void setUpViews(View itemView) {
            mConversationUserProfilePhoto = (ImageView) itemView.findViewById(R.id.inbox_users_user_profile_photo);
            mConversationUsernameTextView = (TextView) itemView.findViewById(R.id.inbox_users_username_text_view);
            mConversationLastMessageTextView = (TextView) itemView.findViewById(R.id.inbox_users_last_message_text_view);
            mContext = itemView.getContext();
        }

        public ConversationHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFirebaseDatabaseOperations.getSpecificUserNodeReference(mConversation.getSecondPartnerId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User secondPartner = dataSnapshot.getValue(User.class);
                                    mContext.startActivity(ConversationActivity.newIntent(mContext, mConversation.getConversationId(), mCurrentUser, secondPartner));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(Constants.INBOX_ACTIVITY, databaseError.getMessage());
                                }
                            })
                    ;
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mAlertDialogBuilder = new AlertDialog.Builder(mContext);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.conversation_options_menu_item);
                    adapter.add("Delete");
                    mAlertDialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                new AlertDialog.Builder(mContext)
                                        .setMessage(mContext.getString(R.string.alert_dialog_are_you_sure_message))
                                        .setPositiveButton(mContext.getText(R.string.alert_dialog_yes_button),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mFirebaseDatabaseOperations
                                                                .getSpecificInboxNodeReference(mCurrentUser.getUserId())
                                                                .child(mConversation.getConversationId())
                                                                .removeValue()
                                                        ;
                                                    }
                                                })
                                        .setNegativeButton(mContext.getText(R.string.alert_dialog_cancel_button),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                        .show()
                                ;
                            }
                        }
                    })
                            .show()
                    ;
                    return true;
                }

            });
        }

        public void bindConversation(Conversation conversation) {
            mConversation = conversation;
            mFirebaseDatabaseOperations.getSpecificUserNodeReference(mConversation.getSecondPartnerId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            Glide.with(mContext)
                                    .load(user.getUserProfilePhotoUri())
                                    .placeholder(R.drawable.user_profile_photo_placeholder)
                                    .dontAnimate()
                                    .into(mConversationUserProfilePhoto)
                            ;
                            mConversationUsernameTextView.setText(user.getUserName());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.INBOX_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
            mFirebaseDatabaseOperations.getSpecificMessagesNodeReference(mConversation.getConversationId())
                    .orderByChild("messageDate").limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot messages : dataSnapshot.getChildren()) {
                                Message message = messages.getValue(Message.class);
                                mConversationLastMessageTextView.setText(message.getMessageText());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(Constants.INBOX_ACTIVITY, databaseError.getMessage());
                        }
                    })
            ;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inbox, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.inbox_menu_add_item) {
            startActivityForResult(NewMessageActivity.newIntent(InboxActivity.this, mCurrentUser), 1000);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
