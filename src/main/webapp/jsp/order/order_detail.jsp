<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="MyCosmetic | Chi tiết đơn hàng" scope="request" />
<c:set var="pageCss" value="/order.css" scope="request" />

<section class="order-detail-page" style="padding: 32px 0;">
    <div class="container" style="max-width: 1180px; margin: 0 auto; padding: 0 16px;">

        <!-- HEADER -->
        <div style="display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 22px;">
            <div>
                <h1 style="margin: 0 0 8px; font-size: 28px; font-weight: 700;">
                    Chi tiết đơn hàng #${order.id}
                </h1>

                <div style="color: #666;">
                    Trạng thái:
                    <c:choose>
                        <c:when test="${order.status == 'completed'}">
              <span style="display: inline-block; padding: 5px 12px; border-radius: 999px; background: #e8f7ef; color: #12804a; font-weight: 600;">
                <c:out value="${order.statusLabel}" />
              </span>
                        </c:when>

                        <c:when test="${order.status == 'cancelled' || order.status == 'canceled'}">
              <span style="display: inline-block; padding: 5px 12px; border-radius: 999px; background: #fdecec; color: #c62828; font-weight: 600;">
                <c:out value="${order.statusLabel}" />
              </span>
                        </c:when>

                        <c:when test="${order.status == 'shipping'}">
              <span style="display: inline-block; padding: 5px 12px; border-radius: 999px; background: #eaf3ff; color: #1769aa; font-weight: 600;">
                <c:out value="${order.statusLabel}" />
              </span>
                        </c:when>

                        <c:otherwise>
              <span style="display: inline-block; padding: 5px 12px; border-radius: 999px; background: #fff6e5; color: #9a5b00; font-weight: 600;">
                <c:out value="${order.statusLabel}" />
              </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/account"
               style="display: inline-flex; align-items: center; justify-content: center; padding: 10px 16px; border-radius: 10px; border: 1px solid #ddd; color: #333; text-decoration: none; background: #fff;">
                Quay lại tài khoản
            </a>
        </div>

        <!-- ORDER INFO -->
        <div style="background: #fff; border: 1px solid #eee; border-radius: 18px; padding: 22px; margin-bottom: 22px; box-shadow: 0 8px 24px rgba(0,0,0,0.04);">
            <h2 style="margin: 0 0 16px; font-size: 20px;">Thông tin nhận hàng</h2>

            <div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px;">
                <div>
                    <div style="font-size: 13px; color: #777; margin-bottom: 4px;">Người nhận</div>
                    <div style="font-weight: 600;">
                        <c:out value="${order.fullName}" />
                    </div>
                </div>

                <div>
                    <div style="font-size: 13px; color: #777; margin-bottom: 4px;">Số điện thoại</div>
                    <div style="font-weight: 600;">
                        <c:out value="${order.phone}" />
                    </div>
                </div>

                <div style="grid-column: 1 / -1;">
                    <div style="font-size: 13px; color: #777; margin-bottom: 4px;">Địa chỉ giao hàng</div>
                    <div style="font-weight: 600;">
                        <c:out value="${order.address}" />
                    </div>
                </div>

                <div>
                    <div style="font-size: 13px; color: #777; margin-bottom: 4px;">Ngày đặt</div>
                    <div style="font-weight: 600;">
                        <fmt:formatDate value="${order.createdAtDate}" pattern="dd/MM/yyyy HH:mm" />
                    </div>
                </div>

                <div>
                    <div style="font-size: 13px; color: #777; margin-bottom: 4px;">Thanh toán</div>
                    <div style="font-weight: 600;">
                        <c:out value="${order.paymentMethod}" />
                        -
                        <c:out value="${order.paymentStatus}" />
                    </div>
                </div>

                <c:if test="${not empty order.vnpTxnRef}">
                    <div style="grid-column: 1 / -1;">
                        <div style="font-size: 13px; color: #777; margin-bottom: 4px;">Mã giao dịch VNPAY</div>
                        <div style="font-weight: 600;">
                            <c:out value="${order.vnpTxnRef}" />
                        </div>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- ORDER ITEMS -->
        <div style="background: #fff; border: 1px solid #eee; border-radius: 18px; padding: 22px; margin-bottom: 22px; box-shadow: 0 8px 24px rgba(0,0,0,0.04);">
            <h2 style="margin: 0 0 16px; font-size: 20px;">Sản phẩm đã mua</h2>

            <%--
              Hỗ trợ cả hai tên attribute:
              - orderItems
              - items
            --%>
            <c:set var="displayItems" value="${orderItems}" />

            <c:if test="${empty displayItems && not empty items}">
                <c:set var="displayItems" value="${items}" />
            </c:if>

            <c:choose>
                <c:when test="${empty displayItems}">
                    <div style="padding: 18px; border-radius: 12px; background: #f8f8f8; color: #777;">
                        Đơn hàng chưa có sản phẩm hoặc chưa load được chi tiết sản phẩm.
                    </div>
                </c:when>

                <c:otherwise>
                    <div style="overflow-x: auto;">
                        <table style="width: 100%; border-collapse: collapse; min-width: 760px;">
                            <thead>
                            <tr style="border-bottom: 1px solid #eee;">
                                <th style="text-align: left; padding: 12px 8px; width: 78px;">Ảnh</th>
                                <th style="text-align: left; padding: 12px 8px;">Sản phẩm</th>
                                <th style="text-align: left; padding: 12px 8px; width: 210px;">Biến thể</th>
                                <th style="text-align: right; padding: 12px 8px; width: 130px;">Đơn giá</th>
                                <th style="text-align: center; padding: 12px 8px; width: 80px;">SL</th>
                                <th style="text-align: right; padding: 12px 8px; width: 150px;">Thành tiền</th>
                            </tr>
                            </thead>

                            <tbody>
                            <c:forEach var="item" items="${displayItems}">
                                <tr style="border-bottom: 1px solid #f1f1f1;">
                                    <td style="padding: 14px 8px;">
                                        <c:choose>
                                            <c:when test="${not empty item.imageUrl}">
                                                <img src="${pageContext.request.contextPath}${item.imageUrl}"
                                                     alt="${item.productName}"
                                                     style="width: 58px; height: 58px; object-fit: cover; border-radius: 12px; border: 1px solid #eee;" />
                                            </c:when>

                                            <c:otherwise>
                                                <div style="width: 58px; height: 58px; border-radius: 12px; background: #f1f1f1; display: flex; align-items: center; justify-content: center; color: #999;">
                                                    —
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 8px;">
                                        <div style="font-weight: 700; color: #222;">
                                            <c:out value="${item.productName}" />
                                        </div>

                                        <div style="font-size: 13px; color: #888; margin-top: 4px;">
                                            Mã sản phẩm:
                                            <c:out value="${item.productId}" />
                                        </div>
                                    </td>

                                    <td style="padding: 14px 8px;">
                                        <c:choose>
                                            <c:when test="${not empty item.variantId
                                        || not empty item.variantName
                                        || not empty item.variantSize
                                        || not empty item.variantType}">
                                                <div style="display: inline-block; padding: 4px 10px; border-radius: 999px; background: #f4edf7; color: #8b4aa8; font-size: 12px; font-weight: 700; margin-bottom: 8px;">
                                                    Có biến thể
                                                </div>

                                                <div style="font-size: 13px; color: #444; line-height: 1.7;">
                                                    <c:if test="${not empty item.variantName}">
                                                        <div>
                                                            <strong>Tên:</strong>
                                                            <c:out value="${item.variantName}" />
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantSize}">
                                                        <div>
                                                            <strong>Size:</strong>
                                                            <c:out value="${item.variantSize}" />
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantType}">
                                                        <div>
                                                            <strong>Loại:</strong>
                                                            <c:out value="${item.variantType}" />
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${not empty item.variantId}">
                                                        <div style="color: #888;">
                                                            Variant ID:
                                                            <c:out value="${item.variantId}" />
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </c:when>

                                            <c:otherwise>
                                                <span style="color: #888;">Không có biến thể</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td style="padding: 14px 8px; text-align: right;">
                                        <fmt:formatNumber value="${item.price}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0" />
                                        ₫
                                    </td>

                                    <td style="padding: 14px 8px; text-align: center;">
                                        <c:out value="${item.quantity}" />
                                    </td>

                                    <td style="padding: 14px 8px; text-align: right; font-weight: 700;">
                                        <fmt:formatNumber value="${item.subtotal}"
                                                          type="number"
                                                          groupingUsed="true"
                                                          minFractionDigits="0"
                                                          maxFractionDigits="0" />
                                        ₫
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- PAYMENT SUMMARY -->
        <div style="background: #fff; border: 1px solid #eee; border-radius: 18px; padding: 22px; box-shadow: 0 8px 24px rgba(0,0,0,0.04);">
            <h2 style="margin: 0 0 16px; font-size: 20px;">Tổng kết thanh toán</h2>

            <div style="max-width: 460px; margin-left: auto;">
                <div style="display: flex; justify-content: space-between; gap: 16px; padding: 8px 0; color: #555;">
                    <span>Giảm giá coupon</span>
                    <strong>
                        -
                        <fmt:formatNumber value="${empty order.couponDiscount ? 0 : order.couponDiscount}"
                                          type="number"
                                          groupingUsed="true"
                                          minFractionDigits="0"
                                          maxFractionDigits="0" />
                        ₫
                    </strong>
                </div>

                <c:if test="${not empty order.shippingFee}">
                    <div style="display: flex; justify-content: space-between; gap: 16px; padding: 8px 0; color: #555;">
                        <span>Phí vận chuyển</span>
                        <strong>
                            <fmt:formatNumber value="${order.shippingFee}"
                                              type="number"
                                              groupingUsed="true"
                                              minFractionDigits="0"
                                              maxFractionDigits="0" />
                            ₫
                        </strong>
                    </div>
                </c:if>

                <div style="display: flex; justify-content: space-between; gap: 16px; padding: 14px 0 0; border-top: 1px solid #eee; font-size: 20px;">
                    <span style="font-weight: 700;">Tổng tiền</span>
                    <strong style="color: #b56a7a;">
                        <c:choose>
                            <c:when test="${not empty order.totalVnd}">
                                <fmt:formatNumber value="${order.totalVnd * 1000}"
                                                  type="number"
                                                  groupingUsed="true"
                                                  minFractionDigits="0"
                                                  maxFractionDigits="0" />
                            </c:when>
                            <c:otherwise>
                                <fmt:formatNumber value="${order.total}"
                                                  type="number"
                                                  groupingUsed="true"
                                                  minFractionDigits="0"
                                                  maxFractionDigits="0" />
                            </c:otherwise>
                        </c:choose>
                        ₫
                    </strong>
                </div>
            </div>
        </div>

    </div>
</section>