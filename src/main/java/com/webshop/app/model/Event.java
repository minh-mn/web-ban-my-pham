package com.webshop.app.model;

import java.util.Date;

public class Event {
    private int id;
    private String title;
    private String summary;
    private String tag;
    private String imageUrl;
    private Date eventDate;

    // Thông tin hỗ trợ hiển thị ở trang người dùng
    private String topicKey;
    private String actionUrl;
    private String actionText;

    public Event() {
    }

    public Event(String title, String summary, String tag, String imageUrl, Date eventDate) {
        this.title = title;
        this.summary = summary;
        this.tag = tag;
        this.imageUrl = imageUrl;
        this.eventDate = eventDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getTopicKey() {
        return topicKey;
    }

    public void setTopicKey(String topicKey) {
        this.topicKey = topicKey;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }
}
