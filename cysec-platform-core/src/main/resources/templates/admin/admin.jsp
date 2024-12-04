<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<div class="d-flex flex-column container-fluid full-height-container pt-3">
    <div>
        <h1>${it.msg.title}</h1>
    </div>

    <div class="row" style="min-height: 0">
        <div id="companies" class="col col-4" style="overflow-y: auto">
            <c:choose>
                <c:when test="${not empty it.companies }">
                    <p>${it.msg.companies}:</p>
                    <ul class="list-group">
                        <c:forEach var="company" items="${it.companies}">
                            <li class="list-group-item">
                                <h3>${company.getId()}</h3>
                                <div>${company.getCompanyname()}</div>
                                <a class="btn btn-link" href="javascript:;" onclick="loadAudits('${company.getId()}')">
                                    show audits
                                </a>
                                <a class="btn btn-link" href="javascript:;" onclick="loadCoaches('${company.getId()}')">
                                    show coaches
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                </c:when>
                <c:otherwise>
                    <p>${it.msg.noCompanies}</p>
                </c:otherwise>
            </c:choose>
        </div>
        <div id="admin-wrapper" class="col col-8 h-100 overflow-y-auto">
        </div>
    </div>
</div>



<%--    <div class="col-xs-9 bg-white">--%>
<%--        <%@include file="config.jsp"%>--%>
<%--        <%@include file="audits.jsp"%>--%>

<%--    </div>--%>
<%--    <div id="sidebar-wrapper" class="col-xs-3 bg-lightbluegrey">--%>
<%--        <ul class="sidebar-nav" id="sidebar-nav">--%>
<%--        </ul>--%>
<%--    </div>--%>