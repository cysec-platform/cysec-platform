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

<t:layout>
    <jsp:attribute name="scripts">
        <script src="resetPasswordController.js" type="application/javascript"></script>
    </jsp:attribute>
    <jsp:attribute name="header">
        <jsp:include page="/WEB-INF/templates/header/smesec.jsp" />
    </jsp:attribute>
    <jsp:body>
        <div class="row">
            <div id="page-content-wrapper" class="col-xs-9 bg-white">
                <div id="content" class="row">
                    <div class="col-md-4 col-md-offset-4">
                        <h2>Reset Password | Step 1</h2>
                        <p>Enter the email address associated to your account below to receive a token.
                            This token will allow to change the password of your account.</p>
                        <a href="resetPassword.html"> Step 2</a>
                        <div class="panel panel-default">
                            <div class="panel-body">
                                <div class="text-center">
                                    <h3><i class="fa fa-lock fa-4x"></i></h3>
                                    <h2 class="text-center">Forgot Password?</h2>
                                    <p>You can reset your password here. Select the company your user belongs to and
                                        enter the corresponding email.</p>
                                    <div class="panel-body">
                                        <form id="mail-form" role="form" autocomplete="off" class="form" method="post">
                                            <div class="form-group">
                                                <div class="input-group">
                                                    <label>Company </label>
                                                    <input id="company" name="company" placeholder="Company name"
                                                           class="form-control" type="text" required>
                                                    <input id="email" name="email" placeholder="email address"
                                                           class="form-control" type="email" required>
                                                </div>
                                            </div>
                                            <div class="form-group">
                                                <input name="recover-submit" class="btn btn-lg btn-primary btn-block"
                                                       onclick="submitRequest(event)" value="Request token"
                                                       type="submit">
                                            </div>
                                            <input type="hidden" class="hide" name="token" id="token" value="">
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

<%--            <div id="sidebar-wrapper" class="col-xs-3 bg-lightbluegrey">--%>
<%--            </div>--%>
        </div>
    </jsp:body>
</t:layout>
