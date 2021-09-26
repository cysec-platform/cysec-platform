<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="coachId" value="${it.coachId}"/>
<c:set var="question" value="${it.question}"/>
<c:set var="answer" value="${it.answer}"/>
<c:set var="qid" value="${question.getId()}"/>

<!-- Likert -->
<div>
    <!-- options -->
    <div class="row">
        <c:forEach var="option" items="${question.getOptions().getOption()}">
            <c:set var="oid" value="${option.getId()}"/>
            <c:set var="otext" value="${option.getText()}"/>
            <div class="likertGroup questionnaire-answers col-xs-12">
                <label for="${oid}">
                    <input type="radio" id="${oid}" name="${question.getId()}" value="${otext}"
                           onchange="updateAnswer(event)"
                    <c:if test="${ answer !=  null && otext.equals(answer.getText())}">
                           checked="checked"
                    </c:if>
                    >
                    <span>${otext}</span>
                </label>
            </div>
        </c:forEach>
    </div>

    <!-- option comment -->
    <div id="comment" class="row" style="display: ${answer != null ? 'block' : 'none'}">
        <c:if test="${answer != null}">
            <c:forEach var="option" items="${question.getOptions().getOption()}">
                <c:if test="${option.getText().equals(answer.getText())}">
                    <div class="col-xs-12">
                        ${ option.comment }
                    </div>
                </c:if>
            </c:forEach>
        </c:if>
    </div>
</div>
