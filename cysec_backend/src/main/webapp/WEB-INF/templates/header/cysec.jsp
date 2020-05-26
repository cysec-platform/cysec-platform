<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="context" value="${pageContext.request.contextPath}"/>

<div class="row">
    <div class="col-xs-8 bg-white no-margin-right" style="margin-left: -10px;">
        <a href="${context}/app#" title="Home">
            <img src="${context}/assets/logo/CYSEC_Logo_RGB.svg" width="106px" height="44px"/></a>
    </div>
    <div class="col-xs-4 text-right bg-lightbluegrey">
        <ul>
            <li>
                <a href="${context}/app/users.jsp" title="User Management">
                    <img src="${context}/assets/icons/icn_user-management.png" width="24px" height="24px"/>
                </a>
            </li>

            <li>
                <a href="${context}/app/coach.jsp#company,q1" title="Profile">
                    <img src="${context}/assets/icons/icn_company.svg" width="24px" height="24px"/>
                </a>
            </li>

            <%--            <c:if test="${fn:containsIgnoreCase(header['Authorization'], 'Basic')}">--%>
            <%--                <!-- Hide link with Basic Auth-->--%>
            <%--            </c:if>--%>

            <c:if test="${not fn:containsIgnoreCase(header['Authorization'], 'Basic')}">
                <!-- <li><a href="${initParam['cysec_header_profile']}" title="Profile"><img src="../assets/icons/icn_company.svg" width="24px" height="24px"/></a></li> -->
                <li>
                    <a href="${initParam['cysec_header_logout']}" title="Logout">
                        <img src="${context}/assets/icons/icn_logout.svg" width="24px" height="24px"/>
                    </a>
                </li>
            </c:if>
        </ul>
    </div>
</div>