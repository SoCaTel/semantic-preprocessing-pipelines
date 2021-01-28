package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.utils.SoCaTelOntologyURIs;

@SuppressWarnings("unchecked")
public class OpenDataJSONTransformer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(OpenDataJSONTransformer.class);
    
    private static final DateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputCkan = new JSONObject();
		
		if (json2Transform.get("id") == null) {
			return outputCkan;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming CKAN with ID: " + json2Transform.get("id"));
		outputCkan.put("@id", SoCaTelOntologyURIs.OPENDATA_INST_URI + "_" + (String) json2Transform.get("id"));
		outputCkan.put("@type", SoCaTelOntologyURIs.OPENDATA_TYPE_URI);
		outputCkan.put("id", (String) json2Transform.get("id"));
		
		// CKAN attributes
		if (json2Transform.get("title") != null) {
			outputCkan.put("title", (String) json2Transform.get("title"));
		}
		if (json2Transform.get("notes") != null) {
			outputCkan.put("notes", (String) json2Transform.get("notes"));
		}
		if (json2Transform.get("issued") != null) {
			String outputDate = (String) json2Transform.get("issued");
			try {
				Date dateObj = INPUT_DATE_FORMAT.parse((String) json2Transform.get("issued"));
				outputDate = OUTPUT_DATE_FORMAT.format(dateObj);
			} catch (ParseException e) {
				LOG.error("Error parsing date: " + (String) json2Transform.get("issued"), e);
			}
			outputCkan.put("issued", outputDate);
		}
		if (json2Transform.get("language") != null) {
			outputCkan.put("language", (String) json2Transform.get("language"));
		}
		if (json2Transform.get("url") != null) {
			outputCkan.put("url", (String) json2Transform.get("url"));
		}
		
		// Location
		if (json2Transform.get("spatial_other") != null) {
			outputCkan.put("location", getGeoLocationObj((String) json2Transform.get("spatial_other")));
		}

		// Theme and tags
		if (json2Transform.get("theme") != null) {
			String themeStr = (String) json2Transform.get("theme");
			outputCkan.put("theme", getSKOSConceptObj(themeStr));
		}
		
		if (json2Transform.get("tags") != null) {
			JSONArray outputTags = new JSONArray();
			JSONArray tags = (JSONArray) json2Transform.get("tags");
			for (Object tagObj : tags) {
				JSONObject tag = (JSONObject) tagObj;
				outputTags.add(getSKOSConceptObj((String) tag.get("display_name")));
			}
			outputCkan.put("tags", outputTags);
		}
		
		// Organization or author as owner (precedence given to "organization" item)
		if (json2Transform.get("organization") != null) {
			JSONObject outputOrg = new JSONObject();
			JSONObject org = (JSONObject) json2Transform.get("organization");
			if (org.get("id") != null) {
				outputOrg.put("@id", SoCaTelOntologyURIs.FOAF_AGENT_INST_URI + "_" + getIdFromObj(org.get("id")));
				outputOrg.put("@type", SoCaTelOntologyURIs.FOAF_AGENT_TYPE_URI);
				if (org.get("title") != null) {
					outputOrg.put("name", (String) org.get("title"));
				}
				outputCkan.put("organization", outputOrg);
			}
		}
		
		if (json2Transform.get("author") != null && 
				!((String)json2Transform.get("author")).equals("")) {
			JSONObject outputAuthor = new JSONObject();
			String authorName = (String) json2Transform.get("author");
			outputAuthor.put("@id", SoCaTelOntologyURIs.FOAF_AGENT_INST_URI + "_" + getIdFromObj(authorName));
			outputAuthor.put("@type", SoCaTelOntologyURIs.FOAF_AGENT_TYPE_URI);
			outputAuthor.put("name", authorName);
			if (json2Transform.get("author_email") != null) {
				outputAuthor.put("email", (String) json2Transform.get("author_email"));
			}
			outputCkan.put("author", outputAuthor);
		}
		
		return outputCkan;
	}
	
}