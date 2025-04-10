/**
 * Retrieves the rendered dashboard from the backend
 * @param {() => void | undefined} onSuccess
 */
const getDashboard = (onSuccess = null) => {
    const url = buildUrl("/api/rest/dashboard");
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.text().then(dashboard => {
                $("#wrapper").html(dashboard);
                if (onSuccess) onSuccess();
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
                getDashboard(() => displaySuccess("CySec coach has been reset to its default state"));
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
 * Fetch metadata for given coach.
 * @param {string} coachId 
 */
const openMetaModal = (coachId) => {
    document.getElementById("meta-send-button").onclick = () => submitMeta(coachId);
    const url = buildUrl(`/api/rest/dashboard/metadata/${coachId}`);
    $("#meta-coach-modal .meta-entry-container").load(url);
};

/**
 * Clear modal on close.
 */
const closeMetaModal = () => {
    document.getElementById("meta-send-button").onclick = undefined;
    document.querySelector("#meta-coach-modal .meta-entry-container").innerHTML = "";
}

/**
 * Add another metadata entry row to the form.
 */
const addMeta = () => {
    const key = crypto.randomUUID();
    const template = document.getElementById("meta-entry-template")
    /** @type {Element} */
    const entry = template.content.cloneNode(true);

    entry.querySelector("div").setAttribute("data-meta-key", key);
    entry.querySelector(`input[type="checkbox"]`).id = `${key}-visible`;
    entry.querySelector(`label.form-check-label`).setAttribute("for", `${key}-visible`);
    entry.querySelector("button").onclick = () => deleteMeta(key);

    document.querySelector("#meta-coach-modal .meta-entry-container").appendChild(entry);
}

/**
 * Remove a specific row from the form.
 * @param {string} key key of metadata entry
 */
const deleteMeta = (key) => document
    .querySelector(`.meta-entry-container div[data-meta-key="${key}"]`)
    .remove();

/**
 * Submit new metadata for given coach (PUT semantic).
 * @param {string} coachId 
 */
const submitMeta = (coachId) => {
    if (Array.from(document.querySelectorAll(`.meta-entry-container input[name="key"]`))
        .filter(input => !input.validity.valid).length > 0) {
        displayWarning("all meta data entries must have a key");
        return;
    }

    const data = [];
    for (const entry of document.querySelector(".meta-entry-container").children) {
        const key = entry.querySelector(`input[name="key"]`).value.trim();
        const value = entry.querySelector(`input[name="value"]`).value.trim();
        const visible = entry.querySelector(`input[type="checkbox"]`).checked;

        data.push({
            "key": key,
            "value": value,
            "visible": visible,
        });
    }

    fetch(buildUrl(`/api/rest/dashboard/metadata/${coachId}`), {
        method: "PUT",
        headers: { "Content-Type": "application/json", },
        body: JSON.stringify(data),
    })
        .then(response => {
            if (response.ok) {
                bootstrap.Modal.getInstance($("#meta-coach-modal")).hide();
                getDashboard(() => displaySuccess("meta data updated successfully"));
            } else {
                console.error(response);
                displayError("failed to update metadata");
                bootstrap.Modal.getInstance($("#meta-coach-modal")).hide();
            }
        })
        .catch(error => {
            console.error(error);
            displayError("failed to update metadata");
            bootstrap.Modal.getInstance($("#meta-coach-modal")).hide();
        });
};

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
            getDashboard(() => displaySuccess("Imported coach"));
        } else {
            displayError("POST " + url + "<br>status code: " + response.status);
            console.debug(response.status);
        }
    }).finally(() => {
        bootstrap.Modal.getInstance($("#adminCoachModal")).hide();
        form.reset();
    });
};

window.addEventListener("load", () => getDashboard());
