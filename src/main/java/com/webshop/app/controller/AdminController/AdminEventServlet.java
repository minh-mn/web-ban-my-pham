package com.webshop.app.controller.AdminController;

import com.webshop.app.config.UploadConfig;
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
    private final EventDAO eventDAO = new EventDAO();

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
                    if (event != null) {
                        request.setAttribute("event", event);
                        request.setAttribute("mode", "edit");
                        request.getRequestDispatcher("/jsp/admin/event/event_form.jsp").forward(request, response);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/admin/events?error=NotFound");
                    }
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
                response.sendRedirect(request.getContextPath() + "/admin/events?error=DeleteFailed");
            }
            return;
        }

        // 2. Logic xử lý THÊM HOẶC CẬP NHẬT
        String title = null;
        String summary = null;
        String tag = null;
        String dateStr = null;

        try {
            title = request.getParameter("title");
            summary = request.getParameter("summary");
            tag = request.getParameter("tag");
            dateStr = request.getParameter("eventDate");

            // CHỐNG LỖI NGẦM: Kiểm tra dữ liệu bắt buộc từ Form gửi lên
            if (title == null || dateStr == null || title.trim().isEmpty() || dateStr.trim().isEmpty()) {
                throw new Exception("Vui lòng điền đầy đủ các thông tin bắt buộc (*)");
            }

            Date eventDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            String imageUrl = null;

            // Xử lý đọc file ảnh trực tiếp từ máy tính
            Part filePart = request.getPart("imageFile");
            if (filePart != null && filePart.getSize() > 0) {
                String originalName = filePart.getSubmittedFileName();
                if (originalName == null || originalName.isEmpty()) {
                    originalName = "event_image_" + System.currentTimeMillis() + ".jpg";
                }

                // Ghi file sử dụng cấu hình mới
                // UploadConfig.resolveEventFile trả về đường dẫn tuyệt đối chuẩn xác
                String filePath = UploadConfig.resolveEventFile(originalName).toString();
                filePart.write(filePath);

                // Lấy URL lưu vào database
                imageUrl = UploadConfig.toEventUrl(originalName);
            }

            boolean isSuccess = false;

            if ("create".equals(action)) {
                Event event = new Event(title, summary, tag, imageUrl, eventDate);
                isSuccess = eventDAO.insertEvent(event);
            } else if ("update".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Event oldEvent = eventDAO.getEventById(id);

                // Giữ lại đường dẫn ảnh cũ nếu người dùng không upload ảnh mới thay thế
                if (imageUrl == null || imageUrl.isEmpty()) {
                    if (oldEvent != null) {
                        imageUrl = oldEvent.getImageUrl();
                    }
                }

                Event event = new Event(title, summary, tag, imageUrl, eventDate);
                event.setId(id);
                isSuccess = eventDAO.updateEvent(event);
            }

            if (isSuccess) {
                response.sendRedirect(request.getContextPath() + "/admin/events");
            } else {
                throw new Exception("Lỗi hệ thống: Không thể lưu dữ liệu vào cơ sở dữ liệu MySQL.");
            }

        } catch (Exception e) {
            e.printStackTrace();

            // GIỮ LẠI DỮ LIỆU ĐÃ NHẬP: Tạo đối tượng tạm để nạp lại vào form tránh mất thông tin của user
            Event fallbackEvent = new Event();
            fallbackEvent.setTitle(title);
            fallbackEvent.setSummary(summary);
            fallbackEvent.setTag(tag);
            try {
                if (dateStr != null && !dateStr.trim().isEmpty()) {
                    fallbackEvent.setEventDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr));
                }
            } catch (Exception ignored) {}

            // Nếu đang ở chế độ sửa, lấy lại ID và ảnh cũ của sự kiện
            if ("update".equals(action)) {
                try {
                    int id = Integer.parseInt(request.getParameter("id"));
                    fallbackEvent.setId(id);
                    Event old = eventDAO.getEventById(id);
                    if (old != null && (fallbackEvent.getImageUrl() == null)) {
                        fallbackEvent.setImageUrl(old.getImageUrl());
                    }
                } catch (Exception ignored) {}
            }

            // Gửi dữ liệu tạm và thông điệp lỗi quay lại trang giao diện form
            request.setAttribute("event", fallbackEvent);
            request.setAttribute("error", "Không thể lưu sự kiện. " + e.getMessage());
            request.setAttribute("mode", "create".equals(action) ? "create" : "edit");
            request.getRequestDispatcher("/jsp/admin/event/event_form.jsp").forward(request, response);
        }
    }
}
