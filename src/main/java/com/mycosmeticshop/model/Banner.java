package com.mycosmeticshop.model;

public class Banner {

    private int id;
    private String title;

    // DB column: image
    // Java: imageUrl (đường dẫn tương đối /assets/images/banner/xxx.png)
    private String imageUrl;

    private String link;

    // DB column: [order] (keyword) -> Java: position
    private int position;

    // DB column: is_active
    private boolean active;

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

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
