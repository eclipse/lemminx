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

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.model.ContentModelManager;
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
	private static final String STARTUP_EVENT_NAME = "server.initialized";

	private static final String DOC_OPEN_EVENT_NAME = "server.document.open";

	private final LanguageClient languageClient;

	private final TelemetryCache telemetryCache;

	private final ScheduledExecutorService executor;

	private boolean enabled;

	public TelemetryManager(LanguageClient languageClient) {
		this.languageClient = languageClient;
		this.telemetryCache = new TelemetryCache();
		this.executor = Executors.newSingleThreadScheduledExecutor();
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

		executor.scheduleAtFixedRate(() -> {
			int hour = LocalDateTime.now().getHour();
			if (hour % 2 == 0) {
				if (isEnabled() && !telemetryCache.isEmpty()) {
					telemetryEvent(DOC_OPEN_EVENT_NAME, telemetryCache.getProperties());
					telemetryCache.clear();
				}
			}
		}, 30, 60, TimeUnit.MINUTES);
	}

	public void onDidOpen(DOMDocument document, ContentModelManager manager) {
		if (isEnabled()) {
			DocumentTelemetryInfo.collectDocumentTelemetryInfo(document, manager, telemetryCache);
		}
	}

	/**
	 * The telemetry notification is sent from the server to the client to ask the
	 * client to log a telemetry event.
	 */
	private void telemetryEvent(String eventName, Object object) {
		if (languageClient != null) {
			languageClient.telemetryEvent(new TelemetryEvent(eventName, object));
		}
	}

}
