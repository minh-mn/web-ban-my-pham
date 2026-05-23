package com.webshop.app.service;

import com.webshop.app.dao.UserDAO;
import com.webshop.app.dao.UserRankDAO;
import com.webshop.app.model.User;
import com.webshop.app.model.UserRank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class UserRankService {

    private final UserRankDAO userRankDAO;
    private final UserDAO userDAO;

    public UserRankService() {
        this(new UserRankDAO(), new UserDAO());
    }

    public UserRankService(UserRankDAO userRankDAO) {
        this(userRankDAO, new UserDAO());
    }

    public UserRankService(UserRankDAO userRankDAO, UserDAO userDAO) {
        this.userRankDAO = userRankDAO == null ? new UserRankDAO() : userRankDAO;
        this.userDAO = userDAO == null ? new UserDAO() : userDAO;
    }

    /* ================= MAIN RANK LOGIC ================= */

    /*
     * Hàm cũ nhưng đã được nâng cấp:
     * Nếu chỉ truyền userId, service vẫn tự đọc users.manual_rank_code.
     * Nếu admin có gán manual rank thì ưu tiên manual rank.
     * Nếu không có manual rank thì tự tính rank theo tổng chi tiêu.
     */
    public RankInfo getRankInfo(long userId) {
        String manualRankCode = resolveManualRankCodeByUserId(userId);
        return getRankInfo(userId, manualRankCode);
    }

    /*
     * Hàm dùng khi controller đã có User object.
     * Ưu tiên manualRankCode trong User.
     * Nếu User object cũ chưa có manualRankCode thì đọc lại từ DB theo userId.
     */
    public RankInfo getRankInfo(User user) {
        if (user == null) {
            return getRankInfo(0L, null);
        }

        String manualRankCode = normalizeManualRankCode(user.getManualRankCode());

        if (manualRankCode == null) {
            manualRankCode = resolveManualRankCodeByUserId(user.getId());
        }

        return getRankInfo(user.getId(), manualRankCode);
    }

    /*
     * Hàm lõi:
     * - totalSpent vẫn luôn tính để hiển thị tiến độ.
     * - manualRankCode hợp lệ thì dùng rank admin gán.
     * - manualRankCode null/AUTO/không hợp lệ thì dùng rank tự động.
     */
    public RankInfo getRankInfo(long userId, String manualRankCode) {

        BigDecimal totalSpent = getTotalPaidSpent(userId);
        int paidOrderCount = getPaidOrderCount(userId);

        UserRank manualRank = resolveManualRank(manualRankCode);
        boolean manualRankApplied = manualRank != null;

        UserRank currentRank = manualRankApplied
                ? manualRank
                : userRankDAO.findBestRankByTotalSpent(totalSpent);

        UserRank nextRank = resolveNextRank(totalSpent, currentRank, manualRankApplied);

        BigDecimal amountToNextRank = calculateAmountToNextRank(totalSpent, nextRank);
        int progressPercent = calculateRankProgressPercent(totalSpent, currentRank, nextRank);

        return new RankInfo(
                currentRank,
                nextRank,
                totalSpent,
                amountToNextRank,
                paidOrderCount,
                progressPercent,
                manualRankApplied
        );
    }

    /*
     * Hàm cũ nhưng đã được nâng cấp:
     * Lấy rank hiện tại có xét manual_rank_code.
     */
    public UserRank getCurrentRank(long userId) {
        String manualRankCode = resolveManualRankCodeByUserId(userId);
        return getCurrentRank(userId, manualRankCode);
    }

    /*
     * Lấy rank hiện tại từ User object.
     */
    public UserRank getCurrentRank(User user) {
        if (user == null) {
            return userRankDAO.findBestRankByTotalSpent(BigDecimal.ZERO);
        }

        String manualRankCode = normalizeManualRankCode(user.getManualRankCode());

        if (manualRankCode == null) {
            manualRankCode = resolveManualRankCodeByUserId(user.getId());
        }

        return getCurrentRank(user.getId(), manualRankCode);
    }

    /*
     * Lấy rank hiện tại theo userId + manualRankCode truyền sẵn.
     */
    public UserRank getCurrentRank(long userId, String manualRankCode) {
        BigDecimal totalSpent = getTotalPaidSpent(userId);

        UserRank manualRank = resolveManualRank(manualRankCode);
        if (manualRank != null) {
            return manualRank;
        }

        return userRankDAO.findBestRankByTotalSpent(totalSpent);
    }

    /*
     * Hàm cũ nhưng đã được nâng cấp:
     * Next rank cũng xét manual rank nếu admin đã gán.
     */
    public UserRank getNextRank(long userId) {
        RankInfo rankInfo = getRankInfo(userId);
        return rankInfo.getNextRank();
    }

    public UserRank getNextRank(User user) {
        RankInfo rankInfo = getRankInfo(user);
        return rankInfo.getNextRank();
    }

    public BigDecimal getTotalPaidSpent(long userId) {
        return money0(userRankDAO.calculateTotalPaidSpentByUserId(userId));
    }

    public int getPaidOrderCount(long userId) {
        return Math.max(0, userRankDAO.countPaidOrdersByUserId(userId));
    }

    /*
     * Hàm cũ nhưng đã được nâng cấp:
     * Nếu manual rank đang cao hơn rank tự động thì amountToNextRank tính theo rank manual.
     */
    public BigDecimal getAmountToNextRank(long userId) {
        RankInfo rankInfo = getRankInfo(userId);
        return rankInfo.getAmountToNextRank();
    }

    public BigDecimal getAmountToNextRank(User user) {
        RankInfo rankInfo = getRankInfo(user);
        return rankInfo.getAmountToNextRank();
    }

    /* ================= DISCOUNT LOGIC ================= */

    /*
     * Hàm cũ nhưng đã được nâng cấp:
     * Chỉ cần truyền userId vẫn tự xét users.manual_rank_code.
     */
    public BigDecimal calculateRankDiscountAmount(long userId, BigDecimal subtotal) {
        UserRank currentRank = getCurrentRank(userId);
        return calculateRankDiscountAmount(currentRank, subtotal);
    }

    /*
     * Tính giảm giá theo rank có xét manualRankCode từ User object.
     */
    public BigDecimal calculateRankDiscountAmount(User user, BigDecimal subtotal) {
        UserRank currentRank = getCurrentRank(user);
        return calculateRankDiscountAmount(currentRank, subtotal);
    }

    /*
     * Tính giảm giá theo userId + manualRankCode truyền sẵn.
     */
    public BigDecimal calculateRankDiscountAmount(long userId,
                                                  String manualRankCode,
                                                  BigDecimal subtotal) {

        UserRank currentRank = getCurrentRank(userId, manualRankCode);
        return calculateRankDiscountAmount(currentRank, subtotal);
    }

    /*
     * Tính số tiền được giảm theo phần trăm của rank.
     */
    public BigDecimal calculateRankDiscountAmount(UserRank rank, BigDecimal subtotal) {

        BigDecimal safeSubtotal = money0(subtotal);

        if (rank == null || safeSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        int discountPercent = rank.getDiscountPercent();

        if (discountPercent <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = safeSubtotal
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        if (discount.compareTo(safeSubtotal) > 0) {
            return safeSubtotal;
        }

        return money0(discount);
    }

    /*
     * Hàm cũ nhưng đã được nâng cấp:
     * applyRankDiscount(userId, subtotal) cũng xét manual_rank_code.
     */
    public BigDecimal applyRankDiscount(long userId, BigDecimal subtotal) {

        BigDecimal safeSubtotal = money0(subtotal);
        BigDecimal discountAmount = calculateRankDiscountAmount(userId, safeSubtotal);

        return subtractDiscount(safeSubtotal, discountAmount);
    }

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
     * Hàm cũ nhưng đã được nâng cấp:
     * Account hoặc controller cũ chỉ truyền userId vẫn tự xét manual_rank_code.
     */
    public Map<String, Object> buildRankAttributes(long userId) {

        RankInfo rankInfo = getRankInfo(userId);

        return buildRankAttributesFromInfo(rankInfo);
    }

    /*
     * Hàm khuyến nghị cho AccountServlet:
     * Truyền User object sau khi đã reload từ UserDAO.
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
        attributes.put("currentRankCode", currentRank == null ? "MEMBER" : currentRank.getCode());
        attributes.put("rankCss", currentRank == null ? "rank-member" : currentRank.getCssClass());
        attributes.put("rankDiscount", currentRank == null ? 0 : currentRank.getDiscountPercent());
        attributes.put("rankDiscountLabel", currentRank == null ? "Không có ưu đãi" : currentRank.getDiscountLabel());

        attributes.put("rankTotalSpent", rankInfo.getTotalSpent());
        attributes.put("rankPaidOrderCount", rankInfo.getPaidOrderCount());

        attributes.put("nextRank", nextRank);
        attributes.put("nextRankLabel", nextRank == null ? null : nextRank.getDisplayName());
        attributes.put("nextRankCode", nextRank == null ? null : nextRank.getCode());
        attributes.put("nextRankMinSpent", nextRank == null ? BigDecimal.ZERO : nextRank.getMinSpent());

        attributes.put("amountToNextRank", rankInfo.getAmountToNextRank());
        attributes.put("rankProgressPercent", rankInfo.getProgressPercent());

        attributes.put("maxRank", nextRank == null);

        /*
         * Dùng cho JSP:
         * - AUTO: rank tự động theo tổng chi tiêu.
         * - MANUAL: rank do admin chỉ định.
         */
        attributes.put("manualRank", rankInfo.isManualRank());
        attributes.put("rankMode", rankInfo.getRankMode());
        attributes.put("rankModeLabel", rankInfo.getRankModeLabel());

        return attributes;
    }

    public void seedDefaultRanksIfEmpty() {
        userRankDAO.insertDefaultRanksIfEmpty();
    }

    /* ================= PUBLIC EFFECTIVE RANK HELPERS ================= */

    /*
     * Lấy mã rank hiệu lực cuối cùng của user.
     * Dùng cho checkout/coupon/account nếu cần đồng bộ rank.
     */
    public String resolveEffectiveRankCode(long userId) {
        UserRank currentRank = getCurrentRank(userId);

        if (currentRank == null || currentRank.getCode() == null || currentRank.getCode().isBlank()) {
            return "MEMBER";
        }

        return currentRank.getCode().trim().toUpperCase();
    }

    public String resolveEffectiveRankCode(User user) {
        UserRank currentRank = getCurrentRank(user);

        if (currentRank == null || currentRank.getCode() == null || currentRank.getCode().isBlank()) {
            return "MEMBER";
        }

        return currentRank.getCode().trim().toUpperCase();
    }

    /* ================= MANUAL RANK HELPERS ================= */

    private String resolveManualRankCodeByUserId(long userId) {

        if (userId <= 0) {
            return null;
        }

        try {
            return normalizeManualRankCode(userDAO.findManualRankCodeByUserId((int) userId));
        } catch (RuntimeException e) {
            /*
             * Nếu database cũ chưa có manual_rank_code hoặc UserDAO chưa migrate,
             * fallback về rank tự động để không làm crash account/checkout.
             */
            return null;
        }
    }

    private UserRank resolveManualRank(String manualRankCode) {

        String normalized = normalizeManualRankCode(manualRankCode);

        if (normalized == null) {
            return null;
        }

        try {
            return userRankDAO.findByCode(normalized);
        } catch (RuntimeException e) {
            return null;
        }
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
         * - Next rank dựa theo mốc của rank hiện tại admin đã chọn.
         * - Tránh trường hợp admin set GOLD nhưng totalSpent thấp,
         *   hệ thống lại báo next rank là SILVER.
         */
        if (manualRank) {
            return userRankDAO.findNextRank(currentRank.getMinSpent());
        }

        return userRankDAO.findNextRank(totalSpent);
    }

    private String normalizeManualRankCode(String manualRankCode) {
        if (manualRankCode == null || manualRankCode.isBlank()) {
            return null;
        }

        String normalized = manualRankCode.trim().toUpperCase();

        if ("AUTO".equals(normalized)) {
            return null;
        }

        return switch (normalized) {
            case "MEMBER", "SILVER", "GOLD", "DIAMOND", "VIP" -> normalized;
            default -> null;
        };
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

        public String getCurrentRankCode() {
            if (currentRank == null || currentRank.getCode() == null || currentRank.getCode().isBlank()) {
                return "MEMBER";
            }

            return currentRank.getCode();
        }

        public String getCurrentRankName() {
            if (currentRank == null) {
                return "Thành viên";
            }

            return currentRank.getDisplayName();
        }

        public String getNextRankCode() {
            if (nextRank == null || nextRank.getCode() == null || nextRank.getCode().isBlank()) {
                return null;
            }

            return nextRank.getCode();
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