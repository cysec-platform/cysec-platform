<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="question" value="${ it.question }"/>
<c:set var="answer" value="${ it.answer }"/>

<div class="flex-grow-1 overflow-y-scroll">
    <div class="container question panel" id="${ question.getId() }}">
        <c:if test="${ question.getIntroduction() != null}">
            <p class="text-secondary fst-italic">${ question.getIntroduction() }</p>
        </c:if>
        <h1 class="mb-4">${ question.getText() }</h1>
        <c:set var="qAttachments" value="${ question.getAttachments() }"/>
        <c:if test="${ qAttachments != null }">
            <div class="row">
            <c:forEach var="qAttachment" items="${ qAttachments.getAttachment() }">
                <div class="col-lg-6">
                    <img src="data:${ qAttachment.getMime() };base64, ${ qAttachment.getContent().getValue() }"
                         width="50"/>
                </div>
            </c:forEach>
            </div>
        </c:if>
        <div>
            <jsp:include page="questions/${ question.getType() }.jsp"/>
        </div>
        <c:if test="${ question.getReadMore() != null}">
            <div class="readmore-container">
                <a href="#" onclick="toggleReadMore()">${it.msg.readmore}</a>
                <div class="readmore-box">
                        ${ question.getReadMore() }
                </div>
            </div>
        </c:if>
    </div>
</div>
