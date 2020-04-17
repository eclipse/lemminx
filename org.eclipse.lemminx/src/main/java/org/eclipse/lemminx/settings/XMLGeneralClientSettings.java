/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.settings;

import org.eclipse.lemminx.utils.JSONUtility;

/**
 * Class to hold all settings from the client side.
 * 
 * See https://github.com/eclipse/lemming/wiki/Configuration for more
 * information.
 * 
 * This class is created through the deseralization of a JSON object. Each
 * internal setting must be represented by a class and have:
 * 
 * 1) A constructor with no parameters
 * 
 * 2) The JSON key/parent for the settings must have the same name as a varible.
 * 
 * eg: {"format" : {...}, "completion" : {...}}
 * 
 * In this class must exist both a "format" and "completion" variable with the
 * appropriate Class to represent the value of each key
 *
 */
public class XMLGeneralClientSettings {

	private LogsSettings logs;

	private XMLFormattingOptions format;

	private XMLCompletionSettings completion;

	private ServerSettings server;

	private XMLSymbolSettings symbols;

	private XMLCodeLensSettings codeLens;

	private int maxItemsComputed; 

	public void setLogs(LogsSettings logs) {
		this.logs = logs;
	}

	public LogsSettings getLogs() {
		return logs;
	}

	public XMLSymbolSettings getSymbols() {
		return symbols;
	}

	public void setSymbols(XMLSymbolSettings symbols) {
		this.symbols = symbols;
	}

	/**
	 * Returns the code lens settings.
	 * 
	 * @return the code lens settings.
	 */
	public XMLCodeLensSettings getCodeLens() {
		return codeLens;
	}

	/**
	 * Set the code lens settings.
	 * 
	 * @param codeLens
	 */
	public void setCodeLens(XMLCodeLensSettings codeLens) {
		this.codeLens = codeLens;
	}

	public void setFormat(XMLFormattingOptions format) {
		this.format = format;
	}

	public XMLFormattingOptions getFormat() {
		return format;
	}

	/**
	 * Set completion settings
	 * 
	 * @param completion
	 */
	public void setCompletion(XMLCompletionSettings completion) {
		this.completion = completion;
	}

	/**
	 * Get completion settings
	 * 
	 * @param completion
	 */
	public XMLCompletionSettings getCompletion() {
		return completion;
	}

	/**
	 * @return the server
	 */
	public ServerSettings getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(ServerSettings server) {
		this.server = server;
	}

	public static XMLGeneralClientSettings getGeneralXMLSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, XMLGeneralClientSettings.class);
	}

	public int getMaxItemsComputed() {
		return maxItemsComputed;
	}

	public void setMaxItemsComputed(int maxItemsComputed) {
		this.maxItemsComputed = maxItemsComputed;
	}
}