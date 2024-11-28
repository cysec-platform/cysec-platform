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
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:error>
    <jsp:attribute name="errorCode">
        400
    </jsp:attribute>
    <jsp:attribute name="errorText">
        <p>
            Something about your request wasn't proper.If you're authenticating with Keycloak, make sure
            to verify your oidc account includes the following claims:
        </p>
        <ul>
            <li>Company</li>
            <li>Username</li>
            <li>Email address</li>
        </ul>
    </jsp:attribute>
    <jsp:attribute name="signText">
        Sorry ... the credentials you provided were invalid.
    </jsp:attribute>
</t:error>
