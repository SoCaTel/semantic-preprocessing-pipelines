package com.linkedpipes.plugin.extractor.datasetMetadata;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(
                com.linkedpipes.plugin.extractor.datasetMetadata.DatasetMetadataConfig.class);
    }

}
