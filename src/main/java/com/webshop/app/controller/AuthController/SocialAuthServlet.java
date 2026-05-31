package com.webshop.app.controller.AuthController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import com.webshop.app.utils.EmailUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Random;

@WebServlet("/social-auth")
public class SocialAuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    private static final String GOOGLE_CLIENT_ID = "78979081819-fo21lsm5idv3pp22779bais8l1f5csnm.apps.googleusercontent.com";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String provider = req.getParameter("provider");
        String credential = req.getParameter("credential");
        String mode = req.getParameter("mode");

        if (!"google".equalsIgnoreCase(provider)) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Provider không hỗ trợ\"}");
            return;
        }

        try {

            GoogleIdToken.Payload payload = verifyGoogleCredential(credential);

            String email = payload.getEmail();
            String googleId = payload.getSubject();
            String fullName = (String) payload.get("name");

            if (email == null || email.isBlank()) {
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Không lấy được email\"}");
                return;
            }

            if ("register".equalsIgnoreCase(mode)) {
                handleRegister(req, resp, email, fullName, googleId);
            } else {
                handleLogin(req, resp, email, fullName, googleId);
            }

        } catch (Exception e) {
            e.printStackTrace();

            resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"" +
                            e.getMessage().replace("\"", "") +
                            "\"}"
            );
        }
    }

    private GoogleIdToken.Payload verifyGoogleCredential(String credential)
            throws GeneralSecurityException, IOException {

        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                transport,
                JacksonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(credential);

        if (idToken == null) {
            throw new RuntimeException("Google token không hợp lệ");
        }

        return idToken.getPayload();
    }

    private void handleLogin(
            HttpServletRequest req,
            HttpServletResponse resp,
            String email,
            String fullName,
            String googleId
    ) throws IOException {

        User user = userDAO.findBySocialId("google", googleId);

        if (user == null) {

            user = userDAO.findByEmail(email);

            if (user == null) {

                User newUser = new User();
                newUser.setUsername(email);
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setActive(true);

                userDAO.saveSocialUser(newUser, "google", googleId);

                user = userDAO.findBySocialId("google", googleId);

            } else {

                userDAO.updateSocialId(user.getId(), "google", googleId);
            }
        }

        HttpSession session = req.getSession();
        session.setAttribute("user", user);

        resp.getWriter().write(
                "{\"status\":\"success\",\"redirectUrl\":\"/home\"}"
        );
    }

    private void handleRegister(
            HttpServletRequest req,
            HttpServletResponse resp,
            String email,
            String fullName,
            String googleId
    ) throws IOException, MessagingException {

        User existingUser = userDAO.findByEmail(email);
        if (existingUser != null) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Email này đã được đăng ký. Vui lòng chọn Đăng nhập thay vì Đăng ký.\"}");
            return;
        }

        HttpSession session = req.getSession();

        User pendingUser = new User();

        pendingUser.setUsername(email);
        pendingUser.setEmail(email);
        pendingUser.setFullName(fullName);
        pendingUser.setActive(true);

        session.setAttribute("pendingUser", pendingUser);
        session.setAttribute("pendingProvider", "google");
        session.setAttribute("pendingSocialId", googleId);

        String otp = String.format("%06d", new Random().nextInt(999999));

        session.setAttribute("REGISTER_OTP", otp);
        session.setAttribute("OTP_TIME", System.currentTimeMillis());

        String subject = "Mã OTP xác thực tài khoản";

        String content = """
                <h2>Xác thực tài khoản</h2>
                <p>Mã OTP của bạn:</p>
                <h1 style='color:#e91e63'>%s</h1>
                <p>Mã có hiệu lực 5 phút.</p>
                """.formatted(otp);

        EmailUtil.sendHtml(email, subject, content);

        resp.getWriter().write(
                "{\"status\":\"otp_required\",\"redirectUrl\":\"/verify-registration\"}"
        );
    }
}
