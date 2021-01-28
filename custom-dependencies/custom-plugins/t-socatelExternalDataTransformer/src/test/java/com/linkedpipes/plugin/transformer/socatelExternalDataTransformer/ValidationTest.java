package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.SocatelExternalDataTransformerConfiguration;

import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(SocatelExternalDataTransformerConfiguration.class);
    }

}
