<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="scheme" value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://"/>
<c:set var="host" value="${header['x-forwarded-host'] != null ? header['x-forwarded-host'] : header.host}"/>

<c:set var="api" value="${pageContext.request.contextPath}/api/rest/resources/"/>

<div class="row dashboard-wrapper bg-lightbluegrey">
    <div id="page-content-wrapper" class="col-xs-12 bg-white">
        <div class="row">
            <%@include file="recommended.jsp"%>
            <%@include file="coaches.jsp"%>
            <%@include file="remaining.jsp"%>
        </div>
    </div>
</div>
