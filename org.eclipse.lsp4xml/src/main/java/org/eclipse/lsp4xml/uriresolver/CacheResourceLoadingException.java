package org.eclipse.lsp4xml.uriresolver;

import java.text.MessageFormat;

public class CacheResourceLoadingException extends RuntimeException {

	private static final String RESOURCE_LOADING_MSG = "The resource ''{0}'' is loading.";

	private final String resourceURI;

	public CacheResourceLoadingException(String resourceURI) {
		super(MessageFormat.format(RESOURCE_LOADING_MSG, resourceURI));
		this.resourceURI = resourceURI;
	}

	public String getResourceURI() {
		return resourceURI;
	}

	public boolean isDTD() {
		return resourceURI != null && resourceURI.endsWith(".dtd");
	}

}
