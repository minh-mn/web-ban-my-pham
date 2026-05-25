package com.webshop.app.model;

import java.sql.Timestamp;

public class FlashSale {
    private int id;
    private String title;

    private Timestamp startTime;
    private Timestamp endTime;

    private boolean active;

    private Timestamp createdAt;

    // getter setter

    public int getId() {
        return id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRunning() {

        long now = System.currentTimeMillis();

        return active
                && now >= startTime.getTime()
                && now <= endTime.getTime();
    }
}
