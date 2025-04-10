let fqcn, qid;

// These options for morphdom solves the problem that script tags are not interpreted by default when using morphdom
// Code taken from: https://github.com/patrick-steele-idem/morphdom/issues/178#issuecomment-652562769
const morphdomOptions = {
    onNodeAdded: function (node) {
        if (node.nodeName === 'SCRIPT') {
            var script = document.createElement('script');
            //copy over the attributes
            [...node.attributes].forEach( attr => { script.setAttribute(attr.nodeName ,attr.nodeValue) })

            script.innerHTML = node.innerHTML;
            node.replaceWith(script)
        }
    },
    onBeforeElUpdated: function (fromEl, toEl) {
        if (fromEl.nodeName === "SCRIPT" && toEl.nodeName === "SCRIPT") {
            var script = document.createElement('script');
            //copy over the attributes
            [...toEl.attributes].forEach( attr => { script.setAttribute(attr.nodeName ,attr.nodeValue) })

            script.innerHTML = toEl.innerHTML;
            fromEl.replaceWith(script)
            return false;
        }
        return true;
    }
};

const init = () => {
    const queryParam = new URLSearchParams(window.location.search);
    fqcn = queryParam.get('fqcn');
    qid = queryParam.get('question');
    if (!redirectQuestion(fqcn, qid)) {
        load();
    }
    registerDebugInfoShortcut();
};

const load = () => {
    console.log("loading question " + qid + " from coach " + fqcn);
    const url = buildUrl("/api/rest/coaches/" + fqcn + "/questions/" + qid + "/render");
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.text().then(question => {
                // Update the content of the question wrapper with the new question
                const newContent = document.getElementById('wrapper').cloneNode(true);
                newContent.innerHTML = question;
                morphdom(document.getElementById('wrapper'), newContent, morphdomOptions);

                $(function() {
                    // opt-in bootstrap tooltips for pagination (do it here and not on init, since the navigation can be replaced at any time)
                    $('[data-bs-toggle="tooltip"]').each(function () {
                        new bootstrap.Tooltip($(this).get(0))
                    })
                });
            })
        } else {
            displayError("Couldn't access question on: " + url + ": " + response.status);
            console.debug(response.status);
        }
    });
};

const registerDebugInfoShortcut = () => {
    document.addEventListener("keydown", e => {
        if (e.altKey && e.shiftKey && e.code === 'KeyD') {
            const modal = new bootstrap.Modal(document.getElementById('coachDebugModal'));
            modal.show();
        }
    })
}

const toggleFlagged = () => {
    const currentFlaggedState = document.querySelector("#question-flag").getAttribute("data-flagged") === 'true';
    const url = buildUrl("/api/rest/coaches/" + fqcn + "/questions/" + qid + "/flag");

    fetch(url, {
        method: "POST",
        body: !currentFlaggedState,
        credentials: "include",
    }).then(response => {
        if (response.ok) {
            load()
        } else {
            console.debug(response.status);
            displayError("Couldn't toggle flagged state on: " + url + ": " + response.status);
        }
    }).catch(e => {
        console.debug(e);
    });
};

const updateAnswer = (event) => {
    sendAnswer(event.target.value);
};

const sendAnswer = (answerPayload) => {
    const url = buildUrl("/api/rest/coaches/" + fqcn + "/answers/" + qid);
    fetch(url, {
        method: "POST",
        body: answerPayload,
        credentials: "include",
    }).then(response => {
        $("#next-button").prop("disabled", false);
        if (response.ok) {
            load()
        } else {
            console.debug(response.status);
            displayError("Couldn't update answer on: " + url + ": " + response.status);
        }
    }).catch(e => {
        console.debug(e);
        $("#next-button").prop("disabled", false);
    });
    /* disable the next button while submitting answer to ensure
       the server will not route the user (request to .../next) to a
       wrong question by using the "old state". */
    $("#next-button").prop("disabled", true);
};

const getSubcoachInstanceFromEl = (el) => {
    const formEl = document.querySelector("#subcoachInstantiatorForm");

    const parentArgumentSelectEl = el.querySelector(".parent-argument-select");
    const instanceNameEl = el.querySelector(".instance-name-input");

    const parentArgument = parentArgumentSelectEl.value;
    const instanceName = instanceNameEl.value;

    const instanceNameRegex = formEl.getAttribute('data-instance-name-regex');
    if (instanceNameRegex && !new RegExp(instanceNameRegex).test(instanceName)) {
        displayWarning("Invalid input, cannot add")
        return;
    }
    return [instanceName, parentArgument];
}

const getExistingSubcoachInstances = () => {
    // Extract input data and validate
    const formEl = document.querySelector("#subcoachInstantiatorForm");

    // Build up payload by looking at each subcoach instance
    let payload = {};
    const subcoachInstanceEls = [...formEl.querySelectorAll(".subcoach-instance.existing-instance")];
    for (let instanceEl of subcoachInstanceEls) {
        const [instanceName, parentArgument] = getSubcoachInstanceFromEl(instanceEl);
        payload[instanceName] = parentArgument;
    }

    return payload;
}

const getNewSubcoachInstance = () => {
    const subcoachInstanceEl = document.querySelector("#subcoachInstantiatorForm .subcoach-instance.new-instance");
    const [instanceName, parentArgument] = getSubcoachInstanceFromEl(subcoachInstanceEl);
    const instance = {};
    instance[instanceName] = parentArgument;
    return instance;
}

const updateSubcoachInstantiatorAnswer = (event) => {
    event.preventDefault();
    const subcoachInstances = {
        ...getExistingSubcoachInstances(),
        ...getNewSubcoachInstance()
    };
    sendAnswer(JSON.stringify(subcoachInstances));
};

const deleteSubcoachInstance = (instanceName) => {
    const subcoachInstances = getExistingSubcoachInstances();
    delete subcoachInstances[instanceName];
    sendAnswer(JSON.stringify(subcoachInstances));
};

const toggleReadMore = (event) => {
    const elem = $(".readmore-box");
    elem.css('visibility', elem.css('visibility') === 'hidden' ? 'visible' : 'hidden');
};

const exclusiveCheck = (event) => {
    // save current state of the checked box
    const box = $("#" + event.target.id); // e.g q1o1
    const checkedState = box.prop('checked');
    // case 'none' option selected
    if(box.attr('value') === event.target.name + 'oNone') {
        $('input:checked').each((index, element) => {
            console.log(element);
            var id = element.value;
            var item = $("#" + id);
            // clear all checkboxes, except 'noneOption'
            if(id !== event.target.name + 'oNone') {
                item.prop('checked', false);
                item.trigger("change");
            }
        }); // revert saved state
        box.prop('checked', checkedState);
    }
    else {
        // find 'None' option and uncheck
        const none = $('#' + event.target.name + 'oNone');
        // only change and fire event, if 'none' was previously checked
        if(none.prop('checked')) {
            none.prop('checked', false);
            none.trigger("change");
        }
    }
};

const loadSubcoach = (subcoachFqcn) => {
    const redirectUrl = buildUrl("/app/coach.jsp?fqcn=" + subcoachFqcn + "&question=_first");
    window.location.replace(redirectUrl);
}

window.addEventListener("load", init);
