<c:set var="context" value="${pageContext.request.contextPath}" />

<div class="col-xs-12 pb-3">
    <h4 class="text-center pt-5 pb-3">${it.msg.coaches}</h4>

    <div class="alert alert-warning" role="alert">
        <strong>Note:</strong>
        All users work on the same instance of the Coach. It is strongly recommended that only one user works with the
        Coach at any one time.
    </div>

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
                                    <a href="javascript:;" onclick="restart('${coachId}')">${it.msg.coachStart}</a>
                                    &nbsp; | &nbsp;
                                    <a href="javascript:;" onclick="resume('${coachId}')">${it.msg.coachContinue}</a>
                                    &nbsp; | &nbsp;
                                    <a href="javascript:;" onclick="reset('${coachId}')">${it.msg.coachReset}</a>
                                    &nbsp; | &nbsp;
                                    <a href="javascript:;" onclick="openMetaModal('${coachId}')"
                                        data-bs-toggle="modal" data-bs-target="#meta-coach-modal" >
                                        Meta
                                    </a>
                                    <c:if test="${it.userIsAdmin}">
                                        &nbsp; | &nbsp;
                                        <a href="javascript:;" onclick="openAdminModal('${coachId}')"
                                            data-bs-toggle="modal" data-bs-target="#adminCoachModal">
                                            ${it.msg.coachMore}
                                        </a>
                                    </c:if>
                                </h5>
                                <div class="d-flex flex-row flex-wrap gap-3">
                                    <c:forEach var="meta" items="${coach.visibleCoachMetadata}">
                                        <span class="badge rounded-pill text-bg-light fs-5">
                                            <span class="me-3">
                                                <c:out value="${meta.key}" />
                                            </span>
                                            <span class="fw-light">
                                                <c:out value="${meta.value}" />
                                            </span>
                                        </span>
                                    </c:forEach>
                                </div>
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
                            <h3 class="modal-title" id="adminModalLabel">${it.msg.adminModalTitle}</h3>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body d-grid">
                            <h4>${it.msg.adminModalExport}</h4>
                            <a class="btn btn-outline-primary text-decoration-none">
                                ${it.msg.adminModalExport}
                            </a>

                            <h4>${it.msg.adminModalImport}</h4>
                            <form enctype="multipart/form-data" class="btn-group" role="group" aria-label="Import">
                                <input class="form-control" type="file" name="file" accept=".zip" required />
                                <input class="btn btn-outline-primary" type="submit"
                                    value="${it.msg.adminModalImport}" />
                            </form>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal" id="meta-coach-modal" tabindex="-1" aria-hidden="true" aria-labelledby="metaModalLabel">
                <div class="modal-dialog modal-dialog-centered modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3 class="modal-title" id="metaModalLabel">Edit Metadata</h3>
                            <button type="button" onclick="closeMetaModal()"
                                class="btn-close" data-bs-dismiss="modal"
                                aria-label="Close">
                            </button>
                        </div>
                        <div class="modal-body d-flex flex-column gap-3">
                            <span class="meta-entry-container d-flex flex-column gap-3">
                                <!-- metadata entires dynamically loaded here -->
                            </span>

                            <div class="row m-0">
                                <button type="button" onclick="addMeta()" class="btn btn-outline-primary btn-lg">
                                    Add Metadata
                                </button>
                            </div>
                            <div class="row m-0">
                                <button id="meta-send-button" type="button" class="btn btn-primary btn-lg">
                                    Save
                                </button>
                            </div>
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

<template id="meta-entry-template">
    <div class="row gx-3">
        <div class="col form-floating">
            <input type="text" required name="key" class="form-control m-0 instance-name-input" placeholder="loremipsum" />
            <label>Key</label>
        </div>
        <div class="col form-floating">
            <input type="text" name="value" class="form-control m-0 instance-name-input" placeholder="loremipsum" />
            <label>Value</label>
        </div>
        <div class="col-auto d-flex align-items-center">
            <div class="form-check">
                <input type="checkbox" name="visible" class="form-check-input">
                <label class="form-check-label">visible</label>
            </div>
        </div>
        <div class="col-auto form-floating">
            <button type="button" class="btn btn-danger h-100">Delete</button>
        </div>
    </div>
</template>