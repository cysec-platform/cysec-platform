<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="question" value="${it.question}"/>

<p class="fs-5">
    Der Subcoach für die Protokollfragen wird geladen. Sie werden in Kürze weitergeleitet!

    <%--  Loading spinner  --%>
    <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><style>.spinner_P7sC{transform-origin:center;animation:spinner_svv2 .75s infinite linear}@keyframes spinner_svv2{100%{transform:rotate(360deg)}}</style><path d="M10.14,1.16a11,11,0,0,0-9,8.92A1.59,1.59,0,0,0,2.46,12,1.52,1.52,0,0,0,4.11,10.7a8,8,0,0,1,6.66-6.61A1.42,1.42,0,0,0,12,2.69h0A1.57,1.57,0,0,0,10.14,1.16Z" class="spinner_P7sC"/></svg>
</p>

<script>
    async function loadSubcoach() {
        const rootId = "${it.fqcn}".split('.')[0];
        const coachName = "${it.question.subcoachId}";
        const instanceName = "${it.question.instanceName}";
        const fqcn = [rootId, coachName, instanceName].join('.');

        const resumeUrl = buildUrl("/api/rest/coaches/" + fqcn + "/resume");
        await fetch(resumeUrl, {
            method: 'POST'
        });

        const redirectUrl = buildUrl("/app/coach.jsp?fqcn=" + fqcn + "&question=_first");
        window.location.replace(redirectUrl);
    }
    loadSubcoach();
</script>