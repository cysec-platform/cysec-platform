<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="question" value="${ it.question }"/>
<c:set var="isFlagged" value="${it.flagStatus.get(question)}"/>
<c:set var="answer" value="${ it.answer }"/>

<div class="p-5">
    <!-- flagging -->
    <button id="question-flag" class="darkblue" data-flagged="${isFlagged}" onclick="toggleFlagged()">
        <c:if test="${isFlagged}">
            ${it.msg.unflagQuestion}
        </c:if>
        <c:if test="${!isFlagged}">
            ${it.msg.flagQuestion}
        </c:if>
    </button>
</div>
