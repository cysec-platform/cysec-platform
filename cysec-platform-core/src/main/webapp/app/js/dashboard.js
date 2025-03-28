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

const reset = (fqcn) => {
    if (confirm("Do you really want to reset the coach and its answers? This cannot be undone!")) {
        const resetUrl = buildUrl("/api/rest/coaches/" + fqcn + "/reset");
        fetch(resetUrl, {
            method: "POST",
            credentials: "include"
        }).then(response => {
            if (response.ok) {
                displaySuccess("CySec coach has been reset to its default state");
            } else {
                displayError("GET " + resetUrl + "<br>status code: " + response.status);
                console.debug(response.status);
            }
        });
    }
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

/**
 * Inject the coach id of the selected coach into the generic modal.
 * @param {string} coachId - full coach id
 */
const openAdminModal = (coachId) => {
    const modal = $("#adminCoachModal .modal-body");
    const exportButton = modal.children("a")[0];
    /** @type{HTMLFormElement} */
    const importForm = modal.children("form")[0];

    exportButton.download = `${coachId}.zip`;
    exportButton.href = buildUrl(`/api/rest/coaches/${coachId}/export`);

    importForm.onsubmit = (event) => {
        event.preventDefault();
        submitImportForm(importForm, coachId)
    };
};

/**
 * 
 * @param {string} coachId 
 */
const openMetaModal = (coachId) => {
    const url = buildUrl(`/api/rest/dashboard/metadata/${coachId}`);
    $("#metaCoachModal .modal-body").load(url);
};

const addMeta = () => {
    const template = $("keyValueFormTemplate");
    const clone = template.content.cloneNode(true);
    $("#metaCoachModal .modal-body").appendChild(clone);
}

/**
 * Overwriting the default behavior of an HTML form to handle 
 * both the error and the success case client side.
 * 
 * @param {HTMLFormElement} form 
 * @param {string} coachId
 */
const submitImportForm = (form, coachId) => {
    const url = buildUrl(`/api/rest/coaches/${coachId}/import`);
    const formData = new FormData(form);

    fetch(url, {
        method: "POST",
        body: formData,
        credentials: "include",
    }).then(response => {
        if (response.ok) {
            displaySuccess("Imported coach");
        } else {
            displayError("POST " + url + "<br>status code: " + response.status);
            console.debug(response.status);
        }
    }).finally(() => {
        bootstrap.Modal.getInstance($("#adminCoachModal")).hide();
        form.reset();
    });
};

window.addEventListener("load", getDashboard);
