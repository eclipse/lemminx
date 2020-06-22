package org.eclipse.lemminx.services;

import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Command;

public interface IXMLNotificationService {

	void sendNotification(String message, Command... commands);

	SharedSettings getSharedSettings();
}
