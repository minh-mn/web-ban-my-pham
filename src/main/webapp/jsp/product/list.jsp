<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isLipPage" value="${lipCollectionPage == true}" />

<link rel="stylesheet" href="${ctx}/assets/css/base.css">
<link rel="stylesheet" href="${ctx}/assets/css/product-list.css">

<style>
  html.is-restoring-product-scroll {
    scroll-behavior: auto !important;
  }

  .mc-category-hero-wrap {
    padding: 0;
    background: #fff;
    border-bottom: 1px solid #f2eef0;
  }

  .mc-category-hero {
    max-width: 1480px;
    margin: 0 auto;
    padding: 26px 18px 18px;
    text-align: center;
  }

  .mc-category-hero__label {
    display: none;
  }

  .mc-category-hero__title {
    margin: 0;
    color: #d92c74;
    font-size: clamp(32px, 4vw, 54px);
    line-height: 1.08;
    font-weight: 950;
    letter-spacing: -0.04em;
  }

  .mc-category-hero__desc {
    max-width: 640px;
    margin: 12px auto 0;
    color: #59606d;
    font-size: 15px;
    line-height: 1.65;
  }

  .collection-body-section {
    padding: 0 0 70px;
    background: #fff;
  }

  .collection-container {
    max-width: 1480px;
  }

  .collection-layout {
    display: grid;
    grid-template-columns: 300px minmax(0, 1fr);
    gap: 42px;
    align-items: start;
  }

  .collection-filter-sidebar {
    position: sticky;
    top: 96px;
    max-height: calc(100vh - 112px);
    overflow-y: auto;
    padding: 28px 26px 34px 0;
    background: #fff;
    border-right: 7px solid #d78ca0;
    scrollbar-width: thin;
    scrollbar-color: #d78ca0 transparent;
  }

  .collection-filter-sidebar::-webkit-scrollbar {
    width: 6px;
  }

  .collection-filter-sidebar::-webkit-scrollbar-thumb {
    background: #d78ca0;
    border-radius: 999px;
  }

  .mc-filter-form {
    display: block;
  }

  .mc-filter-block {
    padding: 0 0 24px;
    margin-bottom: 24px;
    border-bottom: 1px solid #eeeeee;
  }

  .mc-filter-block:last-of-type {
    border-bottom: 0;
    margin-bottom: 0;
  }

  .mc-filter-title {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 0;
    border: 0;
    background: transparent;
    color: #2d2d2d;
    font-size: 18px;
    font-weight: 900;
    letter-spacing: .035em;
    text-transform: uppercase;
    cursor: pointer;
  }

  .mc-filter-title::after {
    content: "⌃";
    color: #222;
    font-size: 24px;
    line-height: 1;
    transform: translateY(4px);
  }

  .mc-filter-list {
    display: grid;
    gap: 15px;
    margin-top: 20px;
  }

  .mc-filter-list.is-scroll {
    max-height: 420px;
    overflow-y: auto;
    padding-right: 14px;
    scrollbar-width: thin;
    scrollbar-color: #444 transparent;
  }

  .mc-filter-list.is-scroll::-webkit-scrollbar {
    width: 4px;
  }

  .mc-filter-list.is-scroll::-webkit-scrollbar-thumb {
    background: #444;
    border-radius: 999px;
  }

  .mc-filter-option {
    display: flex;
    align-items: center;
    gap: 14px;
    min-height: 26px;
    color: #333;
    font-size: 18px;
    line-height: 1.35;
    cursor: pointer;
    user-select: none;
  }

  .mc-filter-option input {
    position: absolute;
    opacity: 0;
    pointer-events: none;
  }

  .mc-filter-box {
    flex: 0 0 auto;
    width: 23px;
    height: 23px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1.5px solid #bdbdbd;
    background: #fff;
  }

  .mc-filter-option input:checked + .mc-filter-box {
    border-color: #bd0031;
    box-shadow: inset 0 0 0 4px #fff;
    background: #bd0031;
  }

  .mc-filter-option input:checked ~ .mc-filter-name {
    color: #bd0031;
    font-weight: 850;
  }

  .mc-filter-name {
    min-width: 0;
  }

  .mc-price-range-row {
    display: grid;
    grid-template-columns: 1fr 24px 1fr;
    gap: 14px;
    align-items: center;
    margin-top: 20px;
  }

  .mc-price-input {
    width: 100%;
    height: 62px;
    border: 1px solid #e5e5e5;
    background: #fff;
    color: #333;
    font-size: 19px;
    text-align: center;
    outline: none;
  }

  .mc-price-separator {
    color: #555;
    font-size: 25px;
    text-align: center;
  }

  .mc-filter-apply {
    width: 100%;
    min-height: 68px;
    margin-top: 18px;
    border: 0;
    background: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #a4041b 100%);
    color: #fff;
    font-size: 19px;
    font-weight: 900;
    cursor: pointer;
    box-shadow: 0 14px 26px rgba(176, 18, 57, .18);
  }

  .mc-filter-reset {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
    color: #9d001f;
    font-size: 17px;
    font-weight: 850;
    text-decoration: none;
  }

  .collection-main {
    min-width: 0;
    padding-top: 28px;
  }

  .collection-toolbar {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 22px;
    align-items: end;
    padding-bottom: 14px;
    margin-bottom: 18px;
    border-bottom: 1px solid #f0d2dc;
  }

  .collection-toolbar__sorts {
    display: grid;
    grid-template-columns: repeat(5, minmax(110px, 1fr));
    gap: 0;
  }

  .collection-sort-tab {
    min-height: 46px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #111;
    font-size: 17px;
    font-weight: 700;
    text-decoration: none;
    border-bottom: 2px solid transparent;
  }

  .collection-sort-tab.active,
  .collection-sort-tab:hover {
    color: #9f001f;
    border-bottom-color: #9f001f;
  }

  .collection-toolbar__count {
    color: #111;
    font-size: 14px;
    font-weight: 850;
    white-space: nowrap;
  }

  .collection-filter-tags {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    min-height: 48px;
    padding: 10px 14px;
    margin-bottom: 18px;
    border: 1px solid #f0d2dc;
    background: #fff8fb;
  }

  .product-filter-tags__label {
    color: #333;
    font-size: 13px;
    font-weight: 900;
  }

  .product-filter-tag {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    min-height: 28px;
    padding: 6px 12px;
    border-radius: 999px;
    background: #ffe8f0;
    color: #b01239;
    font-size: 12px;
    font-weight: 850;
  }

  .product-filter-tag__remove {
    color: #b01239;
    font-size: 18px;
    line-height: 1;
    text-decoration: none;
  }

  .product-filter-clear {
    color: #b01239;
    font-size: 13px;
    font-weight: 850;
    text-decoration: none;
  }

  .collection-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 30px 28px;
    align-items: start;
  }

  .collection-card {
    position: relative;
    min-width: 0;
    background: #fff;
    border: 0;
    box-shadow: none;
    overflow: visible;
  }

  .collection-card__image-link {
    display: block;
    text-decoration: none;
  }

  .collection-card__image-box {
    position: relative;
    width: 100%;
    aspect-ratio: 1 / 1;
    overflow: hidden;
    background: #f5f8fb;
  }

  .collection-card__image-box img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
  }

  .collection-card__image-box.is-missing,
  .collection-card__image-placeholder {
    width: 100%;
    height: 100%;
    min-height: 220px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #b01239;
    font-weight: 950;
    background: linear-gradient(135deg, #fff1f6 0%, #f3f8ff 100%);
  }

  .collection-card__discount {
    position: absolute;
    right: 14px;
    top: 46%;
    z-index: 3;
    width: 48px;
    height: 48px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: #a8d83f !important;
    color: #fff !important;
    font-size: 13px;
    font-weight: 950;
    box-shadow: none;
  }

  .collection-card__body {
    padding: 13px 0 0;
  }

  .collection-card__brand {
    margin-bottom: 8px;
    color: #111;
    font-size: 13px;
    font-weight: 950;
    letter-spacing: .06em;
    text-transform: uppercase;
  }

  .collection-card__title {
    min-height: 48px;
    margin: 0;
    display: -webkit-box;
    overflow: hidden;
    color: #161616;
    font-size: 18px;
    line-height: 1.35;
    font-weight: 500;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .product-title-link {
    color: inherit;
    text-decoration: none;
  }

  .collection-card__price-line {
    display: flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 9px;
    margin-top: 12px;
  }

  .collection-card__sale-price {
    color: #a30624;
    font-size: 20px;
    font-weight: 950;
  }

  .collection-card__old-price {
    color: #8d8d8d;
    font-size: 15px;
    text-decoration: line-through;
  }

  .collection-card__actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
    margin-top: 14px;
  }

  .collection-card__view-btn,
  .collection-card__cart-btn {
    min-height: 42px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 10px 12px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 900;
    text-decoration: none;
    cursor: pointer;
  }

  .collection-card__view-btn {
    border: 1px solid #f0c4cf;
    color: #bd0031;
    background: #fff;
  }

  .collection-card__cart-btn {
    width: 100%;
    border: 0;
    color: #fff;
    background: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #a4041b 100%);
    box-shadow: 0 10px 20px rgba(176, 18, 57, .16);
  }

  .collection-card__cart-btn:disabled {
    opacity: .5;
    cursor: not-allowed;
  }

  .collection-card__bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: 12px;
  }

  .collection-card__meta {
    color: #222;
    font-size: 15px;
    font-weight: 700;
  }

  .collection-card__heart {
    color: #333;
    font-size: 28px;
    line-height: 1;
    text-decoration: none;
  }

  .pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 40px;
  }

  @media (max-width: 1200px) {
    .collection-layout {
      grid-template-columns: 260px minmax(0, 1fr);
      gap: 26px;
    }

    .collection-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  @media (max-width: 900px) {
    .collection-layout {
      display: block;
    }

    .collection-filter-sidebar {
      position: relative;
      top: auto;
      max-height: none;
      border-right: 0;
      border-bottom: 4px solid #d78ca0;
      padding-right: 0;
      margin-bottom: 24px;
    }

    .collection-toolbar {
      grid-template-columns: 1fr;
    }

    .collection-toolbar__sorts {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .collection-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 560px) {
    .collection-grid {
      grid-template-columns: 1fr;
    }

    .collection-card__actions {
      grid-template-columns: 1fr;
    }
  }

  /* =========================================================
     PRODUCT LIST FINAL UI OVERRIDE
     - Đồng bộ bo góc + màu với trang chủ
     - Nút "Xem sản phẩm" không xuống dòng
     - Bố cục sidebar / toolbar / card gọn và logic hơn
  ========================================================= */
  :root {
    --product-main-pink: var(--pink-main, #ff5fa2);
    --product-main-pink-dark: var(--pink-dark, #db2777);
    --product-home-red: var(--skin-red, #9b0012);
    --product-home-red-dark: var(--skin-red-dark, #6f000c);
    --product-soft-pink: var(--skin-pink, #fff1f4);
    --product-card-border: rgba(255, 95, 162, .20);
    --product-soft-border: rgba(15, 23, 42, .08);
    --product-card-shadow: 0 18px 45px rgba(15, 23, 42, .08);
    --product-pink-shadow: 0 14px 28px rgba(155, 0, 18, .16);
    --product-radius-lg: 22px;
    --product-radius-md: 16px;
    --product-radius-pill: 999px;
  }

  .collection-body-section {
    padding: 22px 0 74px !important;
    background:
            radial-gradient(circle at 8% 8%, rgba(255, 95, 162, .10), transparent 28%),
            linear-gradient(180deg, #ffffff 0%, #fff8fb 100%) !important;
  }

  .collection-container,
  .container.collection-container {
    width: min(1660px, calc(100% - 64px)) !important;
    max-width: 1660px !important;
  }

  .mc-category-hero-wrap {
    padding: 34px 16px 24px !important;
    background:
            radial-gradient(circle at 12% 45%, rgba(255, 95, 162, .12), transparent 22%),
            radial-gradient(circle at 88% 28%, rgba(255, 95, 162, .11), transparent 18%),
            linear-gradient(180deg, #fff 0%, #fff7fa 100%) !important;
    border-bottom: 1px solid rgba(255, 95, 162, .13) !important;
  }

  .mc-category-hero__label {
    display: inline-flex !important;
    align-items: center;
    justify-content: center;
    min-height: 28px;
    padding: 5px 17px;
    margin: 0 0 12px;
    border-radius: var(--product-radius-pill);
    border: 1px solid rgba(255, 95, 162, .36);
    background: #fff4f8;
    color: var(--product-home-red);
    font-size: 12px;
    font-weight: 900;
    letter-spacing: .08em;
    text-transform: uppercase;
  }

  .mc-category-hero__title {
    color: transparent !important;
    background: linear-gradient(135deg, var(--product-main-pink) 0%, var(--product-main-pink-dark) 48%, var(--product-home-red) 100%) !important;
    -webkit-background-clip: text !important;
    background-clip: text !important;
    text-shadow: none !important;
  }

  .collection-layout,
  .product-page.collection-layout {
    grid-template-columns: 270px minmax(0, 1fr) !important;
    gap: 28px !important;
    align-items: start !important;
  }

  .collection-filter-sidebar,
  .filter-sidebar.collection-filter-sidebar {
    position: sticky !important;
    top: 118px !important;
    padding: 20px 16px 18px !important;
    max-height: calc(100vh - 136px) !important;
    overflow-y: auto !important;
    overflow-x: hidden !important;
    background: rgba(255, 255, 255, .96) !important;
    border: 1px solid var(--product-soft-border) !important;
    border-radius: var(--product-radius-lg) !important;
    box-shadow: var(--product-card-shadow) !important;
  }

  .mc-filter-form::before {
    content: "Bộ lọc";
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0 0 16px;
    padding-bottom: 14px;
    border-bottom: 1px solid rgba(15, 23, 42, .07);
    color: #141414;
    font-size: 15px;
    font-weight: 950;
    letter-spacing: .02em;
    text-transform: uppercase;
  }

  .mc-filter-block {
    padding: 0 0 18px !important;
    margin-bottom: 18px !important;
    border-bottom: 1px solid rgba(15, 23, 42, .07) !important;
  }

  .mc-filter-title {
    color: #141414 !important;
    font-size: 13px !important;
    font-weight: 950 !important;
    letter-spacing: .05em !important;
  }

  .mc-filter-title::after {
    width: 24px;
    height: 24px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: var(--product-radius-pill);
    background: #fff3f7;
    color: var(--product-main-pink-dark) !important;
    font-size: 16px !important;
    transform: none !important;
  }

  .mc-filter-list {
    gap: 10px !important;
    margin-top: 14px !important;
  }

  .mc-filter-list.is-scroll {
    max-height: 286px !important;
    padding-right: 8px !important;
  }

  .mc-filter-option {
    grid-template-columns: auto minmax(0, 1fr) !important;
    gap: 9px !important;
    min-height: 25px !important;
    color: #344054 !important;
    font-size: 13px !important;
    font-weight: 700 !important;
  }

  .mc-filter-box {
    width: 16px !important;
    height: 16px !important;
    border-radius: 5px !important;
    border-color: #cbd5e1 !important;
  }

  .mc-filter-option input:checked + .mc-filter-box {
    border-color: var(--product-main-pink-dark) !important;
    background: linear-gradient(135deg, var(--product-main-pink), var(--product-home-red)) !important;
    box-shadow: 0 0 0 3px rgba(255, 95, 162, .13), inset 0 0 0 3px #fff !important;
  }

  .mc-price-range-row {
    grid-template-columns: minmax(0, 1fr) 14px minmax(0, 1fr) !important;
    gap: 8px !important;
    margin-top: 14px !important;
  }

  .mc-price-input {
    height: 42px !important;
    border-radius: 12px !important;
    border: 1px solid #e5e7eb !important;
    background: #f9fafb !important;
    color: #111827 !important;
    font-size: 13px !important;
    font-weight: 800 !important;
  }

  .mc-price-separator {
    color: #98a2b3 !important;
    font-size: 16px !important;
  }

  .mc-filter-apply,
  .mc-filter-reset {
    border-radius: 14px !important;
  }

  .mc-filter-apply {
    min-height: 44px !important;
    margin-top: 14px !important;
    background: linear-gradient(135deg, var(--product-main-pink) 0%, var(--product-main-pink-dark) 52%, var(--product-home-red) 100%) !important;
    color: #fff !important;
    font-size: 13px !important;
    font-weight: 950 !important;
    box-shadow: var(--product-pink-shadow) !important;
  }

  .mc-filter-reset {
    min-height: 42px !important;
    justify-content: center !important;
    border: 1px solid rgba(255, 95, 162, .34) !important;
    background: #fff4f8 !important;
    color: var(--product-home-red) !important;
    font-size: 13px !important;
    font-weight: 900 !important;
    text-decoration: none !important;
  }

  .collection-main {
    padding-top: 0 !important;
  }

  .collection-toolbar {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) auto !important;
    align-items: center !important;
    gap: 16px !important;
    min-height: 74px !important;
    margin: 0 0 12px !important;
    padding: 14px 16px !important;
    border: 1px solid var(--product-soft-border) !important;
    border-radius: var(--product-radius-lg) !important;
    background: rgba(255, 255, 255, .96) !important;
    box-shadow: 0 14px 34px rgba(15, 23, 42, .06) !important;
  }

  .collection-toolbar__sorts {
    display: grid !important;
    grid-template-columns: repeat(5, minmax(112px, 1fr)) !important;
    gap: 8px !important;
    align-items: center !important;
    width: 100% !important;
  }

  .collection-sort-tab {
    min-height: 40px !important;
    padding: 0 16px !important;
    border: 1px solid transparent !important;
    border-radius: var(--product-radius-pill) !important;
    color: #141414 !important;
    font-size: 13px !important;
    font-weight: 900 !important;
    line-height: 1 !important;
    white-space: nowrap !important;
    transition: background .18s ease, color .18s ease, border-color .18s ease, box-shadow .18s ease, transform .18s ease !important;
  }

  .collection-sort-tab:hover,
  .collection-sort-tab.active {
    background: #fff1f4 !important;
    border-color: rgba(255, 95, 162, .48) !important;
    color: var(--product-home-red) !important;
    box-shadow: inset 0 0 0 1px rgba(255, 95, 162, .10) !important;
    transform: translateY(-1px);
  }

  .collection-toolbar__count {
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
    min-height: 40px !important;
    padding: 0 18px !important;
    border: 1px solid rgba(15, 23, 42, .08) !important;
    border-radius: var(--product-radius-pill) !important;
    background: #f8fafc !important;
    color: #141414 !important;
    font-size: 13px !important;
    font-weight: 950 !important;
    white-space: nowrap !important;
  }

  .collection-filter-tags {
    min-height: 48px !important;
    margin-bottom: 12px !important;
    padding: 10px 14px !important;
    border-radius: var(--product-radius-md) !important;
    border: 1px solid rgba(255, 95, 162, .26) !important;
    background: rgba(255, 241, 244, .72) !important;
  }

  .product-filter-tags__label {
    color: #141414 !important;
    font-size: 13px !important;
    font-weight: 950 !important;
  }

  .product-filter-tag,
  .product-filter-clear,
  .product-filter-tag__remove {
    border-radius: var(--product-radius-pill) !important;
  }

  .product-filter-tag {
    min-height: 30px !important;
    padding: 0 12px !important;
    background: #fff !important;
    border: 1px solid rgba(255, 95, 162, .24) !important;
    color: var(--product-home-red) !important;
  }

  .product-filter-tag strong {
    color: var(--product-home-red) !important;
  }

  .product-grid,
  .collection-grid {
    display: grid !important;
    grid-template-columns: repeat(5, minmax(0, 1fr)) !important;
    gap: 18px !important;
    align-items: stretch !important;
  }

  .product-card,
  .collection-card {
    display: flex !important;
    flex-direction: column !important;
    min-width: 0 !important;
    height: 100% !important;
    padding: 12px !important;
    background: #fff !important;
    border: 1px solid var(--product-soft-border) !important;
    border-radius: var(--product-radius-lg) !important;
    box-shadow: 0 12px 28px rgba(15, 23, 42, .055) !important;
    overflow: hidden !important;
    transition: transform .18s ease, box-shadow .18s ease, border-color .18s ease !important;
  }

  .product-card:hover,
  .collection-card:hover {
    transform: translateY(-3px) !important;
    border-color: rgba(255, 95, 162, .34) !important;
    box-shadow: var(--product-card-shadow) !important;
  }

  .product-img-box,
  .collection-card__image-box {
    aspect-ratio: 1.22 / .86 !important;
    margin: 0 !important;
    border-radius: 16px !important;
    border: 1px solid rgba(15, 23, 42, .06) !important;
    background: #fff !important;
  }

  .product-img-box img,
  .collection-card__image-box img {
    object-fit: contain !important;
    padding: 8px !important;
    border-radius: 16px !important;
  }

  .badge-sale,
  .collection-card__discount {
    top: 22px !important;
    right: 22px !important;
    width: 44px !important;
    height: 44px !important;
    min-width: 44px !important;
    min-height: 44px !important;
    padding: 0 !important;
    border: 2px solid #fff !important;
    background: linear-gradient(135deg, #9ee02f 0%, #6fca13 100%) !important;
    color: #fff !important;
    font-size: 12px !important;
    font-weight: 950 !important;
    box-shadow: 0 10px 20px rgba(111, 202, 19, .28) !important;
  }

  .collection-card__body {
    display: flex !important;
    flex-direction: column !important;
    flex: 1 1 auto !important;
    padding: 13px 0 0 !important;
  }

  .collection-card__brand {
    min-height: 17px !important;
    margin-bottom: 6px !important;
    color: #344054 !important;
    font-size: 11px !important;
    font-weight: 950 !important;
    letter-spacing: .06em !important;
  }

  .collection-card__title {
    min-height: 43px !important;
    color: #141414 !important;
    font-size: 14px !important;
    line-height: 1.45 !important;
    font-weight: 850 !important;
    -webkit-line-clamp: 2 !important;
  }

  .collection-card__price-wrap {
    min-height: 30px !important;
    margin-top: 8px !important;
  }

  .collection-card__price-line {
    gap: 8px !important;
    margin-top: 0 !important;
  }

  .collection-card__sale-price {
    color: var(--product-home-red) !important;
    font-size: 15px !important;
    font-weight: 950 !important;
    white-space: nowrap !important;
  }

  .collection-card__old-price {
    color: #98a2b3 !important;
    font-size: 12px !important;
    font-weight: 750 !important;
    white-space: nowrap !important;
  }

  .collection-card__actions {
    display: grid !important;
    grid-template-columns: minmax(112px, 1fr) minmax(86px, .82fr) !important;
    gap: 8px !important;
    margin: 12px 0 0 !important;
    align-items: stretch !important;
  }

  .collection-card__cart-form {
    min-width: 0 !important;
    margin: 0 !important;
  }

  .collection-card__view-btn,
  .collection-card__cart-btn,
  .product-card .btn-outline {
    width: 100% !important;
    min-width: 0 !important;
    min-height: 38px !important;
    padding: 0 10px !important;
    border-radius: var(--product-radius-pill) !important;
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
    gap: 6px !important;
    font-size: 12px !important;
    font-weight: 950 !important;
    line-height: 1 !important;
    white-space: nowrap !important;
    word-break: keep-all !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
  }

  .collection-card__view-btn,
  .product-card .btn-outline {
    border: 1px solid rgba(155, 0, 18, .16) !important;
    background: #ffffff !important;
    color: #141414 !important;
    box-shadow: 0 4px 12px rgba(15, 23, 42, .04) !important;
  }

  .collection-card__view-btn:hover,
  .product-card .btn-outline:hover {
    border-color: rgba(255, 95, 162, .52) !important;
    background: #fff1f4 !important;
    color: var(--product-home-red) !important;
    transform: translateY(-1px) !important;
  }

  .collection-card__cart-btn {
    border: 0 !important;
    background: linear-gradient(135deg, var(--product-main-pink) 0%, var(--product-main-pink-dark) 50%, var(--product-home-red) 100%) !important;
    color: #fff !important;
    box-shadow: 0 10px 20px rgba(155, 0, 18, .18) !important;
  }

  .collection-card__cart-btn:hover:not(:disabled) {
    filter: brightness(1.03) !important;
    transform: translateY(-1px) !important;
    box-shadow: 0 13px 24px rgba(155, 0, 18, .24) !important;
  }

  .collection-card__bottom {
    margin-top: auto !important;
    padding-top: 10px !important;
  }

  .collection-card__meta {
    color: #344054 !important;
    font-size: 12px !important;
    font-weight: 850 !important;
  }

  .collection-card__heart {
    width: 32px !important;
    height: 32px !important;
    border-radius: var(--product-radius-pill) !important;
    border: 1px solid rgba(15, 23, 42, .08) !important;
    background: #fff !important;
    color: #667085 !important;
    font-size: 20px !important;
  }

  .collection-card__heart:hover {
    border-color: rgba(255, 95, 162, .38) !important;
    background: #fff1f4 !important;
    color: var(--product-home-red) !important;
  }

  @media (max-width: 1540px) {
    .product-grid,
    .collection-grid {
      grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    }
  }

  @media (max-width: 1280px) {
    .collection-layout,
    .product-page.collection-layout {
      grid-template-columns: 250px minmax(0, 1fr) !important;
      gap: 22px !important;
    }

    .product-grid,
    .collection-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
    }

    .collection-toolbar__sorts {
      grid-template-columns: repeat(5, minmax(96px, 1fr)) !important;
    }
  }

  @media (max-width: 980px) {
    .collection-container,
    .container.collection-container {
      width: min(100% - 30px, 1660px) !important;
    }

    .collection-layout,
    .product-page.collection-layout {
      grid-template-columns: 1fr !important;
    }

    .collection-filter-sidebar,
    .filter-sidebar.collection-filter-sidebar {
      position: static !important;
      max-height: none !important;
    }

    .collection-toolbar {
      grid-template-columns: 1fr !important;
    }

    .collection-toolbar__sorts {
      grid-template-columns: repeat(5, 128px) !important;
      overflow-x: auto !important;
      padding-bottom: 2px !important;
    }

    .collection-toolbar__count {
      width: max-content !important;
    }
  }

  @media (max-width: 720px) {
    .product-grid,
    .collection-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
      gap: 14px !important;
    }

    .product-card,
    .collection-card {
      padding: 10px !important;
      border-radius: 18px !important;
    }

    .collection-card__actions {
      grid-template-columns: 1fr !important;
    }
  }

  @media (max-width: 460px) {
    .product-grid,
    .collection-grid {
      grid-template-columns: 1fr !important;
    }
  }



  /* =========================================================
     PRODUCT LIST - HOME COLOR SYNC OVERRIDE
     Mục tiêu:
     - Đồng bộ màu nút/chữ với trang chủ home.css
     - Nút Xem sản phẩm: trắng + chữ đỏ rượu như trang chủ
     - Nút Thêm giỏ/Áp dụng: gradient hồng -> đỏ như trang chủ
     - Giữ chữ trên nút không xuống dòng
  ========================================================= */
  :root {
    --pl-home-red: #b01239;
    --pl-home-red-hover: #9a0027;
    --pl-home-red-dark: #9b0012;
    --pl-home-red-deep: #a4041b;
    --pl-home-pink: #ff4f97;
    --pl-home-pink-mid: #d9154f;
    --pl-home-soft: #fff4f8;
    --pl-home-soft-2: #fff1f4;
    --pl-home-text: #141414;
    --pl-home-muted: #667085;
    --pl-home-border: rgba(176, 18, 57, .18);
    --pl-home-gradient: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #a4041b 100%);
    --pl-home-gradient-hover: linear-gradient(135deg, #ff6aa7 0%, #e21a59 48%, #9b0012 100%);
    --pl-home-shadow: 0 10px 20px rgba(176, 18, 57, .18);
    --pl-home-shadow-hover: 0 14px 26px rgba(176, 18, 57, .24);
  }

  /* Tiêu đề/nhãn trang sản phẩm đồng bộ gradient trang chủ */
  .mc-category-hero__label {
    background: var(--pl-home-soft) !important;
    border-color: rgba(176, 18, 57, .18) !important;
    color: var(--pl-home-red) !important;
  }

  .mc-category-hero__title {
    color: transparent !important;
    background: linear-gradient(135deg, #ff4f97 0%, #d9154f 48%, #9b0012 100%) !important;
    -webkit-background-clip: text !important;
    background-clip: text !important;
  }

  .mc-category-hero__desc,
  .collection-card__meta,
  .rating-count {
    color: var(--pl-home-muted) !important;
  }

  /* Chữ chính trong card */
  .collection-card__brand {
    color: #344054 !important;
  }

  .collection-card__title,
  .collection-card__title a,
  .product-title-link {
    color: var(--pl-home-text) !important;
  }

  .collection-card__title a:hover,
  .product-title-link:hover {
    color: var(--pl-home-red) !important;
  }

  .collection-card__sale-price,
  .sale-price,
  .price-display,
  .product-price {
    color: var(--pl-home-red) !important;
  }

  .collection-card__old-price,
  .old-price {
    color: #98a2b3 !important;
  }

  /* Nút xem sản phẩm: giống .skin-card-view-btn trong home.css */
  .collection-card__view-btn,
  .product-card .btn-outline,
  .product-empty .btn-outline {
    min-height: 40px !important;
    border-radius: 999px !important;
    background: #ffffff !important;
    color: var(--pl-home-red) !important;
    border: 1px solid var(--pl-home-border) !important;
    box-shadow: none !important;
    font-size: 12px !important;
    font-weight: 950 !important;
    line-height: 1 !important;
    white-space: nowrap !important;
    word-break: keep-all !important;
    text-overflow: ellipsis !important;
    overflow: hidden !important;
  }

  .collection-card__view-btn:hover,
  .product-card .btn-outline:hover,
  .product-empty .btn-outline:hover {
    background: var(--pl-home-soft) !important;
    color: var(--pl-home-red-hover) !important;
    border-color: rgba(176, 18, 57, .28) !important;
    transform: translateY(-1px) !important;
  }

  /* Nút thêm giỏ: giống .skin-card-cart-btn trong home.css */
  .collection-card__cart-btn,
  .btn-login,
  .wishlist-shop-btn {
    min-height: 40px !important;
    border-radius: 999px !important;
    border: 0 !important;
    background: var(--pl-home-gradient) !important;
    color: #ffffff !important;
    box-shadow: var(--pl-home-shadow) !important;
    font-size: 12px !important;
    font-weight: 950 !important;
    line-height: 1 !important;
    white-space: nowrap !important;
    word-break: keep-all !important;
  }

  .collection-card__cart-btn:hover:not(:disabled),
  .btn-login:hover,
  .wishlist-shop-btn:hover {
    background: var(--pl-home-gradient-hover) !important;
    color: #ffffff !important;
    transform: translateY(-1px) !important;
    box-shadow: var(--pl-home-shadow-hover) !important;
    filter: none !important;
  }

  .collection-card__cart-btn:disabled {
    background: #b6b6b6 !important;
    color: #ffffff !important;
    opacity: .65 !important;
    box-shadow: none !important;
  }

  /* Giữ 2 nút cân đối và không làm chữ bị xuống dòng */
  .collection-card__actions {
    grid-template-columns: minmax(116px, 1fr) minmax(92px, .82fr) !important;
    gap: 9px !important;
  }

  .collection-card__view-btn,
  .collection-card__cart-btn {
    padding-inline: 11px !important;
  }

  /* Nút bộ lọc */
  .mc-filter-apply,
  .btn-apply-filter,
  .mc-left-filter__apply {
    background: var(--pl-home-gradient) !important;
    color: #ffffff !important;
    border: 0 !important;
    box-shadow: var(--pl-home-shadow) !important;
  }

  .mc-filter-apply:hover,
  .btn-apply-filter:hover,
  .mc-left-filter__apply:hover {
    background: var(--pl-home-gradient-hover) !important;
    color: #ffffff !important;
    box-shadow: var(--pl-home-shadow-hover) !important;
    filter: none !important;
  }

  .mc-filter-reset,
  .btn-reset-filter,
  .mc-left-filter__reset,
  .product-filter-clear {
    background: #ffffff !important;
    color: var(--pl-home-red) !important;
    border: 1px solid var(--pl-home-border) !important;
    box-shadow: none !important;
  }

  .mc-filter-reset:hover,
  .btn-reset-filter:hover,
  .mc-left-filter__reset:hover,
  .product-filter-clear:hover {
    background: var(--pl-home-soft) !important;
    color: var(--pl-home-red-hover) !important;
    border-color: rgba(176, 18, 57, .30) !important;
    text-decoration: none !important;
  }

  /* Tab sort đồng bộ chữ/nút */
  .collection-sort-tab {
    color: var(--pl-home-text) !important;
    border-color: transparent !important;
    background: transparent !important;
  }

  .collection-sort-tab:hover,
  .collection-sort-tab.active {
    background: var(--pl-home-soft) !important;
    color: var(--pl-home-red) !important;
    border-color: var(--pl-home-border) !important;
    box-shadow: inset 0 0 0 1px rgba(176, 18, 57, .06) !important;
  }

  .collection-toolbar__count {
    background: #ffffff !important;
    color: var(--pl-home-text) !important;
    border-color: rgba(15, 23, 42, .08) !important;
  }

  /* Tag đang lọc */
  .collection-filter-tags {
    background: rgba(255, 244, 248, .84) !important;
    border-color: rgba(176, 18, 57, .18) !important;
  }

  .product-filter-tags__label {
    color: var(--pl-home-text) !important;
  }

  .product-filter-tag {
    background: #ffffff !important;
    color: var(--pl-home-red) !important;
    border-color: var(--pl-home-border) !important;
  }

  .product-filter-tag strong {
    color: var(--pl-home-red) !important;
  }

  .product-filter-tag__remove {
    background: var(--pl-home-soft) !important;
    color: var(--pl-home-red) !important;
  }

  .product-filter-tag__remove:hover {
    background: var(--pl-home-red) !important;
    color: #ffffff !important;
  }

  /* Checkbox/range filter đồng bộ */
  .mc-filter-option:hover,
  .mc-filter-option input:checked ~ .mc-filter-name,
  .filter-check-row:hover,
  .filter-check-row.is-checked .filter-check-row__name {
    color: var(--pl-home-red) !important;
  }

  .mc-filter-option input:checked + .mc-filter-box,
  .filter-check-row input:checked + .filter-check-row__box {
    border-color: var(--pl-home-red) !important;
    background: var(--pl-home-gradient) !important;
    box-shadow: 0 0 0 3px rgba(176, 18, 57, .10), inset 0 0 0 3px #ffffff !important;
  }

  .price-pill span:hover,
  .price-pill input:checked + span {
    border-color: var(--pl-home-border) !important;
    color: var(--pl-home-red) !important;
  }

  .price-pill input:checked + span {
    background: var(--pl-home-soft) !important;
  }

  /* Phân trang */
  .pg-btn,
  .pg-num {
    color: var(--pl-home-red) !important;
    border-color: var(--pl-home-border) !important;
    background: #ffffff !important;
  }

  .pg-btn:hover,
  .pg-num:hover {
    color: var(--pl-home-red-hover) !important;
    background: var(--pl-home-soft) !important;
    border-color: rgba(176, 18, 57, .30) !important;
  }

  .pg-num.active {
    background: var(--pl-home-gradient) !important;
    border-color: transparent !important;
    color: #ffffff !important;
    box-shadow: var(--pl-home-shadow) !important;
  }

  /* Tim yêu thích */
  .collection-card__heart:hover {
    background: var(--pl-home-soft) !important;
    border-color: var(--pl-home-border) !important;
    color: var(--pl-home-red) !important;
  }

  @media (max-width: 720px) {
    .collection-card__actions {
      grid-template-columns: 1fr !important;
    }
  }



  /* =========================================================
     ARTISTIC CATEGORY HERO - giống mockup mới
     - Banner đầu trang mềm hơn, nghệ thuật hơn
     - Tiêu đề đỏ đồng bộ trang chủ
     - Trang điểm môi có hiệu ứng lipstick/gloss bằng CSS
  ========================================================= */
  .mc-category-hero-wrap {
    position: relative !important;
    isolation: isolate !important;
    min-height: 280px !important;
    display: flex !important;
    align-items: center !important;
    overflow: hidden !important;
    padding: 28px 16px 32px !important;
    background:
            radial-gradient(circle at 8% 76%, rgba(176, 18, 57, .14), transparent 19%),
            radial-gradient(circle at 92% 44%, rgba(255, 79, 151, .16), transparent 22%),
            linear-gradient(180deg, #fff9fb 0%, #ffeaf2 100%) !important;
    border-bottom: 1px solid rgba(176, 18, 57, .12) !important;
  }

  .mc-category-hero-wrap::before {
    content: "";
    position: absolute;
    left: -7%;
    right: -7%;
    bottom: -62px;
    height: 190px;
    z-index: -1;
    background:
            radial-gradient(ellipse at 12% 35%, rgba(176, 18, 57, .34), transparent 18%),
            radial-gradient(ellipse at 88% 55%, rgba(255, 79, 151, .28), transparent 22%),
            linear-gradient(135deg, rgba(255, 174, 197, .30), rgba(255, 79, 151, .16));
    border-radius: 50% 50% 0 0 / 70% 70% 0 0;
    filter: blur(.2px);
  }

  .mc-category-hero-wrap::after {
    content: "";
    position: absolute;
    left: 48%;
    right: 7%;
    bottom: 62px;
    height: 56px;
    z-index: -1;
    border-radius: 999px;
    background:
            linear-gradient(100deg, transparent 0%, rgba(255,255,255,.55) 22%, rgba(255, 79, 151, .24) 48%, rgba(176,18,57,.18) 76%, transparent 100%);
    transform: rotate(-8deg);
    box-shadow: 0 18px 30px rgba(176, 18, 57, .08);
  }

  .mc-category-hero {
    position: relative !important;
    max-width: 1480px !important;
    min-height: 220px !important;
    display: flex !important;
    flex-direction: column !important;
    justify-content: center !important;
    align-items: center !important;
    padding: 42px 300px 34px !important;
    text-align: center !important;
  }

  .mc-category-hero::before {
    content: "";
    position: absolute;
    left: 16px;
    bottom: 2px;
    width: 255px;
    height: 172px;
    pointer-events: none;
    background:
            radial-gradient(ellipse at 36% 74%, rgba(176, 18, 57, .18) 0 22%, transparent 23%),
            radial-gradient(ellipse at 58% 82%, rgba(255, 255, 255, .82) 0 10%, transparent 11%),
            linear-gradient(22deg, transparent 0 30%, rgba(176, 18, 57, .72) 31% 42%, rgba(255, 180, 198, .72) 43% 57%, transparent 58%),
            linear-gradient(105deg, transparent 0 58%, #c18445 59% 68%, #f3c77e 69% 75%, transparent 76%),
            linear-gradient(90deg, transparent 0 70%, #b01239 71% 83%, transparent 84%);
    border-radius: 28px;
    transform: rotate(-4deg);
    opacity: .92;
    filter: drop-shadow(0 18px 18px rgba(176, 18, 57, .10));
  }

  .mc-category-hero::after {
    content: "";
    position: absolute;
    right: 10px;
    bottom: 10px;
    width: 268px;
    height: 176px;
    pointer-events: none;
    background:
            radial-gradient(circle at 78% 22%, rgba(255,255,255,.95) 0 7px, transparent 8px),
            radial-gradient(circle at 86% 34%, rgba(255,255,255,.78) 0 5px, transparent 6px),
            linear-gradient(108deg, transparent 0 35%, rgba(176,18,57,.14) 36% 52%, rgba(255,255,255,.36) 53% 60%, transparent 61%),
            linear-gradient(130deg, transparent 0 60%, #f1a0ba 61% 67%, #fff 68% 71%, #d9154f 72% 79%, transparent 80%);
    border-radius: 30px;
    opacity: .92;
    filter: drop-shadow(0 18px 22px rgba(176, 18, 57, .10));
  }

  .mc-category-hero__label {
    position: relative !important;
    z-index: 1 !important;
    display: inline-flex !important;
    min-height: 30px !important;
    padding: 5px 19px !important;
    margin-bottom: 10px !important;
    border-radius: 999px !important;
    border: 1px solid rgba(176, 18, 57, .22) !important;
    background: rgba(255, 255, 255, .74) !important;
    color: #b01239 !important;
    box-shadow: 0 8px 18px rgba(176, 18, 57, .06) !important;
    backdrop-filter: blur(8px);
  }

  .mc-category-hero__title {
    position: relative !important;
    z-index: 1 !important;
    color: #b01239 !important;
    background: none !important;
    -webkit-background-clip: initial !important;
    background-clip: initial !important;
    font-family: Georgia, 'Times New Roman', serif !important;
    font-size: clamp(46px, 5.3vw, 78px) !important;
    font-weight: 900 !important;
    line-height: .98 !important;
    letter-spacing: -.055em !important;
    text-shadow: 0 12px 28px rgba(176, 18, 57, .12) !important;
  }

  .mc-category-hero__title::before {
    content: "✦";
    position: absolute;
    left: -48px;
    top: 10px;
    color: #d9154f;
    font-family: inherit;
    font-size: 26px;
    line-height: 1;
  }

  .mc-category-hero__title::after {
    content: " ❥";
    color: #ff4f97;
    font-size: .42em;
    vertical-align: 32%;
  }

  .mc-category-hero__desc {
    position: relative !important;
    z-index: 1 !important;
    max-width: 640px !important;
    margin-top: 14px !important;
    color: #6b2639 !important;
    font-size: 15px !important;
    font-weight: 600 !important;
    line-height: 1.7 !important;
  }

  .mc-category-hero__desc::after {
    content: "";
    display: block;
    width: 180px;
    height: 12px;
    margin: 18px auto 0;
    background:
            radial-gradient(circle at 50% 50%, #b01239 0 4px, transparent 5px),
            linear-gradient(90deg, transparent 0, #ff4f97 18%, transparent 42%, transparent 58%, #ff4f97 82%, transparent 100%);
    opacity: .9;
  }

  .collection-body-section {
    padding-top: 22px !important;
  }

  @media (max-width: 1180px) {
    .mc-category-hero {
      padding-inline: 210px !important;
    }

    .mc-category-hero::before,
    .mc-category-hero::after {
      width: 190px;
      opacity: .58;
    }
  }

  @media (max-width: 820px) {
    .mc-category-hero-wrap {
      min-height: 250px !important;
    }

    .mc-category-hero {
      padding: 42px 24px 34px !important;
    }

    .mc-category-hero::before,
    .mc-category-hero::after {
      display: none;
    }

    .mc-category-hero__title::before {
      display: none;
    }
  }

  /* =========================================================
     FINAL HEADER + HERO TEXT FIX
     - Sửa lỗi tiêu đề đầu trang bị xuống từng dòng
     - Đồng bộ logo/heading màu đỏ trang chủ
     - Đưa hero về bố cục giống mockup đã thiết kế
  ========================================================= */
  :root {
    --mc-final-red: #d20b42;
    --mc-final-red-dark: #a8062e;
    --mc-final-pink: #ff5f9d;
    --mc-final-soft: #fff1f6;
    --mc-final-border: rgba(210, 11, 66, .18);
    --mc-final-shadow: 0 18px 42px rgba(210, 11, 66, .12);
  }

  .logo,
  .logo-art,
  .logo-art__mark {
    color: var(--mc-final-red) !important;
  }

  .logo-art__mark {
    letter-spacing: -.04em !important;
    text-shadow: 0 8px 22px rgba(210, 11, 66, .16) !important;
  }

  .logo-art__tagline {
    color: #e84d78 !important;
  }

  .site-header {
    background:
            radial-gradient(circle at 7% 20%, rgba(255, 95, 157, .16), transparent 30%),
            radial-gradient(circle at 92% 8%, rgba(255, 217, 228, .78), transparent 27%),
            linear-gradient(180deg, rgba(255,255,255,.98) 0%, rgba(255,244,248,.98) 100%) !important;
  }

  .search-submit-btn,
  .art-menu-link:hover::after,
  .art-menu-link.is-active::after,
  .menu-link-item.art-menu-link.highlight::after {
    background: linear-gradient(135deg, var(--mc-final-pink) 0%, var(--mc-final-red) 56%, var(--mc-final-red-dark) 100%) !important;
  }

  .mc-category-hero-wrap {
    min-height: 300px !important;
    padding: 34px 16px 38px !important;
    display: flex !important;
    align-items: center !important;
    background:
            radial-gradient(circle at 10% 65%, rgba(210, 11, 66, .12), transparent 22%),
            radial-gradient(circle at 88% 42%, rgba(255, 95, 157, .15), transparent 24%),
            linear-gradient(180deg, #fff9fb 0%, #ffeaf2 100%) !important;
    border-bottom: 1px solid var(--mc-final-border) !important;
  }

  .mc-category-hero-wrap::before {
    bottom: -68px !important;
    height: 190px !important;
    background:
            radial-gradient(ellipse at 12% 35%, rgba(210, 11, 66, .28), transparent 18%),
            radial-gradient(ellipse at 88% 55%, rgba(255, 95, 157, .25), transparent 22%),
            linear-gradient(135deg, rgba(255, 190, 206, .36), rgba(255, 95, 157, .16)) !important;
  }

  .mc-category-hero {
    width: min(1480px, 100%) !important;
    max-width: 1480px !important;
    margin: 0 auto !important;
    box-sizing: border-box !important;
    min-height: 220px !important;
    padding: 38px 280px 32px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    text-align: center !important;
  }

  .mc-category-hero__label {
    margin-bottom: 12px !important;
    border-color: rgba(210, 11, 66, .22) !important;
    color: var(--mc-final-red) !important;
  }

  .mc-category-hero__title {
    display: block !important;
    width: 100% !important;
    max-width: none !important;
    margin: 0 auto !important;
    color: var(--mc-final-red) !important;
    background: none !important;
    -webkit-background-clip: initial !important;
    background-clip: initial !important;
    font-family: Georgia, 'Times New Roman', serif !important;
    font-size: clamp(46px, 4.6vw, 74px) !important;
    font-weight: 900 !important;
    line-height: 1.06 !important;
    letter-spacing: -.035em !important;
    text-align: center !important;
    white-space: nowrap !important;
    word-break: keep-all !important;
    overflow-wrap: normal !important;
    text-wrap: nowrap !important;
    text-shadow: 0 12px 28px rgba(210, 11, 66, .13) !important;
  }

  .mc-category-hero__title::before {
    left: max(-42px, -3vw) !important;
    top: 12px !important;
    color: var(--mc-final-red) !important;
  }

  .mc-category-hero__title::after {
    color: var(--mc-final-pink) !important;
  }

  .mc-category-hero__desc {
    max-width: 680px !important;
    margin: 14px auto 0 !important;
    color: #6d2438 !important;
    font-size: 15px !important;
    font-weight: 600 !important;
    line-height: 1.65 !important;
  }

  .mc-category-hero__desc::after {
    background:
            radial-gradient(circle at 50% 50%, var(--mc-final-red) 0 4px, transparent 5px),
            linear-gradient(90deg, transparent 0, var(--mc-final-pink) 18%, transparent 42%, transparent 58%, var(--mc-final-pink) 82%, transparent 100%) !important;
  }

  .product-sort-tabs .sort-tab.is-active,
  .collection-sort .sort-link.is-active,
  .collection-sort a.is-active,
  .mc-sort-link.is-active {
    color: var(--mc-final-red) !important;
    border-color: rgba(210, 11, 66, .28) !important;
    background: #fff3f7 !important;
  }

  @media (max-width: 1180px) {
    .mc-category-hero {
      padding-inline: 210px !important;
    }

    .mc-category-hero__title {
      font-size: clamp(42px, 5vw, 64px) !important;
    }
  }

  @media (max-width: 920px) {
    .mc-category-hero {
      padding-inline: 36px !important;
    }

    .mc-category-hero::before,
    .mc-category-hero::after,
    .mc-category-hero__title::before {
      display: none !important;
    }

    .mc-category-hero__title {
      white-space: normal !important;
      text-wrap: balance !important;
      font-size: clamp(38px, 8vw, 56px) !important;
      line-height: 1.08 !important;
    }
  }



  /* =========================================================
     FIX HERO TITLE TEXT - chống lỗi tách chữ tiếng Việt
     ========================================================= */
  .mc-category-hero__title {
    display: inline-block !important;
    width: auto !important;
    max-width: min(100%, 1180px) !important;
    margin: 0 auto !important;

    /* Dùng font hỗ trợ tiếng Việt tốt để không bị tách chữ: Tấ t */
    font-family: "Be Vietnam Pro", "Inter", "Arial", "Tahoma", sans-serif !important;
    font-size: clamp(48px, 4.25vw, 72px) !important;
    font-weight: 900 !important;
    line-height: 1.08 !important;
    letter-spacing: -0.018em !important;
    word-spacing: 0 !important;

    color: #c70b3f !important;
    background: none !important;
    -webkit-background-clip: initial !important;
    background-clip: initial !important;
    -webkit-text-fill-color: #c70b3f !important;

    white-space: nowrap !important;
    word-break: keep-all !important;
    overflow-wrap: normal !important;
    hyphens: none !important;
    text-wrap: nowrap !important;
    text-align: center !important;

    font-kerning: normal !important;
    font-variant-ligatures: none !important;
    text-rendering: geometricPrecision !important;
    unicode-bidi: isolate !important;
    overflow: visible !important;
    text-shadow: 0 12px 28px rgba(199, 11, 63, .14) !important;
  }

  .mc-category-hero__title * {
    white-space: nowrap !important;
    word-break: keep-all !important;
    overflow-wrap: normal !important;
    hyphens: none !important;
  }

  .mc-category-hero__title::before {
    left: -44px !important;
    top: 14px !important;
  }

  .mc-category-hero__title::after {
    right: -40px !important;
    top: 50% !important;
    transform: translateY(-50%) !important;
  }

  @media (max-width: 920px) {
    .mc-category-hero__title {
      display: block !important;
      width: 100% !important;
      max-width: 720px !important;
      white-space: normal !important;
      text-wrap: balance !important;
      font-size: clamp(36px, 8vw, 56px) !important;
      letter-spacing: -0.012em !important;
    }
  }

  @media (max-width: 560px) {
    .mc-category-hero__title {
      font-size: clamp(32px, 10vw, 44px) !important;
      line-height: 1.12 !important;
    }
  }



  /* =========================================================
     FINAL: Chỉnh kiểu chữ tiêu đề giống logo MyCosmetic
     - Tiêu đề dùng font serif sang giống logo
     - Không tách chữ tiếng Việt
     - Màu đỏ hồng đồng bộ trang chủ
  ========================================================= */
  .mc-category-hero__title {
    display: block !important;
    width: 100% !important;
    max-width: 1120px !important;
    margin: 6px auto 12px !important;

    font-family: var(--mc-brand-font, "Playfair Display", Georgia, "Times New Roman", serif) !important;
    font-size: clamp(54px, 5.1vw, 86px) !important;
    font-weight: 900 !important;
    line-height: .98 !important;
    letter-spacing: -0.035em !important;
    word-spacing: 0 !important;

    color: var(--mc-brand-red, #c70b3f) !important;
    background: none !important;
    -webkit-background-clip: initial !important;
    background-clip: initial !important;
    -webkit-text-fill-color: var(--mc-brand-red, #c70b3f) !important;

    text-align: center !important;
    text-transform: none !important;
    white-space: normal !important;
    word-break: keep-all !important;
    overflow-wrap: normal !important;
    hyphens: none !important;
    text-rendering: optimizeLegibility !important;
    font-kerning: normal !important;
    font-variant-ligatures: normal !important;
    unicode-bidi: isolate !important;
    text-shadow: 0 14px 30px rgba(199, 11, 63, .14) !important;
  }

  .mc-category-hero__title::before {
    left: -44px !important;
    top: 14px !important;
  }

  .mc-category-hero__title::after {
    right: -42px !important;
    top: 50% !important;
    transform: translateY(-50%) !important;
  }

  @media (max-width: 920px) {
    .mc-category-hero__title {
      font-size: clamp(40px, 8vw, 62px) !important;
      line-height: 1.04 !important;
      letter-spacing: -0.025em !important;
    }
  }

  @media (max-width: 560px) {
    .mc-category-hero__title {
      font-size: clamp(34px, 10vw, 46px) !important;
      line-height: 1.08 !important;
      letter-spacing: -0.018em !important;
    }
  }

</style>

<script>
  (function () {
    try {
      window.history.scrollRestoration = "manual";
      if (sessionStorage.getItem("mycosmetic_product_list_scroll_y")) {
        document.documentElement.classList.add("is-restoring-product-scroll");
      }
    } catch (e) {
      // ignore
    }
  })();
</script>

<c:url var="sortDefaultUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
</c:url>

<c:url var="sortNewestUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="created_desc" />
</c:url>

<c:url var="sortBestSellingUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="best_selling" />
</c:url>

<c:url var="sortPriceAscUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="price_asc" />
</c:url>

<c:url var="sortPriceDescUrl" value="/products">
  <c:if test="${not empty param.q}">
    <c:param name="q" value="${param.q}" />
  </c:if>
  <c:if test="${not empty selectedCategoryList}">
    <c:forEach var="cid" items="${selectedCategoryList}">
      <c:param name="category" value="${cid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty selectedBrandList}">
    <c:forEach var="bid" items="${selectedBrandList}">
      <c:param name="brand" value="${bid}" />
    </c:forEach>
  </c:if>
  <c:if test="${not empty priceRangeList}">
    <c:forEach var="pr" items="${priceRangeList}">
      <c:param name="priceRange" value="${pr}" />
    </c:forEach>
  </c:if>
  <c:if test="${minRating != null}">
    <c:param name="rating" value="${minRating}" />
  </c:if>
  <c:param name="sort" value="price_desc" />
</c:url>

<c:choose>
  <c:when test="${not empty primaryCategory}">
    <c:url var="resetFilterUrl" value="/products">
      <c:param name="category" value="${primaryCategory.id}" />
    </c:url>
  </c:when>
  <c:otherwise>
    <c:url var="resetFilterUrl" value="/products" />
  </c:otherwise>
</c:choose>

<section class="mc-category-hero-wrap ${isLipPage ? 'is-lip-collection' : ''}">
  <div class="mc-category-hero">
    <div class="mc-category-hero__label">Danh mục</div>
    <h1 class="mc-category-hero__title">
      <c:out value="${empty collectionTitle ? 'Sản phẩm theo danh mục' : collectionTitle}" />
    </h1>
    <p class="mc-category-hero__desc">
      <c:out value="${empty collectionDesc
                ? 'Đang hiển thị sản phẩm thuộc danh mục đã chọn. Bạn có thể lọc thêm theo thương hiệu, mức giá và đánh giá.'
                : collectionDesc}" />
    </p>
  </div>
</section>

<section class="section collection-body-section">
  <div class="container collection-container">
    <div class="product-page collection-layout">

      <aside class="filter-sidebar collection-filter-sidebar">
        <form id="filterForm" class="mc-filter-form" method="get" action="${ctx}/products#collectionResults">
          <c:if test="${not empty param.q}">
            <input type="hidden" name="q" value="${fn:escapeXml(param.q)}">
          </c:if>
          <c:if test="${not empty param.sort}">
            <input type="hidden" name="sort" value="${fn:escapeXml(param.sort)}">
          </c:if>
          <c:if test="${not empty primaryCategory}">
            <input type="hidden" name="category" value="${primaryCategory.id}">
          </c:if>

          <c:if test="${not empty sidebarMainCategoryItems}">
            <div class="mc-filter-block">
              <button type="button" class="mc-filter-title">
                <span>Danh mục sản phẩm</span>
              </button>
              <div class="mc-filter-list">
                <c:forEach var="item" items="${sidebarMainCategoryItems}">
                  <label class="mc-filter-option">
                    <input type="checkbox" name="category" value="${item.id}" ${item.selected ? 'checked' : ''}>
                    <span class="mc-filter-box"></span>
                    <span class="mc-filter-name"><c:out value="${item.name}" /></span>
                  </label>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <c:if test="${not empty sidebarTypeCategoryItems}">
            <div class="mc-filter-block">
              <button type="button" class="mc-filter-title">
                <span>Loại sản phẩm</span>
              </button>
              <div class="mc-filter-list">
                <c:forEach var="item" items="${sidebarTypeCategoryItems}">
                  <label class="mc-filter-option">
                    <input type="checkbox" name="category" value="${item.id}" ${item.selected ? 'checked' : ''}>
                    <span class="mc-filter-box"></span>
                    <span class="mc-filter-name"><c:out value="${item.name}" /></span>
                  </label>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <div class="mc-filter-block">
            <button type="button" class="mc-filter-title">
              <span>Giá</span>
            </button>
            <div class="mc-price-range-row">
              <input class="mc-price-input" type="text" value="0" aria-label="Giá thấp nhất" readonly>
              <span class="mc-price-separator">–</span>
              <input class="mc-price-input" type="text" value="100,000,000" aria-label="Giá cao nhất" readonly>
            </div>
            <c:set var="priceLt500Selected" value="${false}" />
            <c:set var="price5001000Selected" value="${false}" />
            <c:set var="priceGt1000Selected" value="${false}" />
            <c:forEach var="selectedPrice" items="${priceRangeList}">
              <c:if test="${selectedPrice == 'lt500' || selectedPrice == '0_500' || selectedPrice == 'under_500'}">
                <c:set var="priceLt500Selected" value="${true}" />
              </c:if>
              <c:if test="${selectedPrice == '500_1000'}">
                <c:set var="price5001000Selected" value="${true}" />
              </c:if>
              <c:if test="${selectedPrice == 'gt1000' || selectedPrice == 'over_1000'}">
                <c:set var="priceGt1000Selected" value="${true}" />
              </c:if>
            </c:forEach>

            <div class="mc-filter-list">
              <label class="mc-filter-option">
                <input type="checkbox" name="priceRange" value="lt500" ${priceLt500Selected ? 'checked' : ''}>
                <span class="mc-filter-box"></span>
                <span class="mc-filter-name">Dưới 500.000đ</span>
              </label>
              <label class="mc-filter-option">
                <input type="checkbox" name="priceRange" value="500_1000" ${price5001000Selected ? 'checked' : ''}>
                <span class="mc-filter-box"></span>
                <span class="mc-filter-name">500.000đ - 1.000.000đ</span>
              </label>
              <label class="mc-filter-option">
                <input type="checkbox" name="priceRange" value="gt1000" ${priceGt1000Selected ? 'checked' : ''}>
                <span class="mc-filter-box"></span>
                <span class="mc-filter-name">Trên 1.000.000đ</span>
              </label>
            </div>
            <button type="submit" class="mc-filter-apply">Áp dụng</button>
          </div>

          <c:if test="${not empty brands}">
            <div class="mc-filter-block">
              <button type="button" class="mc-filter-title">
                <span>Thương hiệu</span>
              </button>
              <div class="mc-filter-list is-scroll">
                <c:forEach var="brand" items="${brands}">
                  <c:set var="brandChecked" value="${false}" />
                  <c:forEach var="selectedBrandId" items="${selectedBrandList}">
                    <c:if test="${selectedBrandId == brand.id}">
                      <c:set var="brandChecked" value="${true}" />
                    </c:if>
                  </c:forEach>

                  <label class="mc-filter-option">
                    <input type="checkbox" name="brand" value="${brand.id}" ${brandChecked ? 'checked' : ''}>
                    <span class="mc-filter-box"></span>
                    <span class="mc-filter-name"><c:out value="${brand.name}" /></span>
                  </label>
                </c:forEach>
              </div>
            </div>
          </c:if>

          <a class="mc-filter-reset" href="${resetFilterUrl}#collectionResults">Đặt lại</a>
        </form>
      </aside>

      <main class="product-main collection-main" id="collectionResults">

        <div class="collection-toolbar" id="collectionToolbar">
          <nav class="collection-toolbar__sorts" aria-label="Sắp xếp sản phẩm">
            <a href="${sortDefaultUrl}#collectionResults" class="collection-sort-tab ${empty param.sort ? 'active' : ''}">Phổ biến</a>
            <a href="${sortNewestUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'created_desc' ? 'active' : ''}">Mới nhất</a>
            <a href="${sortBestSellingUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'best_selling' ? 'active' : ''}">Bán chạy</a>
            <a href="${sortPriceAscUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'price_asc' ? 'active' : ''}">Giá thấp</a>
            <a href="${sortPriceDescUrl}#collectionResults" class="collection-sort-tab ${param.sort == 'price_desc' ? 'active' : ''}">Giá cao</a>
          </nav>

          <div class="collection-toolbar__count">
            <c:choose>
              <c:when test="${total != null}">${total} sản phẩm</c:when>
              <c:otherwise>0 sản phẩm</c:otherwise>
            </c:choose>
          </div>
        </div>

        <div class="product-filter-tags collection-filter-tags">
          <span class="product-filter-tags__label">Đang lọc:</span>
          <c:choose>
            <c:when test="${not empty activeFilterTags}">
              <c:forEach var="filterTag" items="${activeFilterTags}">
                <span class="product-filter-tag product-filter-tag--removable">
                  <strong><c:out value="${filterTag.label}" /></strong>
                  <a class="product-filter-tag__remove"
                     href="${filterTag.removeUrl}"
                     title="Xóa bộ lọc này"
                     aria-label="Xóa bộ lọc này">×</a>
                </span>
              </c:forEach>
              <a class="product-filter-clear" href="${resetFilterUrl}#collectionResults">Xóa bộ lọc</a>
            </c:when>
            <c:otherwise>
              <span class="product-filter-tag product-filter-tag--muted"><strong>Tất cả sản phẩm</strong></span>
            </c:otherwise>
          </c:choose>
        </div>

        <div class="product-grid collection-grid">
          <c:choose>
            <c:when test="${not empty products}">
              <c:forEach var="product" items="${products}">
                <c:set var="rawImage" value="${product.imageUrl}" />

                <c:choose>
                  <c:when test="${not empty product.slug}">
                    <c:set var="productUrl" value="${ctx}/product/${product.slug}?id=${product.id}" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="productUrl" value="${ctx}/product?id=${product.id}" />
                  </c:otherwise>
                </c:choose>

                <c:choose>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'http')}">
                    <c:set var="productImageSrc" value="${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, '/')}">
                    <c:set var="productImageSrc" value="${ctx}${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'uploads/')}">
                    <c:set var="productImageSrc" value="${ctx}/${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'assets/')}">
                    <c:set var="productImageSrc" value="${ctx}/${rawImage}" />
                    <c:set var="productImageAlt" value="" />
                  </c:when>
                  <c:when test="${not empty rawImage and fn:startsWith(rawImage, 'products/')}">
                    <c:set var="imageFileName" value="${fn:substringAfter(rawImage, 'products/')}" />
                    <c:set var="productImageSrc" value="${ctx}/uploads/product/${imageFileName}" />
                    <c:set var="productImageAlt" value="${ctx}/${rawImage}" />
                  </c:when>
                  <c:when test="${not empty rawImage}">
                    <c:set var="productImageSrc" value="${ctx}/uploads/product/${rawImage}" />
                    <c:set var="productImageAlt" value="${ctx}/${rawImage}" />
                  </c:when>
                  <c:otherwise>
                    <c:set var="productImageSrc" value="" />
                    <c:set var="productImageAlt" value="" />
                  </c:otherwise>
                </c:choose>

                <article class="product-card collection-card">
                  <c:if test="${product.finalPrice lt product.price}">
                    <div class="badge-sale collection-card__discount">
                      <c:choose>
                        <c:when test="${product.discountPercent > 0}">-${product.discountPercent}%</c:when>
                        <c:otherwise>SALE</c:otherwise>
                      </c:choose>
                    </div>
                  </c:if>

                  <a class="product-img-link collection-card__image-link"
                     href="${productUrl}"
                     aria-label="Xem chi tiết ${fn:escapeXml(product.title)}">
                    <div class="product-img-box collection-card__image-box">
                      <c:choose>
                        <c:when test="${not empty productImageSrc}">
                          <img src="${productImageSrc}"
                               data-alt-src="${productImageAlt}"
                               alt="${fn:escapeXml(product.title)}"
                               onerror="if(this.dataset.altSrc){this.src=this.dataset.altSrc;this.dataset.altSrc='';}else{this.style.display='none';this.closest('.collection-card__image-box').classList.add('is-missing');}">
                        </c:when>
                        <c:otherwise>
                          <div class="collection-card__image-placeholder">MyCosmetic</div>
                        </c:otherwise>
                      </c:choose>
                    </div>
                  </a>

                  <div class="collection-card__body">
                    <div class="collection-card__brand">
                      <c:choose>
                        <c:when test="${not empty product.brandName}"><c:out value="${product.brandName}" /></c:when>
                        <c:otherwise>MYCOSMETIC</c:otherwise>
                      </c:choose>
                    </div>

                    <h3 class="collection-card__title">
                      <a class="product-title-link" href="${productUrl}"><c:out value="${product.title}" /></a>
                    </h3>

                    <div class="collection-card__price-wrap">
                      <c:choose>
                        <c:when test="${product.finalPrice lt product.price}">
                          <div class="collection-card__price-line">
                            <span class="collection-card__sale-price"><fmt:formatNumber value="${product.finalPrice}" type="number" groupingUsed="true"/>₫</span>
                            <span class="collection-card__old-price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>₫</span>
                          </div>
                        </c:when>
                        <c:otherwise>
                          <div class="collection-card__price-line">
                            <span class="collection-card__sale-price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>₫</span>
                          </div>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <div class="collection-card__actions">
                      <a class="collection-card__view-btn" href="${productUrl}">Xem sản phẩm</a>
                      <form method="post" action="${ctx}/cart/add" class="collection-card__cart-form">
                        <input type="hidden" name="productId" value="${product.id}">
                        <input type="hidden" name="quantity" value="1">
                        <input type="hidden" name="quickAdd" value="1">
                        <c:if test="${not empty csrfToken}">
                          <input type="hidden" name="csrfToken" value="${csrfToken}">
                        </c:if>
                        <button type="submit" class="collection-card__cart-btn" ${product.stock <= 0 ? 'disabled' : ''}>Thêm giỏ</button>
                      </form>
                    </div>

                    <div class="collection-card__bottom">
                      <div class="collection-card__meta">
                        <c:choose>
                          <c:when test="${product.soldQuantity > 0}">${product.soldQuantity} đã bán</c:when>
                          <c:otherwise>${product.reviewCount} đánh giá</c:otherwise>
                        </c:choose>
                      </div>
                      <a href="${productUrl}" class="collection-card__heart" aria-label="Xem chi tiết sản phẩm">♡</a>
                    </div>
                  </div>
                </article>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="product-empty">
                <div class="product-empty__title">Không tìm thấy sản phẩm phù hợp</div>
                <div class="product-empty__text">Hãy thử chọn danh mục khác, bỏ bớt bộ lọc hoặc quay lại danh sách tất cả sản phẩm.</div>
                <a class="btn-outline" href="${ctx}/products#collectionResults">Xem tất cả sản phẩm</a>
              </div>
            </c:otherwise>
          </c:choose>
        </div>

        <c:if test="${totalPages != null && totalPages > 1}">
          <div class="pagination-wrap">
            <div class="pagination">
              <c:url var="prevPageUrl" value="/products">
                <c:param name="page" value="${page - 1}" />
                <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}" /></c:if>
                <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
                <c:if test="${not empty priceRangeList}"><c:forEach var="pr" items="${priceRangeList}"><c:param name="priceRange" value="${pr}" /></c:forEach></c:if>
                <c:if test="${not empty selectedCategoryList}"><c:forEach var="cid" items="${selectedCategoryList}"><c:param name="category" value="${cid}" /></c:forEach></c:if>
                <c:if test="${not empty selectedBrandList}"><c:forEach var="bid" items="${selectedBrandList}"><c:param name="brand" value="${bid}" /></c:forEach></c:if>
                <c:if test="${minRating != null}"><c:param name="rating" value="${minRating}" /></c:if>
              </c:url>

              <c:url var="nextPageUrl" value="/products">
                <c:param name="page" value="${page + 1}" />
                <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}" /></c:if>
                <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
                <c:if test="${not empty priceRangeList}"><c:forEach var="pr" items="${priceRangeList}"><c:param name="priceRange" value="${pr}" /></c:forEach></c:if>
                <c:if test="${not empty selectedCategoryList}"><c:forEach var="cid" items="${selectedCategoryList}"><c:param name="category" value="${cid}" /></c:forEach></c:if>
                <c:if test="${not empty selectedBrandList}"><c:forEach var="bid" items="${selectedBrandList}"><c:param name="brand" value="${bid}" /></c:forEach></c:if>
                <c:if test="${minRating != null}"><c:param name="rating" value="${minRating}" /></c:if>
              </c:url>

              <c:choose>
                <c:when test="${page != null && page > 1}"><a class="pg-btn" href="${prevPageUrl}#collectionResults">‹</a></c:when>
                <c:otherwise><span class="pg-btn disabled">‹</span></c:otherwise>
              </c:choose>

              <c:forEach begin="1" end="${totalPages}" var="p">
                <c:choose>
                  <c:when test="${p == page}"><span class="pg-num active">${p}</span></c:when>
                  <c:otherwise>
                    <c:url var="pageUrl" value="/products">
                      <c:param name="page" value="${p}" />
                      <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}" /></c:if>
                      <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}" /></c:if>
                      <c:if test="${not empty priceRangeList}"><c:forEach var="pr" items="${priceRangeList}"><c:param name="priceRange" value="${pr}" /></c:forEach></c:if>
                      <c:if test="${not empty selectedCategoryList}"><c:forEach var="cid" items="${selectedCategoryList}"><c:param name="category" value="${cid}" /></c:forEach></c:if>
                      <c:if test="${not empty selectedBrandList}"><c:forEach var="bid" items="${selectedBrandList}"><c:param name="brand" value="${bid}" /></c:forEach></c:if>
                      <c:if test="${minRating != null}"><c:param name="rating" value="${minRating}" /></c:if>
                    </c:url>
                    <a class="pg-num" href="${pageUrl}#collectionResults">${p}</a>
                  </c:otherwise>
                </c:choose>
              </c:forEach>

              <c:choose>
                <c:when test="${page != null && page < totalPages}"><a class="pg-btn" href="${nextPageUrl}#collectionResults">›</a></c:when>
                <c:otherwise><span class="pg-btn disabled">›</span></c:otherwise>
              </c:choose>
            </div>
          </div>
        </c:if>
      </main>
    </div>
  </div>
</section>

<script>
  (function () {
    const SCROLL_KEY = "mycosmetic_product_list_scroll_y";

    try {
      window.history.scrollRestoration = "manual";
    } catch (e) {
      // ignore
    }

    function shouldRememberScroll(target) {
      if (!target) {
        return false;
      }

      return Boolean(
              target.closest(".collection-toolbar") ||
              target.closest(".collection-filter-sidebar") ||
              target.closest(".collection-filter-tags") ||
              target.closest(".pagination")
      );
    }

    function saveScrollPosition() {
      try {
        sessionStorage.setItem(SCROLL_KEY, String(window.scrollY || window.pageYOffset || 0));
      } catch (e) {
        // ignore
      }
    }

    document.addEventListener("click", function (event) {
      const link = event.target.closest("a");

      if (link && link.href && link.href.indexOf("/products") !== -1 && shouldRememberScroll(link)) {
        saveScrollPosition();
      }
    }, true);

    document.addEventListener("change", function (event) {
      if (event.target && event.target.closest("#filterForm")) {
        saveScrollPosition();
      }
    }, true);

    document.addEventListener("submit", function (event) {
      if (event.target && event.target.matches("#filterForm")) {
        saveScrollPosition();
      }
    }, true);

    function restoreScroll() {
      let savedY = null;

      try {
        savedY = sessionStorage.getItem(SCROLL_KEY);
        sessionStorage.removeItem(SCROLL_KEY);
      } catch (e) {
        savedY = null;
      }

      if (!savedY) {
        document.documentElement.classList.remove("is-restoring-product-scroll");
        return;
      }

      const y = parseInt(savedY, 10) || 0;

      requestAnimationFrame(function () {
        window.scrollTo(0, y);

        requestAnimationFrame(function () {
          window.scrollTo(0, y);
          document.documentElement.classList.remove("is-restoring-product-scroll");
        });
      });
    }

    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", restoreScroll);
    } else {
      restoreScroll();
    }
  })();
</script>
