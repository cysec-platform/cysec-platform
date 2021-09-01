<c:set var="baseUrl" value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}/" />
<c:set var="recommendations" value="${it.recommendations}"/>

<div class="col-xs-12 col-sm-12 row padding-top-small padding-bottom-small">
    <h4 class="text-center">${it.msg.recommendations}</h4>
    <div class="all-recommendations">
        <!-- Recommendations -->
        <c:choose>
            <c:when test="${ not empty recommendations }">
                <c:forEach var="item" items="${ recommendations }">
                    <div class="col-xs-4 col-sm-4 ">
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
                                    <%-- Might have to be changed to x-forwarded-proto and x-forwarded-host on prod server--%>
                                <div class="recommended-action-link">
                                    <img src="../assets/arrow_blue.png" width="28px" height="18px"/>
                                </div>
                            </div>
                        </a>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-xs-4 col-sm-4 ">
                    <a class="recommended-action-wrapper" href="">
                        <div class="recommended-action">
                            <img src="../assets/recommendation_bulb.png" class="recommended-action-icon">
                            <h5>${it.msg.recommendation}</h5>
                            <h3>${it.msg.noRecommendation}</h3>
                            <p class="recommended-action-description">${it.msg.noRecommendationInfo}.</p>
                                <%-- Might have to be changed to x-forwarded-proto and x-forwarded-host on prod server--%>
                            <div class="recommended-action-link">
                                <img src="../assets/arrow_blue.png" width="28px" height="18px"/>
                            </div>
                        </div>
                    </a>
                </div>
                <!--
                <div class="text-center">Nothing recommended</div>
                -->
            </c:otherwise>
        </c:choose>
        <!-- END Recommendations -->
    </div>
</div>
