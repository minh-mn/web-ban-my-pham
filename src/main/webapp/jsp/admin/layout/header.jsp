<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width,initial-scale=1">

	<title>
		<c:out value="${not empty pageTitle ? pageTitle : 'Admin Panel'}" />
	</title>

	<!-- Admin Design System: tokens, buttons, cards, forms, tables -->
	<link rel="stylesheet"
	      href="${pageContext.request.contextPath}/assets/css/admin/admin-base.css?v=20260615_ui7_banner">

	<!-- Red button theme: load sớm để CSS admin/page phía sau có thể đồng bộ lại tone mới -->
	<link rel="stylesheet"
	      href="${pageContext.request.contextPath}/assets/css/theme-red-buttons.css?v=20260613_8">

	<!-- Admin Layout: wrapper, sidebar, main, topbar, notification -->
	<link rel="stylesheet"
	      href="${pageContext.request.contextPath}/assets/css/admin/admin-layout.css?v=20260615_ui7_banner">

	<!-- Page CSS: list/form/detail CSS theo từng trang admin -->
	<c:if test="${not empty pageCss}">
		<link rel="stylesheet"
		      href="${pageContext.request.contextPath}${pageCss}?v=20260615_ui7_banner">
	</c:if>
</head>

<body class="admin-body admin-ui-v2">
<div class="admin-wrapper">
