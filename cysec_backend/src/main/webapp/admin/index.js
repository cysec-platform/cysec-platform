const endpoint = "/api/rest/admin";

const init = () => {
    getAdminPage();
};

const getAdminPage = () => {
    const url = buildUrl(endpoint);
    fetch(url, {
        credentials: "include"
    }).then(response => {
            if (response.ok) {
                response.text().then(adminPage => {
                    $("#wrapper").append(adminPage);
                })
            } else {
                displayError("Couldn't access admin on: " + url + ": " + response.status);
                console.debug(response.status);
            }
        });
};

const loadAudits = (companyId) => {
    const url = buildUrl(`${endpoint}/audits/${companyId}`);
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.text().then(audits => {
                $("#audits").empty().append(audits);
            })
        } else {
            displayError("Couldn't access admin on: " + url + ": " + response.status);
            console.debug(response.status);
        }
    });
};

window.addEventListener("load", init);