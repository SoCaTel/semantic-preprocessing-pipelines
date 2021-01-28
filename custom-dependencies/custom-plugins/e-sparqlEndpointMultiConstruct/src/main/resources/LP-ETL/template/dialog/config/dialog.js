define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointMultiConstruct#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "delimiter": {
            "$type": "str",
            "$label": "Delimiter"
        },
        "quoteChar": {
            "$type": "str",
            "$label": "Quote Character"
        },
        "encoding": {
            "$type": "str",
            "$label": "Encoding"
        },
        "endpoint": {
            "$type": "str",
            "$label": "Endpoint URL"
        },
        "query": {
            "$type": "str",
            "$label": "SPARQL CONSTRUCT query"
        },
        "defaultGraph" : {
            "$type": "value",
            "$array": true,
            "$label" : "Default graph"
        },
        "headerAccept" : {
            "$type" : "str",
            "$label" : "Used MimeType"
        },
        "encodeRdf": {
            "$type": "bool",
            "$label": "Encode RDF"
        },
        "useAuthentication": {
            "$type": "bool",
            "$label": "Use authentication"
        },
        "userName": {
            "$type": "str",
            "$label": "User name"
        },
        "password": {
            "$type": "str",
            "$label": "Password"
        },
        "useTolerantRepository": {
            "$type": "bool",
            "$label": "Fix invalid types"
        },
        "handleInvalidData": {
            "$type": "bool",
            "$label": "Handle invalid data"
        }
    };

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        dialogManager.load();

    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});
