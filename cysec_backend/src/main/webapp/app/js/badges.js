const endpoint = "/api/rest/badges/render";

const init = () => {
    getBadges();
};

const getBadges = () => {
    const url = buildUrl(endpoint);
    fetch(url, {
        credentials: "include"
    }).then(response => {
            if (response.ok) {
                response.text().then(badges => {
                    $("#wrapper").append(badges);
                })
            } else {
                displayError("Couldn't access badges on: " + url + ": " + response.status);
                console.debug(response.status);
            }
        });
};

window.addEventListener("load", init);