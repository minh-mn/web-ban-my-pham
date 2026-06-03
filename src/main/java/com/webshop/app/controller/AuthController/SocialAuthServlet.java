package com.webshop.app.controller.AuthController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/social-auth")
public class SocialAuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    // Giữ nguyên Client ID của bạn
    private static final String GOOGLE_CLIENT_ID = "78979081819-fo21lsm5idv3pp22779bais8l1f5csnm.apps.googleusercontent.com";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        System.out.println("Đã nhận được request tại SocialAuthServlet!");
        System.out.println("Provider: " + req.getParameter("provider"));

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String provider = req.getParameter("provider");
        String credential = req.getParameter("credential");

        if (!"google".equalsIgnoreCase(provider) && !"facebook".equalsIgnoreCase(provider)) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Provider không hỗ trợ\"}");
            return;
        }

        try {
            String email = null;
            String socialId = null;
            String fullName = null;

            // 1. Xác thực và lấy thông tin từ Google/Facebook
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

            // 2. Gom tất cả về một hàm xử lý tài khoản thông minh (Bỏ phân biệt register/login qua OTP)
            handleSocialAuthUnified(req, resp, email, fullName, socialId, provider);

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(
                    "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "") + "\"}"
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
    // HÀM XỬ LÝ ĐĂNG NHẬP & GỘP TÀI KHOẢN TỰ ĐỘNG (UNIFIED FLOW)
    // =========================================================================
    private void handleSocialAuthUnified(
            HttpServletRequest req,
            HttpServletResponse resp,
            String email,
            String fullName,
            String socialId,
            String provider
    ) throws IOException {

        // Bước 1: Kiểm tra xem ID mạng xã hội này đã từng liên kết với tài khoản nào chưa
        User user = userDAO.findBySocialId(provider, socialId);

        if (user == null) {
            // Bước 2: Nếu chưa kết nối Social ID, tìm tài khoản theo Email hệ thống
            user = userDAO.findByEmail(email);

            if (user == null) {
                // TRƯỜNG HỢP A: Email chưa từng tồn tại -> Tạo mới tài khoản hoàn toàn
                User newUser = new User();
                newUser.setUsername(email);
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setActive(true);

                // Lưu ý: Không set giá trị password tại đây để đảm bảo password lưu xuống DB là NULL 
                // (Hãy kiểm tra hàm userDAO.saveSocialUser của bạn xem có đang truyền trực tiếp giá trị NULL vào câu lệnh INSERT SQL không)
                userDAO.saveSocialUser(newUser, provider, socialId);

                // Lấy lại thông tin user vừa tạo
                user = userDAO.findBySocialId(provider, socialId);
            } else {
                // TRƯỜNG HỢP B: Email ĐÃ TỒN TẠI trên hệ thống (Do đăng ký thường hoặc social khác trước đó)
                // Tiến hành cập nhật thêm social_id (google_id hoặc facebook_id) vào chính tài khoản đó mà KHÔNG ĐỔI mật khẩu cũ.
                userDAO.updateSocialId(user.getId(), provider, socialId);

                // Đọc lại dữ liệu mới nhất sau khi cập nhật liên kết
                user = userDAO.findByEmail(email);
            }
        }

        // Bước 3: Kiểm tra trạng thái hoạt động của tài khoản
        if (user != null && !user.isActive()) {
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Tài khoản của bạn hiện đang bị khóa!\"}");
            return;
        }

        // Bước 4: Đăng nhập thành công và ghi nhận Session giống luồng cũ của bạn
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

        try (Scanner scanner = new Scanner(url.openStream())) {
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            return JsonParser.parseString(response.toString()).getAsJsonObject();
        }
    }
}
