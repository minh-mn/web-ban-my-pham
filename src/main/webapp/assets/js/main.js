document.addEventListener("DOMContentLoaded", function () {

  /* ================= SEARCH AJAX (ES5) ================= */
  var input = document.getElementById("search-input");
  var resultsBox = document.getElementById("search-results");
  var debounceTimer = null;
  var controller = null;

  // contextPath, ví dụ: "/MyCosmeticShop"
  var CTX = (window.APP_CTX || "");
  var DEFAULT_IMG = CTX + "/assets/images/default-product.jpg";

  function escapeHtml(str) {
    str = (str === null || str === undefined) ? "" : String(str);
    return str
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  function formatVnd(x) {
    var n = Number(x);
    if (!isNaN(n) && isFinite(n)) {
      // toLocaleString vẫn dùng được ES5
      return n.toLocaleString("vi-VN") + " ₫";
    }
    return escapeHtml(x) + " ₫";
  }

  function buildUrl(path) {
    if (!path) return "#";
    path = String(path);

    if (path.indexOf("http://") === 0 || path.indexOf("https://") === 0) return path;

    if (path.charAt(0) === "/") return CTX + path;

    return CTX + "/" + path;
  }

  function buildImgUrl(img) {
    if (!img) return DEFAULT_IMG;
    img = String(img);

    if (img.indexOf("http://") === 0 || img.indexOf("https://") === 0) return img;

    if (img.charAt(0) === "/") return CTX + img;

    return CTX + "/" + img;
  }

  function buildProductHref(item) {
    if (!item || !item.slug) return "#";
    // nếu bạn cần dấu "/" cuối, thêm + "/"
    return buildUrl("/product/" + encodeURIComponent(String(item.slug)));
  }

  function showResults() {
    if (!resultsBox) return;
    // ưu tiên class show (đúng với CSS)
    if (resultsBox.classList) resultsBox.classList.add("show");
    else resultsBox.style.display = "block";
  }

  function hideResults() {
    if (!resultsBox) return;
    if (resultsBox.classList) resultsBox.classList.remove("show");
    else resultsBox.style.display = "none";
  }

  function setLoading() {
    showResults();
    resultsBox.innerHTML = '<div class="sr-empty">Đang tìm kiếm...</div>';
  }

  function setEmpty(msg) {
    showResults();
    resultsBox.innerHTML = '<div class="sr-empty">' + escapeHtml(msg) + "</div>";
  }

  function renderList(list) {
    resultsBox.innerHTML = "";

    if (!list || !list.length) {
      setEmpty("Không tìm thấy sản phẩm");
      return;
    }

    // giới hạn 8 item
    var max = Math.min(list.length, 8);

    for (var i = 0; i < max; i++) {
      var item = list[i];

      var a = document.createElement("a");
      a.className = "sr-item";
      a.href = buildProductHref(item);

      var img = buildImgUrl(item.img || item.imageUrl || item.image || "");
      var title = escapeHtml(item.title || "");
      var price = formatVnd(
        (item.finalPrice !== undefined && item.finalPrice !== null) ? item.finalPrice :
        (item.price !== undefined && item.price !== null) ? item.price :
        (item.minPrice !== undefined && item.minPrice !== null) ? item.minPrice : 0
      );

      // Lưu ý: onerror cần nháy đơn bên trong src fallback để không vỡ chuỗi
      a.innerHTML =
        '<img class="sr-thumb" src="' + img + '" alt="" ' +
        'onerror="this.onerror=null;this.src=\'' + DEFAULT_IMG + '\';">' +
        '<div class="sr-meta">' +
          '<div class="sr-title">' + title + '</div>' +
          '<div class="sr-price">' + price + '</div>' +
        '</div>';

      resultsBox.appendChild(a);
    }

    showResults();
  }

  if (input && resultsBox) {
    input.addEventListener("input", function () {
      var query = this.value.replace(/^\s+|\s+$/g, ""); // trim ES5
      clearTimeout(debounceTimer);

      if (query.length < 2) {
        hideResults();
        resultsBox.innerHTML = "";
        return;
      }

      debounceTimer = setTimeout(function () {
        if (controller) controller.abort();
        controller = new AbortController();

        setLoading();

        // ✅ bắt buộc nối contextPath
        var endpoint = buildUrl("/ajax-search/?q=" + encodeURIComponent(query));

        fetch(endpoint, { signal: controller.signal })
          .then(function (res) {
            if (!res.ok) throw new Error("HTTP " + res.status);
            return res.json();
          })
          .then(function (data) {
            var results = (data && data.results) ? data.results : [];
            renderList(results);
          })
          .catch(function (err) {
            if (err && err.name === "AbortError") return;
            setEmpty("Có lỗi xảy ra");
          });

      }, 300);
    });

    input.addEventListener("focus", function () {
      if (resultsBox.innerHTML && resultsBox.innerHTML.replace(/\s/g, "").length > 0) {
        showResults();
      }
    });

    document.addEventListener("click", function (e) {
      if (!resultsBox.contains(e.target) && e.target !== input) {
        hideResults();
      }
    });

    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape") hideResults();
    });
  }

  /* ================= AUTH DROPDOWN ================= */
  var userBtn = document.querySelector(".user-btn");
  var userMenu = document.querySelector(".user-menu");

  if (userBtn && userMenu) {
    userBtn.addEventListener("click", function (e) {
      e.stopPropagation();
      userMenu.classList.toggle("show");
      userBtn.setAttribute("aria-expanded", userMenu.classList.contains("show") ? "true" : "false");
    });

    userMenu.addEventListener("click", function (e) { e.stopPropagation(); });

    document.addEventListener("click", function () {
      userMenu.classList.remove("show");
      userBtn.setAttribute("aria-expanded", "false");
    });

    document.addEventListener("keydown", function (e) {
      if (e.key !== "Escape") return;
      userMenu.classList.remove("show");
      userBtn.setAttribute("aria-expanded", "false");
    });
  }

  /* ================= MOBILE MENU ================= */
  var menuToggle = document.getElementById("menu-toggle");
  var mobileNav = document.getElementById("mobile-nav");

  if (menuToggle && mobileNav) {
    menuToggle.addEventListener("click", function (e) {
      e.stopPropagation();
      mobileNav.classList.toggle("active");
    });

    var navEls = mobileNav.querySelectorAll("a, button");
    for (var j = 0; j < navEls.length; j++) {
      navEls[j].addEventListener("click", function () {
        mobileNav.classList.remove("active");
      });
    }

    document.addEventListener("click", function (e) {
      if (!mobileNav.contains(e.target) && e.target !== menuToggle) {
        mobileNav.classList.remove("active");
      }
    });
  }

});

