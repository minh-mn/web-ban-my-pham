<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="${mode eq 'edit' ? 'ADMIN | Sửa danh mục' : 'ADMIN | Thêm danh mục'}" scope="request"/>
<c:set var="activeMenu" value="categories" scope="request"/>
<c:set var="pageCss" value="/assets/css/admin/admin-form.css" scope="request"/>

<jsp:include page="/jsp/admin/layout/header.jsp"/>
<jsp:include page="/jsp/admin/layout/sidebar.jsp"/>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${mode eq 'edit'}" />

<main class="admin-main">
  <div class="admin-container admin-category-form-page">

    <section class="admin-category-form-hero">
      <div class="admin-category-form-hero__content">
        <span class="admin-category-form-eyebrow">SẢN PHẨM &amp; DANH MỤC</span>
        <h1 class="admin-category-form-title">
          <c:choose>
            <c:when test="${isEdit}">Sửa danh mục</c:when>
            <c:otherwise>Thêm danh mục</c:otherwise>
          </c:choose>
        </h1>

        <p class="admin-category-form-subtitle">
          <c:choose>
            <c:when test="${isEdit}">
              Cập nhật thông tin danh mục, trạng thái hiển thị, quan hệ danh mục cha/con và các thẻ hiển thị trên trang sản phẩm.
            </c:when>
            <c:otherwise>
              Tạo danh mục mới để phân loại sản phẩm và thêm thẻ hiển thị trên trang sản phẩm.
            </c:otherwise>
          </c:choose>
        </p>
      </div>

      <div class="admin-category-form-hero__actions">
        <a class="admin-btn" href="${ctx}/admin/categories">
          ← Quay lại danh sách
        </a>
      </div>
    </section>

    <c:if test="${not empty error}">
      <div class="admin-alert admin-alert--danger">
        <c:out value="${error}" />
      </div>
    </c:if>

    <form method="post"
          action="${ctx}/admin/categories"
          class="admin-form admin-category-form">

      <%@ include file="/jsp/common/csrf.jspf" %>

      <input type="hidden"
             name="action"
             value="${isEdit ? 'update' : 'create'}">

      <c:if test="${isEdit}">
        <input type="hidden" name="id" value="${category.id}">
      </c:if>

      <div class="admin-category-form-layout">

        <section class="admin-card admin-category-form-card">
          <div class="admin-card__body">
            <div class="admin-category-form-section-head">
              <div>
                <h2 class="admin-category-form-section-title">Thông tin danh mục</h2>
                <p class="admin-category-form-section-desc">
                  Nhập tên, slug, trạng thái hiển thị và quan hệ cha/con của danh mục.
                </p>
              </div>

              <c:choose>
                <c:when test="${isEdit}">
                  <span class="admin-chip admin-chip--brand">Đang chỉnh sửa</span>
                </c:when>
                <c:otherwise>
                  <span class="admin-chip admin-chip--success">Danh mục mới</span>
                </c:otherwise>
              </c:choose>
            </div>

            <div class="admin-category-form-grid">
              <div class="admin-field">
                <label class="admin-label" for="categoryName">
                  Tên danh mục <span class="admin-required">*</span>
                </label>

                <input class="admin-input"
                       id="categoryName"
                       type="text"
                       name="name"
                       value="${category.name}"
                       placeholder="VD: Chăm sóc da, Trang điểm, Chống nắng..."
                       maxlength="100"
                       required>

                <div class="admin-help">
                  Tên danh mục sẽ được hiển thị ở trang quản trị và trang sản phẩm.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="categoryActive">Trạng thái</label>

                <select class="admin-select" id="categoryActive" name="active">
                  <option value="1" ${not isEdit || category.active ? 'selected' : ''}>
                    Đang hiển thị
                  </option>
                  <option value="0" ${isEdit && not category.active ? 'selected' : ''}>
                    Tạm ẩn
                  </option>
                </select>

                <div class="admin-help">
                  Khi tắt trạng thái, danh mục sẽ bị ẩn khỏi giao diện khách hàng.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="categorySlug">Slug danh mục</label>

                <input class="admin-input"
                       id="categorySlug"
                       type="text"
                       name="slug"
                       value="${category.slug}"
                       placeholder="VD: cham-soc-da"
                       maxlength="150"
                       pattern="[a-z0-9]+(-[a-z0-9]+)*">

                <div class="admin-help">
                  Nếu để trống, hệ thống sẽ tự tạo slug từ tên danh mục.
                  Ví dụ: <strong>Chăm sóc da</strong> → <strong>cham-soc-da</strong>.
                </div>
              </div>

              <div class="admin-field">
                <label class="admin-label" for="categoryParent">Danh mục cha</label>

                <select class="admin-select" id="categoryParent" name="parentId">
                  <option value="">Không có, đây là danh mục cha</option>

                  <c:forEach var="p" items="${parentCategories}">
                    <option value="${p.id}"
                      ${not empty category.parentId && category.parentId == p.id ? 'selected' : ''}>
                      <c:out value="${p.name}" />
                      <c:if test="${not p.active}"> - Tạm ẩn</c:if>
                    </option>
                  </c:forEach>
                </select>

                <div class="admin-help">
                  Nếu chọn danh mục cha, danh mục hiện tại sẽ trở thành danh mục con.
                </div>
              </div>
            </div>

            <div class="admin-category-form-note">
              <span class="admin-category-form-note__icon">💡</span>
              <div>
                <strong>Gợi ý cấu trúc</strong>
                <span>Nên dùng danh mục cha cho nhóm lớn như Chăm sóc da, Trang điểm; danh mục con dùng cho nhóm nhỏ như Sữa rửa mặt, Kem chống nắng.</span>
              </div>
            </div>
          </div>
        </section>

        <aside class="admin-card admin-category-info-card">
          <div class="admin-card__body">
            <div class="admin-category-form-section-head">
              <div>
                <h2 class="admin-category-form-section-title">Tóm tắt</h2>
                <p class="admin-category-form-section-desc">
                  Kiểm tra nhanh thông tin hiện tại của danh mục.
                </p>
              </div>
            </div>

            <div class="admin-category-info-list">
              <div class="admin-category-info-item">
                <span>Loại thao tác</span>
                <strong>
                  <c:choose>
                    <c:when test="${isEdit}">Cập nhật danh mục</c:when>
                    <c:otherwise>Tạo danh mục mới</c:otherwise>
                  </c:choose>
                </strong>
              </div>

              <c:if test="${isEdit}">
                <div class="admin-category-info-item">
                  <span>ID danh mục</span>
                  <strong>#<c:out value="${category.id}" /></strong>
                </div>

                <div class="admin-category-info-item">
                  <span>Số sản phẩm</span>
                  <strong><c:out value="${empty category.productCount ? 0 : category.productCount}" /></strong>
                </div>

                <div class="admin-category-info-item">
                  <span>Số thẻ</span>
                  <strong><c:out value="${empty category.tagCount ? 0 : category.tagCount}" /></strong>
                </div>
              </c:if>

              <div class="admin-category-info-item">
                <span>Thẻ hiển thị</span>
                <strong>Có thể thêm nhiều thẻ</strong>
              </div>
            </div>
          </div>
        </aside>
      </div>

      <section class="admin-card admin-category-tag-card">
        <div class="admin-card__body">
          <div class="admin-category-form-section-head">
            <div>
              <h2 class="admin-category-form-section-title">Thẻ hiển thị trong trang sản phẩm</h2>
              <p class="admin-category-form-section-desc">
                Các thẻ này sẽ được hiển thị ở trang chi tiết sản phẩm thuộc danh mục này.
                Ví dụ: <strong>Da dầu</strong>, <strong>Dưỡng ẩm</strong>, <strong>Làm sáng da</strong>, <strong>SPF50+</strong>.
              </p>
            </div>

            <button type="button" class="admin-btn admin-btn--primary" id="btnAddCategoryTag">
              + Thêm thẻ
            </button>
          </div>

          <div class="category-tag-table-wrap admin-category-tag-table-wrap">
            <table class="category-tag-table admin-category-tag-table">
              <thead>
              <tr>
                <th>Tên thẻ</th>
                <th>Slug</th>
                <th>Thứ tự</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
              </thead>

              <tbody id="categoryTagRows">
              <c:choose>
                <c:when test="${not empty category.tags}">
                  <c:forEach var="tag" items="${category.tags}" varStatus="st">
                    <tr class="category-tag-row">
                      <td>
                        <input class="admin-input category-tag-name"
                               type="text"
                               name="tagNames"
                               value="${tag.name}"
                               maxlength="100"
                               placeholder="VD: Da dầu">
                      </td>

                      <td>
                        <input class="admin-input category-tag-slug"
                               type="text"
                               name="tagSlugs"
                               value="${tag.slug}"
                               maxlength="120"
                               pattern="[a-z0-9]+(-[a-z0-9]+)*"
                               placeholder="VD: da-dau">
                      </td>

                      <td>
                        <input class="admin-input category-tag-order"
                               type="number"
                               name="tagOrders"
                               value="${tag.displayOrder > 0 ? tag.displayOrder : st.index + 1}"
                               min="1"
                               step="1">
                      </td>

                      <td>
                        <select class="admin-select category-tag-active" name="tagActives">
                          <option value="1" ${tag.active ? 'selected' : ''}>Đang hiển thị</option>
                          <option value="0" ${not tag.active ? 'selected' : ''}>Tạm ẩn</option>
                        </select>
                      </td>

                      <td>
                        <button type="button" class="admin-btn admin-btn--danger category-tag-remove">
                          Xóa
                        </button>
                      </td>
                    </tr>
                  </c:forEach>
                </c:when>

                <c:otherwise>
                  <tr class="category-tag-row">
                    <td>
                      <input class="admin-input category-tag-name"
                             type="text"
                             name="tagNames"
                             value=""
                             maxlength="100"
                             placeholder="VD: Da dầu">
                    </td>

                    <td>
                      <input class="admin-input category-tag-slug"
                             type="text"
                             name="tagSlugs"
                             value=""
                             maxlength="120"
                             pattern="[a-z0-9]+(-[a-z0-9]+)*"
                             placeholder="VD: da-dau">
                    </td>

                    <td>
                      <input class="admin-input category-tag-order"
                             type="number"
                             name="tagOrders"
                             value="1"
                             min="1"
                             step="1">
                    </td>

                    <td>
                      <select class="admin-select category-tag-active" name="tagActives">
                        <option value="1" selected>Đang hiển thị</option>
                        <option value="0">Tạm ẩn</option>
                      </select>
                    </td>

                    <td>
                      <button type="button" class="admin-btn admin-btn--danger category-tag-remove">
                        Xóa
                      </button>
                    </td>
                  </tr>
                </c:otherwise>
              </c:choose>
              </tbody>
            </table>
          </div>

          <div class="admin-help category-tag-note">
            Có thể để trống slug thẻ, hệ thống sẽ tự tạo slug từ tên thẻ khi lưu.
            Dòng tag không có tên sẽ tự được bỏ qua.
          </div>
        </div>
      </section>

      <div class="admin-category-form-actions">
        <a class="admin-btn" href="${ctx}/admin/categories">
          Hủy
        </a>

        <button class="admin-btn admin-btn--primary" type="submit">
          <c:choose>
            <c:when test="${isEdit}">Cập nhật danh mục</c:when>
            <c:otherwise>Thêm danh mục</c:otherwise>
          </c:choose>
        </button>
      </div>
    </form>
  </div>
