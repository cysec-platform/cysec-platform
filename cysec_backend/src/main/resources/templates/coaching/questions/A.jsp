<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="coachId" value="${it.coachId}"/>
<c:set var="question" value="${it.question}"/>
<c:set var="answer" value="${it.answer}"/>
<c:set var="qid" value="${question.getId()}"/>

<!-- A -->
<div>
    <!-- options -->
    <div class="row">
        <c:forEach var="option" items="${question.getOptions().getOption()}">
            <c:set var="oid" value="${option.getId()}"/>
            <div class="questionnaire-answers col-xs-12">
                <label>
                    <input type="radio" name="${qid}" value="${oid}" onchange="updateAnswer(event)"
                    <c:if test="${answer != null && answer.getText().equals(oid)}">
                           checked="checked"
                    </c:if>
                    >
                    <span>${option.getText()}</span>
                </label>
            </div>
        </c:forEach>
    </div>

    <!-- option comment -->
    <div id="comment" class="row" style="display: ${answer != null ? 'block' : 'none'}">
        <c:if test="${ answer != null }">
            <c:forEach var="option" items="${question.getOptions().getOption()}">
                <c:if test="${answer.getText().equals(option.getId())}">
                    ${option.getComment()}
                </c:if>
            </c:forEach>
        </c:if>
    </div>
</div>
