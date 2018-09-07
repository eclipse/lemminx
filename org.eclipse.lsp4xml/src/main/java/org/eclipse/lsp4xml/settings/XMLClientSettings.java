/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.settings;

import org.eclipse.lsp4xml.utils.JSONUtility;

/**
 * XML client settings
 *
 */
public class XMLClientSettings {

	private LogsSettings logs;

	private XMLFormattingOptions format;

	public void setLogs(LogsSettings logs) {
		this.logs = logs;
	}

	public LogsSettings getLogs() {
		return logs;
	}

	public void setFormat(XMLFormattingOptions format) {
		this.format = format;
	}

	public XMLFormattingOptions getFormat() {
		return format;
	}

	public static XMLClientSettings getSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, XMLClientSettings.class);
	}
}