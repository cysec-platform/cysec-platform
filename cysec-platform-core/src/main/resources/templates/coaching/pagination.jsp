<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>

<div id="pagination" class="container-fluid align-items-end">
    <div class="d-flex w-100 justify-content-between">
        <div class="">
            <c:forEach var="active" items="${it.actives}">
                <c:set var="question" value="${active.getSecond()}" />
                <c:set var="questionFqcn" value="${active.getFirst()}" />
                <c:set var="answer" value="${it.answers.get(active)}" />
                <c:set var="questionIdUnique" value="${questionFqcn.toString()}:${question.getId()}" />
                <c:set var="isFlagged" value="${it.flagStatus.get(questionIdUnique)}" />
                <c:set var="aid" value="${question.getId()}"/>

                <c:set var="text"> <c:out value="${question.getText()}"/></c:set> <%-- use c:out to escape strings --%>
                <c:set var="intro"><c:out value="${question.getIntroduction()}" /></c:set>

                <a
                        href="${baseUrl}/app/coach.jsp?fqcn=${questionFqcn.toString()}&question=${aid}"
                        class="pagination-element"
                        data-bs-custom-class="pagination-tooltip"
                        data-bs-title="<h3>${text}</h3><p>${intro}</p>"
                        data-bs-toggle="tooltip"
                        data-bs-placement="top"
                        data-bs-html="true"
                >
                    <img
                        class="pagination-img"
                        src="${baseUrl}/assets/${it.question.getId() == aid && it.fqcn.toString() == questionFqcn
                            ? 'status_in_progress.png'
                            : isFlagged
                                    ? 'status_flagged.png'
                                    : answer != null
                                        ? 'status_done.png'
                                        : 'status_empty.png'}"
                    >
                </a>
            </c:forEach>

            &nbsp; | &nbsp;

            <%-- summary page --%>
            <a
                    href="${baseUrl}${it.summary}"
                    class="pagination-element"
                    data-bs-custom-class="pagination-tooltip"
                    data-bs-title="<h3>${it.msg.summary}</h3>"
                    data-bs-toggle="tooltip"
                    data-bs-placement="top"
                    data-bs-html="true"
            >
                <img class="pagination-img" src="${baseUrl}/assets/status_summary.png">
            </a>
        </div>
        <h5 class="next-question align-self-center">
            <button id="next-button" onmousedown="window.location = '${baseUrl}${it.next}'">
                ${it.msg.next}
                <img src="${baseUrl}/assets/arrow_blue.png"/>
                <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><style>.spinner_P7sC{transform-origin:center;animation:spinner_svv2 .75s infinite linear}@keyframes spinner_svv2{100%{transform:rotate(360deg)}}</style><path d="M10.14,1.16a11,11,0,0,0-9,8.92A1.59,1.59,0,0,0,2.46,12,1.52,1.52,0,0,0,4.11,10.7a8,8,0,0,1,6.66-6.61A1.42,1.42,0,0,0,12,2.69h0A1.57,1.57,0,0,0,10.14,1.16Z" class="spinner_P7sC"/></svg>
            </button>
        </h5>
    </div>
</div>
