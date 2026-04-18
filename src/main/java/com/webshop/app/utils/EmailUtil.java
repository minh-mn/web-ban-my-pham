package com.webshop.app.utils;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;


public class EmailUtil {

    private static Session createSession() {
        Properties props = new Properties();

        // ===== Gmail SMTP (TLS 587) =====
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");   // bắt buộc TLS
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(EmailConfig.SMTP_PORT));

        // Timeout để không treo
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        EmailConfig.SMTP_USERNAME,
                        EmailConfig.SMTP_APP_PASSWORD
                );
            }
        });

        // ===== BẬT DEBUG SMTP (tạm thời để xem lỗi rõ) =====
        session.setDebug(true);

        return session;
    }

    public static void sendHtml(String toMail, String subject, String htmlContent) throws MessagingException {
        Session session = createSession();
        MimeMessage msg = new MimeMessage(session);

        // ===== From: nên trùng username Gmail để tránh SendAsDenied =====
        try {
            InternetAddress from = new InternetAddress(
                    EmailConfig.SMTP_USERNAME,               // dùng username làm from
                    EmailConfig.FROM_NAME,
                    StandardCharsets.UTF_8.name()
            );
            msg.setFrom(from);
        } catch (java.io.UnsupportedEncodingException e) {
            msg.setFrom(new InternetAddress(EmailConfig.SMTP_USERNAME));
        }

        // ===== To =====
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toMail, false));
        msg.setSubject(subject, StandardCharsets.UTF_8.name());

        // ===== Body HTML =====
        MimeBodyPart body = new MimeBodyPart();
        body.setContent(htmlContent, "text/html; charset=UTF-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(body);
        msg.setContent(multipart);

        Transport.send(msg);
    }
}
