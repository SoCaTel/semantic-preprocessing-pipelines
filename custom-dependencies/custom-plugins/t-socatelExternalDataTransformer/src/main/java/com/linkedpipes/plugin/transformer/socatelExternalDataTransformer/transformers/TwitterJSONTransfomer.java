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
public class TwitterJSONTransfomer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(TwitterJSONTransfomer.class);
    
    // Parses dates: Sat Mar 02 20:16:05 +0000 2019
    private static final DateFormat INPUT_DATE_FORMAT
    		= new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputTweet = new JSONObject();
		
		if (json2Transform.get("id_str") == null) {
			return outputTweet;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming Tweet with ID: " + json2Transform.get("id_str"));
		outputTweet.put("@id", SoCaTelOntologyURIs.POST_INST_URI + "_" + (String) json2Transform.get("id_str"));
		outputTweet.put("@type", SoCaTelOntologyURIs.POST_TYPE_URI);
		outputTweet.put("id_str", (String) json2Transform.get("id_str"));
		
		// Tweet attributes
		if (json2Transform.get("text") != null) {
			outputTweet.put("full_text", (String) json2Transform.get("text"));
		}
		if (json2Transform.get("lang") != null) {
			outputTweet.put("lang", (String) json2Transform.get("lang"));
		}
		if (json2Transform.get("created_at") != null) {
			String outputDate = (String) json2Transform.get("created_at");
			try {
				Date dateObj = INPUT_DATE_FORMAT.parse((String) json2Transform.get("created_at"));
				outputDate = OUTPUT_DATE_FORMAT.format(dateObj);
			} catch (ParseException e) {
				LOG.error("Error parsing date: " + (String) json2Transform.get("created_at"), e);
			}
			outputTweet.put("created_at", outputDate);
		}
		if (json2Transform.get("retweet_count") != null) {
			outputTweet.put("retweet_count", (Long) json2Transform.get("retweet_count"));
		}
		if (json2Transform.get("favorite_count") != null) {
			outputTweet.put("favorite_count", (Long) json2Transform.get("favorite_count"));
		}
		
		// Location
		if (json2Transform.get("place") != null) {
			JSONObject placeObj = (JSONObject) json2Transform.get("place");
			if (placeObj.get("id") != null) {
				JSONObject locationObj = new JSONObject();
				locationObj.put("@id", SoCaTelOntologyURIs.GN_FEATURE_INST_URI + "_" + (String) placeObj.get("id"));
				locationObj.put("@type", SoCaTelOntologyURIs.GN_FEATURE_TYPE_URI);

				if (placeObj.get("name") != null) {
					locationObj.put("placeName", (String) placeObj.get("name"));
				}
				if (placeObj.get("full_name") != null) {
					locationObj.put("alternateName", (String) placeObj.get("full_name"));
				}
				if (placeObj.get("country_code") != null) {
					locationObj.put("countryCode", (String) placeObj.get("country_code"));
				}
				if (placeObj.get("place_type") != null) {
					locationObj.put("placeType", (String) placeObj.get("place_type"));
				}
				if (json2Transform.get("coordinates") != null) {
					JSONArray coords = (JSONArray) ((JSONObject)json2Transform.get("coordinates")).get("coordinates");
					locationObj.put("long", coords.get(0));
					locationObj.put("lat", coords.get(1));
				}
				outputTweet.put("location", locationObj);
			}
		}
		
		JSONObject entitiesObject = (JSONObject) json2Transform.get("entities");
		// URLs
		JSONArray outputUrls = new JSONArray();
		if (entitiesObject.get("urls") != null) {
			JSONArray entitiesUrls = (JSONArray) entitiesObject.get("urls");
			for (Object url : entitiesUrls) {
				outputUrls.add((String) ((JSONObject) url).get("url"));
			}
		}
		if (entitiesObject.get("media") != null) {
			JSONArray mediaUrls = (JSONArray) entitiesObject.get("media");
			for (Object url : mediaUrls) {
				outputUrls.add((String) ((JSONObject) url).get("url"));
			}
		}
		if (outputUrls.size() > 0) {
			outputTweet.put("url", outputUrls);
		}
		
		// Hashtags
		if (entitiesObject.get("hashtags") != null) {
			JSONArray outputHashtags = new JSONArray();
			JSONArray hashtags = (JSONArray) entitiesObject.get("hashtags");
			for (Object hashtagObj : hashtags) {
				JSONObject hashtag = (JSONObject) hashtagObj;
				outputHashtags.add(getSKOSConceptObj((String) hashtag.get("text")));
			}
			outputTweet.put("hashtags", outputHashtags);
		}
		
		// User mentions
		JSONArray outputUserMentions = new JSONArray();
		if (entitiesObject.get("user_mentions") != null) {
			JSONArray userMentions = (JSONArray) entitiesObject.get("user_mentions");
			for (Object userMentionObj : userMentions) {
				JSONObject outputUserMention = new JSONObject();
				JSONObject userMention = (JSONObject) userMentionObj;
				outputUserMention.put("@id", SoCaTelOntologyURIs.ACCOUNT_INST_URI + "_" + (String) userMention.get("id_str"));
				outputUserMention.put("@type", SoCaTelOntologyURIs.ACCOUNT_TYPE_URI);
				outputUserMention.put("id_str", (String) userMention.get("id_str"));
				outputUserMention.put("screen_name", (String) userMention.get("screen_name"));

				// Created by object for user mentions				
				JSONObject createdByObj = new JSONObject();
				createdByObj.put("@id", SoCaTelOntologyURIs.FOAF_AGENT_INST_URI + "_" + (String) userMention.get("id_str"));
				createdByObj.put("@type", SoCaTelOntologyURIs.FOAF_AGENT_TYPE_URI);
				createdByObj.put("name", (String) userMention.get("name"));
				createdByObj.put("username", (String) userMention.get("screen_name"));
				outputUserMention.put("createdBy", createdByObj);

				outputUserMentions.add(outputUserMention);
			}
		}
		outputTweet.put("user_mentions", outputUserMentions);
		
		// User
		if (json2Transform.get("user") != null) {
			JSONObject outputUser = new JSONObject();
			JSONObject user = (JSONObject) json2Transform.get("user");
			outputUser.put("@id", SoCaTelOntologyURIs.ACCOUNT_INST_URI + "_" + (String) user.get("id_str"));
			outputUser.put("@type", SoCaTelOntologyURIs.ACCOUNT_TYPE_URI);
			if (user.get("id_str") != null) {
				outputUser.put("id_str", (String) user.get("id_str"));
			}
			if (user.get("screen_name") != null) {
				outputUser.put("screen_name", (String) user.get("screen_name"));
			}
			if (user.get("description") != null) {
				outputUser.put("description", (String) user.get("description"));
			}
			if (user.get("created_at") != null) {
				String outputDate = (String) user.get("created_at");
				try {
					Date dateObj = INPUT_DATE_FORMAT.parse((String) user.get("created_at"));
					outputDate = OUTPUT_DATE_FORMAT.format(dateObj);
				} catch (ParseException e) {
					LOG.error("Error parsing date: " + (String) user.get("created_at"), e);
				}
				outputUser.put("created_at", outputDate);
			}
			if (user.get("lang") != null) {
				outputUser.put("lang", (String) user.get("lang"));
			}
			if (user.get("favourites_count") != null) {
				outputUser.put("favourites_count", (Long) user.get("favourites_count"));
			}
			
			// User URLs
			JSONArray outputUserUrls = null;
			if (user.get("entities") != null
					&& ((JSONObject) user.get("entities")).get("url") != null
					&& ((JSONObject)((JSONObject) user.get("entities")).get("url")).get("urls") != null) {
				JSONArray userURLs = (JSONArray) ((JSONObject)((JSONObject) user.get("entities")).get("url")).get("urls");
				outputUserUrls = new JSONArray();
				for (Object url : userURLs) {
					outputUserUrls.add((String) ((JSONObject) url).get("url"));
				}
			}
			outputUser.put("url", outputUserUrls);
			if (user.get("profile_image_url_https") != null) {
				outputUser.put("image_url", (String) user.get("profile_image_url_https"));
			}
			
			// User location
			if (user.get("location") != null) {
				JSONObject userLocationObj = new JSONObject();
				userLocationObj.put("@id", SoCaTelOntologyURIs.GN_FEATURE_INST_URI + "_" + getIdFromObj(user.get("location")));
				userLocationObj.put("@type", SoCaTelOntologyURIs.GN_FEATURE_TYPE_URI);
				userLocationObj.put("placeName", (String) user.get("location"));
				outputUser.put("location", userLocationObj);
			}
			
			// Separate name of user into created by
			JSONObject createdByObj = new JSONObject();
			createdByObj.put("@id", SoCaTelOntologyURIs.FOAF_AGENT_INST_URI + "_" + (String) user.get("id_str"));
			createdByObj.put("@type", SoCaTelOntologyURIs.FOAF_AGENT_TYPE_URI);
			if (user.get("name") != null) {
				createdByObj.put("name", (String) user.get("name"));
			}
			if (user.get("screen_name") != null) {
				createdByObj.put("username", (String) user.get("screen_name"));
			}
			if (user.get("profile_image_url_https") != null) {
				createdByObj.put("image_url", (String) user.get("profile_image_url_https"));
			}
			createdByObj.put("url", outputUserUrls);
			outputUser.put("createdBy", createdByObj);
			
			outputTweet.put("user", outputUser);
			outputTweet.put("createdBy", createdByObj);
		}
		
		// Retweeted status
		if (json2Transform.get("retweeted_status") != null) {
			JSONObject outputRetweetedStatus = transform((JSONObject) json2Transform.get("retweeted_status"));
			outputTweet.put("retweeted_status", outputRetweetedStatus);			
		}
		
		return outputTweet;
	}
	
}