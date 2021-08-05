<h4 class="text-center padding-top-large">
    <a id="sidebar-title" href="#">${it.msg.achievedLevels}</a>
</h4>
<li class="sidebar-brand bg-white margin-bottom-large">
    <c:set var="flag" value="${ true }" />
    <c:forEach var="coach" items="${ it.instantiated }">
        <c:set var="rating" value="${coach.getRating()}" />
        <c:if test="${ rating != null }">
            <c:set var="flag" value="${ false }" />
            <div class="row level-row">
                <div class="col-md-8">${ coach.getReadableName() }:</div>
                <div class="col-md-4">${ rating.getGrade() }(${ rating.getScore() })</div>
            </div>
        </c:if>
    </c:forEach>
    <c:if test="${ flag }">
        <div>${it.msg.noLevels}</div>
    </c:if>
    <!-- static icons requested for demo at review -->
<%--    <div>--%>
<%--        <img src="../assets/icons/icn_levelA_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelA_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelA_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelA_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelA_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelA_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelA_emtpy.svg" width="32px" height="32px"/>--%>
<%--    </div>--%>
<%--    <div>--%>
<%--        <img src="../assets/icons/icn_levelB_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelB_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelB_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelB_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelB_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelB_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelB_emtpy.svg" width="32px" height="32px"/>--%>
<%--    </div>--%>
<%--    <div>--%>
<%--        <img src="../assets/icons/icn_levelC_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelC_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelC_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelC_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelC_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelC_emtpy.svg" width="32px" height="32px"/>--%>
<%--        <img src="../assets/icons/icn_levelC_emtpy.svg" width="32px" height="32px"/>--%>
<%--    </div>--%>
</li>
