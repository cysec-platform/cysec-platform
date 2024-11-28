<c:set var="skills" value="${it.skills.getSecond()}" />
<c:set var="strength" value="${skills.getStrength()}"/>
<c:set var="strengthMax" value="${skills.getStrengthMax()}"/>
<c:set var="knowHow" value="${skills.getKnowHow()}"/>
<c:set var="knowHowMax" value="${skills.getKnowHowMax()}"/>
<c:set var="endurance" value="${skills.getEndurance()}"/>
<c:set var="image" value="${skills.getImage()}"/>

<div class="sidebar-brand">
    <h4 class="text-center">
        <c:out value="${it.skills.getFirst()} ${it.msg.skills}"/>
    </h4>
    <div class="skillboard">
        <div class="pt-3 pb-4 text-center">
            <img class="skill-image" src="${not empty image ? scheme.concat(host).concat(api).concat(image) : '../assets/skillboard.png'}">
        </div>
        <!-- strength -->
        <c:if test="${strengthMax gt 0}">
            <div class="pb-3">
                <div class="row justify-content-between">
                    <p class="col skilllevel skill-strength">&nbsp;&nbsp;Lvl. 1</p>
                    <p class="col text-center">${it.msg.strength}</p>
                    <p class="col text-end skilllevel skill-info"></p>
                </div>
                <div>
                    <div class="progress">
                        <div class="progress-bar" role="progressbar" style="width: ${strength / strengthMax * 100}%;" aria-valuenow="${strength}" aria-valuemin="0" aria-valuemax="${strengthMax}"></div>
                        <div class="progress-bar-title">${strength} / ${strengthMax}</div>
                    </div>
                </div>
            </div>
        </c:if>
        <!-- know how -->
        <c:if test="${knowHowMax gt 0}">
            <div class="pb-3">
                <div class="row justify-content-between">
                    <p class="col skilllevel skill-knowhow">&nbsp;&nbsp;Lvl. 1</p>
                    <p class="col text-center">${it.msg.knowHow}</p>
                    <p class="col text-end skilllevel skill-info"></p>
                </div>
            </div>
            <div class="progress">
                <div class="progress-bar" role="progressbar" style="width: ${knowHow / knowHowMax * 100}%;" aria-valuenow="${knowHow}" aria-valuemin="0" aria-valuemax="${knowHowMax}"></div>
                <div class="progress-bar-title">${knowHow} / ${knowHowMax}</div>
            </div>
        </c:if>
        <!-- endurance/fitness -->
        <div class="pb-3">
            <div class="row justify-content-between">
                <p class="col skilllevel skill-fitness">&nbsp;&nbsp;Lvl. 1</p>
                <p class="col text-center">${it.msg.fitness}</p>
                <p class="col text-end skilllevel skill-info"></p>
            </div>
            <div class="progress">
                <div class="progress-bar" role="progressbar" style="width: ${endurance / 30 * 100}%;" aria-valuenow="${endurance}" aria-valuemin="0" aria-valuemax="100"></div>
            </div>
        </div>
    </div>
</div>
