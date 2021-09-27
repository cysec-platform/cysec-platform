<div class="col-xs-12">
    <h4 class="text-center padding-top-large">${it.msg.remaining}</h4>
    <div class="row">
        <c:set var="remaining" value="${it.remaining}"/>
        <c:choose>
            <c:when test="${ not empty remaining }">
                <c:forEach var="coach" items="${remaining}">
                    <a href="javascript:;" onclick="instantiate('${coach.getId()}');">
                        <div class="col-xs-6 col-md-4">
                            <div class="text-center remaining">
                                <img src="../assets/badges/badge_access_control.png"/>
                                <h4>${coach.getReadableName()}</h4>
                            </div>
                        </div>
                    </a>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-xs-12">
                    <div class="alert bg-lightbluegrey">
                        <p>${it.msg.noRemaining}</p>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
