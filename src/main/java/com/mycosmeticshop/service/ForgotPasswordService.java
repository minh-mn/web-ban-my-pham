package com.mycosmeticshop.service;

import com.mycosmeticshop.service.EmailService;
import com.mycosmeticshop.dao.PasswordResetTokenDAO;
import com.mycosmeticshop.dao.UserDAO;
import com.mycosmeticshop.model.PasswordResetToken;
import com.mycosmeticshop.model.User;
import com.mycosmeticshop.utils.EmailConfig;

import java.time.LocalDateTime;
import java.util.UUID;

public class ForgotPasswordService {

    private final UserDAO userDAO = new UserDAO();
    private final PasswordResetTokenDAO tokenDAO = new PasswordResetTokenDAO();
    private final EmailService emailService = new EmailService();

    /**
     * Yêu cầu reset mật khẩu:
     * - Không tiết lộ email có tồn tại hay không
     * - Nếu user tồn tại + active, tạo token và gửi email reset link
     */
    public void requestReset(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) return;

        String normalizedEmail = email.trim();

        // Best practice: không nói email tồn tại hay không
        User u = userDAO.findByEmail(normalizedEmail);
        if (u == null) return;

        // (Khuyến nghị) Vô hiệu hoá các token cũ của user (tránh nhiều link còn hiệu lực)
        // Chỉ dùng nếu bạn đã implement method này trong PasswordResetTokenDAO
        try {
            tokenDAO.invalidateAllActiveTokensOfUser(u.getId());
        } catch (Exception ignored) {
            // Nếu bạn chưa add method invalidate, có thể bỏ đoạn này
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(EmailConfig.RESET_TOKEN_EXPIRE_MINUTES);

        tokenDAO.create(u.getId(), token, expiresAt);

        // (Tuỳ chọn) Cleanup token cũ/hết hạn để DB gọn
        try {
            tokenDAO.cleanupExpiredOrUsed();
        } catch (Exception ignored) {
            // Nếu bạn chưa add method cleanup, có thể bỏ đoạn này
        }

        String resetLink = EmailConfig.APP_BASE_URL + "/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(u.getEmail(), u.getFullName(), resetLink, expiresAt);
    }

    /**
     * Validate token và lấy User tương ứng để hiển thị form reset.
     * Trả null nếu token không tồn tại / hết hạn / đã dùng.
     */
    public User validateTokenAndGetUser(String token) {
        if (token == null || token.trim().isEmpty()) return null;

        PasswordResetToken t = tokenDAO.findByToken(token.trim());
        if (t == null || !t.isValid(LocalDateTime.now())) return null;

        return userDAO.findById(t.getUserId());
    }

    /**
     * Reset mật khẩu:
     * - Token phải hợp lệ (chưa dùng + chưa hết hạn)
     * - Cập nhật password cho user
     * - Mark token used để không dùng lại
     *
     * Lưu ý: UserDAO.updatePassword(userId, newPlainPassword) sẽ tự hash mật khẩu.
     */
    public void resetPassword(String token, String newRawPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Token không hợp lệ.");
        }
        if (newRawPassword == null || newRawPassword.trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu mới không hợp lệ.");
        }

        PasswordResetToken t = tokenDAO.findByToken(token.trim());
        if (t == null || !t.isValid(LocalDateTime.now())) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn.");
        }

        // DAO tự hash => truyền plain password
        userDAO.updatePassword(t.getUserId(), newRawPassword);

        // đánh dấu token đã dùng
        tokenDAO.markUsed(t.getId());
    }
}
