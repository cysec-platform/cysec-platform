const endpoint = "/api/rest/admin";

const init = () => {
    getAdminPage();
};

const getAdminPage = () => {
    const url = buildUrl(endpoint);
    loadContent(url, "#wrapper");
};

const loadAudits = (companyId) => {
    const url = buildUrl(`${endpoint}/audits/${companyId}`);
    loadContent(url, "#admin-wrapper");
};

const loadCoaches = (companyId) => {
    const url = buildUrl(`${endpoint}/coaches/${companyId}`);
    loadContent(url, "#admin-wrapper");
};

/**
 * Fetch content from an endpoint and append the response to `domId`.
 * 
 * @param {string | URL} url 
 * @param {string} domId - id where to append the fetched content
 */
const loadContent = (url, domId) => {
    const id = domId.startsWith("#") ? domId : `#${domId}`;

    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.text().then(content => {
                $(id).empty().append(content);
            })
        } else {
            displayError("Couldn't access admin on: " + url + ": " + response.status);
            console.debug(response.status);
        }
    });
};

window.addEventListener("load", init);