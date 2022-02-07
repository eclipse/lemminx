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

import java.io.IOException;
import java.io.StringReader;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.XSDDescription;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.extensions.xerces.xmlmodel.msg.XMLModelMessageFormatter;

/**
 * Custom XML entity manager which return an empty string when a DOCTYPE SYSTEM
 * or ENTITY SYSTEM doesn't exist to:
 * 
 * <ul>
 * <li>avoid breaking the XML syntax validation when XML contains a
 * DOCTYPE/ENTITY SYSTEM which doesn't exist</li>
 * <li>report the error DOCTYPE/ENTITY SYSTEM which doesn't exist in the proper
 * range of SYSTEM</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
class LSPXMLEntityManager extends XMLEntityManager {

	private final XMLErrorReporter errorReporter;
	private final LSPXMLGrammarPool grammarPool;
	private boolean hasProblemsWithReferencedDTD;

	public LSPXMLEntityManager(XMLErrorReporter errorReporter, LSPXMLGrammarPool grammarPool) {
		this.errorReporter = errorReporter;
		this.grammarPool = grammarPool;
		this.hasProblemsWithReferencedDTD = false;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws IOException, XNIException {
		if (resourceIdentifier instanceof XSDDescription) {
			return super.resolveEntity(resourceIdentifier);
		}
		try {
			return super.resolveEntity(resourceIdentifier);
		} catch (Exception e) {
			// DTD DOCTYPE/ENTITY SYSTEM doesn't exist:
			// 1. report the error
			reportError(resourceIdentifier.getLiteralSystemId(), e);
			// 2. return an empty string to avoid breaking syntax validation
			XMLInputSource in = new XMLInputSource(resourceIdentifier);
			in.setCharacterStream(new StringReader(""));
			return in;
		}
	}

	@Override
	public String setupCurrentEntity(String name, XMLInputSource xmlInputSource, boolean literal, boolean isExternal)
			throws IOException, XNIException {
		try {
			return super.setupCurrentEntity(name, xmlInputSource, literal, isExternal);
		} catch (Exception e) {
			// DTD DOCTYPE/ENTITY SYSTEM doesn't exist:
			// 1. report the error
			reportError(xmlInputSource.getSystemId(), e);
			// 2. return an empty string to avoid breaking syntax validation
			XMLInputSource in = new XMLInputSource(xmlInputSource.getPublicId(), xmlInputSource.getSystemId(),
					xmlInputSource.getBaseSystemId(), new StringReader(""), null);
			return super.setupCurrentEntity(name, in, literal, isExternal);
		}
	}

	private void reportError(String location, Exception e) {
		hasProblemsWithReferencedDTD = true;
		errorReporter.reportError(NULL_LOCATOR, XMLModelMessageFormatter.XML_MODEL_DOMAIN,
				DTDErrorCode.dtd_not_found.getCode(), new Object[] { null, location }, XMLErrorReporter.SEVERITY_ERROR,
				e);
	}

	public void dispose() {
		if (hasProblemsWithReferencedDTD) {
			// one of DTD which where cached has an error
			// remove all DTDs cached during the validation from the grammar ppol.
			grammarPool.clear();
		}
	}

	/**
	 * The error code 'dtd_not_found' doesn't use the locator, but to avoid having
	 * some NPE, we create an null locator.
	 */
	private static final XMLLocator NULL_LOCATOR = new XMLLocator() {

		@Override
		public String getXMLVersion() {
			return null;
		}

		@Override
		public String getPublicId() {
			return null;
		}

		@Override
		public String getLiteralSystemId() {
			return null;
		}

		@Override
		public int getLineNumber() {
			return 0;
		}

		@Override
		public String getExpandedSystemId() {
			return null;
		}

		@Override
		public String getEncoding() {
			return null;
		}

		@Override
		public int getColumnNumber() {
			return 0;
		}

		@Override
		public int getCharacterOffset() {
			return 0;
		}

		@Override
		public String getBaseSystemId() {
			return null;
		}
	};
}