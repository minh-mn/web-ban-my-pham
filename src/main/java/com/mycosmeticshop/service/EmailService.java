package com.mycosmeticshop.service;

import com.mycosmeticshop.utils.EmailConfig;
import com.mycosmeticshop.utils.EmailUtil;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;

/*
 * Service gửi email cho hệ thống
 *
 * Chức năng:
 * - Gửi email xác nhận đăng ký tài khoản
 * - Gửi email đặt lại mật khẩu
 *
 * EmailUtil chịu trách nhiệm:
 * - kết nối SMTP
 * - gửi email HTML
 */
public class EmailService {

    /*
     * Gửi email xác nhận đăng ký tài khoản
     *
     * @param toEmail    email người nhận
     * @param fullName   tên người dùng
     * @param verifyLink link xác nhận tài khoản
     */
    public void sendRegisterVerifyEmail(String toEmail,
                                        String fullName,
                                        String verifyLink) throws MessagingException {

        String subject = "Xác nhận đăng ký tài khoản";

        String html = buildBaseTemplate(
                "Xác nhận đăng ký",
                "Xin chào " + safe(fullName)
                        + ",<br/>Vui lòng nhấn nút bên dưới để xác nhận tài khoản:",
                "<a style='display:inline-block;padding:10px 16px;background:#111;color:#fff;"
                        + "text-decoration:none;border-radius:8px;' href='" + verifyLink + "'>"
                        + "Xác nhận</a>"
                        + "<br/><br/><small>Nếu bạn không đăng ký, vui lòng bỏ qua email này.</small>"
        );

        EmailUtil.sendHtml(toEmail, subject, html);
    }

    /*
     * Gửi email đặt lại mật khẩu
     *
     * @param toEmail    email người nhận
     * @param fullName   tên người dùng
     * @param resetLink  link reset mật khẩu
     * @param expiresAt  thời gian hết hạn link
     */
    public void sendResetPasswordEmail(String toEmail,
                                       String fullName,
                                       String resetLink,
                                       LocalDateTime expiresAt) throws MessagingException {

        String subject = "Đặt lại mật khẩu";

        String html = buildBaseTemplate(
                "Đặt lại mật khẩu",
                "Xin chào " + safe(fullName)
                        + ",<br/>Bạn vừa yêu cầu đặt lại mật khẩu. "
                        + "Nhấn nút bên dưới để tạo mật khẩu mới.",
                "<a style='display:inline-block;padding:10px 16px;background:#111;color:#fff;"
                        + "text-decoration:none;border-radius:8px;' href='" + resetLink + "'>"
                        + "Đặt lại mật khẩu</a>"
                        + "<br/><br/><small>Link sẽ hết hạn lúc: " + expiresAt + "</small>"
                        + "<br/><small>Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</small>"
        );

        EmailUtil.sendHtml(toEmail, subject, html);
    }

    /*
     * Template HTML chung cho email
     *
     * @param title      tiêu đề email
     * @param intro      nội dung mở đầu
     * @param actionHtml nút hành động (verify/reset)
     */
    private String buildBaseTemplate(String title,
                                     String intro,
                                     String actionHtml) {

        return "<div style='font-family:Arial,sans-serif;"
                + "max-width:640px;margin:auto;padding:16px;"
                + "border:1px solid #eee;border-radius:12px;'>"

                + "<h2 style='margin:0 0 12px 0;'>"
                + title
                + "</h2>"

                + "<p style='margin:0 0 12px 0;line-height:1.5;'>"
                + intro
                + "</p>"

                + "<div style='margin:16px 0;'>"
                + actionHtml
                + "</div>"

                + "<hr style='border:none;border-top:1px solid #eee;margin:16px 0;'/>"

                + "<small style='color:#666;'>© "
                + EmailConfig.FROM_NAME
                + "</small>"

                + "</div>";
    }

    /*
     * Escape HTML để tránh XSS trong email
     */
    private String safe(String s) {
        if (s == null) {
            return "";
        }

        return s
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}