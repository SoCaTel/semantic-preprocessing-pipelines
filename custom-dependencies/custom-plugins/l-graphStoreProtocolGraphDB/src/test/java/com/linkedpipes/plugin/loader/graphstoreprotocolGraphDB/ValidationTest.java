package com.linkedpipes.plugin.loader.graphstoreprotocolGraphDB;

import com.linkedpipes.etl.test.suite.TestConfigurationDescription;
import com.linkedpipes.plugin.loader.graphstoreprotocolGraphDB.GraphStoreProtocolGraphDBConfiguration;

import org.junit.Test;

public class ValidationTest {

    @Test
    public void verifyConfigurationDescription() throws Exception {
        final TestConfigurationDescription test =
                new TestConfigurationDescription();
        test.test(GraphStoreProtocolGraphDBConfiguration.class);
    }

}
