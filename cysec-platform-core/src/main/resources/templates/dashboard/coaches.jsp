<c:set var="context" value="${pageContext.request.contextPath}" />

<div class="col-xs-12 pb-3">
    <h4 class="text-center pt-5 pb-3">${it.msg.coaches}</h4>
    <c:choose>
        <c:when test="${ not empty it.instantiated }">
            <c:forEach var="coach" items="${it.instantiated}">
                <div class="questionnaire-summary">
                    <div class="row">
                        <div class="col col-9 col-sm-10">
                            <c:set var="coachId" value="${ coach.getId() }" />
                            <c:set var="icon" value="${ coach.getIcon() }" />
                            <c:if test="${ icon != null }">
                                <div class="questionnaire-summary-icon">
                                    <a href="javascript:;" onclick="resume('${coachId}')">
                                        <img src="data:image/png;base64, ${icon}" width="100" />
                                    </a>
                                </div>
                            </c:if>
                            <div class="pt-3 questionnaire-summary-title">
                                <h4 class="pb-0">
                                    <a href="javascript:;" onclick="resume('${coachId}')">
                                        ${ coach.getReadableName()}
                                    </a>
                                    <div class="questionnaire-info"><img src="${context}/assets/icons/icn_info.png"
                                            width="24" height="24">
                                        <span class="questionnaire-infotext">${ coach.getDescription() }</span>
                                    </div>
                                </h4>
                                <h5>
                                    <a href="javascript:;" onclick="restart('${coachId}')">${it.msg.coachRestart}</a>
                                    &nbsp; | &nbsp;
                                    <a href="javascript:;" onclick="resume('${coachId}')">${it.msg.coachContinue}</a>
                                    <c:if test="${it.userIsAdmin}">
                                        &nbsp; | &nbsp;
                                        <a href="javascript:;" onclick="openAdminModal('${coachId}')"
                                            data-bs-toggle="modal" data-bs-target="#adminCoachModal">Mehr</a>
                                    </c:if>
                                </h5>
                            </div>
                        </div>
                        <div class="col col-3 col-sm-2">
                            <div class="questionnaire-rating text-right padding-top-small">
                                <c:set var="rating" value="${coach.getRating()}" />
                                <c:choose>
                                    <c:when test="${ rating != null }">
                                        <h4>${ rating.getScore() }</h4>
                                        <h5>score</h5>
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

            <div class="modal" id="adminCoachModal" tabindex="-1" aria-hidden="true" aria-labelledby="adminModalLabel">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3 class="modal-title" id="adminModalLabel">Admin actions</h3>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body d-grid">
                            <h4>Export</h4>
                            <a class="btn btn-outline-primary text-decoration-none">Export</a>

                            <h4>Import</h4>
                            <form enctype="multipart/form-data" class="btn-group" role="group"
                                aria-label="Import">
                                <input class="form-control" type="file" name="file" accept=".zip" required/>
                                <input class="btn btn-outline-primary" type="submit" value="Import"  />
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert bg-lightbluegrey">
                <p>${it.msg.noCoachesStartedInfo}</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>