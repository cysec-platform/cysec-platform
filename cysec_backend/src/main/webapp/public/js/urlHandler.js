const context = window.location.pathname.substring(1, window.location.pathname.indexOf("/", 1));
const home = `${window.location.origin}/${context}`;

const buildUrl = (endpoint) => home + endpoint;