package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.utils.SoCaTelOntologyURIs;

@SuppressWarnings("unchecked")
public class ServiceJSONTransformer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(ServiceJSONTransformer.class);

	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputService = new JSONObject();
		
		if (json2Transform.get("service_id") == null) {
			return outputService;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming Service with ID: " + json2Transform.get("service_id"));
		String serviceIdStr = String.valueOf((Long) json2Transform.get("service_id"));
		String serviceIdentifier = "service_" + serviceIdStr;
		outputService.put("@id", SoCaTelOntologyURIs.SERVICE_INST_URI + "_" + getIdFromObj(serviceIdentifier));
		outputService.put("@type", SoCaTelOntologyURIs.SERVICE_TYPE_URI);
		outputService.put("id", serviceIdStr);
		
		// Service attributes
		if (json2Transform.get("service_name") != null
				&& !((String) json2Transform.get("service_name")).equals("")) {
			outputService.put("name", (String) json2Transform.get("service_name"));
		}
		if (json2Transform.get("service_description") != null
				&& !((String) json2Transform.get("service_description")).equals("")) {
			outputService.put("description", (String) json2Transform.get("service_description"));
		}
		if (json2Transform.get("service_website") != null
				&& !((String) json2Transform.get("service_website")).equals("")) {
			outputService.put("website", (String) json2Transform.get("service_website"));
		}
		if (json2Transform.get("service_hashtag") != null
				&& !((String) json2Transform.get("service_hashtag")).equals("")) {
			outputService.put("hashtag", getSKOSConceptObj(
					(String) json2Transform.get("service_hashtag")));
		}
		
		// Service language
		if (json2Transform.get("language") != null) {
			String languageCode = (String) ((JSONObject) json2Transform.get("language")).get("language_code");
			outputService.put("language", languageCode);
		}
		
		// Service themes
		if (json2Transform.get("themes") != null) {
			JSONArray themesArray = (JSONArray) json2Transform.get("themes");
			JSONArray outputThemes = new JSONArray();
			for (Object themeObj : themesArray) {
				JSONObject themeJson = (JSONObject) themeObj;
				outputThemes.add(getSKOSConceptObj((String) themeJson.get("theme_name")));
			}
			outputService.put("themes", outputThemes);
		}
		
		// Service location
		if (json2Transform.get("locality") != null) {
			outputService.put("location", getGeoLocationObj(
					(String) ((JSONObject) json2Transform.get("locality")).get("locality_name")));
		}
		
		// Service owner object (either organisation or group)
		JSONObject ownerObj = null;
		if (json2Transform.get("organisation_id") != null) {
			String orgIdStr = String.valueOf((Long) json2Transform.get("organisation_id"));
			ownerObj = new JSONObject();
			ownerObj.put("@id", SoCaTelOntologyURIs.ORGANISATION_INST_URI + "_" + getIdFromObj("organisation_" + orgIdStr));
			ownerObj.put("@type", SoCaTelOntologyURIs.ORGANISATION_TYPE_URI);
			ownerObj.put("id", orgIdStr);
		} 
		else if (json2Transform.get("group_id") != null) {
			String groupIdStr = String.valueOf((Long) json2Transform.get("group_id"));
			ownerObj = new JSONObject();
			ownerObj.put("@id", SoCaTelOntologyURIs.GROUP_INST_URI + "_" + getIdFromObj("group_" + groupIdStr));
			ownerObj.put("@type", SoCaTelOntologyURIs.GROUP_TYPE_URI);
			ownerObj.put("id", groupIdStr);
		}
		
		if (ownerObj != null) {
			outputService.put("implementedBy", ownerObj);
		}
		
		// Social Media Accounts
		JSONArray accounts = new JSONArray();
		if (json2Transform.get("twitter_user_id") != null) {
			JSONObject twitterAccount = new JSONObject();
			String twitterId = String.valueOf((Long) json2Transform.get("twitter_user_id"));
			twitterAccount.put("@id", SoCaTelOntologyURIs.ACCOUNT_INST_URI + "_" + twitterId);
			twitterAccount.put("@type", SoCaTelOntologyURIs.ACCOUNT_TYPE_URI);
			twitterAccount.put("id", twitterId);
			if (json2Transform.get("twitter_screen_name") != null) {
				twitterAccount.put("screen_name", (String) json2Transform.get("twitter_screen_name"));
			}
			if (json2Transform.get("twitter_account_description") != null) {
				twitterAccount.put("description", (String) json2Transform.get("twitter_account_description"));
			}
			accounts.add(twitterAccount);
		}
		if (json2Transform.get("facebook_page_id") != null) {
			JSONObject facebookAccount = new JSONObject();
			String facebookId = String.valueOf((Long) json2Transform.get("facebook_page_id"));
			facebookAccount.put("@id", SoCaTelOntologyURIs.ACCOUNT_INST_URI + "_" + facebookId);
			facebookAccount.put("@type", SoCaTelOntologyURIs.ACCOUNT_TYPE_URI);
			facebookAccount.put("id", facebookId);
			accounts.add(facebookAccount);
		}
		if (accounts.size() > 0) {
			outputService.put("accounts", accounts);
		}
		
		return outputService;
    }
		
}
