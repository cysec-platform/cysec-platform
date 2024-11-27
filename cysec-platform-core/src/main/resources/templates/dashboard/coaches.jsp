<c:set var="context" value="${pageContext.request.contextPath}"/>

<div class="col-xs-12 padding-bottom-small">
    <h4 class="text-center padding-top-large padding-bottom-small">${it.msg.coaches}</h4>
    <c:choose>
    <c:when test="${ not empty it.instantiated }">
    <c:forEach var="coach" items="${it.instantiated}">
        <div class="questionnaire-summary">
            <div class="row">
                <div class="col-xs-9 col-sm-10">
                    <c:set var="coachId" value="${ coach.getId() }"/>
                    <c:set var="icon" value="${ coach.getIcon() }"/>
                    <c:if test="${ icon != null }">
                        <div class="questionnaire-summary-icon">
                            <a href="javascript:;" onclick="resume('${coachId}')">
                                <img src="data:image/png;base64, ${icon}" width="100"/>
                            </a>
                        </div>
                    </c:if>
                    <div class="padding-top-small questionnaire-summary-title">
                        <h4 class="no-margin-bottom">
                            <a href="javascript:;" onclick="resume('${coachId}')">${ coach.getReadableName() }</a>
                            <div class="questionnaire-info"><img src="${context}/assets/icons/icn_info.png" width="24"
                                                                 height="24">
                                <span class="questionnaire-infotext">${ coach.getDescription() }</span>
                            </div>
                        </h4>
                        <h5>
                            <a href="javascript:;" onclick="restart('${coachId}')">${it.msg.coachRestart}</a>
                            &nbsp; | &nbsp;
                            <a href="javascript:;" onclick="resume('${coachId}')">${it.msg.coachContinue}</a>
                            &nbsp; | &nbsp;
                            <a href="${context}/api/rest/coaches/${coachId}/export" download="${coachId}.zip">Export</a>
                        </h5>
                    </div>
                </div>
                <div class="col-xs-3 col-sm-2">
                    <div class="questionnaire-rating text-right padding-top-small">
                        <c:set var="rating" value="${coach.getRating()}"/>
                        <c:choose>
                            <c:when test="${ rating != null }">
                                <h4>${ rating.getScore() }</h4>
                                <h5>score</h5>
                                <!--
                                <h5>your grade: ${ rating.getGrade() }</h5>
                                -->
                            </c:when>
                            <c:otherwise>
                                <h4>&mdash;</h4>
                                <h5>score</h5>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </c:forEach>
    </c:when>
    <c:otherwise>
        <div class="alert bg-lightbluegrey">
            <p>${it.msg.noCoachesStartedInfo}</p>
        </div>
    </c:otherwise>
    </c:choose>
</div>
