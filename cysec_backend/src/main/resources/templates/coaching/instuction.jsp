<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="question" value="${ it.question }"/>
<c:set var="answer" value="${ it.answer }"/>

<div class="row">
    <!-- instruction -->
    <c:set var="instruction" value="${ question.getInstruction() }"/>
    <c:if test="${ instruction != null }">
        <div class="col-lg-12 documentation-content-wrapper">
            <div id="documentation_text">
                <p>${fn:replace(instruction.getText(), '[smesec_instance]', baseUrl)}</p>
            </div>
            <div id="documentation_media">
                <c:set var="iAttachments" value="${ instruction.getAttachments() }"/>
                <c:if test="${ iAttachments != null }">
                    <c:forEach var="iAttachment" items="${ iAttachments.getAttachment() }">
                        <div class="col-lg-6">
                            <c:set var="iMime" value="${ iAttachment.getMime() }"/>
                            <c:set var="iValue" value="${ iAttachment.getContent().getValue() }"/>
                            <c:choose>
                                <c:when test="${ iMime.startsWith('image') }">
                                    <img class="img-responsive" src="data:${ iMime };base64, ${ iValue }"
                                         width="50"/>
                                </c:when>
                                <c:otherwise>
                                    <video class="img-responsive" controls>
                                        <source type="${ iMime }" src="data:${ iMime };base64, ${ iValue }"
                                                width="50">
                                    </video>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:forEach>
                </c:if>
            </div>
        </div>
    </c:if>
</div>