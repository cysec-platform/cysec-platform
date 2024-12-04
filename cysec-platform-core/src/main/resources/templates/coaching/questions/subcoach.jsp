<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script>
    function loadSubcoach() {
        const rootId = "${it.fqcn}".split('.')[0];
        const coachName = "${it.question.subcoachId}";
        const instanceName = "${it.question.instanceName}";
        const fqcn = [rootId, coachName, instanceName].join('.');

        const redirectUrl = buildUrl("/app/coach.jsp?fqcn=" + fqcn + "&question=_first");
        window.location.replace(redirectUrl);
    }
    loadSubcoach();
</script>