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

<t:layout>
    <jsp:attribute name="scripts">
        <script src="resetPasswordController.js" type="application/javascript"></script>
    </jsp:attribute>
    <jsp:attribute name="header">
        <jsp:include page="/WEB-INF/templates/header/smesec.jsp" />
    </jsp:attribute>
    <jsp:body>
        <div class="container-fluid">
            <div class="row justify-content-center mt-3">
                <div class="col col-12 col-sm-9 col-md-6 col-lg-4">
                    <div id="content">
                        <h2>Reset Password | Step 1</h2>
                        <p>
                            Enter the email address associated to your account below to receive a token.
                            This token will allow to change the password of your account.
                        </p>
                        <a href="resetPassword.jsp"> Step 2</a>

                        <div class="card text-center">
                            <div class="card-body">
                                <h3><i class="fa fa-lock fa-4x"></i></h3>
                                <h2 class="card-title">Forgot Password?</h2>
                                <p class="card-text">
                                    You can reset your password here. Select the company your user belongs to and
                                    enter the corresponding email.
                                </p>
                                <form id="mail-form" role="form" autocomplete="off" class="form" method="post">
                                    <h5 class="pt-5 pb-2">Company</h5>
                                    <div class="form-floating mb-3">
                                        <input type="text" name="company" class="form-control" id="company" required placeholder="Company Name">
                                        <label for="company">Company name</label>
                                    </div>
                                    <div class="form-floating mb-3">
                                        <input type="text" name="email" class="form-control" id="email" required placeholder="Email Address">
                                        <label for="email">Email Address</label>
                                    </div>
                                    <div class="d-grid">
                                        <input name="recover-submit" class="btn btn-lg btn-primary"
                                               onclick="submitRequest(event)" value="Request token"
                                               type="submit">
                                    </div>
                                    <input type="hidden" class="hide" name="token" id="token" value="">
                                </form>
                            </div>
                        </div>
                    </div>
                </div>

                    <%--            <div id="sidebar-wrapper" class="col-xs-3 bg-lightbluegrey">--%>
                    <%--            </div>--%>
            </div>
        </div>
    </jsp:body>
</t:layout>
