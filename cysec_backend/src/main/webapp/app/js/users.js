
const endpoint = buildUrl("/api/rest/users");

const init = () => {
    load()
};

const load = () => {
    const url = `${endpoint}/render`;
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.text().then(users => {
                $("#wrapper").html(users);
            })
        } else {
            alert("Couldn't load users!");
        }
    })
};

// When the user clicks on the button, open the modal
const openAddUserModal = () => {
    // populateCompaniesDropdown(addModal.find("[name='company']"));
    $('#addUserModal').css("display", "block");
};

const openEditUserModal = (event) => {
    const url = `${endpoint}/${event.target.id}`;
    const editModal = $('#editUserModal');
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.json().then(user => {
                editModal.find("[name='id']").val(user.id);
                editModal.find("[name='username']").val(user.username);
                editModal.find("[name='firstname']").val(user.firstname);
                editModal.find("[name='surname']").val(user.surname);
                editModal.find("[name='email']").val(user.email);
                editModal.find("[name='locale']").val(user.locale);
                editModal.find("[name='roles']").val(user.role !== undefined ? user.role.join(' ') : '');
                editModal.find("[name='lock']").val(user.lock);
                // populateCompaniesDropdown(editModal.find("[name='company']"));
                editModal.css("display", "block");
            })
        } else {
            alert("Filling user data into form failed, please retry.")
        }
    })
};

const populateCompaniesDropdown = (dropdown) => {
    const url = buildUrl("/api/rest/companies");
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.json().then(company => {
                const opt = document.createElement('option');
                opt.value = company;
                opt.innerHTML = company;
                dropdown.append(opt);
            })
        } else {
            alert("Couldn't retrieve companies.")
        }
    })
};

// When the user clicks on <span> (x), close the modal
const closeModals = () => {
    $('#addUserModal').css("display", "none");
    $('#editUserModal').css("display", "none");
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

const addUser = (event) => {
    event.preventDefault();
    const form = document.forms['newUserForm'];
    if (checkForm(form)) {
        const formData = $("#newUserForm").serializeArray();
        const user = {
            "username": formData[0].value,
            "firstname": formData[1].value,
            "surname": formData[2].value,
            "password": formData[3].value,
            "email": formData[4].value,
            "locale": formData[5].value,
            "roles": formData[6].value.split(" ")
        };
        fetch(endpoint, {
            method: "POST",
            body: JSON.stringify(user),
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include"
        }).then(response => {
            if (response.ok) {
                alert("User created!");
                closeModals();
                load();
            } else if (response.status === 409) {
                alert("User already exists!");
            } else {
                alert("Failed to create user!");
            }
        })
    }
};

const editUser = (event) => {
    event.preventDefault();
    const form = document.forms['editUserForm'];
    if (checkForm(form)) {
        const formData = $("#editUserForm").serializeArray();
        const url = `${endpoint}/${formData[0].value}`;
        const user = {
            "username": formData[1].value,
            "firstname": formData[2].value,
            "surname": formData[3].value,
            "email": formData[5].value,
            "locale": formData[6].value,
            "role": formData[7].value.split(" "),
            "lock": formData[8].value
        };
        // "password": formData[4].value,
        fetch(url, {
            method: "PUT",
            body: JSON.stringify(user),
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include"
        }).then(response => {
            if (response.ok) {
                alert("User updated!");
                closeModals();
                load();
            } else if (response.status === 404) {
                alert("User not found!");
            } else {
                alert("Failed to update user!");
            }
        })
    }
};

const deleteUser = (id) => {
    const url = `${endpoint}/${id}`;
    fetch(url, {
        method: "DELETE",
        credentials: "include"
    }).then(response => {
        if (response.status === 201) {
            alert("User deleted!");
            load();
        } else if (response.status === 409) {
            alert("User doesn't exist!");
        } else {
            alert("Failed to delete user!");
        }
    })
};


// When the user clicks anywhere outside of the modal, close it
window.addEventListener("load", init);
window.addEventListener("keydown", e => {
    if (e.keyCode === 27) {
        closeModals()
    }
});
window.addEventListener("click", (event) => {
    if (event.target === $('#addUserModal') || event.target === $('#sidebar-wrapper') || event.target === $('#global-nav')) {
        closeModals();
    }
});