<%@tag description="base layout" pageEncoding="UTF-8" %>
<%@attribute name="links" fragment="true" %>
<%@attribute name="scripts" fragment="true" %>
<%@attribute name="header" fragment="true" %>
<%@attribute name="footer" fragment="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="scheme"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}"/>
<c:set var="host" value="${header['x-forwarded-host'] != null ? header['x-forwarded-host'] : header.host}"/>
<c:set var="context" value="${pageContext.request.contextPath}"/>
<c:set var="home" value="${scheme}://${host}${context}" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>CySec</title>
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/bootstrap-3.3.7-dist/css/bootstrap.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/font-awesome-4.7.0/css/font-awesome.css">
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/css/fonts.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/css/colors.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/css/typography.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/css/simple-sidebar.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/css/main.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/../../src/main/webapp/public/css/grades.css"/>
    <jsp:invoke fragment="links"/>
    <script src="https://code.jquery.com/jquery-3.3.1.min.js"
            integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8="
            crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/promise-polyfill@8.1/dist/polyfill.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/whatwg-fetch@3.0/dist/fetch.umd.min.js"></script>
    <script src="${context}/../../src/main/webapp/public/bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
    <script src="${context}/../../src/main/webapp/public/js/urlHandler.js"></script>
    <script src="${context}/../../src/main/webapp/public/js/alert.js"></script>
    <jsp:invoke fragment="scripts"/>
</head>
<body>
<header class="global-nav">
    <jsp:invoke fragment="header"/>
</header>
<div id="wrapper" class="container">
    <jsp:doBody/>
</div>
<footer>
    <jsp:invoke fragment="footer"/>
</footer>
</body>
</html>