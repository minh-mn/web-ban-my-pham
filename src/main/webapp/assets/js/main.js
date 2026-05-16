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

  /* ================= USER DROPDOWN ================= */

  var userBtn = document.querySelector(".user-btn");
  var userMenu = document.querySelector(".user-menu");

  if (userBtn && userMenu) {
    userBtn.addEventListener("click", function (e) {
      e.stopPropagation();
      userMenu.classList.toggle("show");
    });

    document.addEventListener("click", function () {
      userMenu.classList.remove("show");
    });
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
