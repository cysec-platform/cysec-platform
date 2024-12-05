<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<div class="d-flex full-height-container">
    <div class="col col-7 px-5 pt-5 pb-3 h-100 d-flex flex-column">
        <c:import url="question.jsp"/>
        <c:import url="pagination.jsp"/>
    </div>
    <div id="documentation-wrapper" class="col col-5 bg-lightbluegrey">
        <c:import url="instuction.jsp"/>
        <c:import url="flagging.jsp"/>
    </div>
</div>
