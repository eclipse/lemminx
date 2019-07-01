/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.settings;

import org.eclipse.lsp4xml.services.extensions.CompletionSettings;
import org.eclipse.lsp4xml.utils.JSONUtility;

/**
 * Class to hold all settings from the client side.
 * 
 * See https://github.com/angelozerr/lsp4xml/wiki/Configuration for more
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

	private CompletionSettings completion;

	private ServerSettings server;

	private XMLSymbolSettings symbols;

	private XMLCodeLensSettings codeLens;

	private XMLTelemetrySettings telemetry;

	private XMLExperimentalSettings experimental;

	public XMLExperimentalSettings getExperimental() {
		return experimental;
	}

	public void setExperimental(XMLExperimentalSettings experimental) {
		this.experimental = experimental;
	}

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

	/**
	 * Returns the telemetry settings.
	 * 
	 * @return the telemetry settings.
	 */
	public XMLTelemetrySettings getTelemetry() {
		return telemetry;
	}

	/**
	 * Set the telemetry settings
	 * 
	 * @param telemetry the telementry setting
	 */
	public void setTelemetry(XMLTelemetrySettings telemetry) {
		this.telemetry = telemetry;
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
	public void setCompletion(CompletionSettings completion) {
		this.completion = completion;
	}

	/**
	 * Get completion settings
	 * 
	 * @param completion
	 */
	public CompletionSettings getCompletion() {
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
}