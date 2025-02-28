package com.linkedpipes.etl.dataunit.core.pipeline;

import com.linkedpipes.etl.executor.api.v1.rdf.model.RdfValue;
import com.linkedpipes.etl.executor.api.v1.rdf.pojo.Loadable;
import com.linkedpipes.etl.executor.api.v1.vocabulary.LP_PIPELINE;

class DataUnit implements Loadable {

    private String resource;

    private String binding;

    @Override
    public void resource(String resource) {
        this.resource = resource;
    }

    @Override
    public Loadable load(String predicate, RdfValue value) {
        switch (predicate) {
            case LP_PIPELINE.HAS_BINDING:
                binding = value.asString();
                return null;
            default:
                break;
        }
        return null;
    }

    public String getResource() {
        return resource;
    }

    public String getBinding() {
        return binding;
    }

}
