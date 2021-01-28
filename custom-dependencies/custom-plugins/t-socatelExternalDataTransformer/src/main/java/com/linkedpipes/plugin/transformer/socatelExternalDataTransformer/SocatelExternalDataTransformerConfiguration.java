package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = SocatelExternalDataTransformerVocabulary.CONFIGURATION)
public class SocatelExternalDataTransformerConfiguration {

    public static enum DataSourceType {
        TWITTER,
        FACEBOOK_POST,
        FACEBOOK_PAGE,
        OPENDATA_CKAN,
        SOCATEL_SERVICE,
        SOCATEL_ORGANISATION,
        SOCATEL_USER,
        SOCATEL_GROUP
    }
    
    public static enum SingleOrArray {
    	SINGLE,
    	ARRAY
    }

    @RdfToPojo.Property(iri = SocatelExternalDataTransformerVocabulary.HAS_DATA_SOURCE)
    private DataSourceType dataSource;

    @RdfToPojo.Property(iri = SocatelExternalDataTransformerVocabulary.HAS_SINGLE_ARRAY)
    private SingleOrArray singleArray;

    @RdfToPojo.Property(iri = SocatelExternalDataTransformerVocabulary.HAS_SINGLE_ROOT_PATH)
    private String singleRootPath;

    @RdfToPojo.Property(iri = SocatelExternalDataTransformerVocabulary.HAS_ARRAY_ROOT_PATH)
    private String arrayRootPath;

    public SocatelExternalDataTransformerConfiguration() {
    	
    }
    
    public DataSourceType getDataSource() {
		return dataSource;
	}
    
    public void setDataSource(DataSourceType dataSource) {
		this.dataSource = dataSource;
	}
    
    public SingleOrArray getSingleArray() {
		return singleArray;
	}
    
    public void setSingleArray(SingleOrArray singleArray) {
		this.singleArray = singleArray;
	}
    
    public String getSingleRootPath() {
		return singleRootPath;
	}
    
    public void setSingleRootPath(String singleRootPath) {
		this.singleRootPath = singleRootPath;
	}
    
    public String getArrayRootPath() {
		return arrayRootPath;
	}
    
    public void setArrayRootPath(String arrayRootPath) {
		this.arrayRootPath = arrayRootPath;
	}
}
