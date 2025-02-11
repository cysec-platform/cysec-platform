<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="question" value="${ it.question }"/>
<c:set var="answer" value="${ it.answer }"/>
<c:set var="flagStatusKey" value="${it.flagStatusKey}"/>
<c:set var="isFlagged" value="${it.flagStatus.get(flagStatusKey)}"/>
<c:set var="flagUrl" value="${baseUrl}/assets/${isFlagged ? 'flag-filled.svg' : 'flag-outlined.svg'}"/>

<div class="flex-grow-1 overflow-y-auto">
    <div class="container question panel" id="${ question.getId() }}">
        <div class="mb-3 fw-medium">
            <nav style="--bs-breadcrumb-divider: '>';">
                <ol class="breadcrumb">
                    <c:forEach var="breadcrumb" items="${it.breadcrumbs}">
                        <li class="breadcrumb-item">${breadcrumb}</li>
                    </c:forEach>
                </ol>
            </nav>
        </div>
        <div class="d-flex justify-content-between">
            <div>
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
            </div>
            <div>
                <img id="question-flag" role="button" alt="Flagging" src="${flagUrl}" onclick="toggleFlagged()" data-flagged="${isFlagged}" />
            </div>
        </div>
        <div>
            <jsp:include page="questions/${ question.getType().toString().toLowerCase() }.jsp"/>
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
