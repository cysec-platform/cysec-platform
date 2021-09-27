<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="scheme" value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://"/>
<c:set var="host" value="${header['x-forwarded-host'] != null ? header['x-forwarded-host'] : header.host}"/>

<c:set var="api" value="${pageContext.request.contextPath}/api/rest/resources/"/>

<div class="row dashboard-wrapper bg-lightbluegrey">
    <div id="page-content-wrapper" class="col-xs-8 bg-white">
        <div class="row">
            <%@include file="recommended.jsp"%>
            <%@include file="coaches.jsp"%>
            <%@include file="remaining.jsp"%>
        </div>
    </div>
    <div id="sidebar-wrapper" class="col-xs-4 bg-lightbluegrey">
        <div class="sidebar-nav sidebar-content">
            <%@include file="sidebar/skillboard.jsp"%>
            <%@include file="sidebar/grades.jsp"%>
            <%@include file="sidebar/badges.jsp"%>
        </div>
    </div>
</div>
