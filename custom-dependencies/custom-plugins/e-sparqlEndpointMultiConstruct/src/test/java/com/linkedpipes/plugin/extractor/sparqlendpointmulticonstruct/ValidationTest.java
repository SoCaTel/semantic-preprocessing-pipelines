package com.linkedpipes.plugin.extractor.sparqlendpointmulticonstruct;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import com.linkedpipes.plugin.extractor.sparqlendpointmulticonstruct.SPARQLEndpointMultiCONSTRUCTConfiguration;

import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(SPARQLEndpointMultiCONSTRUCTConfiguration.class);
    }

}
