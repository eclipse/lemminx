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
package org.eclipse.xml.languageserver.services;

import java.util.List;

import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xml.languageserver.model.XMLDocument;

/**
 * XML formatter support.
 *
 */
class XMLFormatter {

	private final XMLExtensionsRegistry extensionsRegistry;

	public XMLFormatter(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<? extends TextEdit> format(TextDocumentItem document, Range range, FormattingOptions options,
			XMLDocument xmlDocument) {
		// TODO implement formatting
		return null;
	}

}
