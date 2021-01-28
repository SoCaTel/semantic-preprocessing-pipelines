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
public class FacebookPageJSONTransformer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(FacebookPageJSONTransformer.class);
    
    // Parses dates: 2019-02-05T08:23:09+0000
    private static final DateFormat INPUT_DATE_FORMAT
    		= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputPage = new JSONObject();
		
		if (json2Transform.get("id") == null) {
			return outputPage;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming Facebook Page with ID: " + json2Transform.get("id"));
		outputPage.put("@id", SoCaTelOntologyURIs.PAGE_INST_URI + "_" + (String) json2Transform.get("id"));
		outputPage.put("@type", SoCaTelOntologyURIs.PAGE_TYPE_URI);
		outputPage.put("id", (String) json2Transform.get("id"));
		
		// Page attributes
		if (json2Transform.get("name") != null) {
			outputPage.put("name", (String) json2Transform.get("name"));
		}
		if (json2Transform.get("about") != null) {
			outputPage.put("about", (String) json2Transform.get("about"));
		}
		if (json2Transform.get("link") != null) {
			outputPage.put("link", (String) json2Transform.get("link"));
		}
		if (json2Transform.get("website") != null) {
			outputPage.put("website", (String) json2Transform.get("website"));
		}
		if (json2Transform.get("fan_count") != null) {
			outputPage.put("fan_count", (Long) json2Transform.get("fan_count"));
		}
		
		// Location
		if (json2Transform.get("location") != null) {
			JSONObject location = (JSONObject) json2Transform.get("location");
			String stringId = "";
			if (location.get("street") != null) {
				stringId += (String)location.get("street"); 
			}
			if (location.get("city") != null) {
				stringId += (String)location.get("city"); 
			}
			if (location.get("state") != null) {
				stringId += (String)location.get("state"); 
			}
			if (location.get("country") != null) {
				stringId += (String)location.get("country"); 
			}
			if (!stringId.equals("")) {
				location.put("@id", SoCaTelOntologyURIs.GN_FEATURE_INST_URI + "_" + getIdFromObj(stringId));
				location.put("@type", SoCaTelOntologyURIs.GN_FEATURE_TYPE_URI);
				outputPage.put("location", location);
			}
		}
		
		// Categories
		JSONArray outputCategories = new JSONArray();
		if (json2Transform.get("category") != null) {
			String categoryStr = (String) json2Transform.get("category");
			outputCategories.add(getSKOSConceptObj(categoryStr));
		}
		if (json2Transform.get("categories") != null) {
			JSONArray categories = (JSONArray) json2Transform.get("category_list");
			for (Object categoryObj : categories) {
				JSONObject category = (JSONObject) categoryObj;
				outputCategories.add(getSKOSConceptObj((String) category.get("name")));
			}
		}
		if (outputCategories.size() > 0) {
			outputPage.put("categories", outputCategories);
		}
		
		// Posts
		if (json2Transform.get("posts") != null) {
			JSONArray posts = (JSONArray) ((JSONObject) json2Transform.get("posts")).get("data");
			outputPage.put("posts", transformPosts(posts));
		}
		
		// Visitor Posts
		if (json2Transform.get("visitor_posts") != null) {
			JSONArray visitorPosts = (JSONArray) ((JSONObject) json2Transform.get("visitor_posts")).get("data");
			outputPage.put("visitor_posts", transformPosts(visitorPosts));
		}
		
		return outputPage;
	}
	
	private JSONArray transformPosts(JSONArray inputPosts) {
		JSONArray outputPosts = new JSONArray();
		for (Object postObj : inputPosts) {
			JSONObject post = (JSONObject) postObj;
			JSONObject outputPost = new JSONObject();
			outputPost.put("@id", SoCaTelOntologyURIs.POST_INST_URI + "_" + (String) post.get("id"));
			outputPost.put("@type", SoCaTelOntologyURIs.POST_TYPE_URI);
			outputPost.put("id", (String) post.get("id"));
			if (post.get("created_time") != null) {
				String outputDate = (String) post.get("created_time");
				try {
					Date dateObj = INPUT_DATE_FORMAT.parse((String) post.get("created_time"));
					outputDate = OUTPUT_DATE_FORMAT.format(dateObj);
				} catch (ParseException e) {
					System.err.println("Error parsing date: " + (String) post.get("created_time"));
					e.printStackTrace();
				}
				outputPost.put("created_time", outputDate);
			}
			String message = "";
			if (post.get("story") != null) {
				message = (String) post.get("story");
			}
			if (post.get("story") != null && post.get("message") != null) {
				message += " - ";
			}
			if (post.get("message") != null) {
				message += (String) post.get("message");
			}
			outputPost.put("message", message);
			outputPosts.add(outputPost);
		}
		
		return outputPosts;
	}

}
