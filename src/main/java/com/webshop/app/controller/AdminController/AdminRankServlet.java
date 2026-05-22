package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.UserRankDAO;
import com.webshop.app.model.UserRank;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/admin/ranks")
public class AdminRankServlet extends HttpServlet {

    private final UserRankDAO userRankDAO = new UserRankDAO();

    private static final String JSP_LIST = "/jsp/admin/rank/rank_list.jsp";
    private static final String JSP_FORM = "/jsp/admin/rank/rank_form.jsp";

    private static final String ACT_LIST = "list";
    private static final String ACT_NEW = "new";
    private static final String ACT_EDIT = "edit";

    private static final String ACT_CREATE = "create";
    private static final String ACT_UPDATE = "update";
    private static final String ACT_DELETE = "delete";
    private static final String ACT_TOGGLE = "toggle";
    private static final String ACT_SEED = "seed";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = ACT_LIST;
        }

        switch (action) {

            case ACT_NEW: {
                UserRank rank = new UserRank();
                rank.setActive(true);
                rank.setDiscountPercent(0);
                rank.setMinSpent(BigDecimal.ZERO);
                rank.setCssClass("rank-member");

                req.setAttribute("mode", "create");
                req.setAttribute("rank", rank);
                req.getRequestDispatcher(JSP_FORM).forward(req, resp);
                return;
            }

            case ACT_EDIT: {
                long id = safeLong(req.getParameter("id"), -1L);

                if (id <= 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/ranks");
                    return;
                }

                UserRank rank = userRankDAO.findById(id);

                if (rank == null) {
                    resp.sendRedirect(req.getContextPath() + "/admin/ranks");
                    return;
                }

                req.setAttribute("mode", "edit");
                req.setAttribute("rank", rank);
                req.getRequestDispatcher(JSP_FORM).forward(req, resp);
                return;
            }

            case ACT_LIST:
            default: {
                renderList(req, resp);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = safe(req.getParameter("action"));
        if (action.isBlank()) {
            action = ACT_CREATE;
        }

        try {
            switch (action) {

                case ACT_CREATE: {
                    UserRank rank = new UserRank();
                    bind(req, rank);
                    validate(rank, false);
                    userRankDAO.create(rank);
                    break;
                }

                case ACT_UPDATE: {
                    long id = safeLong(req.getParameter("id"), -1L);

                    if (id <= 0) {
                        throw new IllegalArgumentException("ID rank không hợp lệ.");
                    }

                    UserRank existingRank = userRankDAO.findById(id);

                    if (existingRank == null) {
                        throw new IllegalArgumentException("Không tìm thấy rank cần cập nhật.");
                    }

                    UserRank rank = new UserRank();
                    rank.setId(id);
                    bind(req, rank);
                    validate(rank, true);
                    userRankDAO.update(rank);
                    break;
                }

                case ACT_DELETE: {
                    long id = safeLong(req.getParameter("id"), -1L);

                    if (id > 0) {
                        UserRank rank = userRankDAO.findById(id);

                        if (rank != null && "MEMBER".equalsIgnoreCase(rank.getCode())) {
                            throw new IllegalArgumentException("Không được xóa hoặc tắt rank MEMBER mặc định.");
                        }

                        userRankDAO.deactivate(id);
                    }

                    break;
                }

                case ACT_TOGGLE: {
                    long id = safeLong(req.getParameter("id"), -1L);

                    if (id > 0) {
                        UserRank rank = userRankDAO.findById(id);

                        if (rank != null && "MEMBER".equalsIgnoreCase(rank.getCode())) {
                            throw new IllegalArgumentException("Không được tắt rank MEMBER mặc định.");
                        }

                        userRankDAO.toggleActive(id);
                    }

                    break;
                }

                case ACT_SEED: {
                    userRankDAO.insertDefaultRanksIfEmpty();
                    break;
                }

                default:
                    break;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/ranks");
            return;

        } catch (IllegalArgumentException ex) {
            forwardFormWithError(req, resp, action, ex.getMessage());
            return;

        } catch (Exception ex) {
            ex.printStackTrace();
            forwardFormWithError(req, resp, action, "Có lỗi xảy ra khi xử lý rank. Vui lòng thử lại.");
        }
    }

    /* ===================== VIEW HELPERS ===================== */

    private void renderList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<UserRank> ranks = userRankDAO.findAllForAdmin();

        req.setAttribute("ranks", ranks);
        req.getRequestDispatcher(JSP_LIST).forward(req, resp);
    }

    private void forwardFormWithError(HttpServletRequest req,
                                      HttpServletResponse resp,
                                      String action,
                                      String message)
            throws ServletException, IOException {

        req.setAttribute("error", message);

        UserRank rank = new UserRank();

        if (ACT_UPDATE.equals(action)) {
            rank.setId(safeLong(req.getParameter("id"), 0L));
            req.setAttribute("mode", "edit");
        } else {
            req.setAttribute("mode", "create");
        }

        try {
            bind(req, rank);
        } catch (Exception ignored) {
        }

        req.setAttribute("rank", rank);
        req.getRequestDispatcher(JSP_FORM).forward(req, resp);
    }

    /* ===================== BIND / VALIDATE ===================== */

    private void bind(HttpServletRequest req, UserRank rank) {

        String code = safe(req.getParameter("code")).toUpperCase();
        String name = safe(req.getParameter("name"));
        BigDecimal minSpent = safeBigDecimal(req.getParameter("minSpent"), BigDecimal.ZERO);
        int discountPercent = safeInt(req.getParameter("discountPercent"), 0);
        String cssClass = safe(req.getParameter("cssClass"));

        if (cssClass.isBlank()) {
            cssClass = "rank-" + code.toLowerCase();
        }

        boolean active = "1".equals(req.getParameter("active"))
                || "true".equalsIgnoreCase(req.getParameter("active"))
                || "on".equalsIgnoreCase(req.getParameter("active"));

        rank.setCode(code);
        rank.setName(name);
        rank.setMinSpent(minSpent);
        rank.setDiscountPercent(discountPercent);
        rank.setCssClass(cssClass);
        rank.setActive(active);
    }

    private void validate(UserRank rank, boolean isUpdate) {

        if (rank.getCode() == null || rank.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã rank không được để trống.");
        }

        if (!rank.getCode().matches("^[A-Z0-9_-]{2,50}$")) {
            throw new IllegalArgumentException("Mã rank chỉ được chứa chữ in hoa, số, dấu gạch dưới hoặc gạch ngang.");
        }

        if (rank.getName() == null || rank.getName().isBlank()) {
            throw new IllegalArgumentException("Tên rank không được để trống.");
        }

        if (rank.getMinSpent() == null || rank.getMinSpent().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Mốc chi tiêu tối thiểu không được âm.");
        }

        if (rank.getDiscountPercent() < 0 || rank.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("Phần trăm ưu đãi phải nằm trong khoảng 0 đến 100.");
        }

        if (rank.getCssClass() == null || rank.getCssClass().isBlank()) {
            throw new IllegalArgumentException("CSS class không được để trống.");
        }

        long currentId = isUpdate && rank.getId() != null ? rank.getId() : 0L;

        if (userRankDAO.existsByCodeExceptId(rank.getCode(), currentId)) {
            throw new IllegalArgumentException("Mã rank đã tồn tại. Vui lòng chọn mã khác.");
        }
    }

    /* ===================== HELPERS ===================== */

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private int safeInt(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long safeLong(String value, long defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private BigDecimal safeBigDecimal(String value, BigDecimal defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            String normalized = value.trim().replace(",", "");

            BigDecimal number = new BigDecimal(normalized);

            if (number.compareTo(BigDecimal.ZERO) < 0) {
                return BigDecimal.ZERO;
            }

            return number;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}