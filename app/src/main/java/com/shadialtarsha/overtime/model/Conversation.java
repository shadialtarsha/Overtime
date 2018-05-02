package com.shadialtarsha.overtime.model;

import java.util.Calendar;
import java.util.Date;

public class Conversation {

    private String mConversationId;
    private String mFirstPartnerId;
    private String mSecondPartnerId;
    private Date mLastModifiedDate;

    public Conversation() {

    }

    public String getConversationId() {
        return mConversationId;
    }

    public void setConversationId(String conversationId) {
        mConversationId = conversationId;
    }

    public String getFirstPartnerId() {
        return mFirstPartnerId;
    }

    public void setFirstPartnerId(String firstPartnerId) {
        mFirstPartnerId = firstPartnerId;
    }

    public String getSecondPartnerId() {
        return mSecondPartnerId;
    }

    public void setSecondPartnerId(String secondPartnerId) {
        mSecondPartnerId = secondPartnerId;
    }

    public Date getLastModifiedDate() {
        return mLastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastModifiedDate);
        Calendar currentCalendar = Calendar.getInstance();
        if (currentCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        mLastModifiedDate = calendar.getTime();
    }
}
