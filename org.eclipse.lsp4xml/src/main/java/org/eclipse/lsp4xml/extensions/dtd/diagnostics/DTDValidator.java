/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.dtd.diagnostics;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.xerces.impl.dtd.XMLDTDLoader;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.diagnostics.LSPErrorReporterForXML;

/**
 * DTD validator
 *
 */
public class DTDValidator {

	public static void doDiagnostics(DOMDocument document, XMLEntityResolver entityResolver,
			List<Diagnostic> diagnostics, CancelChecker monitor) {
		try {
			XMLDTDLoader loader = new XMLDTDLoader();
			loader.setProperty("http://apache.org/xml/properties/internal/error-reporter",
					new LSPErrorReporterForXML(document, diagnostics));

			if (entityResolver != null) {
				loader.setEntityResolver(entityResolver);
			}

			String content = document.getText();
			String uri = document.getDocumentURI();
			InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			XMLInputSource source = new XMLInputSource(null, uri, uri, inputStream, null);
			loader.loadGrammar(source);
		} catch (Exception e) {

		}
	}
}
