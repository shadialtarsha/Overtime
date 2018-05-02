package com.shadialtarsha.overtime.inbox_controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.Date;

public class ConversationActivity extends AppCompatActivity {

    private static final String FIRST_PARTNER_EXTRA = "com.shadialtarsha.overtime.inbox_controllers.ConversationActivity.first_partner";
    private static final String SECOND_PARTNER_EXTRA = "com.shadialtarsha.overtime.inbox_controllers.ConversationActivity.second_partner";
    private static final String CONVERSATION_ID_EXTRA = "com.shadialtarsha.overtime.inbox_controllers.ConversationActivity.conversation_id";

    private Toolbar mConversationToolbar;
    private ImageView mFirstPartnerUserProfilePhoto;
    private EditText mNewMessageEditText;
    private ImageButton mSendMessageImageButton;
    private RecyclerView mMessagesRecyclerView;
    private LinearLayoutManager mMessagesLinearLayoutManager;
    private NestedScrollView mMessagesEmptyLayout;
    private FirebaseRecyclerAdapter mMessagesAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    private FirebaseDatabaseOperations mFirebaseDatabaseOperations;
    private static User mFirstPartner;
    private static User mSecondPartner;
    private String mConversationId;

    public static Intent newIntent(Context packageContext, String conversationId, User firstPartner, User secondPartner) {
        Intent intent = new Intent(packageContext, ConversationActivity.class);
        intent.putExtra(FIRST_PARTNER_EXTRA, firstPartner);
        intent.putExtra(SECOND_PARTNER_EXTRA, secondPartner);
        if(conversationId != null){
            intent.putExtra(CONVERSATION_ID_EXTRA, conversationId);
        }
        return intent;
    }

