package com.linkedpipes.plugin.transformer.shacl;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = ShaclValidatorVocabulary.CONFIGURATION)
public class ShaclValidatorConfiguration {

    @RdfToPojo.Property(iri = ShaclValidatorVocabulary.HAS_FAIL_ON_ERROR)
    private boolean failOnError = false;

    public ShaclValidatorConfiguration() {
    	
    }

    public boolean isFailOnError() {
		return failOnError;
	}
    
    public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

}
