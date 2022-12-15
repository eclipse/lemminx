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

import org.eclipse.lemminx.extensions.contentmodel.participants.diagnostics.LSPErrorReporterForXML;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * {@link XMLReaderCreator} to create the RNG parser.
 * 
 * @author Angelo ZERR
 *
 */
public class RNGXMLReaderCreator implements XMLReaderCreator {

	private final LSPErrorReporterForXML reporter;

	public RNGXMLReaderCreator(LSPErrorReporterForXML reporter) {
		this.reporter = reporter;
	}

	public XMLReader createXMLReader() throws SAXException {
		RNGSAXParser parser = new RNGSAXParser(reporter, new RNGParserConfiguration(reporter));
		parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false); //$NON-NLS-1$
		parser.setFeature("http://xml.org/sax/features/namespaces", true); //$NON-NLS-1$
		parser.setFeature("http://xml.org/sax/features/validation", false);
		return parser;
	}
}
