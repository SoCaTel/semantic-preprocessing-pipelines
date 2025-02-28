package com.linkedpipes.plugin.loader.scp;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;

public final class LoaderScp implements Component, SequentialExecution {

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.OutputPort(iri = "FilesInput")
    public FilesDataUnit input;

    @Component.Configuration
    public LoaderScpConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        try (ScpClient scpClient =createClient()) {
            scpClient.connect(
                    configuration.getHost(),
                    configuration.getPort(),
                    configuration.getUserName(),
                    configuration.getPassword());
            String targetDirectory = configuration.getTargetDirectory();
            if (configuration.isCreateDirectory()) {
                scpClient.createDirectory(targetDirectory);
            }
            if (configuration.isClearDirectory()) {
                scpClient.clearDirectory(targetDirectory);
            }
            scpClient.uploadDirectories(targetDirectory,
                    input.getReadDirectories());
        } catch (Exception ex) {
            throw exceptionFactory.failure("SCP operation failed.", ex);
        }
    }

    private ScpClient createClient() {
        return new ScpClient(configuration.getConnectionTimeOut());
    }

    private void checkConfiguration() throws LpException {
        if (isNullOrEmpty(configuration.getUserName())) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_USERNAME);
        }
        if (isNullOrEmpty(configuration.getPassword())) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_PASSWORD);
        }
        if (isNullOrEmpty(configuration.getHost())) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_HOST);
        }
        if (configuration.getPort() == null) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_PORT);
        }
        if (isNullOrEmpty(configuration.getTargetDirectory())) {
            throw exceptionFactory.failure("Missing property: {}",
                    LoaderScpVocabulary.HAS_TARGET_DIRECTORY);
        }
    }

    private boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
