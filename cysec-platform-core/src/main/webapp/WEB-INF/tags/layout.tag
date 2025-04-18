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

    <!-- This project uses Bootstrap 5.3.3. Note that some of the bootstrap styles are customized. -->
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.8/dist/umd/popper.min.js" integrity="sha384-I7E8VVD/ismYTF4hNIPjVp/Zjvgyol6VFvRkX/vR+Vc4jQkC+hVqc2pM8ODewa9r" crossorigin="anonymous"></script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
    <link rel="stylesheet" type="text/css" href="${context}/public/css/bootstrap-customization.css"/>

    <!-- Merge DOM elements when reloading the sections of the page using this library-->
    <script src="https://cdn.jsdelivr.net/npm/morphdom@2.7.4/dist/morphdom-umd.min.js"></script>

    <link rel="stylesheet" type="text/css" href="${context}/public/font-awesome-4.7.0/css/font-awesome.css">
    <link rel="stylesheet" type="text/css" href="${context}/public/css/fonts.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/public/css/colors.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/public/css/typography.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/public/css/simple-sidebar.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/public/css/main.css"/>
    <link rel="stylesheet" type="text/css" href="${context}/public/css/grades.css"/>
    <jsp:invoke fragment="links"/>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
    <script src="${context}/public/js/urlHandler.js"></script>
    <script src="${context}/public/js/alert.js"></script>
    <jsp:invoke fragment="scripts"/>
</head>
<body>
<jsp:invoke fragment="header"/>
<div id="wrapper">
    <jsp:doBody/>
</div>
<footer>
    <jsp:invoke fragment="footer"/>
</footer>
</body>
</html>
