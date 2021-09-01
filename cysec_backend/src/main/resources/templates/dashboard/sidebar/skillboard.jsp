<c:set var="skills" value="${it.skills.getSecond()}" />
<c:set var="strength" value="${skills.getStrength()}"/>
<c:set var="strengthMax" value="${skills.getStrengthMax()}"/>
<c:set var="knowHow" value="${skills.getKnowHow()}"/>
<c:set var="knowHowMax" value="${skills.getKnowHowMax()}"/>
<c:set var="endurance" value="${skills.getEndurance()}"/>
<c:set var="image" value="${skills.getImage()}"/>

<li class="sidebar-brand">
    <h4 class="text-center">
        <c:out value="${it.skills.getFirst()} ${it.msg.skills}"/>
    </h4>
    <div class="skillboard">
        <div class="padding-top-small padding-bottom-medium">
            <img src="${not empty image ? scheme.concat(host).concat(api).concat(image) : '../assets/skillboard.png'}">
        </div>

        <!-- strength -->
        <div class="row padding-bottom-small">
            <div class="col-xs-3 text-left no-padding-left">
                <p class="skilllevel skill-strength">&nbsp;&nbsp;Lvl. 1</p>
            </div>
            <div class="col-xs-6">
                <p class="text-center">${it.msg.strength}</p>
            </div>
            <div class="col-xs-3 no-padding-right text-right">
                <p class="skilllevel skill-info"></p>
            </div>
            <div class="col-xs-12 no-padding-left no-padding-right">
                <div class="progress">
                    <div class="progress-bar" role="progressbar" style="width: ${strength / strengthMax * 100}%;"
                     aria-valuenow="${strength}" aria-valuemin="0" aria-valuemax="${strengthMax}">
                    </div>
                    <div class="progress-bar-title">${strength} / ${strengthMax}</div>
                </div>
            </div>
        </div>
        <!-- know how -->
        <div class="row padding-bottom-small">
            <div class="col-xs-3 text-left no-padding-left">
                <p class="skilllevel skill-knowhow">&nbsp;&nbsp;Lvl. 1</p>
            </div>
            <div class="col-xs-6">
                <p class="text-center">${it.msg.knowHow}</p>
            </div>
            <div class="col-xs-3 no-padding-right text-right">
                <p class="skilllevel skill-info"></p>
            </div>
            <div class="col-xs-12 no-padding-left no-padding-right">
                <div class="progress">
                    <div class="progress-bar" role="progressbar" style="width: ${knowHow / knowHowMax * 100}%;"
                         aria-valuenow="${knowHow}" aria-valuemin="0" aria-valuemax="${knowHowMax}"></div>
                    <div class="progress-bar-title">${knowHow} / ${knowHowMax}</div>
                </div>
            </div>
        </div>
        <!-- endurance/fitness -->
        <div class="row padding-bottom-small">
            <div class="col-xs-3 text-left no-padding-left">
                <p class="skilllevel skill-fitness">&nbsp;&nbsp;Lvl. 1</p>
            </div>
            <div class="col-xs-6">
                <p class="text-center">${it.msg.fitness}</p>
            </div>
            <div class="col-xs-3 no-padding-right text-right">
                <p class="skilllevel skill-info"></p>
            </div>
            <div class="col-xs-12 no-padding-left no-padding-right">

                <div class="progress">
                    <div class="progress-bar" role="progressbar" style="width: ${endurance / 30 * 100}%;"
                         aria-valuenow="${endurance}" aria-valuemin="0" aria-valuemax="100"></div>
                </div>
            </div>
        </div>
    </div>
</li>
