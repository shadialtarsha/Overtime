package com.shadialtarsha.overtime.model;

import java.util.Calendar;
import java.util.Date;

public class Comment {

    private String mCommentId;
    private String mUserId;
    private String mCommentText;
    private Date mCommentDate;

    public Comment(){

    }

    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String commentId) {
        mCommentId = commentId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getCommentText() {
        return mCommentText;
    }

    public void setCommentText(String commentText) {
        mCommentText = commentText;
    }

    public Date getCommentDate() {
        return mCommentDate;
    }

    public void setCommentDate(Date commentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(commentDate);
        Calendar currentCalendar = Calendar.getInstance();
        if (currentCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        mCommentDate = calendar.getTime();
    }
}
