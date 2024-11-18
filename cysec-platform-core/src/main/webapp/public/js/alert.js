const displaySuccess = (msg) => {
    $("#wrapper").prepend("<div class='alert alert-warning alert-dismissible fade show pt-5' role='alert'>" +
        "<strong>Success: </strong>" + msg + "<div>" +
        "<button type='button' class='btn-close' data-bs-dismiss='alert' aria-label='Close'></button>");
};

const displayInfo = (msg) => {
    $("#wrapper").prepend("<div class='alert alert-warning alert-dismissible fade show pt-5' role='alert'>" +
        "<strong>Info: </strong>" + msg + "<div>" +
        "<button type='button' class='btn-close' data-bs-dismiss='alert' aria-label='Close'></button>");
};

const displayWarning = (msg) => {
    $("#wrapper").prepend("<div class='alert alert-warning alert-dismissible fade show pt-5' role='alert'>" +
        "<strong>Warning: </strong>" + msg + "<div>" +
        "<button type='button' class='btn-close' data-bs-dismiss='alert' aria-label='Close'></button>");
};

const displayError = (msg) => {
    $("#wrapper").empty().append("<div class='alert alert-danger pt-5' role='alert'>" +
    "<strong>Error: </strong>" + msg + "<div>");
};