/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Red Hat <nkomonen@redhat.com> - initial API and implementation
 */

package org.eclipse.lsp4xml.settings;

import com.google.gson.annotations.JsonAdapter;

import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;
import org.eclipse.lsp4xml.utils.JSONUtility;

/**
 * Represents all settings under the 'xml' key
 * 
 * {
 *  'xml': {...} 
 * } 
 */
public class AllXMLSettings {

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object xml;

	/**
	 * @return the xml
	 */
	public Object getXml() {
		return xml;
	}

	/**
	 * @param xml the xml to set
	 */
	public void setXml(Object xml) {
		
		this.xml = xml;
	}

	public static Object getAllXMLSettings(Object initializationOptionsSettings) {
		AllXMLSettings settings = JSONUtility.toModel(initializationOptionsSettings, AllXMLSettings.class);
		return settings != null ? settings.getXml() : null;
	}
}