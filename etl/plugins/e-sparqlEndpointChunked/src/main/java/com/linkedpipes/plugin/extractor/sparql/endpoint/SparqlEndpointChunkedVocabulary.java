package com.linkedpipes.plugin.extractor.sparql.endpoint;

final class SparqlEndpointChunkedVocabulary {

    private static final String PREFIX =
            "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointChunked#";

    public static final String CONFIG = PREFIX + "Configuration";

    public static final String HAS_QUERY = PREFIX + "query";

    public static final String HAS_ENDPOINT = PREFIX + "endpoint";

    public static final String HAS_DEFAULT_GRAPH = PREFIX + "defaultGraph";

    public static final String HAS_HEADER_ACCEPT = PREFIX + "headerAccept";

    public static final String HAS_CHUNK_SIZE = PREFIX + "chunkSize";

    public static final String HAS_SKIP_ON_ERROR = PREFIX + "skipOnError";

    public static final String HAS_ENCODE_RDF = PREFIX + "encodeRdf";

    public static final String HAS_AUTH = PREFIX + "useAuthentication";

    public static final String HAS_USERNAME = PREFIX + "userName";

    public static final String HAS_PASSWORD = PREFIX + "password";

    public static final String HAS_USE_TOLERANT_REPOSITORY =
            PREFIX + "useTolerantRepository";

    public static final String HAS_AS_LITERALS = PREFIX + "literals";

    public static final String HAS_HANDLE_INVALID =
            PREFIX + "handleInvalidData";

    private SparqlEndpointChunkedVocabulary() {
    }

}
