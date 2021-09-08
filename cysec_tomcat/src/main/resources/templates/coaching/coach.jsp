<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row">
    <!-- left side -->
    <div id="page-content-wrapper" class="col-xs-7">
        <eu.smesec.totalcross.main>
            <div class="row">
                <div class="col-lg-12">
                    <!-- question -->
                    <c:import url="question.jsp"/>
                    <!-- pagination -->
                    <c:import url="pagination.jsp"/>
                </div>
            </div>
        </eu.smesec.totalcross.main>
    </div>
    <!-- rights side -->
    <div id="documentation-wrapper" class="col-xs-5 bg-lightbluegrey">
        <c:import url="instuction.jsp"/>
    </div>
</div>
