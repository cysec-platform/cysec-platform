<%--
  #%L
  CYSEC Platform Core
  %%
  Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<c:set var="context" value="${pageContext.request.contextPath}"/>

<t:header>
    <jsp:attribute name="brand">
        <a class="navbar-brand ms-4" href="${context}/app#" title="Home">
            <img alt="Cysec Logo" src="${context}/assets/logo/CYSEC_Logo_RGB.svg" width="106px" height="44px"/>
        </a>
    </jsp:attribute>
    <jsp:attribute name="links">
        <li class="nav-item">
            <a class="nav-link" href="${context}/app/users.jsp" title="User Management">
                <img alt="User Management" src="${context}/assets/icons/icn_user-management.png" width="24px" height="24px"/>
            </a>
        </li>
        <c:if test="${not fn:containsIgnoreCase(header['Authorization'], 'Basic')}">
            <li class="nav-item">
                <a class="nav-link" href="${initParam['header_profile_href']}" title="Profile">
                    <img alt="Profile" src="${context}/assets/icons/icn_company.svg" width="24px" height="24px"/>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="${initParam['header_logout_href']}" title="Logout">
                    <img alt="Logout" src="${context}/assets/icons/icn_logout.svg" width="24px" height="24px"/>
                </a>
            </li>
        </c:if>
    </jsp:attribute>
</t:header>
