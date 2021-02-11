/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.telemetry;

import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Telemetry manager.
 *
 * @author Angelo ZERR
 */
public class TelemetryManager {

	/**
	 * "startup" telemetry event name
	 */
	private static final String STARTUP_EVENT_NAME = "server_initialized";

	private final LanguageClient languageClient;

	private boolean enabled;

	public TelemetryManager(LanguageClient languageClient) {
		this.languageClient = languageClient;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Send a telemetry event on start of the XML server
	 *
	 * @param params
	 */
	public void onInitialized(InitializedParams params) {
		if (isEnabled()) {
			telemetryEvent(STARTUP_EVENT_NAME, InitializationTelemetryInfo.getInitializationTelemetryInfo());
		}
	}

	/**
	 * The telemetry notification is sent from the server to the client to ask the
	 * client to log a telemetry event.
	 */
	private void telemetryEvent(String eventName, Object object) {
		languageClient.telemetryEvent(new TelemetryEvent(eventName, object));
	}

}
