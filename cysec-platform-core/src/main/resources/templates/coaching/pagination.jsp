<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>

<div id="pagination" class="row">
    <div class="col-xs-8">
        <c:forEach var="active" items="${it.actives}">
            <c:set var="aid" value="${active.getId()}"/>

            <c:set var="text"> <c:out value="${active.getText()}"/></c:set> <%-- use c:out to escape strings --%>
            <c:set var="intro"><c:out value="${active.getIntroduction()}" /></c:set> 

            <a 
                href="${baseUrl}/app/coach.jsp?fqcn=${it.fqcn}&question=${aid}"
                class="pagination-element"
                data-title="<h3>${text}</h3><p>${intro}</p>"
                data-toggle="tooltip"
                data-placement="top"
                data-html="true"
            >
                <img class="pagination-img" src="${baseUrl}/assets/${it.question.getId().equals(aid) ? 'status_in_progress.png' : 'status_empty.png'}">
            </a>
        </c:forEach>
        <%-- summary page --%>
        <a 
            href="${baseUrl}${it.summary}"
            class="pagination-element"
            data-title="<h3>${it.msg.summary}</h3>"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
        >
            <img class="pagination-img" src="${baseUrl}/assets/status_summary.png">
        </a>
    </div>
    <div class="col-xs-4 text-right">
        <h4 class="next-question">
            <a onmousedown="window.location = '${baseUrl}${it.next}'">${it.msg.next}
                <img src="${baseUrl}/assets/arrow_blue.png"/>
            </a>
        </h4>
    </div>
</div>
