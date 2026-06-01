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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

        if (!"google".equalsIgnoreCase(provider) && !"facebook".equalsIgnoreCase(provider)) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Provider không hỗ trợ\"}");
            return;
        }

        try {
            String email = null;
            String socialId = null;
            String fullName = null;

            if ("google".equalsIgnoreCase(provider)) {
                GoogleIdToken.Payload payload = verifyGoogleCredential(credential);
                email = payload.getEmail();
                socialId = payload.getSubject();
                fullName = (String) payload.get("name");
            }
            else if ("facebook".equalsIgnoreCase(provider)) {
                String accessToken = req.getParameter("accessToken");
                JsonObject fbUser = verifyFacebookToken(accessToken);

                socialId = fbUser.get("id").getAsString();
                fullName = fbUser.has("name") ? fbUser.get("name").getAsString() : "Người dùng Facebook";

                if (fbUser.has("email")) {
                    email = fbUser.get("email").getAsString();
                } else {
                    email = socialId + "@facebook.com";
                }
            }

            if (email == null || email.isBlank()) {
                resp.getWriter().write("{\"status\":\"error\",\"message\":\"Không lấy được email từ " + provider + "\"}");
                return;
            }

            if ("register".equalsIgnoreCase(mode)) {
                handleRegister(req, resp, email, fullName, socialId, provider);
            } else {
                handleLogin(req, resp, email, fullName, socialId, provider);
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

    // =========================================================================
    // HÀM XỬ LÝ ĐĂNG KÝ (TỰ ĐỘNG CHUYỂN ĐĂNG NHẬP NẾU TỒN TẠI TÀI KHOẢN)
    // =========================================================================
    private void handleRegister(
            HttpServletRequest req,
            HttpServletResponse resp,
            String email,
            String fullName,
            String socialId,
            String provider
    ) throws IOException, MessagingException {

        User existingUser = userDAO.findByEmail(email);

        if (existingUser != null) {
            handleLogin(req, resp, email, fullName, socialId, provider);
            return;
        }

        HttpSession session = req.getSession();
        User pendingUser = new User();

        pendingUser.setUsername(email);
        pendingUser.setEmail(email);
        pendingUser.setFullName(fullName);
        pendingUser.setActive(true);

        session.setAttribute("pendingUser", pendingUser);
        session.setAttribute("pendingProvider", provider);
        session.setAttribute("pendingSocialId", socialId);

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
                "{\"status\":\"otp_required\",\"redirectUrl\":\"/verify-registration\",\"message\":\"Mã OTP đã được gửi về email của bạn.\"}"
        );
    }

    // =========================================================================
    // HÀM XỬ LÝ ĐĂNG NHẬP
    // =========================================================================
    private void handleLogin(
            HttpServletRequest req,
            HttpServletResponse resp,
            String email,
            String fullName,
            String socialId,
            String provider
    ) throws IOException {

        User user = userDAO.findBySocialId(provider, socialId);

        if (user == null) {
            user = userDAO.findByEmail(email);

            if (user == null) {
                User newUser = new User();
                newUser.setUsername(email);
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setActive(true);

                userDAO.saveSocialUser(newUser, provider, socialId);
                user = userDAO.findBySocialId(provider, socialId);

            } else {
                userDAO.updateSocialId(user.getId(), provider, socialId);
            }
        }
        
        HttpSession session = req.getSession();
        session.setAttribute("user", user);

        resp.getWriter().write(
                "{\"status\":\"success\",\"redirectUrl\":\"/home\",\"message\":\"Đăng nhập thành công!\"}"
        );
    }

    private JsonObject verifyFacebookToken(String accessToken) throws IOException {
        String urlString = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Facebook token không hợp lệ hoặc đã hết hạn");
        }

        Scanner scanner = new Scanner(url.openStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        return JsonParser.parseString(response.toString()).getAsJsonObject();
    }
}
