package com.webshop.app.model;

import java.sql.Timestamp;

public class Permission {

    private int id;
    private String code;
    private String name;
    private String module;
    private String description;
    private Timestamp createdAt;

    public Permission() {
    }

    public Permission(int id, String code, String name, String module, String description) {
        this.id = id;
        this.code = normalizeUpper(code);
        this.name = normalize(name);
        this.module = normalizeUpper(module);
        this.description = normalize(description);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = Math.max(id, 0);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = normalizeUpper(code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = normalizeUpper(module);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalize(description);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
    }
}
