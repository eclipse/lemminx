package org.eclipse.lemminx.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lemminx.services.IXMLNotificationService;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Command;

public class PathWarnings extends AbstractXMLNotification {
	private final Map<String /* setting name */, Set<String> /* file paths */> cache;

	public PathWarnings(IXMLNotificationService notificationService ) {
		super(notificationService);
		this.cache = new HashMap<String, Set<String>>();
	}

	public void onInvalidFilePath(Set<String> invalidPaths, String settingId) {
		if (this.cache.containsKey(settingId) && this.cache.get(settingId).equals(invalidPaths)) {
			return;
		}
		sendInvalidFilePathWarning(invalidPaths, settingId);
		evictCache(settingId);
		this.cache.put(settingId, invalidPaths);
	}

	private void evictCache(String settingId) {
		cache.get(settingId).clear();
	}

	private void sendInvalidFilePathWarning(Set<String> invalidPaths, String settingId) {

		// TODO improve message
		String message = "Invalid catalog path: " + invalidPaths.toString();
		Command command = new Command("Configure path", ClientCommands.OPEN_SETTINGS,
					Collections.singletonList(settingId));

		super.sendNotification(message, command);
	}
}