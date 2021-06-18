<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/../../src/main/webapp/WEB-INF/tags" %>

<t:layout>
    <jsp:attribute name="scripts">
        <script src="index.js" type="application/javascript"></script>
    </jsp:attribute>
    <jsp:attribute name="header">
        <jsp:include page="/../../src/main/webapp/WEB-INF/templates/header/adminModel.jsp" />
    </jsp:attribute>
</t:layout>
