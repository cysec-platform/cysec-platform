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

<%@ tag description="Layout for error pages" pageEncoding="UTF-8" %>

<%@ attribute name="errorCode" required="true" fragment="true" %>
<%@ attribute name="errorText" required="true" fragment="true" %>
<%@ attribute name="signText" required="true" fragment="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout>
    <jsp:attribute name="scripts">
        <script src="${pageContext.request.contextPath}/public/js/errorpage.js" type="application/javascript"></script>
    </jsp:attribute>
    <jsp:attribute name="header">
        <jsp:include page="/WEB-INF/templates/header/cysec.jsp" />
    </jsp:attribute>
    <jsp:attribute name="links">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/public/css/error.css">
    </jsp:attribute>
    <jsp:body>
        <div class="container-fluid sign error-page-wrapper pt-5">
            <div class="row align-items-center">
                <div class="col col-12 col-md-6">
                    <div class="text-container">
                        <div class="headline secondary-text-color">
                            <jsp:invoke fragment="errorCode"/>
                        </div>
                        <div class="context primary-text-color">
                            <p>
                                <jsp:invoke fragment="errorText"/>
                            </p>
                        </div>
                        <div class="buttons-container">
                            <a class="border-button" href="/"><span class="fa fa-home"></span> Home page</a>
                            <a class="border-button" href="mailto:support@smesec.eu?subject=Broken%20link" target="_blank">
                                <span class="fa fa-warning"></span>
                                Report problem
                            </a>
                        </div>
                    </div>
                </div>
                <div class="col col-12 col-md-6">
                    <div class="sign-container">
                        <div class="nob"></div>
                        <div class="post left"></div>
                        <div class="post right"></div>
                        <div class="pane">
                            <div class="headline sign-text-color">
                                <jsp:invoke fragment="errorCode"/>
                            </div>
                            <div class="context sign-text-color">
                                <jsp:invoke fragment="signText"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>
