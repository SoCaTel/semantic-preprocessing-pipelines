"use strict";

const express = require("express");
const config = require("./../modules/configuration");
const request = require("request"); // https://github.com/request/request
const errors = require("./error-codes");
const info = require("./../modules/info");

const router = express.Router();
module.exports = router;

const storageApiUrlPrefix = config.storage.url + "/api/v1/components/";
const monitorApiUrl = config.executor.monitor.url;

router.get("/components/:type", (req, res) => {
    let url = storageApiUrlPrefix;
    switch (req.params.type) {
        case "interface":
            url += "interface?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            break;
        case "definition":
            url += "definition?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            break;
        case "effective":
            url += "configEffective?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            break;
        case "config":
            url += "config?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            break;
        case "configTemplate":
            url += "configTemplate?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            break;
        case "configDescription":
            url += "configDescription?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            break;
        case "static":
            url += "static?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            url += "&file=" + encodeURIComponent(req.query.file);
            break;
        case "dialog":
            url += "dialog?";
            url += "iri=" + encodeURIComponent(req.query.iri);
            url += "&file=" + encodeURIComponent(req.query.file);
            url += "&name=" + encodeURIComponent(req.query.name);
            break;
        default:
            res.status(400).json({
                "error": {
                    "type": errors.INVALID_REQUEST,
                    "source": "FRONTEND",
                    "message": "Unexpected type: " + req.params.type
                }
            });
            return;
    }
    request.get({"url": url, "headers": req.headers})
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

function handleConnectionError(res, error) {
    console.error("Request failed:\n", error);
    res.status(503).json({
        "error": {
            "type": errors.CONNECTION,
            "source": "FRONTEND"
        }
    });
}

router.post("/components/config", (req, res) => {
    const url = storageApiUrlPrefix + "config?iri=" +
        encodeURIComponent(req.query.iri);
    req.pipe(request.post(url, {"form": req.body}), {"end": false})
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

router.post("/components/component", (req, res) => {
    const url = storageApiUrlPrefix + "component?iri=" +
        encodeURIComponent(req.query.iri);
    req.pipe(request.post(url, {"form": req.body}), {"end": false})
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

router.get("/info", (req, res) => {
    res.status(200).json(info);
});

router.get("/usage", (req, res) => {
    const options = {
        "url": config.storage.url + "/api/v1/components/usage?iri=" +
        encodeURIComponent(req.query.iri),
        "headers": req.headers
    };
    request.get(options)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

router.get("/jars/file", (req, res) => {
    let iri = config.storage.url + "/api/v1/jars/file?";
    iri += "iri=" + encodeURIComponent(req.query.iri);
    //
    request.get(iri)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

router.get("/autocomplete/terms", (req, res) => {
    const endpoint = "http://lov.linkeddata.es/dataset/lov/api/v2/autocomplete/terms";
    const remote_url = req.originalUrl.replace("/api/v1/autocomplete/terms", endpoint);
    //
    request.get(remote_url)
        .on("error", (error) => handleConnectionError(res, error))
        .pipe(res);
});

router.get("/debug/metadata/**", (req, res) => {
  let url = monitorApiUrl + req.originalUrl.replace("/api/v1/", "");
  console.log(url);
  request.get(url)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});


router.get("/debug/data/**", (req, res) => {
  let url = monitorApiUrl + req.originalUrl.replace("/api/v1/", "");
  console.log(url);
  request.get(url)
    .on("error", (error) => handleConnectionError(res, error))
    .pipe(res);
});
