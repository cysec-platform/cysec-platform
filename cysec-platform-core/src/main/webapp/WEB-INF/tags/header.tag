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

<%@tag description="generic header for base layout" pageEncoding="UTF-8" %>

<%@ attribute name="brand" required="true" fragment="true" %>
<%@ attribute name="links" required="true" fragment="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="context" value="${pageContext.request.contextPath}"/>

<nav class="navbar navbar-expand bg-bluegrey sticky-top">
    <div class="container-fluid">
        <jsp:invoke fragment="brand"/>
        <ul class="navbar-nav gap-4">
            <jsp:invoke fragment="links"/>
        </ul>
    </div>
</nav>
