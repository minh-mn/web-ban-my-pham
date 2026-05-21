package com.webshop.app.service;

import com.webshop.app.dao.UserRankDAO;
import com.webshop.app.model.UserRank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class UserRankService {

    private final UserRankDAO userRankDAO;

    public UserRankService() {
        this.userRankDAO = new UserRankDAO();
    }

    public UserRankService(UserRankDAO userRankDAO) {
        this.userRankDAO = userRankDAO;
    }

    /* ================= MAIN RANK LOGIC ================= */

    public RankInfo getRankInfo(long userId) {

        BigDecimal totalSpent = getTotalPaidSpent(userId);
        int paidOrderCount = getPaidOrderCount(userId);

        UserRank currentRank = userRankDAO.findBestRankByTotalSpent(totalSpent);
        UserRank nextRank = userRankDAO.findNextRank(totalSpent);

        BigDecimal amountToNextRank = calculateAmountToNextRank(totalSpent, nextRank);
        int progressPercent = calculateRankProgressPercent(totalSpent, currentRank, nextRank);

        return new RankInfo(
                currentRank,
                nextRank,
                totalSpent,
                amountToNextRank,
                paidOrderCount,
                progressPercent
        );
    }

    public UserRank getCurrentRank(long userId) {
        BigDecimal totalSpent = getTotalPaidSpent(userId);
        return userRankDAO.findBestRankByTotalSpent(totalSpent);
    }

    public UserRank getNextRank(long userId) {
        BigDecimal totalSpent = getTotalPaidSpent(userId);
        return userRankDAO.findNextRank(totalSpent);
    }

    public BigDecimal getTotalPaidSpent(long userId) {
        return money0(userRankDAO.calculateTotalPaidSpentByUserId(userId));
    }

    public int getPaidOrderCount(long userId) {
        return userRankDAO.countPaidOrdersByUserId(userId);
    }

    public BigDecimal getAmountToNextRank(long userId) {
        BigDecimal totalSpent = getTotalPaidSpent(userId);
        UserRank nextRank = userRankDAO.findNextRank(totalSpent);

        return calculateAmountToNextRank(totalSpent, nextRank);
    }

    /* ================= DISCOUNT LOGIC ================= */

    public BigDecimal calculateRankDiscountAmount(long userId, BigDecimal subtotal) {

        UserRank currentRank = getCurrentRank(userId);

        return calculateRankDiscountAmount(currentRank, subtotal);
    }

    public BigDecimal calculateRankDiscountAmount(UserRank rank, BigDecimal subtotal) {

        BigDecimal safeSubtotal = money0(subtotal);

        if (rank == null || safeSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        int discountPercent = rank.getDiscountPercent();

        if (discountPercent <= 0) {
            return BigDecimal.ZERO;
        }

        return safeSubtotal
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    public BigDecimal applyRankDiscount(long userId, BigDecimal subtotal) {

        BigDecimal safeSubtotal = money0(subtotal);
        BigDecimal discountAmount = calculateRankDiscountAmount(userId, safeSubtotal);

        BigDecimal total = safeSubtotal.subtract(discountAmount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return money0(total);
    }

    /* ================= ACCOUNT JSP SUPPORT ================= */

    public Map<String, Object> buildRankAttributes(long userId) {

        RankInfo rankInfo = getRankInfo(userId);
        UserRank currentRank = rankInfo.getCurrentRank();
        UserRank nextRank = rankInfo.getNextRank();

        Map<String, Object> attributes = new HashMap<>();

        attributes.put("rankInfo", rankInfo);

        attributes.put("rankLabel", currentRank.getDisplayName());
        attributes.put("rankCode", currentRank.getCode());
        attributes.put("rankCss", currentRank.getCssClass());
        attributes.put("rankDiscount", currentRank.getDiscountPercent());
        attributes.put("rankDiscountLabel", currentRank.getDiscountLabel());

        attributes.put("rankTotalSpent", rankInfo.getTotalSpent());
        attributes.put("rankPaidOrderCount", rankInfo.getPaidOrderCount());

        attributes.put("nextRank", nextRank);
        attributes.put("nextRankLabel", nextRank == null ? null : nextRank.getDisplayName());
        attributes.put("nextRankMinSpent", nextRank == null ? BigDecimal.ZERO : nextRank.getMinSpent());

        attributes.put("amountToNextRank", rankInfo.getAmountToNextRank());
        attributes.put("rankProgressPercent", rankInfo.getProgressPercent());

        attributes.put("maxRank", nextRank == null);

        return attributes;
    }

    public void seedDefaultRanksIfEmpty() {
        userRankDAO.insertDefaultRanksIfEmpty();
    }

    /* ================= CALCULATION HELPERS ================= */

    private BigDecimal calculateAmountToNextRank(BigDecimal totalSpent, UserRank nextRank) {

        if (nextRank == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal safeTotalSpent = money0(totalSpent);
        BigDecimal amountNeeded = nextRank.getMinSpent().subtract(safeTotalSpent);

        if (amountNeeded.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return money0(amountNeeded);
    }

    private int calculateRankProgressPercent(BigDecimal totalSpent, UserRank currentRank, UserRank nextRank) {

        BigDecimal safeTotalSpent = money0(totalSpent);

        if (currentRank == null) {
            return 0;
        }

        if (nextRank == null) {
            return 100;
        }

        BigDecimal currentMin = currentRank.getMinSpent();
        BigDecimal nextMin = nextRank.getMinSpent();

        BigDecimal rankRange = nextMin.subtract(currentMin);
        BigDecimal userProgress = safeTotalSpent.subtract(currentMin);

        if (rankRange.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        if (userProgress.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal percent = userProgress
                .multiply(BigDecimal.valueOf(100))
                .divide(rankRange, 0, RoundingMode.HALF_UP);

        if (percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            return 100;
        }

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }

        return percent.intValue();
    }

    private static BigDecimal money0(BigDecimal value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }

    /* ================= DTO ================= */

    public static class RankInfo {

        private final UserRank currentRank;
        private final UserRank nextRank;
        private final BigDecimal totalSpent;
        private final BigDecimal amountToNextRank;
        private final int paidOrderCount;
        private final int progressPercent;

        public RankInfo(UserRank currentRank,
                        UserRank nextRank,
                        BigDecimal totalSpent,
                        BigDecimal amountToNextRank,
                        int paidOrderCount,
                        int progressPercent) {
            this.currentRank = currentRank;
            this.nextRank = nextRank;
            this.totalSpent = money0(totalSpent);
            this.amountToNextRank = money0(amountToNextRank);
            this.paidOrderCount = Math.max(paidOrderCount, 0);
            this.progressPercent = Math.max(0, Math.min(progressPercent, 100));
        }

        public UserRank getCurrentRank() {
            return currentRank;
        }

        public UserRank getNextRank() {
            return nextRank;
        }

        public BigDecimal getTotalSpent() {
            return totalSpent;
        }

        public BigDecimal getAmountToNextRank() {
            return amountToNextRank;
        }

        public int getPaidOrderCount() {
            return paidOrderCount;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public boolean isMaxRank() {
            return nextRank == null;
        }

        public String getCurrentRankName() {
            if (currentRank == null) {
                return "Thành viên";
            }

            return currentRank.getDisplayName();
        }

        public String getNextRankName() {
            if (nextRank == null) {
                return null;
            }

            return nextRank.getDisplayName();
        }

        public int getDiscountPercent() {
            if (currentRank == null) {
                return 0;
            }

            return currentRank.getDiscountPercent();
        }

        public String getCssClass() {
            if (currentRank == null) {
                return "rank-member";
            }

            return currentRank.getCssClass();
        }
    }
}