package com.webshop.app.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Role {

    private int id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private boolean systemRole;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<String> permissionCodes;

    public Role() {
        this.active = true;
        this.systemRole = false;
        this.permissionCodes = new ArrayList<>();
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
        this.code = normalizeCode(code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = normalize(description);
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public boolean getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
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

    public List<String> getPermissionCodes() {
        if (permissionCodes == null) {
            permissionCodes = new ArrayList<>();
        }
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        Set<String> normalized = new LinkedHashSet<>();
        if (permissionCodes != null) {
            for (String permissionCode : permissionCodes) {
                String code = normalizeCode(permissionCode);
                if (code != null) {
                    normalized.add(code);
                }
            }
        }
        this.permissionCodes = new ArrayList<>(normalized);
    }

    public boolean hasPermission(String permissionCode) {
        String code = normalizeCode(permissionCode);
        return code != null && getPermissionCodes().contains(code);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeCode(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
    }
}
