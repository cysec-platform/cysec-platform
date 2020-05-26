const _endpoint = "/api/rest/signUp";
const _home = buildUrl("/app");

/**
 * Retrieves the rendered page content
 */
const load = () => {
    const queryParam = new URLSearchParams(window.location.search);
    const url = buildUrl(_endpoint) + "/" + (!queryParam.has('company') ? "user" : "company");
    fetch(url)
        .then((response) => {
            if (response.ok) {
                response.text().then((signUpForm) => {
                    const wrapper = $("#wrapper");
                    wrapper.empty();
                    wrapper.append(signUpForm);
                })
            } else {
                displayError("Couldn't access SignUpController on: " + _endpoint + ": " + response.status);
                console.debug(response.status);
            }
        })
};

/**
 * Adds a new user to the specified company.
 * The user will have a pending lock.
 */
const addUser = () => {
    const formData = $("#new-user-form").serializeArray();
    const user = {
        "username": formData[1].value,
        "firstname": formData[2].value,
        "surname": formData[3].value,
        "email": formData[4].value,
        "password": formData[5].value,
    };
    const companyId = encodeURI(formData[0].value);
    const form = document.forms['new-user-form'];
    if (checkForm(form)) {
        const url = buildUrl(_endpoint) + "/user?company=" + companyId;
        fetch(url, {
            method: "POST",
            body: JSON.stringify(user),
            headers: {
                "Content-Type": "application/json"
            }
        }).then((response) => {
            if (response.ok) {
                displaySuccess("created user '" + user.username
                    + "'.<br>You are currently locked, please contact your company admin."
                    + "<br>Navigate to dashboard <a href=" + _home + " class='alert-link'>here</a>.");
            } else if (response.status === 409) {
                displayWarning("User '" + user.username + "' already exists.");
            } else {
                displayWarning("Failed to create user '" + user.username + "'.");
            }
        })
    }
};

/**
 * Adds a new company.
 * The user will be admin of that company.
 */
const addCompany = () => {
    const formData = $("#new-user-form").serializeArray();
    const user = {
        "username": formData[2].value,
        "firstname": formData[3].value,
        "surname": formData[4].value,
        "email": formData[5].value,
        "password": formData[6].value,
    };
    const companyId = encodeURI(formData[0].value);
    const companyName = encodeURI(formData[1].value);
    const form = document.forms['new-user-form'];
    if (checkForm(form)) {
        const url = buildUrl(_endpoint) + "/company?id=" + companyId + "&name=" + companyName;
        fetch(url, {
            method: "POST",
            body: JSON.stringify(user),
            headers: {
                "Content-Type": "application/json"
            }
        }).then((response) => {
            if (response.ok) {
                displaySuccess("Created company '" + companyId
                    + "'.<br>You are admin of this company."
                    + "<br>Navigate to dashboard <a href=" + _home + " class='alert-link'>here</a>.");
            } else if (response.status === 409) {
                displayWarning("Company '" + companyId + "' already exists.");
            } else {
                displayWarning("Failed to create company '" + companyId + "'.");
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
    if (form.checkValidity()) {
        return true;
    } else {
        // fake submit button in form
        let tmpSubmit = document.createElement('button');
        form.appendChild(tmpSubmit);
        // force native UI hints
        tmpSubmit.click();
        form.removeChild(tmpSubmit);
        return false;
    }
};

window.addEventListener("load", load);