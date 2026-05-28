package com.webshop.app.model;
import java.util.Date;

public class Event {
    private int id;
    private String title;
    private String summary;
    private String tag;
    private String imageUrl;
    private Date eventDate;

    // Constructors
    public Event() {}

    public Event(String title, String summary, String tag, String imageUrl, Date eventDate) {
        this.title = title;
        this.summary = summary;
        this.tag = tag;
        this.imageUrl = imageUrl;
        this.eventDate = eventDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }
}