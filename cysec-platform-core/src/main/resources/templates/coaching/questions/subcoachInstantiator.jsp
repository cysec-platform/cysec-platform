<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<form class="container" id="subcoachInstantiatorForm" onsubmit="updateSubcoachInstantiatorAnswer(event)" data-subcoach-id-regex="${it.question.subcoachIdRegex}">

    <h3>Bestehende</h3>
    <c:choose>
        <c:when test="${fn:length(it.subcoachInstantiatorAnswers) == 0 }">
            <p>Noch keine Einträge erfasst</p>
        </c:when>
        <c:otherwise>
            <c:forEach items="${it.subcoachInstantiatorAnswers}" var="answer">
                <div class="row gx-3 mb-3 subcoach-instance existing-instance">
                    <div class="col-5">
                        <div class="form-floating">
                            <input type="text" readonly class="form-control-plaintext m-0 pt-5" id="parentArgumentText-${answer.key}" placeholder="lorem" value="${answer.value}">
                            <label id="parentArgumentText-${answer.key}">${it.question.parentArgumentValues.label}</label>
                        </div>
                    </div>
                    <div class="col-4">
                        <div class="form-floating">
                            <input type="text" readonly class="form-control-plaintext m-0 pt-5" id="subcoachIdText-${answer.key}" placeholder="lorem" value="${answer.key}">
                            <label id="subcoachIdText-${answer.key}">${it.question.subcoachIdLabel}</label>
                        </div>
                    </div>
                    <div class="col-3">
                        <button type="button" class="btn btn-danger w-100 h-100" onclick="deleteSubcoachInstance('${answer.key}')">Entfernen</button>
                    </div>

                    <select hidden class="form-select parent-argument-select">
                        <c:forEach items="${it.question.parentArgumentValues.value}" var="parentArgumentValue">
                            <option ${answer.value == parentArgumentValue ? "selected" : ""} value="${parentArgumentValue}">${parentArgumentValue}</option>
                        </c:forEach>
                    </select>
                    <input hidden type="text" class="form-control m-0 subcoach-id-input"  value="${answer.key}"/>
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
                <input type="text" class="form-control m-0 subcoach-id-input" id="subcoachId" placeholder="loremipsum"/>
                <label for="subcoachId">${it.question.subcoachIdLabel}</label>
            </div>
        </div>
        <div class="col-3">
            <button type="submit" class="btn btn-primary w-100 h-100">Hinzufügen</button>
        </div>
    </div>
</form>


