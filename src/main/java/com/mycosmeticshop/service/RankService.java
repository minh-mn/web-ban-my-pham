package com.mycosmeticshop.service;

import java.math.BigDecimal;

public class RankService {

    public static class RankInfo {
        public String rank;
        public int percent;
        public BigDecimal maxDiscountAmount;

        public RankInfo(String rank, int percent, BigDecimal max) {
            this.rank = rank;
            this.percent = percent;
            this.maxDiscountAmount = max;
        }
    }

    public RankInfo calculate(int totalOrders, BigDecimal totalSpent) {
        if (totalOrders >= 50 || totalSpent.compareTo(new BigDecimal("50000000")) >= 0) {
            return new RankInfo("DIAMOND", 15, new BigDecimal("300000"));
        }
        if (totalOrders >= 20 || totalSpent.compareTo(new BigDecimal("20000000")) >= 0) {
            return new RankInfo("GOLD", 10, new BigDecimal("200000"));
        }
        if (totalOrders >= 5 || totalSpent.compareTo(new BigDecimal("5000000")) >= 0) {
            return new RankInfo("SILVER", 5, new BigDecimal("100000"));
        }
        return new RankInfo("BRONZE", 0, BigDecimal.ZERO);
    }
}
