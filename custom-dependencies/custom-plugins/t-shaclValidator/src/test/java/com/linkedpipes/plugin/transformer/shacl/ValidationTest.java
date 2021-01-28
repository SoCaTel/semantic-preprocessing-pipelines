package com.linkedpipes.plugin.transformer.shacl;

import org.junit.Test;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(ShaclValidatorConfiguration.class);
    }

}
