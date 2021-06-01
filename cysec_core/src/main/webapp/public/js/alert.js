const displaySuccess = (msg) => {
    $("#wrapper").prepend("<div class='alert alert-success alert-dismissible below-header' role='alert'>" +
        "<button type='button' class='close' data-dismiss='alert' aria-label='Close'><span aria-hidden='true'>&times;</span></button>" +
    "<strong>Success: </strong>" + msg + "<div>");
};

const displayInfo = (msg) => {
    $("#wrapper").prepend("<div class='alert alert-success alert-dismissible below-header' role='alert'>" +
        "<button type='button' class='close' data-dismiss='alert' aria-label='Close'><span aria-hidden='true'>&times;</span></button>" +
    "<strong>Info: </strong>" + msg + "<div>");
};

const displayWarning = (msg) => {
    $("#wrapper").prepend("<div class='alert alert-warning alert-dismissible below-header' role='alert'>" +
        "<button type='button' class='close' data-dismiss='alert' aria-label='Close'><span aria-hidden='true'>&times;</span></button>" +
        "<strong>Warning: </strong>" + msg + "<div>");
};

const displayError = (msg) => {
    $("#wrapper").empty().append("<div class='alert alert-danger below-header' role='alert'>" +
    "<strong>Error: </strong>" + msg + "<div>");
};