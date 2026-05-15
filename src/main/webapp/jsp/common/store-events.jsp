<%--
  Created by IntelliJ IDEA.
  User: ASUS
  Date: 26/04/2026
  Time: 7:58 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<section class="section store-events">
  <div class="container">
    <div class="section-header">
      <h2 class="section-title">SỰ KIỆN CỬA HÀNG</h2>
      <a href="#" class="view-all">Xem tất cả ›</a>
    </div>

    <div class="event-grid">
      <div class="event-card">
        <div class="event-img">
          <img src="${pageContext.request.contextPath}/assets/images/events/event1.jpg" alt="Workshop">
          <div class="event-date">
            <span class="day">28</span>
            <span class="month">Th4</span>
          </div>
        </div>
        <div class="event-content">
          <span class="event-tag">Workshop</span>
          <h3>Bí kíp chăm sóc da mùa hè cùng chuyên gia</h3>
          <p>Tham gia buổi chia sẻ miễn phí về cách bảo vệ làn da dưới ánh nắng...</p>
          <a href="#" class="btn-event">Đăng ký tham gia</a>
        </div>
      </div>

      <div class="event-card">
        <div class="event-img">
          <img src="${pageContext.request.contextPath}/assets/images/events/event2.jpg" alt="Grand Opening">
          <div class="event-date">
            <span class="day">05</span>
            <span class="month">Th5</span>
          </div>
        </div>
        <div class="event-content">
          <span class="event-tag">Khai trương</span>
          <h3>Khai trương chi nhánh thứ 10 tại Quận 1</h3>
          <p>Nhận ngay bộ quà tặng trị giá 500k cho 100 khách hàng đầu tiên...</p>
          <a href="#" class="btn-event">Xem chi tiết</a>
        </div>
      </div>
    </div>
  </div>
</section>