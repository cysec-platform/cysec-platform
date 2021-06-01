const maxCountRecommendations = 4;

/**
 * Retrieves the rendered dashboard from the backend
 */
const getDashboard = () => {
    const url = buildUrl("/api/rest/dashboard");
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.text().then(dashboard => {
                $("#wrapper").append(dashboard);
            })
        } else {
            displayError("GET " + url + "<br>status code " + response.status);
            console.debug(response.status);
        }
    });
};

/**
 * Open a coach with the last answered question
 * @param fqcn - fully qualified coach name
 */
const resume = (fqcn) => {
    const resumeUrl = buildUrl("/api/rest/coaches/" + fqcn + "/resume");
    fetch(resumeUrl, {
        method: "POST",
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            const url = buildUrl("/app/coach.jsp?fqcn=" + fqcn + "&question=_current");
            window.location.href = url;
        } else {
            displayError("GET " + resumeUrl + "<br>status code: " + response.status);
            console.debug(response.status);
        }
    });
};

/**
 * Opens a coach with the first question, no answers will be deleted
 * @param fqcn - fully qualified coach name
 */
const restart = (fqcn) => {
    const restartUrl = buildUrl("/api/rest/coaches/" + fqcn + "/resume");
    fetch(restartUrl, {
        method: "POST",
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            const url = buildUrl("/app/coach.jsp?fqcn=" + fqcn + "&question=_first");
            window.location.href = url;
        } else {
            displayError("GET " + restartUrl + "<br>status code: " + response.status);
            console.debug(response.status);
        }
    });
};

/**
 * Instantiates a new sub-coach
 * @param coachId - full coach id, including parent coach ids
 */
const instantiate = (coachId) => {
    const instantiateUrl = buildUrl("/api/rest/coaches/" + coachId + "/instantiate");
    fetch(instantiateUrl, {
        method: "POST",
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            const url = buildUrl("/app/coach.jsp?fqcn=" + coachId + ".default&question=_first");
            window.location.href = url;
        } else {
            displayError("GET " + instantiateUrl + "<br>status code: " + response.status);
            console.debug(response.status);
        }
    });
};

window.addEventListener("load", getDashboard);