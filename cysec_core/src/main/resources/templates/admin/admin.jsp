<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<div class="row">
    <div class="col-xs-12">
        <h1>${it.msg.title}</h1>
    </div>
</div>

<div class="row">
    <div id="companies" class="col-xs-4" style="height: calc(100vh - 70px); overflow-y: auto">
        <c:choose>
            <c:when test="${not empty it.companies }">
                <p>${it.msg.companies}:</p>
                <ul class="list-group">
                    <c:forEach var="company" items="${it.companies}">
                        <li class="list-group-item">
                            <h3>${company.getId()}</h3>
                            <div>${company.getCompanyname()}</div>
                            <a href="javascript:;" onclick="loadAudits('${company.getId()}')">show audits</a>
                        </li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <p>${it.msg.noCompanies}</p>
            </c:otherwise>
        </c:choose>
    </div>
    <div id="audits" class="col-xs-8" style="height: calc(100vh - 70px); overflow-y: auto">
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