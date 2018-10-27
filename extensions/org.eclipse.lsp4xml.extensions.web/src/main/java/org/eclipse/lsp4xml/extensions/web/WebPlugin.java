package org.eclipse.lsp4xml.extensions.web;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.references.XMLReferencesManager;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;

public class WebPlugin implements IXMLExtension {

	private static final String WEB_XML = "web.xml";

	@Override
	public void updateSettings(Object settings) {

	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		try {
			XMLReferencesManager.getInstance() //
					.addReference("//*:servlet-mapping/*:servlet-name/*/*", WebPlugin::match) //
					.addTo("//*[local-name()='servlet']/*[local-name() ='servlet-name']/text()");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
	}

	public static boolean match(XMLDocument document) {
		return document.getDocumentURI().endsWith(WEB_XML);
	}
}
