<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="coachId" value="${it.coachId}"/>
<c:set var="question" value="${it.question}"/>
<c:set var="answer" value="${it.answer}"/>
<c:set var="qid" value="${question.getId()}"/>
<c:set var="options" value="${question.getOptions().getOption()}"/>

<!-- Yes No -->
<div>
    <!-- options -->
    <div class="row">
        <div class="questionnaire-answers col col-6">
            <label>
                <input type="radio" name="${qid}" value="${qid}o1"
                       onchange="updateAnswer(event)"
                        <c:if test="${answer != null && answer.getText().endsWith('o1')}">
                            checked="checked"
                        </c:if>
                />
                <span class="questionnaire-answers-text">Yes</span>
            </label>
        </div>
        <div class="questionnaire-answers col col-6">
            <label>
                <input type="radio" name="${qid}" value="${qid}o2"
                       onchange="updateAnswer(event)"
                        <c:if test="${answer != null && answer.getText().endsWith('o2')}">
                            checked="checked"
                        </c:if>
                />
                <span class="questionnaire-answers-text">No</span>
            </label>
        </div>
    </div>
    <!-- option comment -->
    <div id="comment" class="row" style="display: ${answer != null ? 'block' : 'none'}">
        <c:if test="${answer != null}">
            <c:forEach var="option" items="${options}">
                <c:if test="${option.getId().equals(answer.getText())}">
                    <div class="col col-12">
                        ${option.getComment()}
                    </div>
                </c:if>
            </c:forEach>
        </c:if>
    </div>
</div>
