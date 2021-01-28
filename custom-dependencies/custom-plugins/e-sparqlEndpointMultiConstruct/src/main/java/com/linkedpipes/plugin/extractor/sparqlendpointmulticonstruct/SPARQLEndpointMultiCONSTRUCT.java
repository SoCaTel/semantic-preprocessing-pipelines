package com.linkedpipes.plugin.extractor.sparqlendpointmulticonstruct;

import java.net.IDN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.WritableSingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;

public final class SPARQLEndpointMultiCONSTRUCT implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(SPARQLEndpointMultiCONSTRUCT.class);

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFilesDataUnit;

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "OutputRdf")
    public WritableSingleGraphDataUnit outputRdf;

    @Component.Configuration
    public SPARQLEndpointMultiCONSTRUCTConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;
    
    private List<String> mappedVariables;
    
    private List<List<String>> mappedVariableValues;
    
    private SPARQLRepository repository;

    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public void execute() throws LpException {
        checkConfiguration();
        parseInputFiles();
        executeQuery();
    }

    private void checkConfiguration() throws LpException {
        if (configuration.getEndpoint() == null
                || configuration.getEndpoint().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
            		SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_ENDPOINT);
        }
        if (configuration.getQuery() == null
                || configuration.getQuery().isEmpty()) {
            throw exceptionFactory.failure("Missing property: {}",
                    SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_QUERY);
        }
    }
    
    private void parseInputFiles() throws LpException {
    	mappedVariables = new ArrayList<String>();
    	mappedVariableValues = new ArrayList<List<String>>();
    	
    	final CSVParser parser = new CSVParser(configuration, exceptionFactory);
    	
    	for (FilesDataUnit.Entry entry : inputFilesDataUnit) {
            LOG.info("Processing file: {}", entry.toFile());
            parser.parse(entry, mappedVariables, mappedVariableValues);
    	}
    }
    
    private void executeQuery() throws LpException {
        repository = createRepository();
        //
        try {
            repository.initialize();
        } catch (RepositoryException ex) {
            throw exceptionFactory.failure("Can't connnect to endpoint.", ex);
        }
        repository.setHttpClient(getHttpClient());
        //
    	for (List<String> varValues : mappedVariableValues) {
    		LOG.info("Replacing mapped variables {} with values {}",
    				mappedVariables.toString(),
    				varValues.toString());
    		String query = configuration.getQuery();
    		for (int i = 0; i < mappedVariables.size(); i++) {
    			query = query.replace("?" + mappedVariables.get(i), varValues.get(i));
    		}
    		try {
    			queryRemoteRepository(query);
    		} catch (Throwable t) {
                LOG.error("Error executing SPARQL query. "
                		+ "Skipping mapped variable values {}", varValues, t);
            }
    	}
    	//
        try {
            repository.shutDown();
        } catch (RepositoryException ex) {
            LOG.error("Can't close repository.", ex);
        }
    }

    private SPARQLRepository createRepository() {
        TolerantSparqlRepository repository =
                new TolerantSparqlRepository(getEndpoint());
        if (configuration.isUseTolerantRepository()) {
            repository.fixMissingLanguageTag();
        }
        if (configuration.isHandleInvalid()) {
            repository.ignoreInvalidData();
        }
        // Set headers.
        Map<String, String> headers = new HashMap<>();
        headers.putAll(repository.getAdditionalHttpHeaders());
        if (configuration.getTransferMimeType() != null) {
            headers.put("Accept", configuration.getTransferMimeType());
        }
        repository.setAdditionalHttpHeaders(headers);

        return repository;
    }

    private String getEndpoint() {
        String[] tokens = configuration.getEndpoint().split("://", 2);
        return tokens[0] + "://" + IDN.toASCII(tokens[1]);
    }

    private CloseableHttpClient getHttpClient() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        if (configuration.isUseAuthentication()) {
            provider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(
                            configuration.getUsername(),
                            configuration.getPassword()));
        }
        return HttpClients.custom()
                .setDefaultCredentialsProvider(provider).build();
    }

    public void queryRemoteRepository(String query) throws LpException {
        final IRI graph = outputRdf.getWriteGraph();
        try (RepositoryConnection outputConnection
                     = outputRdf.getRepository().getConnection()) {
            outputConnection.begin();
            // We can't use Repositories.graphQuery (Repositories.get) here,
            // as Virtuoso fail with
            // 'No permission to execute procedure DB.DBA.SPARUL_RUN'
            // as sesame try to execute given action in a transaction.
    		LOG.debug("Executing remote query: {}", query);
            try (RepositoryConnection remoteConnection
                         = repository.getConnection()) {
                final GraphQuery preparedQuery
                        = remoteConnection.prepareGraphQuery(
                        QueryLanguage.SPARQL,
                        query);
                // Construct dataset.
                final SimpleDataset dataset = new SimpleDataset();
                for (String iri : configuration.getDefaultGraphs()) {
                    dataset.addDefaultGraph(valueFactory.createIRI(iri));
                }
                preparedQuery.setDataset(dataset);
                RDFHandler handler = new AbstractRDFHandler() {
                    @Override
                    public void handleStatement(Statement st)
                            throws RDFHandlerException {
                        outputConnection.add(st, graph);
                    }
                };
                if (configuration.isFixIncomingRdf()) {
                    handler = new RdfEncodeHandler(handler);
                }
                preparedQuery.evaluate(handler);
            }
            outputConnection.commit();
        }
    }
}
