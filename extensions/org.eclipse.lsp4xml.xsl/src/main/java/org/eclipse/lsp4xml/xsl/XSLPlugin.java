package org.eclipse.lsp4xml.xsl;

import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.uriresolver.URIResolverExtensionManager;

public class XSLPlugin implements IXMLExtension {

	private final XSLURIResolverExtension uiResolver;

	public XSLPlugin() {
		uiResolver = new XSLURIResolverExtension();
	}

	@Override
	public void updateSettings(Object settings) {

	}

	@Override
	public void start(XMLExtensionsRegistry registry) {
		URIResolverExtensionManager.getInstance().registerResolver(uiResolver);
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		URIResolverExtensionManager.getInstance().unregisterResolver(uiResolver);
	}
}
