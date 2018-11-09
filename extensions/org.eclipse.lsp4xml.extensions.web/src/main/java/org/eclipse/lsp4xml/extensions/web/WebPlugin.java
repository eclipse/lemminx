/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.references.XMLReferencesManager;
import org.eclipse.lsp4xml.services.extensions.IXMLExtension;
import org.eclipse.lsp4xml.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.services.extensions.save.ISaveContext;

/**
 * Extension for web.xml.
 *
 */
public class WebPlugin implements IXMLExtension {

	private static Logger LOGGER = Logger.getLogger(WebPlugin.class.getName());

	private static final String WEB_XML = "web.xml";

	@Override
	public void doSave(ISaveContext context) {
		
	}

	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		try {
			XMLReferencesManager.getInstance() //
					.referencesFor(WebPlugin::match) //
					.from("//*:servlet-mapping/*:servlet-name/*/*") //
					.to("//*[local-name()='servlet']/*[local-name() ='servlet-name']/text()");
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, "Error while registering XML references for web.xml", e);
		}
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
	}

	public static boolean match(XMLDocument document) {
		return document.getDocumentURI().endsWith(WEB_XML);
	}
}
