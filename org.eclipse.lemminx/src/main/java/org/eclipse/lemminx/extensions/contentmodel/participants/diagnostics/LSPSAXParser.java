/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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

import java.io.IOException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.impl.dtd.XMLEntityDecl;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMDocumentType;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Extension of Xerces SAX Parser to fix some Xerces bugs:
 *
 * <ul>
 * <li>[BUG 2]: when Xerces XML grammar pool is used, the second validation
 * ignore the existing of entities. See
 * https://github.com/redhat-developer/vscode-xml/issues/234</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 */
public class LSPSAXParser extends SAXParser {

	protected static final String VALIDATION_MANAGER = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.VALIDATION_MANAGER_PROPERTY;

	protected static final String ENTITY_MANAGER = Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;

	private final LSPXMLGrammarPool grammarPool;

	private final DOMDocument document;

	public LSPSAXParser(LSPErrorReporterForXML reporter, XMLParserConfiguration config, LSPXMLGrammarPool grammarPool,
			DOMDocument document) {
		super(config);
		this.grammarPool = grammarPool;
		this.document = document;
		super.setContentHandler(new MultipleContentHandler());
		init(reporter);
	}

	private void init(LSPErrorReporterForXML reporter) {
		try {
			// Add LSP error reporter to fill LSP diagnostics from Xerces errors
			super.setProperty("http://apache.org/xml/properties/internal/error-reporter", reporter);
		} catch (SAXNotRecognizedException | SAXNotSupportedException e) {
			// Should never occur.
		}
	}

	private XMLLocator locator;

	@Override
	public void startDocument(XMLLocator locator, String encoding, NamespaceContext namespaceContext,
			Augmentations augs) throws XNIException {
		this.locator = locator;
		if (grammarPool != null) {
			DOMDocumentType docType = document.getDoctype();
			if (docType != null) {
				String systemId = document.getDoctype().getSystemIdWithoutQuotes();
				if (systemId != null) {
					// ex : <!DOCTYPE chapter PUBLIC "-//OASIS//DTD DocBook XML V4.4//EN"
					// "http://www.docbook.org/xml/4.4/docbookx.dtd"
					String rootElement = document.getDocumentElement().getTagName();
					String publicId = document.getDoctype().getPublicIdWithoutQuotes();
					XMLDTDDescription grammarDesc = createGrammarDescription(rootElement, publicId, systemId);

					// Try to get the DTD grammar from the xerces cache
					DTDGrammar grammar = (DTDGrammar) grammarPool.retrieveGrammar(grammarDesc);
					if (grammar != null) {
						// Internal subset means:
						// ex : <!DOCTYPE chapter... [
						// some internal subset content like <!ENTITY ...
						//

						// Compare the current internal subset with the DTD grammar internal subset
						if (grammarPool.setInternalSubset(grammarDesc, docType.getInternalSubset())) {
							// The internal subset which can defines some entities changed, remove the
							// grammar from the cache
							grammarPool.removeGrammar(grammarDesc);
						}
					}
				}
			}
		}
		super.startDocument(locator, encoding, namespaceContext, augs);
	}

	@Override
	public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
			throws XNIException {
		if (systemId != null && grammarPool != null) {
			// There is a declared DTD in the DOCTYPE
			// <!DOCTYPE root-element SYSTEM "./extended.dtd" []>

			XMLEntityManager entityManager = (XMLEntityManager) fConfiguration.getProperty(ENTITY_MANAGER);
			XMLDTDDescription grammarDesc = createGrammarDescription(rootElement, publicId, systemId);

			// FIX [BUG 2]
			// DTD exists, get the DTD grammar from the cache

			DTDGrammar grammar = (DTDGrammar) grammarPool.retrieveGrammar(grammarDesc);
			if (grammar != null) {
				// The DTD grammar is in cache, we need to fill XML entity manager with the
				// entities declared in the cached DTD grammar
				fillEntities(grammar, entityManager);
			}
		}
		super.doctypeDecl(rootElement, publicId, systemId, augs);
	}

	/**
	 * Create DTD grammar description by expanding the system id.
	 *
	 * @param rootElement the root element
	 * @param publicId    the public ID.
	 * @param systemId    the system ID.
	 * @return the DTD grammar description by expanding the system id.
	 */
	private XMLDTDDescription createGrammarDescription(String rootElement, String publicId, String systemId) {
		String eid = null;
		try {
			eid = XMLEntityManager.expandSystemId(systemId, locator.getExpandedSystemId(), false);
		} catch (java.io.IOException e) {
		}

		return new XMLDTDDescription(publicId, systemId, locator.getExpandedSystemId(), eid, rootElement);
	}

	/**
	 * Fill entities from the given DTD grammar to the given entity manager.
	 *
	 * @param grammar       the DTD grammar
	 * @param entityManager the entitymanager to update with entities of the DTD
	 *                      grammar.
	 */
	private static void fillEntities(DTDGrammar grammar, XMLEntityManager entityManager) {
		int index = 0;
		XMLEntityDecl entityDecl = new XMLEntityDecl() {

			@Override
			public void setValues(String name, String publicId, String systemId, String baseSystemId, String notation,
					String value, boolean isPE, boolean inExternal) {
				if (systemId != null) {
					if (notation != null) {
						entityManager.addUnparsedEntity(name, publicId, systemId, baseSystemId, notation);
					} else {
						try {
							entityManager.addExternalEntity(name, publicId, systemId, baseSystemId);
						} catch (IOException e) {
							// Do nothing
						}
					}
				} else {
					entityManager.addInternalEntity(name, value);
				}
			}
		};
		while (grammar.getEntityDecl(index, entityDecl))

		{
			index++;
		}
	}

	@Override
	public void setContentHandler(ContentHandler contentHandler) {
		((MultipleContentHandler) getContentHandler()).addContentHandler(contentHandler);
	}

}