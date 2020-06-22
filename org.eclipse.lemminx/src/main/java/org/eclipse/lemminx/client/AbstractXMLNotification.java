package org.eclipse.lemminx.client;

import org.eclipse.lemminx.services.IXMLNotificationService;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Command;

public abstract class AbstractXMLNotification {

	private final IXMLNotificationService notificationService;


	public AbstractXMLNotification(IXMLNotificationService notificationService) {
		this.notificationService = notificationService;
	}


	protected void sendNotification(String message,  Command... commands) {
		notificationService.sendNotification(message, commands);
	}

	protected SharedSettings getSharedSettings() {
		return notificationService.getSharedSettings();
	}

	
	/*protected void sendNotification(String message, Command... commands) {
		if (sharedSettings.isActionableNotificationSupport() && sharedSettings.isOpenSettingsCommandSupport()) {
			ActionableNotification notification = new ActionableNotification().withSeverity(MessageType.Info)
					.withMessage(message).withCommands(Arrays.asList(commands));
			client.actionableNotification(notification);
		} else {
			// the open settings command is not supported by the client, display a simple
			// message with LSP
			client.showMessage(new MessageParams(MessageType.Warning, message));
		}
	}*/
}