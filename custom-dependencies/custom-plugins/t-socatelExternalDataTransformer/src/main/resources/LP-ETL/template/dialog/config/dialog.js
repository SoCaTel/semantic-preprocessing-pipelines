define([], function () {
    "use strict";

    const DESC = {
        "$namespace": "http://plugins.linkedpipes.com/ontology/t-socatelExternalDataTransformer#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "dataSource": {
            "$type": "str",
            "$label": "Type of data source"
        },
        "singleArray": {
            "$type": "str",
            "$label": "Single or array transformations"
        },
        "singleRootPath": {
            "$type": "str",
            "$label": "JSON path to root of single element"
        },
        "arrayRootPath": {
            "$type": "str",
            "$label": "JSON path to root of array element"
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
