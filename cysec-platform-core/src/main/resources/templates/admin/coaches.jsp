<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <c:choose>
        <c:when test="${not empty it.coaches }">
            <p>
                <c:out value="${it.companyId} Coaches:" />
            </p>
            <table class="table">
                <thead>
                    <tr>
                        <th scope="col">ID</th>
                        <th scope="col">Description</th>
                        <th scope="col">Import</th>
                        <th scope="col">Export</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="coach" items="${it.coaches}">
                        <c:set var="coachId" value="${coach.getId()}" />

                        <tr>
                            <td>${coachId}</td>
                            <td>${coach.getDescription()}</td>
                            <td>
                                <form action="${context}/api/rest/coaches/${coachId}/import" method="post"
                                    enctype="multipart/form-data" class="btn-group" role="group" aria-label="Import">
                                    <input class="form-control" type="file" name="file" accept=".zip" />
                                    <input class="btn btn-outline-primary" type="submit" value="Import" />
                                </form>
                            </td>
                            <td>
                                <a href="${context}/api/rest/coaches/${coachId}/export" download="${coachId}.zip"
                                    class="btn btn-outline-primary text-decoration-none">Export</a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <p>${it.companyId} no coaches</p>
        </c:otherwise>
    </c:choose>