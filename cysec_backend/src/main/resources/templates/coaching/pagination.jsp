<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>

<div id="pagination" class="row">
    <div id="pagination-pages" class="col-xs-8">
        <!-- icons for pagination are inserted here -->
        <c:forEach var="active" items="${it.actives}">
            <c:set var="aid" value="${active.getId()}"/>
            <a href="${baseUrl}/app/coach.jsp?fqcn=${it.fqcn}&question=${aid}">
                <div id="page-${aid}" title="page-${aid}"
                     class="col-xs-1 ${it.question.getId().equals(aid) ? 'pagination-current' : ''}"
                     style="cursor: pointer">
                    <img class="pagination-img" src="${baseUrl}/assets/status_empty.png">
                    <span class="pagination-text">${aid}</span>
                </div>
            </a>
        </c:forEach>
    </div>

    <div id="pagination-next" class="col-xs-4 text-right">
        <h4 id="next-question" class="next-question">
            <a href="${baseUrl}${it.next}">${it.msg.next}
                <img src="${baseUrl}/assets/arrow_blue.png"/>
            </a>
        </h4>
    </div>
</div>
