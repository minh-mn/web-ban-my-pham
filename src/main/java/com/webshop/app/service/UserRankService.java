package com.webshop.app.service;

import com.webshop.app.dao.UserRankDAO;
import com.webshop.app.model.User;
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

    /*
     * Cơ chế cũ: chỉ tính rank tự động theo tổng chi tiêu.
     * Giữ lại để các controller cũ không bị lỗi.
     */
    public RankInfo getRankInfo(long userId) {
        return getRankInfo(userId, null);
    }

    /*
     * Cơ chế mới: nhận User để kiểm tra manualRankCode.
     * Nếu user.manualRankCode có giá trị => ưu tiên rank do admin chọn.
     * Nếu user.manualRankCode null/AUTO => tự động tính theo tổng chi tiêu.
     */
    public RankInfo getRankInfo(User user) {
        if (user == null) {
            return getRankInfo(0L, null);
        }

        return getRankInfo(user.getId(), user.getManualRankCode());
    }

    public RankInfo getRankInfo(long userId, String manualRankCode) {

        BigDecimal totalSpent = getTotalPaidSpent(userId);
        int paidOrderCount = getPaidOrderCount(userId);

        UserRank currentRank = resolveCurrentRank(totalSpent, manualRankCode);
        boolean manualRank = isValidManualRank(manualRankCode) && currentRank != null;

        UserRank nextRank = resolveNextRank(totalSpent, currentRank, manualRank);

        BigDecimal amountToNextRank = calculateAmountToNextRank(totalSpent, nextRank);
        int progressPercent = calculateRankProgressPercent(totalSpent, currentRank, nextRank);

        return new RankInfo(
                currentRank,
                nextRank,
                totalSpent,
                amountToNextRank,
                paidOrderCount,
                progressPercent,
                manualRank
        );
    }

    /*
     * Cơ chế cũ: lấy rank tự động.
     */
    public UserRank getCurrentRank(long userId) {
        BigDecimal totalSpent = getTotalPaidSpent(userId);
        return userRankDAO.findBestRankByTotalSpent(totalSpent);
    }

    /*
     * Cơ chế mới: lấy rank có xét manualRankCode.
     */
    public UserRank getCurrentRank(User user) {
        if (user == null) {
            return userRankDAO.findBestRankByTotalSpent(BigDecimal.ZERO);
        }

        return getCurrentRank(user.getId(), user.getManualRankCode());
    }

    public UserRank getCurrentRank(long userId, String manualRankCode) {
        BigDecimal totalSpent = getTotalPaidSpent(userId);
        return resolveCurrentRank(totalSpent, manualRankCode);
    }

    public UserRank getNextRank(long userId) {
        BigDecimal totalSpent = getTotalPaidSpent(userId);
        return userRankDAO.findNextRank(totalSpent);
    }

    public UserRank getNextRank(User user) {
        if (user == null) {
            return userRankDAO.findNextRank(BigDecimal.ZERO);
        }

        RankInfo rankInfo = getRankInfo(user);
        return rankInfo.getNextRank();
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

    public BigDecimal getAmountToNextRank(User user) {
        if (user == null) {
            return BigDecimal.ZERO;
        }

        RankInfo rankInfo = getRankInfo(user);
        return rankInfo.getAmountToNextRank();
    }

    /* ================= DISCOUNT LOGIC ================= */

    /*
     * Cơ chế cũ: tính giảm giá theo rank tự động.
     */
    public BigDecimal calculateRankDiscountAmount(long userId, BigDecimal subtotal) {

        UserRank currentRank = getCurrentRank(userId);

        return calculateRankDiscountAmount(currentRank, subtotal);
    }

    /*
     * Cơ chế mới: tính giảm giá theo rank có xét manualRankCode.
     */
    public BigDecimal calculateRankDiscountAmount(User user, BigDecimal subtotal) {

        UserRank currentRank = getCurrentRank(user);

        return calculateRankDiscountAmount(currentRank, subtotal);
    }

    public BigDecimal calculateRankDiscountAmount(long userId,
                                                  String manualRankCode,
                                                  BigDecimal subtotal) {

        UserRank currentRank = getCurrentRank(userId, manualRankCode);

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

    /*
     * Cơ chế cũ.
     */
    public BigDecimal applyRankDiscount(long userId, BigDecimal subtotal) {

        BigDecimal safeSubtotal = money0(subtotal);
        BigDecimal discountAmount = calculateRankDiscountAmount(userId, safeSubtotal);

        return subtractDiscount(safeSubtotal, discountAmount);
    }

    /*
     * Cơ chế mới.
     */
    public BigDecimal applyRankDiscount(User user, BigDecimal subtotal) {

        BigDecimal safeSubtotal = money0(subtotal);
        BigDecimal discountAmount = calculateRankDiscountAmount(user, safeSubtotal);

        return subtractDiscount(safeSubtotal, discountAmount);
    }

    public BigDecimal applyRankDiscount(long userId,
                                        String manualRankCode,
                                        BigDecimal subtotal) {

        BigDecimal safeSubtotal = money0(subtotal);
        BigDecimal discountAmount = calculateRankDiscountAmount(userId, manualRankCode, safeSubtotal);

        return subtractDiscount(safeSubtotal, discountAmount);
    }

    /* ================= ACCOUNT JSP SUPPORT ================= */

    /*
     * Cơ chế cũ.
     * Nếu controller chỉ truyền userId thì rank vẫn tính tự động.
     */
    public Map<String, Object> buildRankAttributes(long userId) {

        RankInfo rankInfo = getRankInfo(userId);

        return buildRankAttributesFromInfo(rankInfo);
    }

    /*
     * Cơ chế mới.
     * AccountServlet nên gọi hàm này để rank thủ công có hiệu lực.
     */
    public Map<String, Object> buildRankAttributes(User user) {

        RankInfo rankInfo = getRankInfo(user);

        return buildRankAttributesFromInfo(rankInfo);
    }

    public Map<String, Object> buildRankAttributes(long userId, String manualRankCode) {

        RankInfo rankInfo = getRankInfo(userId, manualRankCode);

        return buildRankAttributesFromInfo(rankInfo);
    }

    private Map<String, Object> buildRankAttributesFromInfo(RankInfo rankInfo) {

        UserRank currentRank = rankInfo.getCurrentRank();
        UserRank nextRank = rankInfo.getNextRank();

        Map<String, Object> attributes = new HashMap<>();

        attributes.put("rankInfo", rankInfo);

        attributes.put("rankLabel", currentRank == null ? "Thành viên" : currentRank.getDisplayName());
        attributes.put("rankCode", currentRank == null ? "MEMBER" : currentRank.getCode());
        attributes.put("rankCss", currentRank == null ? "rank-member" : currentRank.getCssClass());
        attributes.put("rankDiscount", currentRank == null ? 0 : currentRank.getDiscountPercent());
        attributes.put("rankDiscountLabel", currentRank == null ? "Không có ưu đãi" : currentRank.getDiscountLabel());

        attributes.put("rankTotalSpent", rankInfo.getTotalSpent());
        attributes.put("rankPaidOrderCount", rankInfo.getPaidOrderCount());

        attributes.put("nextRank", nextRank);
        attributes.put("nextRankLabel", nextRank == null ? null : nextRank.getDisplayName());
        attributes.put("nextRankMinSpent", nextRank == null ? BigDecimal.ZERO : nextRank.getMinSpent());

        attributes.put("amountToNextRank", rankInfo.getAmountToNextRank());
        attributes.put("rankProgressPercent", rankInfo.getProgressPercent());

        attributes.put("maxRank", nextRank == null);

        /*
         * Dùng cho JSP nếu muốn hiển thị:
         * - AUTO: rank tự động theo tổng chi tiêu.
         * - MANUAL: rank do admin nâng trực tiếp.
         */
        attributes.put("manualRank", rankInfo.isManualRank());
        attributes.put("rankMode", rankInfo.isManualRank() ? "MANUAL" : "AUTO");
        attributes.put("rankModeLabel", rankInfo.isManualRank()
                ? "Rank do admin chỉ định"
                : "Rank tự động theo tổng chi tiêu");

        return attributes;
    }

    public void seedDefaultRanksIfEmpty() {
        userRankDAO.insertDefaultRanksIfEmpty();
    }

    /* ================= MANUAL RANK HELPERS ================= */

    private UserRank resolveCurrentRank(BigDecimal totalSpent, String manualRankCode) {

        if (isValidManualRank(manualRankCode)) {
            UserRank manualRank = userRankDAO.findByCode(normalizeRankCode(manualRankCode));

            if (manualRank != null) {
                return manualRank;
            }
        }

        return userRankDAO.findBestRankByTotalSpent(totalSpent);
    }

    private UserRank resolveNextRank(BigDecimal totalSpent,
                                     UserRank currentRank,
                                     boolean manualRank) {

        if (currentRank == null) {
            return userRankDAO.findNextRank(totalSpent);
        }

        /*
         * Nếu rank là AUTO:
         * - Next rank dựa theo tổng chi tiêu hiện tại.
         *
         * Nếu rank là MANUAL:
         * - Next rank phải dựa theo mốc của rank hiện tại admin đã chọn.
         * - Tránh trường hợp admin set GOLD nhưng totalSpent thấp,
         *   hệ thống lại báo next rank là SILVER.
         */
        if (manualRank) {
            return userRankDAO.findNextRank(currentRank.getMinSpent());
        }

        return userRankDAO.findNextRank(totalSpent);
    }

    private boolean isValidManualRank(String manualRankCode) {
        return manualRankCode != null
                && !manualRankCode.isBlank()
                && !"AUTO".equalsIgnoreCase(manualRankCode);
    }

    private String normalizeRankCode(String manualRankCode) {
        if (manualRankCode == null || manualRankCode.isBlank()) {
            return null;
        }

        if ("AUTO".equalsIgnoreCase(manualRankCode)) {
            return null;
        }

        return manualRankCode.trim().toUpperCase();
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

    private int calculateRankProgressPercent(BigDecimal totalSpent,
                                             UserRank currentRank,
                                             UserRank nextRank) {

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

    private BigDecimal subtractDiscount(BigDecimal subtotal, BigDecimal discountAmount) {

        BigDecimal safeSubtotal = money0(subtotal);
        BigDecimal safeDiscountAmount = money0(discountAmount);

        BigDecimal total = safeSubtotal.subtract(safeDiscountAmount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return money0(total);
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
        private final boolean manualRank;

        public RankInfo(UserRank currentRank,
                        UserRank nextRank,
                        BigDecimal totalSpent,
                        BigDecimal amountToNextRank,
                        int paidOrderCount,
                        int progressPercent,
                        boolean manualRank) {
            this.currentRank = currentRank;
            this.nextRank = nextRank;
            this.totalSpent = money0(totalSpent);
            this.amountToNextRank = money0(amountToNextRank);
            this.paidOrderCount = Math.max(paidOrderCount, 0);
            this.progressPercent = Math.max(0, Math.min(progressPercent, 100));
            this.manualRank = manualRank;
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

        public boolean isManualRank() {
            return manualRank;
        }

        public boolean isAutoRank() {
            return !manualRank;
        }

        public boolean isMaxRank() {
            return nextRank == null;
        }

        public String getRankMode() {
            return manualRank ? "MANUAL" : "AUTO";
        }

        public String getRankModeLabel() {
            return manualRank ? "Rank do admin chỉ định" : "Rank tự động theo tổng chi tiêu";
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