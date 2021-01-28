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
public class FacebookPostJSONTransformer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(FacebookPostJSONTransformer.class);
    
    // Parses dates: 2019-02-05T08:23:09+0000
    private static final DateFormat INPUT_DATE_FORMAT
    		= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputPost = new JSONObject();
		
		if (json2Transform.get("id") == null) {
			return outputPost;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming Facebook Post with ID: " + json2Transform.get("id"));
		outputPost.put("@id", SoCaTelOntologyURIs.POST_INST_URI + "_" + (String) json2Transform.get("id"));
		outputPost.put("@type", SoCaTelOntologyURIs.POST_TYPE_URI);
		outputPost.put("id", (String) json2Transform.get("id"));
		
		// Post attributes
		if (json2Transform.get("message") != null) {
			outputPost.put("message", (String) json2Transform.get("message"));
		}
		if (json2Transform.get("permalink_url") != null) {
			outputPost.put("permalink_url", (String) json2Transform.get("permalink_url"));
		}
		if (json2Transform.get("created_time") != null) {
			String outputDate = (String) json2Transform.get("created_time");
			try {
				Date dateObj = INPUT_DATE_FORMAT.parse((String) json2Transform.get("created_time"));
				outputDate = OUTPUT_DATE_FORMAT.format(dateObj);
			} catch (ParseException e) {
				System.err.println("Error parsing date: " + (String) json2Transform.get("created_time"));
				e.printStackTrace();
			}
			outputPost.put("created_time", outputDate);
		}
		if (json2Transform.get("coordinates") != null) {
			outputPost.put("coordinates", (JSONObject) json2Transform.get("coordinates"));
		}
		
		// From user
		if (json2Transform.get("from") != null) {
			outputPost.put("from", transformFromUser((JSONObject) json2Transform.get("from")));
		}
		
		// Comments
		JSONArray outputComments = new JSONArray();
		if (json2Transform.get("comments") != null) {
			JSONArray comments = (JSONArray) ((JSONObject) json2Transform.get("comments")).get("data");
			for (Object commentObj : comments) {
				JSONObject comment = (JSONObject) commentObj;
				JSONObject outputComment = transform(comment);
				outputComments.add(outputComment);
			}
			outputPost.put("comments", outputComments);
		}
		if (json2Transform.get("comment_count") != null) {
			outputPost.put("comment_count", (Long) json2Transform.get("comment_count"));
		} else {
			outputPost.put("comment_count", outputComments.size());
		}
		
		// Reactions
		JSONArray outputReactions = new JSONArray();
		if (json2Transform.get("reactions") != null ) {
			JSONArray reactions = (JSONArray) ((JSONObject) json2Transform.get("reactions")).get("data");
			for (Object reactionObj : reactions) {
				JSONObject reaction = (JSONObject) reactionObj;
				JSONObject outputReaction = transformFromUser(reaction);
				outputReactions.add(outputReaction);
			}
			outputPost.put("reactions", outputReactions);
		}
		if (json2Transform.get("like_count") != null) {
			outputPost.put("like_count", (Long) json2Transform.get("like_count"));
		} else {
			outputPost.put("like_count", outputReactions.size());
		}
		
		return outputPost;
	}
	
	private JSONObject transformFromUser(JSONObject userObj) {
		JSONObject outputUser = new JSONObject();
		outputUser.put("@id", SoCaTelOntologyURIs.FOAF_AGENT_INST_URI + "_" + (String) userObj.get("id"));
		outputUser.put("@type", SoCaTelOntologyURIs.FOAF_AGENT_TYPE_URI);
		outputUser.put("id", (String) userObj.get("id"));
		if (userObj.get("name") != null) {
			outputUser.put("name", (String) userObj.get("name"));
		}
		return outputUser;
	}

}
