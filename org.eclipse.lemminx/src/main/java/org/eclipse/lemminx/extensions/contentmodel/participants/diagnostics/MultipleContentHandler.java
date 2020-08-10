/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics;

import java.util.LinkedHashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Multiple SAX {@link ContentHandler}.
 * 
 * @author Angelo ZERR
 *
 */
public class MultipleContentHandler implements ContentHandler {

	private final Set<ContentHandler> handlers;

	public MultipleContentHandler() {
		this.handlers = new LinkedHashSet<>();
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.setDocumentLocator(locator);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.startDocument();
		}
	}

	@Override
	public void endDocument() throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.endDocument();
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.startPrefixMapping(prefix, uri);
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.endPrefixMapping(prefix);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.startElement(uri, localName, qName, atts);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.endElement(uri, localName, qName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.characters(ch, start, length);
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.ignorableWhitespace(ch, start, length);
		}
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.processingInstruction(target, data);
		}
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		for (ContentHandler contentHandler : handlers) {
			contentHandler.skippedEntity(name);
		}
	}

	public void addContentHandler(ContentHandler contentHandler) {
		if (!handlers.contains(contentHandler)) {
			handlers.add(contentHandler);
		}
	}

}
