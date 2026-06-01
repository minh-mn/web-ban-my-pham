package com.webshop.app.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class UserSearchHistory {

    private long id;
    private int userId;
    private String keyword;
    private String normalizedKeyword;
    private int resultCount;
    private String searchUrl;
    private int searchCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastSearchedAt;

    public UserSearchHistory() {
    }

    public UserSearchHistory(
            long id,
            int userId,
            String keyword,
            String normalizedKeyword,
            int resultCount,
            String searchUrl,
            int searchCount,
            Timestamp createdAt,
            Timestamp updatedAt,
            Timestamp lastSearchedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.keyword = keyword;
        this.normalizedKeyword = normalizedKeyword;
        this.resultCount = resultCount;
        this.searchUrl = searchUrl;
        this.searchCount = searchCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastSearchedAt = lastSearchedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getNormalizedKeyword() {
        return normalizedKeyword;
    }

    public void setNormalizedKeyword(String normalizedKeyword) {
        this.normalizedKeyword = normalizedKeyword;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(int searchCount) {
        this.searchCount = searchCount;
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

    public Timestamp getLastSearchedAt() {
        return lastSearchedAt;
    }

    public void setLastSearchedAt(Timestamp lastSearchedAt) {
        this.lastSearchedAt = lastSearchedAt;
    }

    public String getDisplayLastSearchedAt() {
        if (lastSearchedAt == null) {
            return "--";
        }

        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lastSearchedAt);
    }

    public String getDisplayCreatedAt() {
        if (createdAt == null) {
            return "--";
        }

        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(createdAt);
    }

    public String getDisplayUpdatedAt() {
        if (updatedAt == null) {
            return "--";
        }

        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(updatedAt);
    }

    public boolean hasSearchUrl() {
        return searchUrl != null && !searchUrl.isBlank();
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }
}
