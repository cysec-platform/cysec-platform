<div class="container-fluid">
    <h4 class="text-center pt-5">${it.msg.remaining}</h4>
    <div class="row remaining-col-wrap">
        <c:set var="remaining" value="${it.remaining}"/>
        <c:choose>
            <c:when test="${ not empty remaining }">
                <c:forEach var="coach" items="${remaining}">
                    <div class="col col-md-6 col-lg-4">
                        <a href="javascript:;" onclick="instantiate('${coach.getId()}');">
                            <div class="text-center remaining">
                                <img src="../assets/badges/badge_access_control.png"/>
                                <h4>${coach.getReadableName()}</h4>
                            </div>
                        </a>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div>
                    <div class="alert bg-lightbluegrey">
                        <p>${it.msg.noRemaining}</p>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>
