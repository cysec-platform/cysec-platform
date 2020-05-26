<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="coachId" value="${it.coachId}"/>
<c:set var="question" value="${it.question}"/>
<c:set var="answer" value="${it.answer}"/>
<c:set var="qid" value="${question.getId()}"/>

<!-- Date -->
<div class="questionnaire-answers col-xs-12">
    <input type="date" name="${qid}" required pattern="[0-9]{4}-[0-9]{2}-[0-9]{2}"
           onchange="updateAnswer(event)"
    <c:if test="${answer != null}">
           value="${answer.getText()}"
    </c:if>
    >
</div>