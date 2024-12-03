<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="question" value="${it.question}"/>

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