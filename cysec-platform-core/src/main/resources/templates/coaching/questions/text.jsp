<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="coachId" value="${it.coachId}"/>
<c:set var="question" value="${it.question}"/>
<c:set var="answer" value="${it.answer}"/>
<c:set var="qid" value="${question.getId()}"/>

<!-- Text -->
<div class="row">
    <div class="col-xs-12">
        <c:set var="content" value="${answer != null ? answer.getText() : '' }" />
        <textarea name="${qid}" style="padding: 5px;" rows="8" cols="70"
                  onchange="updateAnswer(event)">
            <c:out value="${content}" />
        </textarea>
    </div>
</div>
