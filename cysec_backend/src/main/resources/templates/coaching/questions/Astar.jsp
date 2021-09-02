<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="coachId" value="${it.coachId}"/>
<c:set var="question" value="${it.question}"/>
<c:set var="answer" value="${it.answer}"/>
<c:set var="qid" value="${question.getId()}"/>

<!-- A star -->
<div>
    <!-- options -->
    <div class="row">
        <c:forEach var="option" items="${question.getOptions().getOption()}">
            <c:set var="oid" value="${option.getId()}" />
            <div class="questionnaire-answers col-xs-12">
                <label>
                    <input type="checkbox" name="${question.getId()}" value="${oid}"
                           onchange="updateAnswer(event)"
                    <c:if test="${answer != null && answer.getAidList().contains(oid)}">
                           checked="checked"
                    </c:if>
                    >
                    <span>${ option.getText() }</span>
                </label>

                <c:set var="oAttachments" value="${option.getAttachments()}"/>
                <c:if test="${oAttachments != null}">
                    <c:forEach var="oAttachment" items="${oAttachments.getAttachment()}">
                        <div class="col-lg-6">
                            <img src="data:${ oAttachment.getMime() };base64, ${ oAttachment.getContent().getValue() }"
                                 width="50"/>
                        </div>
                    </c:forEach>
                </c:if>
            </div>
        </c:forEach>
    </div>

    <!-- option comment -->
    <div>
        <c:forEach var="option" items="${question.getOptions().getOption()}">
            <c:if test="${answer != null && answer.getAidList().contains(option.getId()) && option.getComment() != null}">
                <div id="comment-${option.getId()}" class="row">
                    <div class="col-xs-12 padding-top-small">
                        ${option.getComment()}
                    </div>
                </div>
            </c:if>
        </c:forEach>
    </div>
</div>
