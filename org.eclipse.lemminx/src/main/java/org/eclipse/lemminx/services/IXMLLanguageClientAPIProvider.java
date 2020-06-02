package org.eclipse.lemminx.services;

import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;

public interface IXMLLanguageClientAPIProvider {
	XMLLanguageClientAPI getLanguageClient();
}