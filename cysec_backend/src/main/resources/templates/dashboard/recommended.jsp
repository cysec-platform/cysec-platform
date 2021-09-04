<c:set var="baseUrl" value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}/" />
<c:set var="recommendations" value="${it.recommendations}"/>

<div class="col-xs-12 padding-top-small padding-bottom-small">
    <h4 class="text-center">${it.msg.recommendations}</h4>
    <div class="row">
        <c:choose>
            <c:when test="${ not empty recommendations }">
                <c:forEach var="item" items="${ recommendations }">
                    <div class="col-sm-12 col-md-6 col-lg-4">
                        <c:set var="link" value="${item.getLink()}" />
                        <c:set var="prefix" value="ext" />
                        <c:set var="url" value="${link.startsWith('ext ')
                        ? link.substring(4)
                        : baseUrl.concat(link)}" />
                        <a class="recommended-action-wrapper" href="${url}">
                            <div class="recommended-action">
                                <img src="../assets/recommendation_bulb.png" class="recommended-action-icon">
                                <h5>${it.msg.recommendation}</h5>
                                <h3>${item.getTitle()}</h3>
                                <p class="recommended-action-description">${item.getDescription()}</p>
                                <div class="recommended-action-link">
                                    <img src="../assets/arrow_blue.png" width="28px" height="18px"/>
                                </div>
                            </div>
                        </a>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-xs-12">
                    <div class="alert bg-lightbluegrey">
                        <p><strong>${it.msg.noRecommendation}</strong></p>
                        <p>${it.msg.noRecommendationInfo}</p>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
