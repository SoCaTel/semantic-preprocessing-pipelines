package com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.transformers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkedpipes.plugin.transformer.socatelExternalDataTransformer.utils.SoCaTelOntologyURIs;

@SuppressWarnings("unchecked")
public class OrganisationJSONTransformer extends AbstractJSONTransformer {

    private static final Logger LOG
            = LoggerFactory.getLogger(ServiceJSONTransformer.class);

	@Override
	public JSONObject transform(JSONObject json2Transform) {
		JSONObject outputOrganisation = new JSONObject();
		
		if (json2Transform.get("organisation_id") == null) {
			return outputOrganisation;
		}
		
		// JSON-LD @id and @type
		LOG.info("Transforming Organisation with ID: " + json2Transform.get("organisation_id"));
		String orgIdStr = String.valueOf((Long) json2Transform.get("organisation_id"));
		outputOrganisation.put("@id", SoCaTelOntologyURIs.ORGANISATION_INST_URI + "_" + getIdFromObj("organisation_" + orgIdStr));
		outputOrganisation.put("@type", SoCaTelOntologyURIs.ORGANISATION_TYPE_URI);
		outputOrganisation.put("id", orgIdStr);
		
		// Organisation attributes
		if (json2Transform.get("organisation_name") != null
				&& !((String) json2Transform.get("organisation_name")).equals("")) {
			outputOrganisation.put("name", (String) json2Transform.get("organisation_name"));
		}
		if (json2Transform.get("organisation_description") != null
				&& !((String) json2Transform.get("organisation_description")).equals("")) {
			outputOrganisation.put("description", (String) json2Transform.get("organisation_description"));
		}
		if (json2Transform.get("organisation_website") != null
				&& !((String) json2Transform.get("organisation_website")).equals("")) {
			outputOrganisation.put("website", (String) json2Transform.get("organisation_website"));
		}
		
		// Organisation language (currently not present)
		if (json2Transform.get("language") != null) {
			String languageCode = (String) ((JSONObject) json2Transform.get("language")).get("language_code");
			outputOrganisation.put("language", languageCode);
		}
		
		// Organisation themes (currenty not present)
		if (json2Transform.get("themes") != null) {
			JSONArray themesArray = (JSONArray) json2Transform.get("themes");
			JSONArray outputThemes = new JSONArray();
			for (Object themeObj : themesArray) {
				JSONObject themeJson = (JSONObject) themeObj;
				outputThemes.add(getSKOSConceptObj((String) themeJson.get("theme_name")));
			}
			outputOrganisation.put("themes", outputThemes);
		}
		
		// Organisation location (currenty not present)
		if (json2Transform.get("locality") != null) {
			outputOrganisation.put("location", getGeoLocationObj(
					(String) ((JSONObject) json2Transform.get("locality")).get("locality_name")));
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
			outputOrganisation.put("accounts", accounts);
		}
		
		return outputOrganisation;
    }

}
