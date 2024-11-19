<div id="addUserModal" class="modal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title">${it.msg.addUser}</h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" onclick="closeModals()"></button>
            </div>
            <div class="modal-body">
                <form id="newUserForm">
                    <div class="form-floating mb-3">
                        <input type="text" name="username" class="form-control" id="createUserUsernameInput" required placeholder="Username">
                        <label for="createUserUsernameInput">${it.msg.username}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="text" name="firstname" class="form-control" id="createUserFirstnameInput" required placeholder="Firstname">
                        <label for="createUserFirstnameInput" class="form-label">${it.msg.firstname}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="text" name="surname" class="form-control" id="createUserSurnameInput" required placeholder="Surname">
                        <label for="createUserSurnameInput" class="form-label">${it.msg.surname}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="password" name="password" class="form-control" id="createUserPasswordInput" required placeholder="Password">
                        <label for="createUserPasswordInput" class="form-label">${it.msg.password}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="email" name="email" class="form-control" id="createUserEmailInput" required placeholder="Email">
                        <label for="createUserEmailInput" class="form-label">${it.msg.email}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <select name="locale" type="dropdown" class="form-select" id="createUserLocaleInput">
                            <option value="" selected>${it.msg.selectLocale}</option>
                            <c:forEach var="locale" items="${it.locales}">
                                <option value="${locale}">${locale}</option>
                            </c:forEach>
                        </select>
                        <label for="createUserLocaleInput" class="form-label">${it.msg.locale}</label>
                    </div>
                    <div class="form-floating mb-3">
                        <input type="text" name="roles" class="form-control" id="createUserRolesInput" required placeholder="Role">
                        <label for="createUserRolesInput" class="form-label">${it.msg.roles}</label>
                    </div>
                    <div class="d-grid">
                        <input type="submit" class="btn btn-outline-primary" value="${it.msg.add}" onclick="addUser(event)"/>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
