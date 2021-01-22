/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.settings.adapters;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter;

/**
 * XMLValidationSettingsSerializer
 */
public class XMLValidationSettingsTypeAdapter
		implements JsonDeserializer<XMLValidationSettings>, JsonSerializer<XMLValidationSettings> {


	private static final Logger LOGGER = Logger.getLogger(XMLValidationSettingsTypeAdapter.class.getName());

	// Doesn't include this class as a type adapter in this GSON instance order to avoid infinite recursion
	private static final Gson LOCAL_GSON = new GsonBuilder().registerTypeAdapterFactory(new EitherTypeAdapter.Factory()).create();

	@Override
	public XMLValidationSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		try {
			return LOCAL_GSON.fromJson(json, XMLValidationSettings.class);
		} catch (Exception e) {
			LOGGER.warning(
					"The setting 'xml.validation.schema' was recent changed from a boolean to an object with several sub-settings. "
							+ //
							"Please remove the old configuration option, and refer to the documentation on these settings.");
			return new XMLValidationSettings();
		}
	}

	@Override
	public JsonElement serialize(XMLValidationSettings src, Type typeOfSrc, JsonSerializationContext context) {
		return LOCAL_GSON.toJsonTree(src);
	}

}