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
package org.eclipse.lemminx.extensions.relaxng.grammar.rng;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.LSPErrorReporterForXML;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Extends Xerces SAX Parser to use the LSP error reporter.
 * 
 * @author Angelo ZERR
 *
 */
public class RNGSAXParser extends SAXParser {

	protected static final String VALIDATION_MANAGER = Constants.XERCES_PROPERTY_PREFIX
			+ Constants.VALIDATION_MANAGER_PROPERTY;

	protected static final String ENTITY_MANAGER = Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;

	public RNGSAXParser(LSPErrorReporterForXML reporter, XMLParserConfiguration config) {
		super(config);
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

}
