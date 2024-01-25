<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row">
    <div id="page-content-wrapper" class="col-xs-7">
        <main>
            <div class="row">
                <div class="col-xs-12">
                    <c:import url="question.jsp"/>
                    <c:import url="pagination.jsp"/>
                </div>
            </div>
        </main>
    </div>
    <div id="documentation-wrapper" class="col-xs-5 bg-lightbluegrey">
        <c:import url="instuction.jsp"/>
    </div>
</div>
