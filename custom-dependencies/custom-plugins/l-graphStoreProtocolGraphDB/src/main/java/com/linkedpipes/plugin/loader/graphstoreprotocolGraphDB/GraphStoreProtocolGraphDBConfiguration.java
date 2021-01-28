package com.linkedpipes.plugin.loader.graphstoreprotocolGraphDB;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = GraphStoreProtocolGraphDBVocabulary.CONFIG)
public class GraphStoreProtocolGraphDBConfiguration {

    public static enum RepositoryType {
        GRAPHDB
    }

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_GRAPH)
    private String targetGraph;

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_TYPE)
    private RepositoryType repository = RepositoryType.GRAPHDB;

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_AUTH)
    private boolean useAuthentification = false;

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_USER)
    private String userName = "";

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_PASSWORD)
    private String password = "";

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_CHECK_SIZE)
    private boolean checkSize = false;

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_SELECT)
    private String endpointSelect;

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_CRUD)
    private String endpoint;

    @RdfToPojo.Property(iri = GraphStoreProtocolGraphDBVocabulary.HAS_REPLACE)
    private boolean replace = false;

    public GraphStoreProtocolGraphDBConfiguration() {
    }

    public String getTargetGraph() {
        return targetGraph;
    }

    public void setTargetGraph(String targetGraph) {
        this.targetGraph = targetGraph;
    }

    public RepositoryType getRepository() {
        return repository;
    }

    public void setRepository(RepositoryType repository) {
        this.repository = repository;
    }

    public boolean isUseAuthentification() {
        return useAuthentification;
    }

    public void setUseAuthentification(boolean useAuthentification) {
        this.useAuthentification = useAuthentification;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCheckSize() {
        return checkSize;
    }

    public void setCheckSize(boolean checkSize) {
        this.checkSize = checkSize;
    }

    public String getEndpointSelect() {
        return endpointSelect;
    }

    public void setEndpointSelect(String endpointSelect) {
        this.endpointSelect = endpointSelect;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

}
