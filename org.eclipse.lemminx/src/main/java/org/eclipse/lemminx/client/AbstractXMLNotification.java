package org.eclipse.lemminx.client;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.lemminx.customservice.ActionableNotification;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public abstract class AbstractXMLNotification {

	private final XMLLanguageClientAPI client;
	protected final SharedSettings sharedSettings;

	public AbstractXMLNotification(XMLLanguageClientAPI client, SharedSettings sharedSettings) {
		this.client = client;
		this.sharedSettings = sharedSettings;
	}

	protected void sendNotification(String message, Command... commands) {
		if (sharedSettings.isActionableNotificationSupport() && sharedSettings.isOpenSettingsCommandSupport()) {
			ActionableNotification notification = new ActionableNotification().withSeverity(MessageType.Info)
					.withMessage(message).withCommands(Arrays.asList(commands));
			client.actionableNotification(notification);
		} else {
			// the open settings command is not supported by the client, display a simple
			// message with LSP
			client.showMessage(new MessageParams(MessageType.Warning, message));
		}
	}
}