<div class="row col-xs-12 col-sm-12">
    <h4 class="text-center padding-top-large">${it.msg.remaining}</h4>
    <div class="all-remaining ">
        <!-- remaining coaches -->
        <c:set var="remaining" value="${it.remaining}"/>
        <c:choose>
            <c:when test="${ not empty remaining }">
                <c:forEach var="coach" items="${remaining}">
                    <a href="javascript:;" onclick="instantiate('${coach.getId()}');">
                        <div class="col-xs-3 col-sm-3">
                            <div class="text-center remaining">
                                <img src="../assets/badges/badge_access_control.png"/>
                                <h4>${coach.getReadableName()}</h4>
                            </div>
                        </div>
                    </a>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div>${it.msg.noRemaining}</div>
            </c:otherwise>
        </c:choose>
        <!-- END remaining coaches -->
    </div>
</div>