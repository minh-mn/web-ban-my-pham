document.addEventListener("DOMContentLoaded", function () {

  /* ================= SEARCH AUTOCOMPLETE (SHOPEE STYLE) ================= */

  var input = document.getElementById("search-input");
  var resultsBox = document.getElementById("search-results");

  var debounceTimer = null;
  var controller = null;

  var CTX = window.APP_CTX || "";
  var DEFAULT_IMG = CTX + "/assets/images/default-product.jpg";

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
  }

  function highlight(text, keyword) {
    if (!keyword) return escapeHtml(text);
    var safeText = escapeHtml(text);
    var re = new RegExp("(" + keyword + ")", "ig");
    return safeText.replace(re, "<b>$1</b>");
  }

  function buildUrl(path) {
    if (!path) return "#";
    if (path.indexOf("http") === 0) return path;
    return CTX + path;
  }

  function buildImg(img) {
    if (!img) return DEFAULT_IMG;
    if (img.indexOf("http") === 0) return img;
    return CTX + "/" + img;
  }

  function buildProductLink(item) {
    if (!item || !item.slug) return "#";
    return buildUrl("/product/" + encodeURIComponent(item.slug));
  }

  function showBox() {
    resultsBox.classList.add("show");
  }

  function hideBox() {
    resultsBox.classList.remove("show");
  }

  function setLoading() {
    showBox();
    resultsBox.innerHTML = '<div class="sr-empty">Đang tìm kiếm...</div>';
  }

  function setEmpty(msg) {
    showBox();
    resultsBox.innerHTML = '<div class="sr-empty">' + escapeHtml(msg) + '</div>';
  }

  /* ================= RENDER LIST ================= */

  function renderList(list, keyword) {
    resultsBox.innerHTML = "";

    if (!list || list.length === 0) {
      setEmpty("Không tìm thấy sản phẩm");
      return;
    }

    var max = Math.min(list.length, 8);

    for (var i = 0; i < max; i++) {
      var p = list[i];

      var a = document.createElement("a");
      a.className = "sr-item";
      a.href = buildProductLink(p);

      var img = buildImg(p.image);
      var title = highlight(p.title, keyword);

      a.innerHTML =
          '<img class="sr-thumb" src="' + img + '">' +
          '<div class="sr-meta">' +
          '<div class="sr-title">' + title + '</div>' +
          '</div>';

      resultsBox.appendChild(a);
    }

    showBox();
  }

  /* ================= SEARCH EVENT ================= */

  if (input && resultsBox) {

    input.addEventListener("input", function () {

      var keyword = this.value.trim();

      clearTimeout(debounceTimer);

      if (keyword.length < 2) {
        hideBox();
        resultsBox.innerHTML = "";
        return;
      }

      debounceTimer = setTimeout(function () {

        if (controller) controller.abort();
        controller = new AbortController();

        setLoading();

        var url = buildUrl("/ajax-search?q=" + encodeURIComponent(keyword));

        fetch(url, { signal: controller.signal })
            .then(function (res) {
              if (!res.ok) throw new Error("HTTP " + res.status);
              return res.json();
            })
            .then(function (data) {
              var results = (data && data.results) ? data.results : [];
              renderList(results, keyword);
            })
            .catch(function (err) {
              if (err.name === "AbortError") return;
              setEmpty("Có lỗi xảy ra");
            });

      }, 250); // giống Shopee (250ms)
    });

    input.addEventListener("focus", function () {
      if (resultsBox.innerHTML.trim() !== "") {
        showBox();
      }
    });

    document.addEventListener("click", function (e) {
      if (!resultsBox.contains(e.target) && e.target !== input) {
        hideBox();
      }
    });

    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape") {
        hideBox();
      }
    });
  }

  /* ================= USER DROPDOWN================= */
  try {
    var userBtn = document.querySelector(".user-btn");
    var userMenu = document.querySelector(".user-menu");
    var userDropdown = document.querySelector(".user-dropdown");

    if (userBtn && userMenu && userDropdown) {

      // Tạo một biến cờ hiệu nằm trên đối tượng window để dùng chung cho cả 2 thực thể JS
      if (typeof window.isDropdownDropdownLocked === "undefined") {
        window.isDropdownDropdownLocked = false;
      }

      userBtn.addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();

        // Nếu lượt click này quá nhanh (do file JS thứ 2 kích hoạt ngay sau đó), ta bỏ qua luôn
        if (window.isDropdownDropdownLocked) {
          return;
        }

        // Bật khóa
        window.isDropdownDropdownLocked = true;

        // Tiến hành đóng / mở menu
        userMenu.classList.toggle("show");
        console.log("Đã click nút User. Trạng thái class 'show':", userMenu.classList.contains("show"));

        // Sau 100ms mới mở khóa để người dùng có thể click lần tiếp theo bình thường
        setTimeout(function() {
          window.isDropdownDropdownLocked = false;
        }, 100);
      });

      // Xử lý click ra ngoài để đóng menu
      document.addEventListener("click", function (e) {
        if (!userDropdown.contains(e.target)) {
          userMenu.classList.remove("show");
        }
      });

      // Xử lý nút ESC
      document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
          userMenu.classList.remove("show");
        }
      });

      // Click bên trong menu không bị đóng
      userMenu.addEventListener("click", function (e) {
        e.stopPropagation();
      });

    }
  } catch (error) {
    console.error("Lỗi khi thiết lập Dropdown:", error);
  }

  /* ================= TOGGLE PASSWORD ================= */

  var toggleBtns = document.querySelectorAll(".toggle-password");

  for (var i = 0; i < toggleBtns.length; i++) {
    toggleBtns[i].addEventListener("click", function () {
      var inp = this.parentElement.querySelector("input");
      if (!inp) return;

      if (inp.type === "password") {
        inp.type = "text";
        this.textContent = "🙈";
      } else {
        inp.type = "password";
        this.textContent = "👁️";
      }
    });
  }

});
