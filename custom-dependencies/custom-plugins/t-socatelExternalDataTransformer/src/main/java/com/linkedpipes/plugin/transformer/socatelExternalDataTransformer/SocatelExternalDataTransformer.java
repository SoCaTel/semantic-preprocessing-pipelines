package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.etl.dataunit.core.files.FilesDataUnit;
import com.linkedpipes.etl.dataunit.core.files.WritableFilesDataUnit;
import com.linkedpipes.etl.dataunit.core.rdf.SingleGraphDataUnit;
import com.linkedpipes.etl.executor.api.v1.LpException;
import com.linkedpipes.etl.executor.api.v1.component.Component;
import com.linkedpipes.etl.executor.api.v1.component.SequentialExecution;
import com.linkedpipes.etl.executor.api.v1.service.ExceptionFactory;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.AbstractJSONTransformer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.FacebookPageJSONTransformer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.FacebookPostJSONTransformer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.GroupJSONTransformer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.OpenDataJSONTransformer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.OrganisationJSONTransformer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.ServiceJSONTransformer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.TwitterJSONTransfomer;
import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers.UserJSONTransformer;

public class SocatelExternalDataTransformer implements Component, SequentialExecution {

    private static final Logger LOG = LoggerFactory.getLogger(SocatelExternalDataTransformer.class);

    @Component.ContainsConfiguration
    @Component.InputPort(iri = "Configuration")
    public SingleGraphDataUnit configurationRdf;

    @Component.InputPort(iri = "InputFiles")
    public FilesDataUnit inputFiles;

    @Component.InputPort(iri = "OutputFiles")
    public WritableFilesDataUnit outputFiles;

    @Component.Configuration
    public SocatelExternalDataTransformerConfiguration configuration;

    @Component.Inject
    public ExceptionFactory exceptionFactory;

    @Component.Inject
    public ProgressReport progressReport;

    @Override
    public void execute() throws LpException {
        progressReport.start(inputFiles.size());
        for (FilesDataUnit.Entry inputEntry : inputFiles) {
            processEntry(inputEntry);
            progressReport.entryProcessed();
        }
        progressReport.done();
    }

    private void processEntry(FilesDataUnit.Entry entry) throws LpException {
    	// Read input file
    	String inputFileContent;
		try {
			inputFileContent = FileUtils.readFileToString(entry.toFile(), "UTF-8");
		} catch (IOException e) {
            throw exceptionFactory.failure("Can't read input file: {}", entry.getFileName(), e);
		}
    	
		// Parse input file content as JSON
    	JSONParser jsonParser = new JSONParser();
    	Object rootObj = null;
        try {
			rootObj = jsonParser.parse(inputFileContent);
		} catch (ParseException e) {
            throw exceptionFactory.failure("Can't parse input file as JSON: {}", entry.getFileName(), e);
		}
        
        // Transform input JSON
        String transformedJsonString;
        switch (configuration.getSingleArray()) {
        	case ARRAY:
        		transformedJsonString = transformJsonArray(rootObj).toJSONString();
            	break;
        	case SINGLE:
        		transformedJsonString = transformJsonObj(rootObj).toJSONString();
        		break;
            default:
                throw exceptionFactory.failure("Unknown JSON element type! Must be Single or Array");
        }
        
        // Write transformed JSON
        String outputFileName = entry.getFileName() + "_trans.json";
        final File outputFile = outputFiles.createFile(outputFileName);
        try {
			FileUtils.writeStringToFile(outputFile, transformedJsonString, "UTF-8");
		} catch (IOException e) {
            throw exceptionFactory.failure("Can't write output file: {}", entry.getFileName(), e);
		}
    }
    
    @SuppressWarnings("unchecked")
	private JSONArray transformJsonArray(Object rootObj) throws LpException {
    	String pathToArray = configuration.getArrayRootPath();
    	Object arrayObj = getJsonElementByPath(rootObj, pathToArray);
    	if (!(arrayObj instanceof JSONArray)) {
            throw exceptionFactory.failure("Specified array path does not return JSON array: {}", pathToArray);
    	}
    	
    	JSONArray jsonArray = (JSONArray)arrayObj;
    	JSONArray transformedArray = new JSONArray();
    	for (Object arrayElemObj : jsonArray) {
    		JSONObject transformedJsonObj = transformJsonObj(arrayElemObj);
    		if (transformedJsonObj.size() != 0) {
        		transformedArray.add(transformedJsonObj);
    		}
    	}
    	
    	return transformedArray;
    }
    
    private JSONObject transformJsonObj(Object rootObj) throws LpException {
    	String pathToSingle = configuration.getSingleRootPath();
    	Object singleObj = getJsonElementByPath(rootObj, pathToSingle);	
    	if (!(singleObj instanceof JSONObject)) {
            throw exceptionFactory.failure("Specified single path does not return JSON object: {}", pathToSingle);
    	}
    	
    	AbstractJSONTransformer transformer;
    	switch (configuration.getDataSource()) {
            case TWITTER:
            	transformer = new TwitterJSONTransfomer();
                break;
            case FACEBOOK_POST:
            	transformer = new FacebookPostJSONTransformer();
                break;
            case FACEBOOK_PAGE:
            	transformer = new FacebookPageJSONTransformer();
                break;
            case OPENDATA_CKAN:
            	transformer = new OpenDataJSONTransformer();
                break;
            case SOCATEL_SERVICE:
            	transformer = new ServiceJSONTransformer();
                break;
            case SOCATEL_ORGANISATION:
            	transformer = new OrganisationJSONTransformer();
                break;
            case SOCATEL_GROUP:
            	transformer = new GroupJSONTransformer();
                break;
            case SOCATEL_USER:
            	transformer = new UserJSONTransformer();
                break;
            default:
                throw exceptionFactory.failure("Unknown data source type!");
    	}

    	JSONObject transformedObj = new JSONObject();
    	try {
    		transformedObj = transformer.transform((JSONObject) singleObj);
    	} catch (Exception e) {
            LOG.error("Error during transformation process", e);
    	}

    	return transformedObj;    	
    }
    
    private Object getJsonElementByPath(Object rootObj, String jsonPath) throws LpException {
    	if (jsonPath == null || jsonPath.equals("")) {
    		return rootObj;
    	}
    	
    	if (rootObj instanceof JSONArray) {
            throw exceptionFactory.failure("Path to JSON element cannot contain array {}", jsonPath);
    	}
    	
    	String[] pathElems;
    	if (jsonPath.contains(".")) {
    		pathElems = jsonPath.split("\\.");
    	} else {
    		pathElems = new String[1];
    		pathElems[0] = jsonPath;
    	}
    	
    	Object pathObj = rootObj;
    	for (String pathElem : pathElems) {
    		if (pathObj instanceof JSONArray) {
                throw exceptionFactory.failure("Path to JSON element cannot contain array {}", jsonPath);
    		}
    		JSONObject prevJson = (JSONObject) pathObj;
			pathObj = prevJson.get(pathElem);
    	}
    	
    	return pathObj;
    }

}
