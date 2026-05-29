package com.webshop.app.model;

import java.sql.Timestamp;

public class CategoryTag {

    private int id;
    private int categoryId;
    private String name;
    private String slug;
    private int displayOrder;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public CategoryTag() {
    }

    public CategoryTag(int id, int categoryId, String name, String slug, int displayOrder, boolean active) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public CategoryTag(int categoryId, String name, String slug, int displayOrder, boolean active) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    /* ===================== GETTER / SETTER ===================== */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = Math.max(categoryId, 0);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }


    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug == null ? null : slug.trim();
    }


    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = Math.max(displayOrder, 0);
    }


    public boolean isActive() {
        return active;
    }

    /**
     * JSP helper: ${tag.active}
     */
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }


    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    /* ===================== HELPER METHODS ===================== */

    public boolean isValidForSave() {
        return categoryId > 0 && name != null && !name.isBlank();
    }

    public String getDisplayName() {
        if (name == null || name.isBlank()) {
            return "Tag #" + id;
        }

        return name;
    }

    @Override
    public String toString() {
        return "CategoryTag{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", displayOrder=" + displayOrder +
                ", active=" + active +
                '}';
    }
}