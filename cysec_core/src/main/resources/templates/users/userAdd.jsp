<div id="addUserModal" class="modal">
    <!-- Modal content -->
    <div id="modal-content" class="modal-content">
        <a id="closeAddModal" class="close" data-dismiss="modal" onclick="closeModals()">X</a>
        <h3>${it.msg.addUser}:</h3>
        <form id="newUserForm">
            <label>${it.msg.username}: </label><input name="username" type="text" required/><br/>
            <label>${it.msg.firstname}: </label><input name="firstname" type="text" required/><br/>
            <label>${it.msg.surname}: </label><input name="surname" type="text" required/><br/>
            <label>${it.msg.password}: </label><input name="password" type="password" required/><br/>
            <label>${it.msg.email}: </label><input name="email" type="email" required/><br/>
            <label>${it.msg.locale}: </label><select name="locale" type="dropdown">
            <option value="" selected>${it.msg.selectLocale}</option>
            <c:forEach var="locale" items="${it.locales}">
                <option value="${locale}">${locale}</option>
            </c:forEach>
        </select><br/>
            <label>${it.msg.roles}: </label><input name="roles" type="text"/><br/>
            <%--                    <label>Company: </label><select name="company" type="dropdown" /><br />--%>
            <input type="submit" value="${it.msg.add}" onclick="addUser(event)"/>
        </form>
    </div>
</div>