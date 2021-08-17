<c:set var="context" value="${pageContext.request.contextPath}"/>

<div class="row col-xs-12 col-sm-12">
    <h4 class="row text-center padding-top-large no-margin-bottom">${it.msg.coaches}</h4>
    <div class="all-questionnaires padding-bottom-medium">
        <c:forEach var="coach" items="${it.instantiated}">
            <div class="row questionnaire-summary">
                <div class="col-xs-9 col-sm-10 no-padding-left">
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
                                <h4>0</h4>
                                <h5>score</h5>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>
