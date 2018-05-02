package com.shadialtarsha.overtime.model;

import java.util.Calendar;
import java.util.Date;

public class Message {

    private String mMessageId;
    private String mMessageSenderId;
    private String mMessageReceiverId;
    private String mConversationId;
    private Date mMessageDate;
    private String mMessageText;

    public Message() {

    }

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public String getMessageSenderId() {
        return mMessageSenderId;
    }

    public void setMessageSenderId(String messageSenderId) {
        mMessageSenderId = messageSenderId;
    }

    public String getMessageReceiverId() {
        return mMessageReceiverId;
    }

    public void setMessageReceiverId(String messageReceiverId) {
        mMessageReceiverId = messageReceiverId;
    }

    public String getConversationId() {
        return mConversationId;
    }

    public void setConversationId(String conversationId) {
        mConversationId = conversationId;
    }

    public Date getMessageDate() {
        return mMessageDate;
    }

    public void setMessageDate(Date messageDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(messageDate);
        Calendar currentCalendar = Calendar.getInstance();
        if (currentCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        mMessageDate = calendar.getTime();
    }

    public String getMessageText() {
        return mMessageText;
    }

    public void setMessageText(String messageText) {
        mMessageText = messageText;
    }
}
