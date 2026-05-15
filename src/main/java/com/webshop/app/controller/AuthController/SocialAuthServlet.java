package com.webshop.app.controller.AuthController;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.webshop.app.dao.SocialUserInfo;
import com.webshop.app.dao.UserDAO;
import com.webshop.app.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@WebServlet("/social-auth")
public class SocialAuthServlet extends HttpServlet {

    // Khai báo DAO để fix lỗi "Cannot resolve symbol userDAO"
    private UserDAO userDAO = new UserDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String provider = request.getParameter("provider");
        String token = request.getParameter("token");
        SocialUserInfo socialInfo = null;

        try {
            if ("google".equals(provider)) {
                socialInfo = verifyGoogle(token);
            } else if ("facebook".equals(provider)) {
                socialInfo = verifyFacebook(token);
            }

            if (socialInfo != null) {
                processUser(socialInfo, provider, request, response);
            } else {
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Không lấy được thông tin từ " + provider + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}");
        }
    }

    private SocialUserInfo verifyGoogle(String accessToken) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request req = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken)
                .build();

        try (Response res = client.newCall(req).execute()) {
            JsonNode node = new ObjectMapper().readTree(res.body().string());
            return new SocialUserInfo(
                    node.get("sub").asText(),
                    node.get("email").asText(),
                    node.get("name").asText(),
                    node.has("picture") ? node.get("picture").asText() : ""
            );
        }
    }

    // Bổ sung hàm verifyFacebook
    private SocialUserInfo verifyFacebook(String accessToken) {
        FacebookClient fbClient = new DefaultFacebookClient(accessToken, Version.LATEST);
        com.restfb.types.User fbUser = fbClient.fetchObject("me", com.restfb.types.User.class,
                com.restfb.Parameter.with("fields", "id,name,email"));

        return new SocialUserInfo(fbUser.getId(), fbUser.getEmail(), fbUser.getName(), "");
    }

    private void processUser(SocialUserInfo info, String provider, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = userDAO.findBySocialId(provider, info.getId());

        if (user == null) {
            // TÌM THEO EMAIL ĐỂ XEM CÓ TÀI KHOẢN TRƯỚC ĐÓ CHƯA
            user = userDAO.findByEmail(info.getEmail());

            if (user == null) {
                // ĐÂY LÀ ĐĂNG KÝ (Tạo mới)
                user = new User();
                user.setUsername(info.getEmail());
                user.setFullName(info.getName());
                user.setEmail(info.getEmail());
                userDAO.saveSocialUser(user, provider, info.getId());
                // Lấy lại user sau khi save để có ID
                user = userDAO.findBySocialId(provider, info.getId());
            } else {
                // ĐÂY LÀ LIÊN KẾT (Tài khoản đã có, giờ cập nhật thêm Google/FB ID)
                userDAO.updateSocialId(user.getId(), provider, info.getId());
            }
        }

        // ĐĂNG NHẬP (Lưu vào session)
        req.getSession().setAttribute("user", user);
        resp.getWriter().write("{\"status\":\"success\",\"redirectUrl\":\"/index.jsp\"}");
    }
}