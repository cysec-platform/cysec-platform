<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<form class="container" id="subcoachInstantiatorForm" onsubmit="updateSubcoachInstantiatorAnswer(event)" data-instance-name-regex="${it.question.instanceNameRegex}">

    <h3>Bestehende</h3>
    <c:choose>
        <c:when test="${fn:length(it.answer.subcoachInstances.subcoachInstance) == 0 }">
            <p>Noch keine Einträge erfasst</p>
        </c:when>
        <c:otherwise>
            <c:forEach items="${it.answer.subcoachInstances.subcoachInstance}" var="instance">
                <div class="row gx-3 mb-3 subcoach-instance existing-instance">
                    <div class="col-5">
                        <div class="form-floating">
                            <input type="text" readonly class="form-control-plaintext m-0 pt-5" id="parentArgumentText-${instance.instanceName}" placeholder="lorem" value="${instance.parentArgument}">
                            <label id="parentArgumentText-${instance.instanceName}">${it.question.parentArgumentValues.label}</label>
                        </div>
                    </div>
                    <div class="col-4">
                        <div class="form-floating">
                            <input type="text" readonly class="form-control-plaintext m-0 pt-5" id="instanceNameText-${instance.instanceName}" placeholder="lorem" value="${instance.instanceName}">
                            <label id="instanceNameText-${instance.instanceName}">${it.question.instanceNameLabel}</label>
                        </div>
                    </div>
                    <div class="col-3">
                        <button type="button" class="btn btn-danger w-100 h-100" onclick="deleteSubcoachInstance('${instance.instanceName}')">Entfernen</button>
                    </div>

                    <select hidden class="form-select parent-argument-select">
                        <c:forEach items="${it.question.parentArgumentValues.value}" var="parentArgumentValue">
                            <option ${instance.parentArgument == parentArgumentValue ? "selected" : ""} value="${parentArgumentValue}">${parentArgumentValue}</option>
                        </c:forEach>
                    </select>
                    <input hidden type="text" class="form-control m-0 instance-name-input"  value="${instance.instanceName}"/>
                </div>
            </c:forEach>
        </c:otherwise>
    </c:choose>

    <h3 class="mt-3">Neue hinzufügen</h3>
    <div class="row gx-3 subcoach-instance new-instance">
        <div class="col-5">
            <div class="form-floating">
                <select class="form-select parent-argument-select" id="parentArgumentSelect">
                    <c:forEach items="${it.question.parentArgumentValues.value}" var="parentArgumentValue">
                        <option value="${parentArgumentValue}">${parentArgumentValue}</option>
                    </c:forEach>
                </select>
                <label for="parentArgumentSelect">${it.question.parentArgumentValues.label}</label>
            </div>
        </div>
        <div class="col-4">
            <div class="form-floating">
                <input type="text" class="form-control m-0 instance-name-input" id="instanceName" placeholder="loremipsum"/>
                <label for="instanceName">${it.question.instanceNameLabel}</label>
            </div>
        </div>
        <div class="col-3">
            <button type="submit" class="btn btn-primary w-100 h-100">Hinzufügen</button>
        </div>
    </div>
</form>


