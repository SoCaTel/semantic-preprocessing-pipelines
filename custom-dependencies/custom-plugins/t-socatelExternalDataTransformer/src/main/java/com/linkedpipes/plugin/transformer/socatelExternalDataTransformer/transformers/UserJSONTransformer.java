package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.utils.SoCaTelOntologyURIs;

@SuppressWarnings("unchecked")
public class UserJSONTransformer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(UserJSONTransformer.class);

	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputUser = new JSONObject();
		
		if (json2Transform.get("user_id") == null) {
			return outputUser;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming User with ID: " + json2Transform.get("user_id"));
		String userIdStr = (String) json2Transform.get("user_id");
		outputUser.put("@id", SoCaTelOntologyURIs.USER_INST_URI + "_" + userIdStr);
		outputUser.put("@type", SoCaTelOntologyURIs.USER_TYPE_URI);
		outputUser.put("id", userIdStr);
		
		// User primary and secondary languages (if they exist)
		if (json2Transform.get("primary_language") != null) {
			String languageCode = (String) ((JSONObject) json2Transform.get("primary_language")).get("language_code");
			outputUser.put("primary_language", languageCode);
		}
		if (json2Transform.get("secondary_language") != null) {
			String languageCode = (String) ((JSONObject) json2Transform.get("secondary_language")).get("language_code");
			outputUser.put("secondary_language", languageCode);
		}
		
		// User themes (currenty not present)
		if (json2Transform.get("themes") != null) {
			JSONArray themesArray = (JSONArray) json2Transform.get("themes");
			JSONArray outputThemes = new JSONArray();
			for (Object themeObj : themesArray) {
				JSONObject themeJson = (JSONObject) themeObj;
				outputThemes.add(getSKOSConceptObj((String) themeJson.get("theme_name")));
			}
			outputUser.put("themes", outputThemes);
		}
		
		// User skills (currenty not present)
		if (json2Transform.get("skills") != null) {
			JSONArray skillsArray = (JSONArray) json2Transform.get("skills");
			JSONArray outputSkills = new JSONArray();
			for (Object skillObj : skillsArray) {
				JSONObject skillJson = (JSONObject) skillObj;
				outputSkills.add(getSKOSConceptObj((String) skillJson.get("skill_name")));
			}
			outputUser.put("skills", outputSkills);
		}
		
		// User location
		if (json2Transform.get("locality") != null) {
			outputUser.put("location", getGeoLocationObj(
					(String) ((JSONObject) json2Transform.get("locality")).get("locality_name")));
		}
		
		// User organisation (if it exists)
		if (json2Transform.get("organisation_id") != null) {
			String orgIdStr = String.valueOf((Long) json2Transform.get("organisation_id"));
			JSONObject userOrganisation = new JSONObject();
			userOrganisation.put("@id", SoCaTelOntologyURIs.ORGANISATION_INST_URI + "_" + getIdFromObj("organisation_" + orgIdStr));
			userOrganisation.put("@type", SoCaTelOntologyURIs.ORGANISATION_TYPE_URI);
			userOrganisation.put("id", orgIdStr);
			outputUser.put("belongsTo", userOrganisation);
		} 

		// User groups (if they exist)
		if (json2Transform.get("groups") != null
				&& ((JSONArray) json2Transform.get("groups")).size() > 0) {
			JSONArray userGroups = (JSONArray) json2Transform.get("groups");
			JSONArray outputUserGroups = new JSONArray();
			for (Object userGroupObj : userGroups) {
				JSONObject userGroup = (JSONObject) userGroupObj;
				String groupIdStr = String.valueOf((Long) userGroup.get("group_id"));
				JSONObject outputUserGroup = new JSONObject();
				outputUserGroup.put("@id", SoCaTelOntologyURIs.GROUP_INST_URI + "_" + getIdFromObj("group_" + groupIdStr));
				outputUserGroup.put("@type", SoCaTelOntologyURIs.GROUP_TYPE_URI);
				outputUserGroup.put("id", groupIdStr);
				outputUserGroups.add(outputUserGroup);
			}
			outputUser.put("isMemberOf", outputUserGroups);
		}
		
		return outputUser;
    }

}