</main>

<script>
  (function () {
    const rowsBody = document.getElementById('categoryTagRows');
    const addButton = document.getElementById('btnAddCategoryTag');

    if (!rowsBody || !addButton) {
      return;
    }

    function toSlug(value) {
      if (!value) {
        return '';
      }

      return value
              .trim()
              .toLowerCase()
              .normalize('NFD')
              .replace(/[\u0300-\u036f]/g, '')
              .replace(/đ/g, 'd')
              .replace(/Đ/g, 'd')
              .replace(/[^a-z0-9\s-]/g, '')
              .replace(/\s+/g, '-')
              .replace(/-+/g, '-')
              .replace(/^-+|-+$/g, '');
    }

    function nextOrder() {
      const rows = rowsBody.querySelectorAll('.category-tag-row');
      return rows.length + 1;
    }

    function createRow() {
      const tr = document.createElement('tr');
      tr.className = 'category-tag-row';

      const orderValue = nextOrder();

      tr.innerHTML =
              '<td>' +
              '<input class="admin-input category-tag-name" ' +
              'type="text" ' +
              'name="tagNames" ' +
              'value="" ' +
              'maxlength="100" ' +
              'placeholder="VD: Da dầu">' +
              '</td>' +

              '<td>' +
              '<input class="admin-input category-tag-slug" ' +
              'type="text" ' +
              'name="tagSlugs" ' +
              'value="" ' +
              'maxlength="120" ' +
              'pattern="[a-z0-9]+(-[a-z0-9]+)*" ' +
              'placeholder="VD: da-dau">' +
              '</td>' +

              '<td>' +
              '<input class="admin-input category-tag-order" ' +
              'type="number" ' +
              'name="tagOrders" ' +
              'value="' + orderValue + '" ' +
              'min="1" ' +
              'step="1">' +
              '</td>' +

              '<td>' +
              '<select class="admin-select category-tag-active" name="tagActives">' +
              '<option value="1" selected>Đang hiển thị</option>' +
              '<option value="0">Tạm ẩn</option>' +
              '</select>' +
              '</td>' +

              '<td>' +
              '<button type="button" ' +
              'class="admin-btn admin-btn--danger category-tag-remove">' +
              'Xóa' +
              '</button>' +
              '</td>';

      return tr;
    }

    function renumberOrders() {
      const rows = rowsBody.querySelectorAll('.category-tag-row');

      rows.forEach(function (row, index) {
        const orderInput = row.querySelector('.category-tag-order');

        if (orderInput && (!orderInput.value || Number(orderInput.value) <= 0)) {
          orderInput.value = index + 1;
        }
      });
    }

    addButton.addEventListener('click', function () {
      rowsBody.appendChild(createRow());

      const lastRow = rowsBody.querySelector('.category-tag-row:last-child');
      const nameInput = lastRow ? lastRow.querySelector('.category-tag-name') : null;

      if (nameInput) {
        nameInput.focus();
      }
    });

    rowsBody.addEventListener('click', function (event) {
      const removeButton = event.target.closest('.category-tag-remove');

      if (!removeButton) {
        return;
      }

      const row = removeButton.closest('.category-tag-row');

      if (row) {
        row.remove();
      }

      if (rowsBody.querySelectorAll('.category-tag-row').length === 0) {
        rowsBody.appendChild(createRow());
      }

      renumberOrders();
    });

    rowsBody.addEventListener('input', function (event) {
      const nameInput = event.target.closest('.category-tag-name');

      if (!nameInput) {
        return;
      }

      const row = nameInput.closest('.category-tag-row');
      const slugInput = row ? row.querySelector('.category-tag-slug') : null;

      if (!slugInput) {
        return;
      }

      if (!slugInput.dataset.manual && !slugInput.value.trim()) {
        slugInput.value = toSlug(nameInput.value);
      }
    });

    rowsBody.addEventListener('input', function (event) {
      const slugInput = event.target.closest('.category-tag-slug');

      if (!slugInput) {
        return;
      }

      slugInput.dataset.manual = '1';
      slugInput.value = toSlug(slugInput.value);
    });
  })();
</script>

<jsp:include page="/jsp/admin/layout/footer.jsp"/>
