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
package org.eclipse.lsp4xml.extensions.xsd.participants.diagnostics;

import java.util.List;

import org.apache.xerces.xni.XMLLocator;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.XMLDocument;
import org.eclipse.lsp4xml.extensions.xsd.participants.XSDErrorCode;
import org.eclipse.lsp4xml.services.extensions.diagnostics.AbstractLSPErrorReporter;
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

	public LSPErrorReporterForXSD(XMLDocument xmlDocument, List<Diagnostic> diagnostics) {
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
	protected Range toLSPRange(XMLLocator location, String key, Object[] arguments, XMLDocument document) {
		// try adjust positions for XSD error
		XSDErrorCode xsdCode = XSDErrorCode.get(key);
		if (xsdCode != null) {
			Range range = XSDErrorCode.toLSPRange(location, xsdCode, arguments, document);
			if (range != null) {
				return range;
			}
		}
		return null;
	}
}
