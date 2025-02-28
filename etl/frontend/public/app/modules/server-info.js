((definition) => {
    if (typeof define === "function" && define.amd) {
        define(["./http"], definition);
    }
})((http) => {
    "use strict";

    const cache = {
        "ready": false,
        "data": {}
    };

    function load() {
        if (cache.ready) {
            return Promise.resolve(cache.data);
        } else {
            return fetchFromRemote();
        }
    }

    function fetchFromRemote() {
        return http.getJson("api/v1/info").then((response) => {
            cache.data = response.payload;
            cache.ready = true;
            return cache.data;
        }).catch((error) => {
            console.error("Can't fetch info file.");
            throw error;
        });
    }

    function getCommit() {
        return cache.data["version"]["commit"];
    }

    function getFtpUrl() {
        return cache.data["path"]["ftp"];
    }

    return {
        "load": load,
        "getCommit": getCommit,
        "getFtpUrl": getFtpUrl
    };

});
