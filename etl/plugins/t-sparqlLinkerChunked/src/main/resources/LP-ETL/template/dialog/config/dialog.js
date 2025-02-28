define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-sparqlConstruct#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "query": {
            "$type": "str",
            "$label": "SPARQL CONSTRUCT query"
        },
        "outputMode": {
            "$type": "iri",
            "$label": "Output mode",
            "$onLoad": defaultOutputMode
        }
    };

    function defaultOutputMode(value) {
        if (value === undefined) {
           return DESC["$namespace"] + "createNewChunk";
        } else {
            return value;
        }
    }

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
