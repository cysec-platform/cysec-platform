<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="question" value="${it.question}"/>

<!-- Info -->
<div class="row">
    <div class="col col-12">
        ${question.getInfotext()}
    </div>
</div>
