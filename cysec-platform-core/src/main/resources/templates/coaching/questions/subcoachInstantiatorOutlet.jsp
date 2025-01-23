<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>


<c:choose>
    <c:when test="${empty it.subcoachFqcn}">
        <p>No subcoaches active!</p>
    </c:when>
    <c:otherwise>
        <script>
            loadSubcoach('${it.subcoachFqcn}');
        </script>
    </c:otherwise>
</c:choose>