    private void setUpViews() {
        mConversationToolbar = (Toolbar) findViewById(R.id.conversation_toolbar);
        mConversationToolbar.setTitle(mSecondPartner.getUserName());
        setSupportActionBar(mConversationToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirstPartnerUserProfilePhoto = (ImageView) findViewById(R.id.conversation_user_profile_image_view);
        mNewMessageEditText = (EditText) findViewById(R.id.conversation_new_message_edit_text);
        mSendMessageImageButton = (ImageButton) findViewById(R.id.send_message_image_button);
        mMessagesRecyclerView = (RecyclerView) findViewById(R.id.messages_recycler_view);
        mMessagesEmptyLayout = (NestedScrollView) findViewById(R.id.messages_empty_layout);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        mFirstPartner = (User) getIntent().getSerializableExtra(FIRST_PARTNER_EXTRA);
        mSecondPartner = (User) getIntent().getSerializableExtra(SECOND_PARTNER_EXTRA);
        if(getIntent().getStringExtra(CONVERSATION_ID_EXTRA) != null){
            mConversationId = getIntent().getStringExtra(CONVERSATION_ID_EXTRA);
        }
        setUpViews();
        mFirebaseDatabaseOperations = new FirebaseDatabaseOperations();
        Glide.with(this)
                .load(mFirstPartner.getUserProfilePhotoUri())
                .placeholder(R.drawable.user_profile_photo_placeholder)
                .dontAnimate()
                .into(mFirstPartnerUserProfilePhoto)
        ;
        setUpMessagesRecyclerView();
        mNewMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence string, int start, int before, int count) {
                if (string.toString().trim().length() > 0) {
                    mSendMessageImageButton.setVisibility(View.VISIBLE);
                } else {
                    mSendMessageImageButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewMessage();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMessagesRecyclerView() {
        mMessagesLinearLayoutManager = new LinearLayoutManager(this);
        mMessagesLinearLayoutManager.setStackFromEnd(true);
        mMessagesRecyclerView.setLayoutManager(mMessagesLinearLayoutManager);
        mMessagesRecyclerView.setHasFixedSize(false);
        mMessagesRecyclerView.setItemViewCacheSize(20);
        mMessagesRecyclerView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = mMessagesRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        if (mConversationId != null) {
            setUpMessagesAdapter(mConversationId);
        }
    }

    private void addNewMessage() {
        mFirebaseDatabaseOperations.getSpecificInboxNodeReference(mFirstPartner.getUserId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean isConversation = false;
                        for (DataSnapshot conversations : dataSnapshot.getChildren()) {
                            Conversation conversation = conversations.getValue(Conversation.class);
                            if (conversation.getSecondPartnerId().equals(mSecondPartner.getUserId())) {

                                if (mMessagesAdapter == null) {
                                    mConversationId = conversation.getConversationId();
                                    setUpMessagesAdapter(mConversationId);
                                }

                                final Message message = new Message();
                                message.setMessageSenderId(mFirstPartner.getUserId());
                                message.setMessageReceiverId(mSecondPartner.getUserId());
                                message.setMessageDate(new Date());
                                message.setConversationId(conversation.getConversationId());
                                message.setMessageText(mNewMessageEditText.getText().toString());
                                message.setMessageId(
                                        mFirebaseDatabaseOperations
                                                .getSpecificMessagesNodeReference(conversation.getConversationId())
                                                .push()
                                                .getKey()
                                );
                                mFirebaseDatabaseOperations
                                        .getSpecificMessagesNodeReference(conversation.getConversationId())
                                        .child(message.getMessageId())
                                        .setValue(message)
                                ;
                                conversation.setLastModifiedDate(message.getMessageDate());

                                mFirebaseDatabaseOperations
                                        .getSpecificInboxNodeReference(mFirstPartner.getUserId())
                                        .child(conversation.getConversationId())
                                        .setValue(conversation)
                                ;
                                mFirebaseDatabaseOperations.getSpecificInboxNodeReference(mSecondPartner.getUserId())
                                        .child(conversation.getConversationId())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Conversation c = dataSnapshot.getValue(Conversation.class);
                                                c.setLastModifiedDate(message.getMessageDate());
                                                mFirebaseDatabaseOperations
                                                        .getSpecificInboxNodeReference(mSecondPartner.getUserId())
                                                        .child(c.getConversationId())
                                                        .setValue(c)
                                                ;
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(Constants.CONVERSATION_ACTIVITY, databaseError.getMessage());
                                            }
                                        })
                                ;
                                isConversation = true;
                                break;
                            }
                        }

                        if (!isConversation) {
                            Message message = new Message();
                            message.setMessageSenderId(mFirstPartner.getUserId());
                            message.setMessageReceiverId(mSecondPartner.getUserId());
                            message.setMessageDate(new Date());
                            message.setMessageText(mNewMessageEditText.getText().toString());

                            Conversation conversation = new Conversation();
                            String conversationId = mFirebaseDatabaseOperations
                                    .getSpecificInboxNodeReference(mFirstPartner.getUserId())
                                    .push().getKey();
                            conversation.setConversationId(conversationId);
                            conversation.setFirstPartnerId(mFirstPartner.getUserId());
                            conversation.setSecondPartnerId(mSecondPartner.getUserId());
                            conversation.setLastModifiedDate(message.getMessageDate());
                            mFirebaseDatabaseOperations
                                    .getSpecificInboxNodeReference(mFirstPartner.getUserId())
                                    .child(conversationId)
                                    .setValue(conversation)
                            ;
                            conversation.setFirstPartnerId(mSecondPartner.getUserId());
                            conversation.setSecondPartnerId(mFirstPartner.getUserId());

                            mFirebaseDatabaseOperations
                                    .getSpecificInboxNodeReference(mSecondPartner.getUserId())
                                    .child(conversationId)
                                    .setValue(conversation)
                            ;
                            message.setConversationId(conversationId);
                            message.setMessageId(
                                    mFirebaseDatabaseOperations
                                            .getSpecificMessagesNodeReference(conversationId)
                                            .push()
                                            .getKey()
                            );
                            mFirebaseDatabaseOperations
                                    .getSpecificMessagesNodeReference(conversationId)
                                    .child(message.getMessageId())
                                    .setValue(message)
                            ;
                            if (mMessagesAdapter == null) {
                                mConversationId = conversationId;
                                setUpMessagesAdapter(mConversationId);
                            }
                        }
                        mNewMessageEditText.setText("");
                        mMessagesLinearLayoutManager.scrollToPositionWithOffset(mMessagesAdapter.getItemCount() - 1, 0);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.CONVERSATION_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;

    }

    private void setUpMessagesAdapter(String conversationId) {
        mMessagesAdapter = new FirebaseRecyclerAdapter<Message, MessageHolder>(
                Message.class,
                R.layout.messages_list_item,
                MessageHolder.class,
                mFirebaseDatabaseOperations.getSpecificMessagesNodeReference(conversationId)
        ) {
            @Override
            protected void populateViewHolder(MessageHolder viewHolder, Message model, int position) {
                viewHolder.bindMessage(model);

            }
        };

        mFirebaseDatabaseOperations.getSpecificMessagesNodeReference(conversationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mMessagesEmptyLayout.setVisibility(View.VISIBLE);
                            mMessagesRecyclerView.setVisibility(View.INVISIBLE);
                        } else {
                            mMessagesEmptyLayout.setVisibility(View.INVISIBLE);
                            mMessagesRecyclerView.setVisibility(View.VISIBLE);
                        }
                        addObserverForRecyclerView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Constants.CONVERSATION_ACTIVITY, databaseError.getMessage());
                    }
                })
        ;
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);
    }

    private void addObserverForRecyclerView() {
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (mMessagesAdapter.getItemCount() > 0) {
                    mMessagesRecyclerView.setVisibility(View.VISIBLE);
                    mMessagesEmptyLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mMessagesAdapter.getItemCount() == 0) {
                    mMessagesRecyclerView.setVisibility(View.INVISIBLE);
                    mMessagesEmptyLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        mMessagesAdapter.registerAdapterDataObserver(mObserver);
    }

    public static class MessageHolder extends RecyclerView.ViewHolder {

        private TextView mOutMessageTextView;
        private TextView mInMessageTextView;
        private Message mMessage;

        public void setUpViews(View itemView) {
            mOutMessageTextView = (TextView) itemView.findViewById(R.id.out_message_text_view);
            mInMessageTextView = (TextView) itemView.findViewById(R.id.in_message_text_view);
        }


        public MessageHolder(View itemView) {
            super(itemView);
            setUpViews(itemView);
        }

        public void bindMessage(Message message) {
            mMessage = message;
            if (mMessage.getMessageSenderId().equals(mFirstPartner.getUserId())) {
                mOutMessageTextView.setVisibility(View.VISIBLE);
                mOutMessageTextView.setText(message.getMessageText());
                mInMessageTextView.setVisibility(View.GONE);
            } else if (mMessage.getMessageSenderId().equals(mSecondPartner.getUserId())) {
                mInMessageTextView.setVisibility(View.VISIBLE);
                mInMessageTextView.setText(message.getMessageText());
                mOutMessageTextView.setVisibility(View.GONE);
            }
        }
    }
}
