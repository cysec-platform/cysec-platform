<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row dashboard-wrapper bg-lightbluegrey">
    <div id="page-content-wrapper" class="col-xs-8 bg-white">
        <!-- user content -->
        <div class="row">
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
                                    <span class="pull-right" onclick="openEditUserModal(event) ">
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
            <p class="text-link-box" onclick="openAddUserModal()">
                <a onclick="openAddUserModal()">${it.msg.addUser}</a></p>


            <%@include file="userAdd.jsp" %>
            <%@include file="userEdit.jsp" %>
        </div>
        <!-- tokens -->
        <div class="row table-responsive">
            <h1>${it.msg.token_header}</h1>
            <c:choose>
                <c:when test="${it.replica == null}">
                    <div class="user-table" id="users">${it.msg.token_empty}</div>
                </c:when>
                <c:otherwise>
                    <div>${it.replica}</div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div id="sidebar-wrapper" class="col-xs-4 bg-lightbluegrey">
        <div class=sidebar-content">
            <p>${it.msg.info}</p>
        </div>
    </div>
</div>

