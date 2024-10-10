let fqcn, qid;

const init = () => {
    const queryParam = new URLSearchParams(window.location.search);
    fqcn = queryParam.get('fqcn');
    qid = queryParam.get('question');
    if (!redirectQuestion(fqcn, qid)) {
        load();
    }
};

const load = () => {
    console.log("loading question " + qid + " from coach " + fqcn);
    const url = buildUrl("/api/rest/coaches/" + fqcn + "/questions/" + qid + "/render");
    fetch(url, {
        credentials: "include"
    }).then(response => {
        if (response.ok) {
            response.text().then(question => {
                const wrapper = $("#wrapper");
                wrapper.empty();
                wrapper.append(question);
                $(function() {$('[data-toggle="tooltip"]').tooltip()}); // opt-in bootstrap tooltips for pagination (do it here and not on init, since the navigation can be replaced at any time)
            })
        } else {
            displayError("Couldn't access question on: " + url + ": " + response.status);
            console.debug(response.status);
        }
    });
};

const updateAnswer = (event) => {
    // separate comma from coach string in window location
    const url = buildUrl("/api/rest/coaches/" + fqcn + "/answers/" + qid);
    const answer = event.target.value;
    fetch(url, {
        method: "POST",
        body: answer,
        credentials: "include",
    }).then(response => {
        if (response.ok) {
            load()
        } else {
            displayError("Couldn't update answer on: " + url + ": " + response.status);
            console.debug(response.status);
        }
    });
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

window.addEventListener("load", init);
