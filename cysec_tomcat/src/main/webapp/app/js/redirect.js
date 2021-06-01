const redirectQuestion = (fqcn, qid) => {
    let url;
    switch (qid) {
        case "_current":
            url = buildUrl("/api/rest/coaches/" + fqcn + "/questions/current");
            break;
        case "_first":
            url = buildUrl("/api/rest/coaches/" + fqcn + "/questions/first");
            break;
        case "_last":
            url = buildUrl("/api/rest/coaches/" + fqcn + "/questions/last");
            break;
        default:
            return false;
    }
    fetch(url, {
        credentials: "include",
    }).then(response => {
        if (response.ok) {
            response.json().then(question => {
                const nid = question["id"];
                const url = buildUrl("/app/coach.jsp?fqcn=" + fqcn + "&question=" + nid);
                window.location.replace(url);
            });
        } else {
            displayError("Couldn't access question on: " + endpoint + ": " + response.status);
            console.debug(response.status);
        }
    });
    return true;
};