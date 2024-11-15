<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>

<div id="pagination" class="container-fluid">
    <div class="row">
        <div class="col col-8">
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
        <div class="col col-4 text-right">
            <h5 class="next-question">
                <button id="next-button" onmousedown="window.location = '${baseUrl}${it.next}'">
                    ${it.msg.next}
                    <img src="${baseUrl}/assets/arrow_blue.png"/>
                    <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><style>.spinner_P7sC{transform-origin:center;animation:spinner_svv2 .75s infinite linear}@keyframes spinner_svv2{100%{transform:rotate(360deg)}}</style><path d="M10.14,1.16a11,11,0,0,0-9,8.92A1.59,1.59,0,0,0,2.46,12,1.52,1.52,0,0,0,4.11,10.7a8,8,0,0,1,6.66-6.61A1.42,1.42,0,0,0,12,2.69h0A1.57,1.57,0,0,0,10.14,1.16Z" class="spinner_P7sC"/></svg>
                </button>
            </h5>
        </div>
    </div>
</div>
