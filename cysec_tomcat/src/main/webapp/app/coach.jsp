<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%--<%@taglib prefix="t" tagdir="/../../src/eu.smesec.totalcross.main/webapp/WEB-INF/tags" %>--%>

<t:layout>
    <jsp:attribute name="scripts">
        <script src="js/redirect.js" type="application/javascript"></script>
        <script src="js/coach.js" type="application/javascript"></script>
    </jsp:attribute>
    <jsp:attribute name="header">
        <jsp:include page="/WEB-INF/templates/header/cysec.jsp" />
    </jsp:attribute>
</t:layout>