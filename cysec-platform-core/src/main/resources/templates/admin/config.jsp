<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="col-xs-12 col-sm-12">
    <h4 class="text-center">Default Config</h4>
    <div class="">
        <!-- Config -->
        <c:choose>
            <c:when test="${ not empty it.getConfigString() }">
                <textarea name="myTextArea" cols="100" rows="20" readonly=readonly>
                    ${it.getConfigString()}
                </textarea>

            </c:when>
            <c:otherwise>
                <div class="text-center">No config loaded</div>
            </c:otherwise>
        </c:choose>
        <!-- End Config -->
    </div>
</div>
