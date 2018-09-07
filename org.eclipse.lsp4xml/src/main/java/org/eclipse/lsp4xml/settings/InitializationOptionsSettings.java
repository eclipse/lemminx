package org.eclipse.lsp4xml.settings;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;
import org.eclipse.lsp4xml.utils.JSONUtility;

import com.google.gson.annotations.JsonAdapter;

public class InitializationOptionsSettings {

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object settings;

	public Object getSettings() {
		return settings;
	}

	public void setSettings(Object settings) {
		this.settings = settings;
	}

	/**
	 * Returns the "settings" section of
	 * {@link InitializeParams#getInitializationOptions()}.
	 * 
	 * Here a sample of initializationOptions
	 * 
	 * <pre>
	 * "initializationOptions": {
			"settings": {
			    "catalogs": [
			      "catalog.xml",
			      "catalog2.xml"
			    ],
			    "logs": {
			      "client": true
			    },
			    "format": {
			      "joinCommentLines": false,
			      "formatComments": true
			    }
			}
		}
	 * </pre>
	 * 
	 * @param initializeParams
	 * @return the "settings" section of
	 *         {@link InitializeParams#getInitializationOptions()}.
	 */
	public static Object getSettings(InitializeParams initializeParams) {
		InitializationOptionsSettings root = JSONUtility.toModel(initializeParams.getInitializationOptions(),
				InitializationOptionsSettings.class);
		return root != null ? root.getSettings() : null;
	}
}
