/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.xsd.participants.diagnostics;

import java.util.List;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.extensions.xerces.AbstractLSPErrorReporter;
import org.eclipse.lemminx.extensions.xsd.participants.XSDErrorCode;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.xml.sax.ErrorHandler;

/**
 * The SAX {@link ErrorHandler} gives just information of the offset where there
 * is an error. To improve highlight XML error, this class extends the Xerces
 * XML reporter to catch location, key, arguments which is helpful to adjust the
 * LSP range.
 *
 */
public class LSPErrorReporterForXSD extends AbstractLSPErrorReporter {

	private static final String XSD_DIAGNOSTIC_SOURCE = "xsd";

	public LSPErrorReporterForXSD(DOMDocument xmlDocument, List<Diagnostic> diagnostics) {
		super(XSD_DIAGNOSTIC_SOURCE, xmlDocument, diagnostics);
	}

	/**
	 * Create the LSP range from the SAX error.
	 * 
	 * @param location
	 * @param key
	 * @param arguments
	 * @param document
	 * @return the LSP range from the SAX error.
	 */
	@Override
	protected Range toLSPRange(XMLLocator location, String key, Object[] arguments, DOMDocument document) {
		// try adjust positions for XSD error
		XSDErrorCode xsdCode = XSDErrorCode.get(key);
		if (xsdCode != null) {
			Range range = XSDErrorCode.toLSPRange(location, xsdCode, arguments, document);
			if (range != null) {
				return range;
			}
		}
		XMLSyntaxErrorCode syntaxCode = XMLSyntaxErrorCode.get(key);
		if (syntaxCode != null) {
			Range range = XMLSyntaxErrorCode.toLSPRange(location, syntaxCode, arguments, document);
			if (range != null) {
				return range;
			}
		}
		return null;
	}
}
