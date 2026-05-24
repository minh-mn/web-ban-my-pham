package com.webshop.app.model;

public class Policy {

    private int id;
    private String title;
    private String slug;
    private String fileName;

    public Policy() {
    }

    public Policy(int id, String title, String slug, String fileName) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.fileName = fileName;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}