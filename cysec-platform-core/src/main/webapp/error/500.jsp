<%--
  #%L
  CYSEC Platform Core
  %%
  Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:error>
    <jsp:attribute name="errorCode">
        500
    </jsp:attribute>
    <jsp:attribute name="errorText">
        <div>
            <!-- Get the exception object -->
            <c:set var="exception" value="${requestScope['javax.servlet.error.exception']}"/>
            <p>${exception.message}</p>
            <p>${exception.stackTrace}</p>
            <c:forEach var = "trace" items = "${exception.stackTrace}">
                <pre>${trace}</pre>
            </c:forEach>
        </div>
    </jsp:attribute>
    <jsp:attribute name="signText">
        Something bad happened
    </jsp:attribute>
</t:error>
