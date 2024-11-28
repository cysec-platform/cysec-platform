<div id="editUserModal" class="modal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title">${it.msg.editUser}</h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" onclick="closeModals()"></button>
            </div>
            <div class="modal-body">
                <form id="editUserForm">
                    <input name="id" type="hidden"/>
                    <div class="form-floating mb-3">
                        <input type="text" name="username" class="form-control" id="editUserUsernameInput" required placeholder="Username">
                        <label for="editUserUsernameInput">${it.msg.username}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="text" name="firstname" class="form-control" id="editUserFirstnameInput" required placeholder="Firstname">
                        <label for="editUserFirstnameInput" class="form-label">${it.msg.firstname}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="text" name="surname" class="form-control" id="editUserSurnameInput" required placeholder="Surname">
                        <label for="editUserSurnameInput" class="form-label">${it.msg.surname}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="password" name="password" class="form-control" id="editUserPasswordInput" required placeholder="Password">
                        <label for="editUserPasswordInput" class="form-label">${it.msg.password}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="email" name="email" class="form-control" id="editUserEmailInput" required placeholder="Email">
                        <label for="editUserEmailInput" class="form-label">${it.msg.email}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <select name="locale" type="dropdown" class="form-select" id="editUserLocaleInput">
                            <option value="" selected>${it.msg.selectLocale}</option>
                            <c:forEach var="locale" items="${it.locales}">
                                <option value="${locale}">${locale}</option>
                            </c:forEach>
                        </select>
                        <label for="editUserLocaleInput" class="form-label">${it.msg.locale}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="text" name="roles" class="form-control" id="editUserRolesInput" required placeholder="Role">
                        <label for="editUserRolesInput" class="form-label">${it.msg.roles}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <select name="lock" type="dropdown" class="form-select" id="editUserLockInput">
                            <c:forEach var="lock" items="${it.locks}">
                                <option value="${lock}">${lock}</option>
                            </c:forEach>
                        </select>
                        <label for="editUserLockInput" class="form-label">${it.msg.lock}</label>
                    </div>
                    <div class="d-grid">
                        <input type="submit" class="btn btn-outline-primary" value="${it.msg.edit}" onclick="editUser(event)"/>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
