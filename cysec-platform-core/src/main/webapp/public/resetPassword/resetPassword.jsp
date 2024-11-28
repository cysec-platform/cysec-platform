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
        <div class="container-fluid">
            <div class="row justify-content-center mt-3">
                <div class="col col-12 col-sm-9 col-md-6 col-lg-4">
                    <h2>Reset Password | Step 2</h2>
                    <p>Enter the new password twice.</p>
                    <div class="card text-center">
                        <div class="card-body">
                            <h3><i class="fa fa-lock fa-4x"></i></h3>
                            <h2>Enter new password</h2>
                            <p>You can reset your password here.</p>
                            <form id="reset-form" role="form" autocomplete="off" class="form">
                                <span class="input-group-addon"><i class="glyphicon glyphicon-envelope color-blue"></i></span>
                                <div class="form-floating mb-3">
                                    <input id="newPassword" name="newPassword" placeholder="new Password" class="form-control" type="password" required>
                                    <label for="newPassword">New Password</label>
                                </div>
                                <div class="form-floating mb-3">
                                    <input id="newPassword2" name="newPassword2" placeholder="verify new Password" class="form-control" type="password" required>
                                    <label for="newPassword2">Verify New Password</label>
                                </div>
                                <div class="form-floating mb-3">
                                    <input id="companyId" name="token" placeholder="Enter company id..." class="form-control" type="password" required>
                                    <label for="companyId">Company ID</label>
                                </div>
                                <div class="form-floating mb-3">
                                    <input id="token" name="token" placeholder="Enter Token id..." class="form-control" type="password" required>
                                    <label for="token">Token Id</label>
                                </div>
                                <div class="d-grid">
                                    <input name="reset-submit" class="btn btn-lg btn-primary" onclick="resetPassword(event)" value="Reset password" type="submit">
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>
