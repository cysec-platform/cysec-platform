<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="scheme" value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://"/>
<c:set var="host" value="${header['x-forwarded-host'] != null ? header['x-forwarded-host'] : header.host}"/>

<c:set var="api" value="${pageContext.request.contextPath}/api/rest/resources/"/>

<div class="container-fluid">
    <div class="row min-vh-100">
        <div class="col p-5">
            <div class="d-grid">
                <c:if test="${it.enableRecommendations}">
                    <%@include file="recommended.jsp"%>
                </c:if>
                <%@include file="coaches.jsp"%>
                <%@include file="remaining.jsp"%>
            </div>
        </div>
        <c:if test="${it.enableSidebar}">
            <div class="col col-4 p-5 bg-lightbluegrey">
                <div class="sidebar-nav sidebar-content">
                    <%@include file="sidebar/skillboard.jsp"%>
                    <%@include file="sidebar/grades.jsp"%>
                    <%@include file="sidebar/badges.jsp"%>
                </div>
            </div>
        </c:if>
    </div>
</div>
