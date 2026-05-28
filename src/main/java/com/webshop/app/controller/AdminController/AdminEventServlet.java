package com.webshop.app.controller.AdminController;

import com.webshop.app.dao.EventDAO;
import com.webshop.app.model.Event;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/admin/events")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
        maxFileSize = 1024 * 1024 * 10,       // 10MB
        maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class AdminEventServlet extends HttpServlet {
    private EventDAO eventDAO = new EventDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                request.setAttribute("mode", "create");
                request.getRequestDispatcher("/jsp/admin/event/event_form.jsp").forward(request, response);
                break;

            case "edit":
                try {
                    int id = Integer.parseInt(request.getParameter("id"));
                    Event event = eventDAO.getEventById(id);
                    request.setAttribute("event", event);
                    request.setAttribute("mode", "edit");
                    request.getRequestDispatcher("/jsp/admin/event/event_form.jsp").forward(request, response);
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/admin/events?error=InvalidID");
                }
                break;

            case "list":
            default:
                String searchQuery = request.getParameter("q");
                List<Event> list;
                if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                    list = eventDAO.searchEvents(searchQuery);
                } else {
                    list = eventDAO.getAllEvents();
                }
                request.setAttribute("events", list);
                request.getRequestDispatcher("/jsp/admin/event/event_list.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // 1. Logic xử lý XÓA SỰ KIỆN
        if ("delete".equals(action)) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                eventDAO.deleteEvent(id);
                response.sendRedirect(request.getContextPath() + "/admin/events");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // 2. Logic xử lý THÊM HOẶC CẬP NHẬT
        try {
            String title = request.getParameter("title");
            String summary = request.getParameter("summary");
            String tag = request.getParameter("tag");
            String dateStr = request.getParameter("eventDate");

            // CHỐNG LỖI NGẦM: Bắt lỗi nếu form gửi lên trống trơn (do sai Multipart hoặc Filter chặn)
            if (title == null || dateStr == null) {
                throw new Exception("Không nhận được dữ liệu. Vui lòng kiểm tra lại cấu hình Form hoặc các trường bắt buộc!");
            }

            Date eventDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            String imageUrl = null;

            // Xử lý đọc file ảnh
            Part filePart = request.getPart("imageFile");
            if (filePart != null && filePart.getSize() > 0) {
                String originalName = getFileName(filePart);
                String fileName = System.currentTimeMillis() + "_" + originalName;

                String uploadPath = getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "events";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                filePart.write(uploadPath + File.separator + fileName);
                imageUrl = "/assets/images/events/" + fileName;
            }

            boolean isSuccess = false;

            // Xử lý lưu DB
            if ("create".equals(action)) {
                Event event = new Event(title, summary, tag, imageUrl, eventDate);
                isSuccess = eventDAO.insertEvent(event);
            } else if ("update".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Event oldEvent = eventDAO.getEventById(id);

                // Giữ lại ảnh cũ nếu không update ảnh mới
                if (oldEvent != null && (imageUrl == null || imageUrl.isEmpty())) {
                    imageUrl = oldEvent.getImageUrl();
                }

                Event event = new Event(title, summary, tag, imageUrl, eventDate);
                event.setId(id);
                isSuccess = eventDAO.updateEvent(event);
            }

            // KIỂM TRA KẾT QUẢ
            if (isSuccess) {
                response.sendRedirect(request.getContextPath() + "/admin/events");
                return; // Thành công thì về trang list
            } else {
                // Thất bại ném văng ra lỗi để catch xử lý
                throw new Exception("Lỗi Database: Lưu dữ liệu không thành công. Hãy kiểm tra lại kết nối MySQL!");
            }

        } catch (Exception e) {
            e.printStackTrace(); // In ra console cho lập trình viên

            // Trả lỗi về GIAO DIỆN FORM thay vì gọi doGet
            request.setAttribute("error", "Chi tiết lỗi: " + e.getMessage());
            request.setAttribute("mode", "create".equals(action) ? "create" : "edit");

            // CHỈ ĐỊNH ĐÍCH DANH FILE FORM ĐỂ HIỂN THỊ LỖI
            request.getRequestDispatcher("/jsp/admin/event/event_form.jsp").forward(request, response);
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "default.jpg";
    }
}