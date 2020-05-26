const endpoint = "/api/rest/resetPassword/";

const submitRequest = (event) => {
    event.preventDefault();
    let formData = $("#mail-form").serializeArray();
    let company = formData[0].value;
    let email = formData[1].value;
    let form = document.forms['mail-form'];

    // only process if fields validated
    if(checkForm(form)) {
        const url = buildUrl(endpoint
            + "create?company=" + encodeURIComponent(company)
            + "&email=" + encodeURIComponent(email));
        fetch(url, {
            method: "POST",
            body: JSON.stringify({"company": company}),
            headers: {
                "Content-Type": "application/json"
            }
        }).then((response) => {
            if (response.status === 200) {
                alert("Submitted token request")
            } else if (response.status === 404) {
                alert("No user with given email registered");
            } else {
                alert("Couldn't save recovery token. Please get in touch with the CySec development team.");
            }
        })
    }
};

/**
 * Helper method to trigger native form validation.
 * Because button do not invoke 'submit', this trick has to be applied
 * @param form the form to check
 * @returns {boolean} true if form is valid, false otherwise
 */
const checkForm = (form) => {
    if(form.checkValidity()) {
        return true;
    } else {
        // fake submit button in form
        const tmpSubmit = document.createElement('button');
        form.appendChild(tmpSubmit);
        // force native UI hints
        tmpSubmit.click();
        form.removeChild(tmpSubmit);
        return false;
    }
};

const resetPassword = (event) => {
    event.preventDefault();
    const formData = $("#reset-form").serializeArray();
    const password1 = formData[0].value;
    const password2 = formData[1].value;
    const company = formData[2].value;
    const token = formData[3].value;
    const form = document.forms['reset-form'];
    // only process if all fields validated
    if(checkForm(form)) {
        fetch(buildUrl(endpoint + "verifyToken/" + token
            + "?password1=" + encodeURIComponent(password1)
            + "&password2=" + encodeURIComponent(password2)
            + "&company=" + encodeURIComponent(company)
        ), {
            method: "POST",
            // body: JSON.stringify({}),
            headers: {
                "Content-Type": "application/json"
            }
        }).then((response) => {
            if(response.status === 200){
                alert("Password changed")
            } else if (response.status === 404) {
                alert("No user for token found");
            } else if (response.status === 304) {
                alert("Invalid form data. Check the passwords matched");
            } else {
                alert("Couldn't process reset. Please get in touch with the CySec development team.");
            }
        })
    }
};
