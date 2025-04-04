<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

    <c:forEach var="meta" items="${it.metadata}">
        <div data-meta-key="${meta.key}" class="row gx-3">
            <div class="col form-floating">
                <input type="text" required name="key" value="${fn:escapeXml(meta.key)}" id="${fn:escapeXml(meta.key)}-key"
                    class="form-control m-0 instance-name-input" placeholder="loremipsum" />
                <label for="${fn:escapeXml(meta.key)}-key">Key</label>
            </div>
            <div class="col form-floating">
                <input type="text" name="value" value="${fn:escapeXml(meta.value)}"
                    id="${fn:escapeXml(meta.key)}-value"
                    class="form-control m-0 instance-name-input" placeholder="loremipsum" />
                <label for="${fn:escapeXml(meta.key)}-value">Value</label>
            </div>
            <div class="col-auto d-flex align-items-center">
                <div class="form-check">
                    <input type="checkbox" name="visible" ${meta.visible ? "checked" : "" } id="${fn:escapeXml(meta.key)}-visible"
                        class="form-check-input">
                    <label for="${fn:escapeXml(meta.key)}-visible" class="form-check-label">visible</label>
                </div>
            </div>
            <div class="col-auto form-floating">
                <button type="button" onclick="deleteMeta('${fn:escapeXml(meta.key)}')"
                    class="btn btn-danger h-100">
                    Delete
                </button>
            </div>
        </div>
    </c:forEach>