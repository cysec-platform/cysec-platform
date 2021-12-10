<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>

<div id="pagination" class="row">
    <div class="col-xs-8">
        <c:forEach var="active" items="${it.actives}">
            <c:set var="aid" value="${active.getId()}"/>
            <a href="${baseUrl}/app/coach.jsp?fqcn=${it.fqcn}&question=${aid}" class="pagination-element" title="${aid}">
                <img class="pagination-img" src="${baseUrl}/assets/${it.question.getId().equals(aid) ? 'status_in_progress.png' : 'status_empty.png'}">
            </a>
        </c:forEach>
    </div>
    <div class="col-xs-4 text-right">
        <h4 class="next-question">
            <a href="${baseUrl}${it.next}">${it.msg.next}
                <img src="${baseUrl}/assets/arrow_blue.png"/>
            </a>
        </h4>
    </div>
</div>
