package com.webshop.app.model.admin;

import java.util.List;

public class AdminPromotionStats {

    private final int total;
    private final int active;
    private final int inactive;
    private final int expired;
    private final int upcoming;

    public AdminPromotionStats(int total, int active, int inactive, int expired, int upcoming) {
        this.total = total;
        this.active = active;
        this.inactive = inactive;
        this.expired = expired;
        this.upcoming = upcoming;
    }

    public static AdminPromotionStats fromRows(List<AdminPromotionRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return new AdminPromotionStats(0, 0, 0, 0, 0);
        }

        int active = 0;
        int inactive = 0;
        int expired = 0;
        int upcoming = 0;

        for (AdminPromotionRow row : rows) {
            if (row == null) {
                continue;
            }

            if (row.isExpired()) {
                expired++;
            } else if (row.isUpcoming()) {
                upcoming++;
            } else if (row.isActive()) {
                active++;
            } else {
                inactive++;
            }
        }

        return new AdminPromotionStats(rows.size(), active, inactive, expired, upcoming);
    }

    public int getTotal() {
        return total;
    }

    public int getActive() {
        return active;
    }

    public int getInactive() {
        return inactive;
    }

    public int getExpired() {
        return expired;
    }

    public int getUpcoming() {
        return upcoming;
    }
}