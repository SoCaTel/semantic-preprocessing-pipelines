package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.utils.SoCaTelOntologyURIs;

@SuppressWarnings("unchecked")
public class GroupJSONTransformer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(GroupJSONTransformer.class);

	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputGroup = new JSONObject();
		
		if (json2Transform.get("group_id") == null) {
			return outputGroup;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming Group with ID: " + json2Transform.get("group_id"));
		String groupIdStr = String.valueOf((Long) json2Transform.get("group_id"));
		String groupIdentifier = "group_" + groupIdStr;
		outputGroup.put("@id", SoCaTelOntologyURIs.GROUP_INST_URI + "_" + getIdFromObj(groupIdentifier));
		outputGroup.put("@type", SoCaTelOntologyURIs.GROUP_TYPE_URI);
		outputGroup.put("id", groupIdStr);
		
		// Group attributes
		if (json2Transform.get("group_name") != null
				&& !((String) json2Transform.get("group_name")).equals("")) {
			outputGroup.put("name", (String) json2Transform.get("group_name"));
		}
		if (json2Transform.get("group_description") != null
				&& !((String) json2Transform.get("group_description")).equals("")) {
			outputGroup.put("description", (String) json2Transform.get("group_description"));
		}
		
		// Group language
		if (json2Transform.get("language") != null) {
			String languageCode = (String) ((JSONObject) json2Transform.get("language")).get("language_code");
			outputGroup.put("language", languageCode);
		}
		
		// Group themes
		if (json2Transform.get("themes") != null) {
			JSONArray themesArray = (JSONArray) json2Transform.get("themes");
			JSONArray outputThemes = new JSONArray();
			for (Object themeObj : themesArray) {
				JSONObject themeJson = (JSONObject) themeObj;
				outputThemes.add(getSKOSConceptObj((String) themeJson.get("theme_name")));
			}
			outputGroup.put("themes", outputThemes);
		}
		
		// Group location
		if (json2Transform.get("locality") != null) {
			outputGroup.put("location", getGeoLocationObj(
					(String) ((JSONObject) json2Transform.get("locality")).get("locality_name")));
		}

		// Group users
		if (json2Transform.get("user_initiator_id") != null
				&& !((String) json2Transform.get("user_initiator_id")).equals("")) {
			String userIdStr = (String) json2Transform.get("user_initiator_id");
			JSONObject outputInitiatorUser = new JSONObject();
			outputInitiatorUser.put("@id", SoCaTelOntologyURIs.USER_INST_URI + "_" + userIdStr);
			outputInitiatorUser.put("@type", SoCaTelOntologyURIs.USER_TYPE_URI);
			outputInitiatorUser.put("id", userIdStr);
			outputGroup.put("initiatedBy", outputInitiatorUser);
			
		}

		if (json2Transform.get("users") != null
				&& ((JSONArray) json2Transform.get("users")).size() > 0) {
			JSONArray groupUsers = (JSONArray) json2Transform.get("users");
			JSONArray outputGroupUsers = new JSONArray();
			for (Object userGroupObj : groupUsers) {
				String userIdStr = (String) userGroupObj;
				JSONObject outputGroupUser = new JSONObject();
				outputGroupUser.put("@id", SoCaTelOntologyURIs.USER_INST_URI + "_" + userIdStr);
				outputGroupUser.put("@type", SoCaTelOntologyURIs.USER_TYPE_URI);
				outputGroupUser.put("id", userIdStr);
				outputGroupUsers.add(outputGroupUser);
			}
			outputGroup.put("hasMember", outputGroupUsers);
		}
		
		return outputGroup;
    }

}
