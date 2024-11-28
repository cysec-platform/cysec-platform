<h4 class="text-center pt-3">
    ${it.msg.latestAchievements}
</h4>
<div class="bg-white p-3">
    <!-- Badge -->
    <div class="row">
        <c:choose>
            <c:when test="${not empty it.badges}">
                <c:forEach var="badge" items="${it.badges}">
                    <c:if test="${not empty badge.getImagePath()}">
                        <div class="col col-12 col-sm-4">
                            <img src="${scheme.concat(host).concat(api).concat(badge.getImagePath())}">
                                <%--                    <div>${ badge.getName() }</div>--%>
                        </div>
                    </c:if>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col col-12">${it.msg.noAchievements}</div>
            </c:otherwise>
        </c:choose>
    </div>
    <!-- END Badge -->
    <div class="text-end textlink">
        <a href="${pageContext.request.contextPath}/app/badges.jsp">
            ${it.msg.showAll}
            <img src="../assets/arrow_blue.svg" width="24px" height="24px"/>
        </a>
    </div>
</div>
