/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.telemetry;

import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Telemetry manager.
 * 
 * @author Angelo ZERRs
 *
 */
public class TelemetryManager {

	/**
	 * "startup" telemetry event name
	 */
	private static final String STARTUP_EVENT_NAME = "startup";

	/**
	 * "service" telemetry event name
	 */
	private static final String SERVICE_EVENT_NAME = "service";

	/**
	 * Telemetry event
	 *
	 */
	public static class TelementryEvent {

		public final String name;
		public final Object properties;

		TelementryEvent(String name, Object properties) {
			this.name = name;
			this.properties = properties;
		}
	}

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
			telemetryEvent(STARTUP_EVENT_NAME, new ServerInfo());
		}
	}

	/**
	 * Send a telemetry event on call of a service.
	 * 
	 * @param serviceName service name.
	 */
	public void onServiceCall(String serviceName) {
		if (isEnabled()) {
			telemetryEvent(SERVICE_EVENT_NAME, serviceName);
		}
	}

	/**
	 * The telemetry notification is sent from the server to the client to ask the
	 * client to log a telemetry event.
	 */
	private void telemetryEvent(String eventName, Object object) {
		languageClient.telemetryEvent(new TelementryEvent(eventName, object));
	}

}
