package com.mycosmeticshop.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/*
 * Utility gửi email bằng SMTP
 *
 * Chức năng:
 * - Tạo SMTP Session
 * - Gửi email HTML
 *
 * Cấu hình SMTP được lấy từ EmailConfig:
 * - SMTP_HOST
 * - SMTP_PORT
 * - SMTP_USERNAME
 * - SMTP_APP_PASSWORD
 *
 * Hiện tại đang cấu hình cho Gmail SMTP (TLS - port 587)
 */
public class EmailUtil {

    /*
     * Tạo SMTP Session
     *
     * Session chứa:
     * - cấu hình SMTP
     * - thông tin đăng nhập email
     */
    private static Session createSession() {

        Properties props = new Properties();

        // ===== Gmail SMTP (TLS 587) =====
        props.put("mail.smtp.auth", "true");              // bật authentication
        props.put("mail.smtp.starttls.enable", "true");   // bật TLS
        props.put("mail.smtp.starttls.required", "true"); // bắt buộc TLS
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(EmailConfig.SMTP_PORT));

        // ===== Timeout để tránh treo khi SMTP lỗi =====
        props.put("mail.smtp.connectiontimeout", "10000"); // timeout connect
        props.put("mail.smtp.timeout", "10000");           // timeout read
        props.put("mail.smtp.writetimeout", "10000");      // timeout write

        /*
         * Tạo session kèm Authenticator
         * dùng username + app password để login SMTP
         */
        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                EmailConfig.SMTP_USERNAME,
                                EmailConfig.SMTP_APP_PASSWORD
                        );
                    }
                }
        );

        // ===== DEBUG SMTP =====
        // In log SMTP ra console để debug lỗi gửi mail
        session.setDebug(true);

        return session;
    }

    /*
     * Gửi email HTML
     *
     * @param toMail      email người nhận
     * @param subject     tiêu đề email
     * @param htmlContent nội dung HTML
     */
    public static void sendHtml(
            String toMail,
            String subject,
            String htmlContent
    ) throws MessagingException {

        Session session = createSession();

        // Tạo email message
        MimeMessage msg = new MimeMessage(session);

        // ===== FROM =====
        // Gmail yêu cầu from trùng username
        try {

            InternetAddress from = new InternetAddress(
                    EmailConfig.SMTP_USERNAME,   // email gửi
                    EmailConfig.FROM_NAME,       // tên hiển thị
                    StandardCharsets.UTF_8.name()
            );

            msg.setFrom(from);

        } catch (java.io.UnsupportedEncodingException e) {

            // fallback nếu encoding lỗi
            msg.setFrom(new InternetAddress(EmailConfig.SMTP_USERNAME));
        }

        // ===== TO =====
        msg.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toMail, false)
        );

        // ===== SUBJECT =====
        msg.setSubject(subject, StandardCharsets.UTF_8.name());

        // ===== BODY HTML =====
        MimeBodyPart body = new MimeBodyPart();

        body.setContent(
                htmlContent,
                "text/html; charset=UTF-8"
        );

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(body);

        msg.setContent(multipart);

        // ===== SEND =====
        Transport.send(msg);
    }
}