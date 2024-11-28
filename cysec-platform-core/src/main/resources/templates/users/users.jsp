<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row min-vh-100">
    <div class="col col-8 px-4 py-3 bg-white">
        <!-- user content -->
        <div class="container-fluid">
            <div class="mb-4">
                <h1>${it.msg.users}</h1>
                <c:choose>
                    <c:when test="${empty it.users}">
                        <div class="user-table" id="users">${it.msg.noUsers}</div>
                    </c:when>
                    <c:otherwise>
                        <table class='table table-bordered table-responsive table-striped'>
                            <thead>
                            <tr>
                                <th>${it.msg.username}</th>
                                <th>${it.msg.roles}</th>
                                <th>${it.msg.lock}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="user" items="${it.users}">
                                <tr>
                                    <td>${user.getUsername()}</td>
                                    <td>${user.getRole()}
                                        <span class="pull-right" onclick="openEditUserModal(event)" data-bs-toggle="modal" data-bs-target="#editUserModal">
                                        <i id="${user.getId()}" class="fa fa-pencil" aria-hidden="true" title="${it.msg.editUser}"></i>
                                    </span>
                                    </td>
                                    <td>${user.getLock()}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
                <div class="d-grid">
                    <button class="btn btn-outline-primary" type="button" data-bs-toggle="modal" data-bs-target="#addUserModal">${it.msg.addUser}</button>
                </div>
            </div>

            <!-- tokens -->
            <div class="mb-4">
                <h1>${it.msg.token_header}</h1>
                <c:choose>
                    <c:when test="${it.replica == null}">
                        <div>${it.msg.token_empty}</div>
                    </c:when>
                    <c:otherwise>
                        <div>${it.replica}</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <%@include file="userAdd.jsp" %>
            <%@include file="userEdit.jsp" %>
        </div>
    </div>
    <div class="col col-4 px-4 py-3 bg-lightbluegrey">
        <div class="container-fluid">
            <p>${it.msg.info}</p>
        </div>
    </div>
</div>

