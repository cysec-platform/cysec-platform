<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<div class="d-flex full-height-container">
    <div class="col col-7 px-5 pt-5 pb-3 h-100 d-flex flex-column">
        <c:import url="question.jsp"/>
        <c:import url="pagination.jsp"/>
    </div>
    <div id="documentation-wrapper" class="col col-5 bg-lightbluegrey overflow-auto">
        <c:import url="instuction.jsp"/>
    </div>
</div>

<div class="modal modal-xl fade" id="coachDebugModal" data-bs-keyboard="true" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title fs-5" id="staticBackdropLabel">Debug Information</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                ${ it.libJspModel }
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

