package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;

import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.utils.SoCaTelOntologyURIs;

@SuppressWarnings("unchecked")
public abstract class AbstractJSONTransformer {

    // Parses dates: 2019-01-01T00:00:00+02:00
    public static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	
	public AbstractJSONTransformer() {
		
	}
	
	public abstract JSONObject transform(JSONObject json2Transform);
    
	protected int getIdFromObj(Object obj) {
		return Math.abs(obj.hashCode());
	}
	
	protected JSONObject getSKOSConceptObj(String concept) {
		String uniformConceptStr = concept.toLowerCase().replace("#", "");
		
		JSONObject skosConceptObj = new JSONObject();
		skosConceptObj.put("@id", SoCaTelOntologyURIs.SKOS_CONCEPT_INST_URI + "_" + getIdFromObj(uniformConceptStr));
		skosConceptObj.put("@type", SoCaTelOntologyURIs.SKOS_CONCEPT_TYPE_URI);
		skosConceptObj.put("label", uniformConceptStr);
		
		return skosConceptObj;
	}
	
	protected JSONObject getGeoLocationObj(String location) {
		JSONObject locationObj = new JSONObject();
		locationObj.put("@id", SoCaTelOntologyURIs.GN_FEATURE_INST_URI + "_" + getIdFromObj(location));
		locationObj.put("@type", SoCaTelOntologyURIs.GN_FEATURE_TYPE_URI);
		locationObj.put("placeName", (String) location);
		
		return locationObj;
	}
}
