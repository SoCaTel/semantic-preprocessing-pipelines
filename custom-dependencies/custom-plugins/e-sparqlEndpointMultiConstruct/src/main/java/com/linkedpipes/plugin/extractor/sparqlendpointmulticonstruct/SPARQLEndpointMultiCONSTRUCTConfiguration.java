package com.linkedpipes.plugin.extractor.sparqlendpointmulticonstruct;

import java.util.ArrayList;
import java.util.List;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.CONFIG)
public class SPARQLEndpointMultiCONSTRUCTConfiguration {

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_DELIMITER)
    private String delimiter = ",";

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_QUOTE_CHAR)
    private String quoteChar = "\"";

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_ENCODING)
    private String encoding = "UTF-8";

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_QUERY)
    private String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_ENDPOINT)
    private String endpoint;

    /**
     * Default graphs can be specified only via the runtime configuration.
     */
    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_DEFAULT_GRAPH)
    private List<String> defaultGraphs = new ArrayList<>();

    /**
     * Used as a Accept value in header.
     */
    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_HEADER_ACCEPT)
    private String transferMimeType = null;

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_ENCODE_RDF)
    private boolean fixIncomingRdf = false;

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_AUTH)
    private boolean useAuthentication = false;

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_USERNAME)
    private String username;

    @RdfToPojo.Property(iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(
            iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_USE_TOLERANT_REPOSITORY)
    private boolean useTolerantRepository = false;

    @RdfToPojo.Property(
            iri = SPARQLEndpointMultiCONSTRUCTVocabulary.HAS_HANDLE_INVALID)
    private boolean handleInvalid = false;

    public SPARQLEndpointMultiCONSTRUCTConfiguration() {
    
    }

    public String getDelimiter() {
        return delimiter;
    }
    
    public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
    
    public String getQuoteChar() {
		return quoteChar;
	}
    
    public void setQuoteChar(String quoteChar) {
		this.quoteChar = quoteChar;
	}
    
    public String getEncoding() {
		return encoding;
	}
    
    public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getEndpoint() {
		return endpoint;
	}
    
    public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

    public List<String> getDefaultGraphs() {
        return defaultGraphs;
    }

    public void setDefaultGraphs(List<String> defaultGraphs) {
        this.defaultGraphs = defaultGraphs;
    }

    public String getTransferMimeType() {
        return transferMimeType;
    }

    public void setTransferMimeType(String transferMimeType) {
        this.transferMimeType = transferMimeType;
    }

    public boolean isFixIncomingRdf() {
        return fixIncomingRdf;
    }

    public void setFixIncomingRdf(boolean fixIncomingRdf) {
        this.fixIncomingRdf = fixIncomingRdf;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUseTolerantRepository() {
        return useTolerantRepository;
    }

    public void setUseTolerantRepository(boolean useTolerantRepository) {
        this.useTolerantRepository = useTolerantRepository;
    }

    public boolean isHandleInvalid() {
        return handleInvalid;
    }

    public void setHandleInvalid(boolean handleInvalid) {
        this.handleInvalid = handleInvalid;
    }

}
