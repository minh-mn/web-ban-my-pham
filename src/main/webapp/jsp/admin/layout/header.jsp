<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title><c:out
		value="${pageTitle != null ? pageTitle : 'Admin Panel'}" /></title>

<!-- Global Base (chung toàn site) -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/base.css?v=1">

<!-- Admin Design System (tokens/components) -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/admin/admin-base.css?v=1">

<!-- Admin Layout (wrapper/sidebar/main/container) -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/admin/admin-layout.css?v=1">

<!-- Page CSS (tuỳ trang) -->
<c:if test="${not empty pageCss}">
	<link rel="stylesheet"
		href="${pageContext.request.contextPath}${pageCss}?v=1">
</c:if>
</head>

<body class="admin-body">
	<div class="admin-wrapper">