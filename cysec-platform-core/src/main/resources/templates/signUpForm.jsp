<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="companyIds" value="${ it.companyIds }"/>

<div id="content" class="row">
    <div class="col-md-4 col-md-offset-4">
        <h2>Account Details</h2>
        <p>Please fill in this form to create an account.</p>
        <div class="panel panel-default">
            <div class="panel-body">
                <div class="text-center">
                    <h3><i class="fa fa-lock fa-4x"></i></h3>
                    <!--<h2 class="text-center">Sign up</h2>-->
                    <!--<p>Please fill in this form to create an account.</p>-->
                    <form id="new-user-form" action="#" method="post">
                        <div class="form-group">
                            <div class="input-group">
                                <c:choose>
                                    <c:when test="${ companyIds != null }">
                                        <select id="company" class="form-control" name="company" required="">
                                            <option value="default">Select company below... (required)</option>
                                            <c:forEach var="companyId" items="${companyIds}">
                                                <option value="${ companyId }">${ companyId }</option>
                                            </c:forEach>
                                        </select>
                                        <a href="${baseUrl}/public/signup?company">My company isn't listed</a>
                                    </c:when>
                                    <c:otherwise>
                                        <input id="companyId" name="companyId" class="form-control" type="text" placeholder="Enter company identifaction" required><br />
                                        <input id="companyName" name="companyName" class="form-control" type="text" placeholder="Enter full company name" required><br />
                                        <a href="${baseUrl}/public/signup">Use existing company</a>
                                    </c:otherwise>
                                </c:choose>
                                <br>
                                <input id="username" name="username" class="form-control" type="text" placeholder="Enter username (required)" required="">
                                <input id="firstname" name="firstname" class="form-control" type="text" placeholder="Enter firstname (required)" required="">
                                <input id="surname" name="surname" class="form-control" type="text" placeholder="Enter surname (required)" required="">
                                <input id="email"  name="email" class="form-control" type="email" placeholder="Enter Email (required)" required="">
                                <input id="password" name="password" class="form-control" type="password" placeholder="Enter Password (required)" required="">
                                <input id="password2" name="password2" class="form-control" type="password" placeholder="Repeat Password (required)" required="">
                            </div>
                        </div>
                        <p>By creating an account you agree to our <a href="#" style="color:dodgerblue">Terms & Privacy</a>.</p>
                        <div class="clearfix">
                            <c:choose>
                                <c:when test="${ companyIds != null }">
                                    <button type="button" onClick="addUser()" class="signupbtn">Sign Up</button>
                                </c:when>
                                <c:otherwise>
                                    <button type="button" onClick="addCompany()" class="signupbtn">Sign Up</button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>