/* ================= TOGGLE PASSWORD ================= */
var toggleBtns = document.querySelectorAll(".toggle-password");
for (var k = 0; k < toggleBtns.length; k++) {
  toggleBtns[k].addEventListener("click", function () {
    var inp = this.parentElement ? this.parentElement.querySelector("input") : null;
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

document.addEventListener("DOMContentLoaded", function () {

  const input = document.getElementById("search-input");
  const box = document.getElementById("search-results");

  if (!input || !box) return;

  let timer = null;

  input.addEventListener("input", function () {

    const keyword = this.value.trim();

    clearTimeout(timer);

    if (keyword.length < 2) {
      box.style.display = "none";
      box.innerHTML = "";
      return;
    }

    timer = setTimeout(() => {

      fetch(`${window.APP_CTX}/api/search-suggest?q=${encodeURIComponent(keyword)}`)
          .then(async res => {
            const text = await res.text();
            try {
              return JSON.parse(text);
            } catch (e) {
              console.log("Invalid JSON:", text);
              return [];
            }
          })
          .then(data => render(data, keyword))
          .catch(err => console.error(err));

    }, 200);
  });

  function highlight(text, keyword) {
    if (!keyword) return text;
    return text.replace(
        new RegExp(keyword, "gi"),
        match => `<b style="color:#ee4d2d">${match}</b>`
    );
  }

  function render(products, keyword) {

    if (!products || products.length === 0) {
      box.innerHTML = `<div class="suggest-empty">Không tìm thấy sản phẩm</div>`;
      box.style.display = "block";
      return;
    }

    let html = "";

    products.forEach(p => {

      const url = `${window.APP_CTX}/product/${p.slug}`;
      const img = p.image
          ? `${window.APP_CTX}/${p.image}`
          : `${window.APP_CTX}/assets/img/default-product.jpg`;

      const price = new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
      }).format(p.price || 0);

      html += `
        <a href="${url}" class="suggest-item">
            <img src="${img}" class="suggest-item-img" />
            <div class="suggest-item-info">
                <div class="suggest-item-title">
                    ${highlight(p.title, keyword)}
                </div>
                <div class="suggest-item-price">${price}</div>
            </div>
        </a>
      `;
    });

    box.innerHTML = html;
    box.style.display = "block";
  }

  document.addEventListener("click", function (e) {
    if (!input.contains(e.target) && !box.contains(e.target)) {
      box.style.display = "none";
    }
  });

});
