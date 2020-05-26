<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<c:choose>
    <c:when test="${not empty it.audits }">
        <p><c:out value="${it.companyId} ${it.msg.audits}:" /></p>
        <table class="table">
            <thead>
            <tr>
                <th scope="col">${it.msg.headerTime}</th>
                <th scope="col">${it.msg.headerUser}</th>
                <th scope="col">${it.msg.headerAction}</th>
                <th scope="col">${it.msg.headerBefore}</th>
                <th scope="col">${it.msg.headerAfter}</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="audit" items="${it.audits}">
                <tr>
                    <td>${audit.getTime()}</td>
                    <td>${audit.getUser()}</td>
                    <td>${audit.getAction()}</td>
                    <td>${audit.getBefore()}</td>
                    <td>${audit.getAfter()}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        <p>${it.companyId} ${it.msg.noAudits}</p>
    </c:otherwise>
</c:choose>
