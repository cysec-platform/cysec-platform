<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="scheme" value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://"/>
<c:set var="host" value="${header['x-forwarded-host'] != null ? header['x-forwarded-host'] : header.host}"/>
<c:set var="api" value="${pageContext.request.contextPath}/api/rest/resources/"/>

<div class="badge-content-wrapper scrollable">
    <eu.smesec.totalcross.main>
        <div class="row">
            <div class="col-xs-12">
                <h1>${it.msg.title}</h1>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12">
                <c:choose>
                    <c:when test="${not empty it.badges }">
                        <p>${it.msg.unlocked}:</p>
                        <c:forEach var="badge" items="${it.badges}">
                            <div class="badge-single">
                                <img src="${not empty badge.getImagePath() ? scheme.concat(host).concat(api).concat(badge.getImagePath()) : '../assets/skillboard.png'}">
                                <h3>${badge.getName()}</h3>
                                <div>${badge.getDescription()}</div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p>No badges unlocked yet.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </eu.smesec.totalcross.main>
</div>