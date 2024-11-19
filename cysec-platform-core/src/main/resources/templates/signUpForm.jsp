<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="baseUrl"
       value="${header['x-forwarded-proto'] != null ? header['x-forwarded-proto'] : pageContext.request.scheme}://${header.host}${pageContext.request.contextPath}"/>
<c:set var="companyIds" value="${ it.companyIds }"/>

<div class="container d-flex justify-content-center">
    <div class="d-flex flex-column w-50">
        <h2 class="mt-3">Account Details</h2>
        <p>Please fill in this form to create an account.</p>
        <div class="card text-center">
            <div class="card-body">
                <h3><i class="fa fa-lock fa-4x"></i></h3>
                <form id="new-user-form" action="#" method="post">
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

                    <div class="d-flex flex-column gap-1 my-3">
                        <input id="username" name="username" class="form-control" type="text" placeholder="Enter username (required)" required="">
                        <input id="firstname" name="firstname" class="form-control" type="text" placeholder="Enter firstname (required)" required="">
                        <input id="surname" name="surname" class="form-control" type="text" placeholder="Enter surname (required)" required="">
                        <input id="email"  name="email" class="form-control" type="email" placeholder="Enter Email (required)" required="">
                        <input id="password" name="password" class="form-control" type="password" placeholder="Enter Password (required)" required="">
                        <input id="password2" name="password2" class="form-control" type="password" placeholder="Repeat Password (required)" required="">
                    </div>

                    <p>By creating an account you agree to our <a href="#">Terms & Privacy</a>.</p>
                    <c:choose>
                        <c:when test="${ companyIds != null }">
                            <button type="button" onClick="addUser()" class="btn btn-outline-primary">Sign Up</button>
                        </c:when>
                        <c:otherwise>
                            <button type="button" onClick="addCompany()" class="btn btn-outline-primary">Sign Up</button>
                        </c:otherwise>
                    </c:choose>
                </form>
            </div>
        </div>
    </div>
</div>
