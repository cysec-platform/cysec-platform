<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="question" value="${ it.question }"/>
<c:set var="answer" value="${ it.answer }"/>

<div id="blocks">
    <div class="col-xs-12 container question panel" id="${ question.getId() }}">
        <!-- question attachment -->
        <h1 class="row panel-heading">${ question.getText() }</h1>
        <c:set var="qAttachments" value="${ question.getAttachments() }"/>
        <c:if test="${ qAttachments != null }">
            <c:forEach var="qAttachment" items="${ qAttachments.getAttachment() }">
                <div class="col-lg-6">
                    <img src="data:${ qAttachment.getMime() };base64, ${ qAttachment.getContent().getValue() }"
                         width="50"/>
                </div>
            </c:forEach>
        </c:if>
        <!-- answer layout -->
        <div class="row">
            <jsp:include page="questions/${ question.getType() }.jsp"/>
        </div>
        <!-- read more -->
        <c:if test="${ question.getReadMore() != null}">
            <div class="row">
                <!-- Bootstrap collapse wont work -->
                <a href="javascript:;" onclick="readMore()">${it.msg.readmore}</a>
                <div class="readmore">
                        ${ question.getReadMore() }
                </div>
            </div>
        </c:if>
    </div>
</div>